package org.zkoss.zss.model.impl.sys;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.poi.hpsf.Util;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.impl.sys.utils.*;
import org.zkoss.zss.model.sys.dependency.Ref;

import org.model.LruCache;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class DependencyTableComp {

    private final String dependencyTableName = "compressed_dependency";
    private final String logTableName = "staged_log";

    private final int SHIFT_STEP = 1;
    private final int FIRST_ROW = 1;
    private final int FIRST_COL = 1;
    private final int CACHE_SIZE = 1000000;
    private final int REFRESH_THRESHOLD = 10;

    /** Map<dependant, precedent> */
    protected LruCache<Ref, List<RefWithMeta>> _mapCache = new LruCache<>(CACHE_SIZE);
    protected LruCache<Ref, List<RefWithMeta>> _reverseMapCache = new LruCache<>(CACHE_SIZE);
    private RTree<Ref, Rectangle> _rectToRefCache  = RTree.create();
    private int refNum = 0;
    private int logEntryNum = 0;
    private int insertEntryNum = 0;

    public DependencyTableComp() {}

    Set<Ref> getDependents(Ref precedent) {
        final boolean isDirectDep = false;
        LinkedHashSet<Ref> result = new LinkedHashSet<>();
        getDependentsInternal(precedent, result, isDirectDep);

        if (insertEntryNum != 0) {
            final boolean isInsertOnly = true;
            getLogEntries(precedent.getBookName(), precedent.getSheetName(), isInsertOnly)
                    .forEach(logEntry -> {
                        getDependentsInternal(logEntry.prec, result, isDirectDep);
                        getDependentsInternal(logEntry.dep, result, isDirectDep);
                    });
        }

        return result;
    }

    Set<Ref> getDirectDependents(Ref precedent) {

        // enforce consuming all unprocessed log entries
        refreshCache(precedent.getBookName(), precedent.getSheetName());

        boolean isDirectDep = true;
        LinkedHashSet<Ref> result = new LinkedHashSet<>() ;
        getDependentsInternal(precedent, result, isDirectDep);
        return result;
    }

    private LinkedList<LogEntry> getLogEntries(
            String bookName,
            String sheetName,
            boolean isInsertOnly) {
        String selectQuery =
                "SELECT range, dep_range, isInsert FROM " + logTableName +
                "WHERE bookname = ?    " +
                "AND sheetname = ?     ";
        if (isInsertOnly) selectQuery += "WHERE isInsert = TRUE";

        LinkedList<LogEntry> result = new LinkedList<>();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setString(1, bookName);
            stmt.setString(2, sheetName);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next())
            {
                PGbox range = (PGbox) rs.getObject(1);
                PGbox dep_range = (PGbox) rs.getObject(2);
                Ref prec = null;
                if (range != null) {
                    prec = new RefImpl(bookName, sheetName,
                            (int) range.point[0].x,
                            (int) range.point[0].y,
                            (int) range.point[1].x,
                            (int) range.point[1].y);
                }
                Ref dep = new RefImpl(bookName, sheetName,
                           (int) dep_range.point[0].x,
                           (int) dep_range.point[0].y,
                           (int) dep_range.point[1].x,
                           (int) dep_range.point[1].y);
                boolean isInsert = rs.getBoolean(3);
                result.add(new LogEntry(prec, dep, isInsert));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void getDependentsInternal(Ref precUpdate,
                                       LinkedHashSet<Ref> result,
                                       boolean isDirectDep) {
        Queue<Ref> updateQueue = new LinkedList<>();
        updateQueue.add(precUpdate);
        while (!updateQueue.isEmpty()) {
            Ref updateRef = updateQueue.remove();
            Iterator<Ref> refIter = findOverlappingRefs(updateRef);
            while (refIter.hasNext()) {
                Ref precRef = refIter.next();

                Ref realUpdateRef = updateRef.getOverlap(precRef);
                findDeps(precRef).forEach(depRef -> {
                    Ref depUpdateRef = depRef.findDepUpdateRef(realUpdateRef);
                    if (!result.contains(depUpdateRef)) {
                        result.add(depUpdateRef);
                        if (!isDirectDep) updateQueue.add(depUpdateRef);
                    }
                });
            }
        }
    }

    void add(Ref dependent, Ref precedent) {
        String insertQuery = "INSERT INTO " + logTableName
                + "VALUES (?,?,?,?,?,?,TRUE)";
        boolean isInsert = true;
        appendOneLog(insertQuery, precedent, dependent, isInsert);
    }

    void clearDependents(Ref dependant) {
        String deleteQuery = "INSERT INTO " + logTableName
                + "VALUES (?,?,?,?,?,?,FALSE)";
        boolean isInsert = false;
        appendOneLog(deleteQuery, null, dependant, isInsert);
    }

    private void appendOneLog(String appendLogQuery,
                              Ref prec,
                              Ref dep,
                              boolean isInsert) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(appendLogQuery)) {
            if (!isInsert) {
                stmt.setString(1, prec.getBookName());
                stmt.setString(2, prec.getSheetName());
                stmt.setObject(3, new PGbox(prec.getRow(),
                        prec.getColumn(), prec.getLastRow(),
                        prec.getLastColumn()), Types.OTHER);
            } else {
                stmt.setNull(1, Types.NVARCHAR);
                stmt.setNull(2, Types.NVARCHAR);
                stmt.setNull(3, Types.OTHER);
            }
            stmt.setString(4, dep.getBookName());
            stmt.setString(5, dep.getSheetName());
            stmt.setObject(6, new PGbox(dep.getRow(),
                    dep.getColumn(), dep.getLastRow(),
                    dep.getLastColumn()), Types.OTHER);
            stmt.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logEntryNum += 1;
        if (isInsert) insertEntryNum += 1;
    }

    void refreshCache(String bookName, String sheetName) {
        boolean isInsertOnly = false;
        getLogEntries(bookName, sheetName, isInsertOnly)
                .forEach(logEntry -> {
                    if (logEntry.isInsert) performOneInsert(logEntry.prec, logEntry.dep);
                    else performOneDelete(logEntry.dep);
                });
    }

    private void performOneInsert(Ref prec, Ref dep) {
        findOverlapAndAdjacency(dep).forEach(candDep -> {
            findPrecs(candDep).forEach(candPrecWithMeta -> {
                checkCompressible(prec, dep, candPrecWithMeta, candDep);
            });
        });
    }

    private void performOneDelete(Ref dep) {

    }

    private PatternType checkCompressible(Ref prec, Ref dep,
                                          RefWithMeta candPrecWithMeta, Ref candDep) {
        return PatternType.NOTYPE;
    }

    private Iterable<Ref> findOverlapAndAdjacency(Ref ref) {
        LinkedList<Ref> res = new LinkedList<>();

        findOverlappingRefs(ref).forEachRemaining(res::addLast);
        Arrays.stream(Direction.values()).forEach(direction ->
                findOverlappingRefs(shiftRef(ref, direction))
                .forEachRemaining(adjRef -> {
                    if (isValidAdjacency(adjRef, ref)) res.addLast(ref); // valid adjacency
                }));

        return res;
    }

    private boolean isValidAdjacency(Ref adjRef, Ref ref) {
        int adjFirstRow = adjRef.getRow();
        int adjFirstCol = adjRef.getColumn();
        int adjLastRow = adjRef.getLastRow();
        int adjLastCol = adjRef.getLastColumn();

        int firstRow = ref.getRow();
        int firstCol = ref.getColumn();
        int lastRow = ref.getLastRow();
        int lastCol = ref.getLastColumn();


        if (adjFirstRow == firstRow && adjLastRow == lastRow
                && adjLastCol + SHIFT_STEP == firstCol) { // To Left
            return true;
        } else if (adjFirstRow == firstRow && adjLastRow == lastRow
                && lastCol + SHIFT_STEP == adjFirstCol) { // To Right
            return true;
        } else // To Down
            if (adjFirstCol == firstCol && adjLastCol == lastCol
                && adjLastRow + SHIFT_STEP == firstRow) { // To Up
            return true;
        } else return adjFirstCol == firstCol && adjLastCol == lastCol // To Down
                    && lastRow + SHIFT_STEP == adjFirstRow;
    }

    private Ref shiftRef(Ref ref, Direction direction) {
        Ref res = null;
        switch (direction) {
            case TOLEFT:
                if (ref.getColumn() != FIRST_COL) {
                    res = new RefImpl(ref.getBookName(),
                            ref.getSheetName(),
                            ref.getRow(), ref.getColumn() - SHIFT_STEP,
                            ref.getLastRow(), ref.getLastColumn() - SHIFT_STEP);
                }
                break;
            case TORIGHT:
                res = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        ref.getRow(), ref.getColumn() + SHIFT_STEP,
                        ref.getLastRow(), ref.getLastColumn() + SHIFT_STEP);
                break;
            case TOUP:
                if (ref.getRow() != FIRST_ROW) {
                    res = new RefImpl(ref.getBookName(),
                            ref.getSheetName(),
                            ref.getRow() - SHIFT_STEP, ref.getColumn(),
                            ref.getLastRow() - SHIFT_STEP, ref.getLastColumn());
                }
                break;
            default: // TODOWN
                res = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        ref.getRow() + SHIFT_STEP, ref.getColumn(),
                        ref.getLastRow() + SHIFT_STEP, ref.getLastRow());
        }
        return res;
    }

    boolean shouldRefreshCache() {
        return logEntryNum > REFRESH_THRESHOLD;
    }

    private Rectangle getRectangleFromRef(Ref ref)
    {
        return RectangleFloat.create(ref.getRow(),ref.getColumn(),
                (float) 0.5 + ref.getLastRow(), (float) 0.5 + ref.getLastColumn());
    }

    private Iterable<RefWithMeta> findPrecs(Ref dep) {
        List<RefWithMeta> precIter = _reverseMapCache.get(dep);
        if (precIter.isEmpty()) {
            precIter = findPrecsFromDB(dep);
            _reverseMapCache.put(dep, precIter);
        }
        return precIter;
    }

    private Iterable<RefWithMeta> findDeps(Ref prec) {
        List<RefWithMeta> depIter = _mapCache.get(prec);
        if (depIter.isEmpty()) {
            depIter = findDepsFromDB(prec);
            _mapCache.put(prec, depIter);
        }
        return depIter;
    }

    private List<RefWithMeta> findDepsFromDB(Ref prec) {
        String selectQuery =
                "  SELECT dep_range::box, patternType, offsetRange::box, lastPredRange::box" +
                "  FROM " + dependencyTableName +
                "  WHERE  bookname  = ?" +
                "  AND    sheetname =  ?" +
                "  AND    range ~= ?";
        return findRefsFromDB(prec, selectQuery);
    }

    private List<RefWithMeta> findPrecsFromDB(Ref dep) {
        String selectQuery =
                "  SELECT range::box, patternType, offsetRange::box, lastPredRange::box" +
                        "  FROM " + dependencyTableName +
                        "  WHERE  bookname  = ?" +
                        "  AND    sheetname =  ?" +
                        "  AND    dep_range ~= ?";
        return findRefsFromDB(dep, selectQuery);
    }

    private List<RefWithMeta> findRefsFromDB(Ref ref, String selectQuery) {
        List<RefWithMeta> result = new LinkedList<>();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setString(1, ref.getBookName());
            stmt.setString(2, ref.getSheetName());
            stmt.setObject(3, new PGbox(ref.getRow(),
                    ref.getColumn(), ref.getLastRow(),
                    ref.getLastColumn()), Types.OTHER);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next())
            {
                PGbox range = (PGbox) rs.getObject(1);
                Ref newRef = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        (int) range.point[0].x,
                        (int) range.point[0].y,
                        (int) range.point[1].x,
                        (int) range.point[1].y);
                PatternType patternType = PatternType.values()[rs.getInt(2)];
                PGbox offsetRange = (PGbox) rs.getObject(3);
                Offset startOffset = new Offset((int) offsetRange.point[0].x,
                        (int) offsetRange.point[0].y);
                Offset endOffset = new Offset((int) offsetRange.point[1].x,
                        (int) offsetRange.point[1].y);
                PGbox lastPrecRange = (PGbox) rs.getObject(4);
                Ref lastPrecRef = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        (int) lastPrecRange.point[0].x,
                        (int) lastPrecRange.point[0].y,
                        (int) lastPrecRange.point[1].x,
                        (int) lastPrecRange.point[1].y);
                EdgeMeta edgeMeta =
                        new EdgeMeta(patternType, startOffset, endOffset, lastPrecRef);
                result.add(new RefWithMeta(newRef, edgeMeta));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Iterator<Ref> findOverlappingRefs(Ref updateRef) {
        Iterator<Ref> refIter = null;

        if (updateRef == null) {
            return new Iterator<Ref>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Ref next() {
                    return null;
                }
            };
        }

        if (_rectToRefCache != null) {
            Iterator<Entry<Ref, Rectangle>> entryIter =
                    _rectToRefCache.search(getRectangleFromRef(updateRef))
                            .toBlocking().getIterator();
            refIter = Iterators.transform(entryIter, new Function<Entry<Ref, Rectangle>, Ref>() {
                @Override
                public @Nullable Ref apply(@Nullable Entry<Ref, Rectangle> refRectangleEntry) {
                    return refRectangleEntry.value();
                }
            });
        } else {
            refIter = findOverlappingRefsFromDB(updateRef);
        }

        return refIter;
    }

    private Iterator<Ref> findOverlappingRefsFromDB(Ref updateRef) {
        String selectQuery =
                "  SELECT range::box from " + dependencyTableName +
                "  WHERE  bookname  = ?" +
                "  AND    sheetname =  ?" +
                "  AND    range && ?" +
                "  UNION " +
                "  SELECT dep_range::box from " + dependencyTableName +
                "  WHERE  dep_bookname  = ?" +
                "  AND    dep_sheetname =  ?" +
                "  AND    dep_range && ?";

       LinkedList<Ref> result = new LinkedList<>();
       try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
            PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
           stmt.setString(1, updateRef.getBookName());
           stmt.setString(2, updateRef.getSheetName());
           stmt.setObject(3, new PGbox(updateRef.getRow(),
                   updateRef.getColumn(), updateRef.getLastRow(),
                   updateRef.getLastColumn()), Types.OTHER);

           stmt.setString(4, updateRef.getBookName());
           stmt.setString(5, updateRef.getSheetName());
           stmt.setObject(6, new PGbox(updateRef.getRow(),
                   updateRef.getColumn(), updateRef.getLastRow(),
                   updateRef.getLastColumn()), Types.OTHER);

           ResultSet rs =  stmt.executeQuery();
           while(rs.next())
           {
               PGbox range = (PGbox) rs.getObject(1);
               // The order of points is based on how postgres stores them
               result.add(
                       new RefImpl(updateRef.getBookName(),
                               updateRef.getSheetName(),
                               (int) range.point[0].x,
                               (int) range.point[0].y,
                               (int) range.point[1].x,
                               (int) range.point[1].y));
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }

       return result.iterator();
    }
}
