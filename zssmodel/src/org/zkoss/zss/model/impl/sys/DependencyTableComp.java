package org.zkoss.zss.model.impl.sys;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.impl.sys.utils.*;
import org.zkoss.zss.model.sys.dependency.Ref;

import org.model.LruCache;

import java.sql.*;
import java.util.*;

import static org.zkoss.zss.model.impl.sys.utils.PatternTools.*;

public class DependencyTableComp extends DependencyTableAdv {

    private final String dependencyTableName = "compressed_dependency";
    private final String logTableName = "staged_log";

    private final int CACHE_SIZE = 1000000;
    private final int UPDATE_CACHE_SIZE = 100;

    /** Map<dependant, precedent> */
    protected LruCache<Ref, List<RefWithMeta>> _mapCache = new LruCache<>(CACHE_SIZE);
    protected LruCache<Ref, List<RefWithMeta>> _reverseMapCache = new LruCache<>(CACHE_SIZE);
    private RTree<Ref, Rectangle> _rectToRefCache  = RTree.create();
    private int refNum = 0;
    private int logEntryNum = 0;
    private int insertEntryNum = 0;

    private CompressInfoComparator compressInfoComparator = new CompressInfoComparator();

    public DependencyTableComp() {}

    @Override
    public long getLastLookupTime() {
        return 0;
    }

    @Override
    public Set<Ref> getDependents(Ref precedent) {
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

    @Override
    public Set<Ref> getActualDependents(Ref precedent) {
        return null;
    }

    @Override
    public Set<Ref> getDirectDependents(Ref precedent) {

        // enforce consuming all unprocessed log entries
        refreshCache(precedent.getBookName(), precedent.getSheetName());

        boolean isDirectDep = true;
        LinkedHashSet<Ref> result = new LinkedHashSet<>() ;
        getDependentsInternal(precedent, result, isDirectDep);
        return result;
    }

    @Override
    public void add(Ref dependent, Ref precedent) {
        String insertQuery = "INSERT INTO " + logTableName
                + "VALUES (?,?,?,?,?,?,?,TRUE)";
        boolean isInsert = true;
        appendOneLog(insertQuery, precedent, dependent, isInsert);
    }

    @Override
    public void addPreDep(Ref precedent, Set<Ref> dependent) {

    }

    @Override
    public void clearDependents(Ref dependant) {
        String deleteQuery = "INSERT INTO " + logTableName
                + "VALUES (?,?,?,?,?,?,?,FALSE)";
        boolean isInsert = false;
        appendOneLog(deleteQuery, null, dependant, isInsert);
    }

    @Override
    public Set<Ref> searchPrecedents(RefFilter filter) {
        return null;
    }

    @Override
    public void addBatch(Set<Pair<Ref, Ref>> edgeBatch) {
        AutoRollbackConnection connection = DBHandler.instance.getConnection();
        DBContext dbContext = new DBContext(connection);
        LinkedList<EdgeUpdate> updateCache = new LinkedList<>();
        edgeBatch.forEach(oneEdge -> {
            Ref prec = oneEdge.getX();
            Ref dep = oneEdge.getY();

            CompressInfo selectedInfo = null;
            Pair<CompressInfo, EdgeUpdate> updatePair =
                    findCompressInfoInUpdateCache(prec, dep, updateCache);
            if (updatePair != null) {
                selectedInfo = updatePair.getX();
                EdgeUpdate updateMatch = updatePair.getY();
                if (!selectedInfo.isDuplicate) {
                    updateCache.remove(updateMatch);
                    Ref newPrec = selectedInfo.prec.getBoundingBox(selectedInfo.candPrec);
                    Ref newDep = selectedInfo.dep.getBoundingBox(selectedInfo.candDep);
                    Pair<Offset, Offset> offsetPair = computeOffset(newPrec, newDep, selectedInfo.compType);
                    updateMatch.updateEdge(newPrec, newDep,
                            new EdgeMeta(selectedInfo.compType, offsetPair.getX(), offsetPair.getY()));
                    updateCache.addFirst(updateMatch);
                }
            } else {
                try {
                    LinkedList<CompressInfo> compressInfoList = findCompressInfo(prec, dep);
                    if (!compressInfoList.isEmpty()) {
                        selectedInfo = Collections.min(compressInfoList, compressInfoComparator);

                        Ref newPrec = selectedInfo.prec.getBoundingBox(selectedInfo.candPrec);
                        Ref newDep = selectedInfo.dep.getBoundingBox(selectedInfo.candDep);
                        Pair<Offset, Offset> offsetPair = computeOffset(newPrec, newDep, selectedInfo.compType);

                        EdgeUpdate evicted = addToUpdateCache(updateCache, newPrec, newDep,
                                new EdgeMeta(selectedInfo.compType, offsetPair.getX(), offsetPair.getY()));
                        deleteDBEntry(dbContext, evicted.oldPrec, evicted.oldDep, evicted.oldEdgeMeta);
                        insertDBEntry(dbContext, evicted.newPrec, evicted.newDep, evicted.newEdgeMeta.patternType,
                                evicted.newEdgeMeta.startOffset, evicted.newEdgeMeta.endOffset);

                    } else {
                        EdgeMeta noTypeEdgeMeta = new EdgeMeta(PatternType.NOTYPE,
                                Offset.noOffset, Offset.noOffset);
                        EdgeUpdate evicted = addToUpdateCache(updateCache, prec, dep, noTypeEdgeMeta);
                        deleteDBEntry(dbContext, evicted.oldPrec, evicted.oldDep, evicted.oldEdgeMeta);
                        insertDBEntry(dbContext, evicted.newPrec, evicted.newDep, evicted.newEdgeMeta.patternType,
                                evicted.newEdgeMeta.startOffset, evicted.newEdgeMeta.endOffset);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        connection.commit();
    }

    @Override
    public void refreshCache(String bookName, String sheetName) {
        boolean isInsertOnly = false;
        getLogEntries(bookName, sheetName, isInsertOnly)
                .forEach(logEntry -> {
                    if (logEntry.isInsert) performOneInsert(logEntry.id, logEntry.prec, logEntry.dep);
                    else performOneDelete(logEntry.id, logEntry.dep);
                });

        maintainRectToRefCache(bookName, sheetName);
    }

    private LinkedList<LogEntry> getLogEntries(
            String bookName,
            String sheetName,
            boolean isInsertOnly) {
        String selectQuery =
                "SELECT id, range, dep_range, isInsert FROM " + logTableName +
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
                int id = rs.getInt(1);
                PGbox range = (PGbox) rs.getObject(2);
                PGbox dep_range = (PGbox) rs.getObject(3);
                Ref prec = null;
                if (range != null) prec = boxToRef(range, bookName, sheetName);
                Ref dep = boxToRef(dep_range, bookName, sheetName);
                boolean isInsert = rs.getBoolean(3);
                result.add(new LogEntry(id, prec, dep, isInsert));
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
                findDeps(precRef).forEach(depRefWithMeta -> {
                    Ref depUpdateRef = findUpdateDepRef(precRef, depRefWithMeta.getRef(),
                            depRefWithMeta.getEdgeMeta(), realUpdateRef);
                    if (!result.contains(depUpdateRef)) {
                        result.add(depUpdateRef);
                        if (!isDirectDep) updateQueue.add(depUpdateRef);
                    }
                });
            }
        }
    }

    private void appendOneLog(String appendLogQuery,
                              Ref prec,
                              Ref dep,
                              boolean isInsert) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(appendLogQuery)) {
            stmt.setInt(1, logEntryNum);
            if (!isInsert) {
                stmt.setString(2, prec.getBookName());
                stmt.setString(3, prec.getSheetName());
                stmt.setObject(4, refToPGBox(prec), Types.OTHER);
            } else {
                stmt.setNull(2, Types.NVARCHAR);
                stmt.setNull(3, Types.NVARCHAR);
                stmt.setNull(4, Types.OTHER);
            }
            stmt.setString(5, dep.getBookName());
            stmt.setString(6, dep.getSheetName());
            stmt.setObject(7, refToPGBox(dep), Types.OTHER);
            stmt.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logEntryNum += 1;
        if (isInsert) insertEntryNum += 1;
    }

    private EdgeUpdate addToUpdateCache(LinkedList<EdgeUpdate> updateCache,
                                  Ref prec, Ref dep, EdgeMeta edgeMeta) {
        EdgeUpdate evicted = null;
        if (updateCache.size() >= UPDATE_CACHE_SIZE) { // Evict one
            evicted = updateCache.removeLast();
        }
        EdgeUpdate newEdgeUpdate =
                new EdgeUpdate(prec, dep, edgeMeta, prec, dep, edgeMeta);
        updateCache.addFirst(newEdgeUpdate);
        return evicted;
    }

    private void performOneInsert(int id, Ref prec, Ref dep) {
        LinkedList<CompressInfo> compressInfoList = findCompressInfo(prec, dep);

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            deleteLogEntry(dbContext, id, true);
            if (compressInfoList.isEmpty()) {
                insertDBEntry(dbContext, prec, dep, PatternType.NOTYPE,
                        Offset.noOffset, Offset.noOffset);
            } else {
                CompressInfo selectedInfo =
                        Collections.min(compressInfoList, compressInfoComparator);
                updateOneCompressEntry(dbContext, selectedInfo);
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateOneCompressEntry(DBContext dbContext,
                                        CompressInfo selectedInfo) throws SQLException {
        if (selectedInfo.isDuplicate) return;
        deleteDBEntry(dbContext, selectedInfo.prec, selectedInfo.dep, selectedInfo.edgeMeta);

        Ref newPrec = selectedInfo.prec.getBoundingBox(selectedInfo.candPrec);
        Ref newDep = selectedInfo.dep.getBoundingBox(selectedInfo.candDep);
        Pair<Offset, Offset> offsetPair = computeOffset(newPrec, newDep, selectedInfo.compType);
        insertDBEntry(dbContext, newPrec, newDep, selectedInfo.compType,
                offsetPair.x, offsetPair.y);
    }

    private Pair<CompressInfo, EdgeUpdate> findCompressInfoInUpdateCache(Ref prec, Ref dep,
                                                                   LinkedList<EdgeUpdate> updateCache) {
        Pair<CompressInfo, EdgeUpdate> updatePair = null;
        Iterator<EdgeUpdate> edgeUpdateIter = updateCache.iterator();
        while (edgeUpdateIter.hasNext()) {
            EdgeUpdate oneUpdate = edgeUpdateIter.next();
            CompressInfo compRes = findCompressionPattern(prec, dep,
                    oneUpdate.newPrec, oneUpdate.newDep, oneUpdate.newEdgeMeta);
            Boolean isDuplicate = compRes.isDuplicate;
            PatternType compType = compRes.compType;
            if (isDuplicate || compType != PatternType.NOTYPE) {
                updatePair = new Pair<>(compRes, oneUpdate);
                break;
            }
        }
        return updatePair;
    }

    private LinkedList<CompressInfo> findCompressInfo(Ref prec, Ref dep) {
        LinkedList<CompressInfo> compressInfoList = new LinkedList<>();
        findOverlapAndAdjacency(dep).forEach(candDep -> {
            findPrecs(candDep).forEach(candPrecWithMeta -> {
                CompressInfo compRes = findCompressionPattern(prec, dep,
                        candPrecWithMeta.getRef(), candDep, candPrecWithMeta.getEdgeMeta());
                addToCompressionInfoList(compressInfoList, compRes);
            });
        });

        return compressInfoList;
    }

    private void addToCompressionInfoList(LinkedList<CompressInfo> compressInfoList,
                                          CompressInfo compRes) {
        Boolean isDuplicate = compRes.isDuplicate;
        PatternType compType = compRes.compType;
        if (isDuplicate || compType != PatternType.NOTYPE) {
            compressInfoList.add(compRes);
        }
    }

    private Pair<Offset, Offset> computeOffset(Ref prec,
                                               Ref dep,
                                               PatternType compType) {
        Offset startOffset;
        Offset endOffset;

        assert(compType != PatternType.NOTYPE);
        switch (compType) {
            case TYPEZERO:
            case TYPEONE:
                startOffset = refToOffset(prec, dep, true);
                endOffset = refToOffset(prec, dep, false);
                break;
            case TYPETWO:
                startOffset = refToOffset(prec, dep, true);
                endOffset = Offset.noOffset;
                break;
            case TYPETHREE:
                startOffset = Offset.noOffset;
                endOffset = refToOffset(prec, dep, false);
                break;
            default: // TYPEFOUR
                startOffset = Offset.noOffset;
                endOffset = Offset.noOffset;
        }

        return new Pair<>(startOffset, endOffset);
    }

    private Offset refToOffset(Ref prec, Ref dep, boolean isStart) {
        if (isStart) {
            return new Offset(dep.getRow() - prec.getRow(),
                    dep.getColumn() - prec.getColumn());
        } else {
            return new Offset(dep.getLastRow() - prec.getLastRow(),
                    dep.getColumn() - prec.getLastColumn());
        }
    }

    private void deleteLogEntry(DBContext dbContext,
                                int id, boolean isInsert) throws SQLException {
        String query = "DELETE FROM " + logTableName +
                " WHERE id = ?";
        PreparedStatement retStmt = dbContext.getConnection().prepareStatement(query);
        retStmt.setInt(1, id);
        retStmt.execute();
        logEntryNum -= 1;
        if (isInsert) insertEntryNum -= 1;
    }

    private void deleteDBEntry(DBContext dbContext,
                               Ref prec,
                               Ref dep,
                               EdgeMeta edgeMeta) throws SQLException {
        deleteMemEntry(prec, dep, edgeMeta);

        String query = "DELETE FROM" + dependencyTableName +
                " WHERE  bookname  = ?" +
                " AND    sheetname =  ?" +
                " AND    range ~= ?" +
                " AND    dep_range ~= ?" +
                " AND    offsetRange ~= ?";
        PreparedStatement retStmt = dbContext.getConnection().prepareStatement(query);
        retStmt.setString(1, prec.getBookName());
        retStmt.setString(2, prec.getSheetName());
        retStmt.setObject(3, refToPGBox(prec), Types.OTHER);
        retStmt.setObject(4, refToPGBox(dep), Types.OTHER);
        retStmt.setObject(5, offsetToPGBox(edgeMeta.startOffset, edgeMeta.endOffset), Types.OTHER);
        retStmt.execute();

        refNum -= 1;
    }

    // TODO: when the transaction aborts, this mem structure will not be consistent
    private void insertDBEntry(DBContext dbContext,
                               Ref newPrec,
                               Ref newDep,
                               PatternType patternType,
                               Offset startOffset,
                               Offset endOffset) throws SQLException {
        insertMemEntry(newPrec, newDep, new EdgeMeta(patternType, startOffset, endOffset));
        String query = "INSERT INTO" + dependencyTableName +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement retStmt = dbContext.getConnection().prepareStatement(query);
        retStmt.setString(1, newPrec.getBookName());
        retStmt.setString(2, newPrec.getSheetName());
        retStmt.setObject(3, refToPGBox(newPrec), Types.OTHER);
        retStmt.setString(4, newDep.getBookName());
        retStmt.setString(5, newDep.getSheetName());
        retStmt.setObject(6, refToPGBox(newDep), Types.OTHER);
        retStmt.setBoolean(7, true);
        retStmt.setInt(8, patternType.ordinal());
        retStmt.setObject(9, offsetToPGBox(startOffset, endOffset));
        retStmt.execute();

        refNum += 1;
    }

    private void deleteMemEntry(Ref prec,
                                Ref dep,
                                EdgeMeta edgeMeta) {
        // This is not very efficient, but it is correct
        _mapCache.remove(prec);
        _reverseMapCache.remove(dep);
    }

    private void insertMemEntry(Ref prec,
                                Ref dep,
                                EdgeMeta edgeMeta) {
        List<RefWithMeta> depList = _mapCache.get(prec);
        depList.add(new RefWithMeta(dep, edgeMeta));
        _mapCache.put(prec, depList);

        List<RefWithMeta> precList = _reverseMapCache.get(dep);
        precList.add(new RefWithMeta(prec, edgeMeta));
        _reverseMapCache.put(dep, precList);
    }

    private void performOneDelete(int id, Ref delDep) {
        assert (delDep.getRow() == delDep.getLastRow() &&
                delDep.getColumn() == delDep.getLastColumn());

        AutoRollbackConnection connection = DBHandler.instance.getConnection();
        DBContext dbContext = new DBContext(connection);
        try {
            deleteLogEntry(dbContext, id, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        findOverlappingRefs(delDep).forEachRemaining(depRange -> {
            findPrecs(depRange).forEach(precRangeWithMeta -> {
                Ref precRange = precRangeWithMeta.getRef();
                EdgeMeta edgeMeta = precRangeWithMeta.getEdgeMeta();
                List<Pair<Ref, RefWithMeta>> newEdges =
                        deleteOneCell(precRange, depRange, edgeMeta, delDep);
                try {
                    deleteDBEntry(dbContext, precRange, depRange, edgeMeta);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                newEdges.forEach(pair -> {
                    Ref newPrec = pair.getX();
                    Ref newDep = pair.getY().getRef();
                    EdgeMeta newEdgeMeta = pair.getY().getEdgeMeta();
                    try {
                        insertDBEntry(dbContext, newPrec, newDep, newEdgeMeta.patternType,
                                newEdgeMeta.startOffset, newEdgeMeta.endOffset);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            });
        });
        connection.commit();
    }

    private List<Pair<Ref, RefWithMeta>> deleteOneCell(Ref prec, Ref dep,
                                                       EdgeMeta edgeMeta,
                                                       Ref delDep) {
        List<Pair<Ref, RefWithMeta>> ret = new LinkedList<>();
        boolean isDirectPrec = true;
        splitRangeByOneCell(dep, delDep).forEach(splitDep -> {
            Ref splitPrec = findUpdatePrecRef(prec, dep, edgeMeta, splitDep, isDirectPrec);
            ret.add(new Pair<>(splitPrec, new RefWithMeta(splitDep, edgeMeta)));
        });
        return ret;
    }

    private List<Ref> splitRangeByOneCell(Ref dep, Ref delDep) {
        int firstRow = dep.getRow();
        int firstCol = dep.getColumn();
        int lastRow = dep.getLastRow();
        int lastCol = dep.getLastColumn();

        int delRow = delDep.getRow();
        int delCol = delDep.getColumn();

        assert(firstRow == lastRow || firstCol == lastCol);
        List<Ref> refList = new LinkedList<>();

        // This range is actually a cell
        if (firstRow == lastRow && firstCol == lastCol) return refList;

        if (firstRow == lastRow) { // Row range
            if (delCol != firstCol)
                refList.add(coordToRef(dep, firstRow, firstCol, lastRow, delCol - 1));
            if (delCol != lastCol)
                refList.add(coordToRef(dep, firstRow, delCol + 1, lastRow, lastCol));
        } else { // Column range
            if (delRow != firstRow)
                refList.add(coordToRef(dep, firstRow, firstCol, delRow - 1, lastCol));
            if (delRow != lastRow)
                refList.add(coordToRef(dep, delRow + 1, firstCol, lastRow, lastCol));
        }

        return refList;
    }

    private void maintainRectToRefCache(String bookName, String sheetName) {
        if (refNum > CACHE_SIZE) { // discard _rectToRefCache
            _rectToRefCache = RTree.create();
        } else if (_rectToRefCache.isEmpty() && refNum > 0) { // rebuild the cache if necessary
            rebuildRectToRefCache(bookName, sheetName);
        }
    }

    private void rebuildRectToRefCache(String bookName, String sheetName) {
        String selectQuery =
                " SELECT range  FROM " + dependencyTableName +
                " WHERE bookname = ?    " +
                " AND sheetname = ?     " +
                " UNION " +
                " SELECT dep_range FROM " + dependencyTableName +
                " WHERE bookname = ?    " +
                " AND sheetname = ?     ";

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {

            stmt.setString(1, bookName);
            stmt.setString(2, sheetName);
            stmt.setString(3, bookName);
            stmt.setString(4, sheetName);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next()) {
                PGbox range = (PGbox) rs.getObject(1);
                _rectToRefCache.add(boxToRef(range, bookName, sheetName), boxtoRef(range));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Rectangle boxtoRef(PGbox box) {
        return RectangleFloat.create((float) box.point[0].x, (float) box.point[0].y,
                (float) (0.5 + box.point[1].x), (float) (0.5 + box.point[1].y));
    }

    private Ref coordToRef(Ref ref, int firstRow, int firstCol,
                          int lastRow, int lastCol) {
        return new RefImpl(ref.getBookName(), ref.getSheetName(),
                firstRow, firstCol, lastRow, lastCol);
    }

    private PGbox refToPGBox(Ref ref) {
        return new PGbox(ref.getRow(), ref.getColumn(),
                ref.getLastRow(), ref.getLastColumn());
    }

    private Ref boxToRef(PGbox range, String bookName, String sheetName) {
        return new RefImpl(bookName, sheetName,
                (int) range.point[0].x,
                (int) range.point[0].y,
                (int) range.point[1].x,
                (int) range.point[1].y);
    }

    private PGbox offsetToPGBox(Offset startOffset,
                                Offset endOffset) {
        return new PGbox(startOffset.getRowOffset(),
                startOffset.getColOffset(),
                endOffset.getRowOffset(),
                endOffset.getColOffset());
    }

    // Output a pair that includes whether it is a duplicate edge and the compress type
    private CompressInfo findCompressionPattern(Ref prec, Ref dep,
                                                Ref candPrec, Ref candDep, EdgeMeta metaData) {
        PatternType curCompType = metaData.patternType;

        // Check the duplicate edge
        if (isSubsume(candPrec, prec) && isSubsume(candDep, dep))
            return new CompressInfo(true, Direction.NODIRECTION, curCompType,
                    prec, dep, candPrec, candDep, metaData);

        // Otherwise, find the compression type
        Direction direction = findAdjacencyDirection(dep, candDep);
        Ref lastCandPrec = findLastPrec(prec, dep, metaData, direction);
        PatternType compressType =
                findCompPatternHelper(direction, prec, dep, candPrec, candDep, lastCandPrec);
        PatternType retCompType = PatternType.NOTYPE;
        if (curCompType == PatternType.NOTYPE) retCompType = compressType;
        else if (curCompType == compressType) retCompType = compressType;

        return new CompressInfo(false, direction, retCompType,
                prec, dep, candPrec, candDep, metaData);
    }

    private PatternType findCompPatternHelper(Direction direction,
                                              Ref prec, Ref dep,
                                              Ref candPrec, Ref candDep,
                                              Ref lastCandPrec) {
        PatternType compressType = PatternType.NOTYPE;
        if (isCompressibleTypeOne(lastCandPrec, prec, direction)) {
            compressType = PatternType.TYPEONE;
            if (isCompressibleTypeZero(candPrec, candDep, direction))
                compressType = PatternType.TYPEZERO;
        } else if (isCompressibleTypeTwo(lastCandPrec, prec, direction))
            compressType = PatternType.TYPETWO;
        else if (isCompressibleTypeThree(lastCandPrec, prec, direction))
            compressType = PatternType.TYPETHREE;
        else if (isCompressibleTypeFour(lastCandPrec, prec))
            compressType = PatternType.TYPEFOUR;

        return compressType;
    }

    private boolean isSubsume(Ref large, Ref small) {
        return large.getOverlap(small).equals(small);
    }

    private Iterable<Ref> findOverlapAndAdjacency(Ref ref) {
        LinkedList<Ref> res = new LinkedList<>();

        findOverlappingRefs(ref).forEachRemaining(res::addLast);
        Arrays.stream(Direction.values()).filter(direction -> direction != Direction.NODIRECTION)
                .forEach(direction ->
                        findOverlappingRefs(shiftRef(ref, direction))
                .forEachRemaining(adjRef -> {
                    if (isValidAdjacency(adjRef, ref)) res.addLast(adjRef); // valid adjacency
                }));

        return res;
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
                "  SELECT dep_range::box, patternType, offsetRange::box" +
                "  FROM " + dependencyTableName +
                "  WHERE  bookname  = ?" +
                "  AND    sheetname =  ?" +
                "  AND    range ~= ?";
        return findRefsFromDB(prec, selectQuery);
    }

    private List<RefWithMeta> findPrecsFromDB(Ref dep) {
        String selectQuery =
                "  SELECT range::box, patternType, offsetRange::box" +
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
            stmt.setObject(3, refToPGBox(ref), Types.OTHER);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next())
            {
                PGbox range = (PGbox) rs.getObject(1);
                Ref newRef = boxToRef(range, ref.getBookName(), ref.getSheetName());
                PatternType patternType = PatternType.values()[rs.getInt(2)];
                PGbox offsetRange = (PGbox) rs.getObject(3);
                Offset startOffset = new Offset((int) offsetRange.point[0].x,
                        (int) offsetRange.point[0].y);
                Offset endOffset = new Offset((int) offsetRange.point[1].x,
                        (int) offsetRange.point[1].y);
                EdgeMeta edgeMeta =
                        new EdgeMeta(patternType, startOffset, endOffset);
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
           stmt.setObject(3, refToPGBox(updateRef), Types.OTHER);

           stmt.setString(4, updateRef.getBookName());
           stmt.setString(5, updateRef.getSheetName());
           stmt.setObject(6, refToPGBox(updateRef), Types.OTHER);

           ResultSet rs =  stmt.executeQuery();
           while(rs.next())
           {
               PGbox range = (PGbox) rs.getObject(1);
               // The order of points is based on how postgres stores them
               result.add(boxToRef(range, updateRef.getBookName(), updateRef.getSheetName()));
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }

       return result.iterator();
    }

    @Override
    public void setBookSeries(SBookSeries series) {

    }

    @Override
    public void merge(DependencyTableAdv dependencyTable) {

    }

    @Override
    public Set<Ref> getDirectPrecedents(Ref dependent) {
        return null;
    }

    @Override
    public void adjustSheetIndex(String bookName, int index, int size) {

    }

    @Override
    public void moveSheetIndex(String bookName, int oldIndex, int newIndex) {

    }

    private class EdgeUpdate {
        Ref oldPrec;
        Ref oldDep;
        EdgeMeta oldEdgeMeta;

        Ref newPrec;
        Ref newDep;
        EdgeMeta newEdgeMeta;

        EdgeUpdate(Ref oldPrec,
                   Ref oldDep,
                   EdgeMeta oldEdgeMeta,
                   Ref newPrec,
                   Ref newDep,
                   EdgeMeta newEdgeMeta) {
            this.oldPrec = oldPrec;
            this.oldDep = oldDep;
            this.oldEdgeMeta = oldEdgeMeta;
            updateEdge(newPrec, newDep, newEdgeMeta);
        }

        void updateEdge(Ref newPrec,
                        Ref newDep,
                        EdgeMeta newEdgeMeta) {
            this.newPrec = newPrec;
            this.newDep = newDep;
            this.newEdgeMeta = newEdgeMeta;
        }
    }

    private class CompressInfo {
        Boolean isDuplicate;
        Direction direction;
        PatternType compType;
        Ref prec;
        Ref dep;
        Ref candPrec;
        Ref candDep;
        EdgeMeta edgeMeta;

        CompressInfo(Boolean isDuplicate,
                     Direction direction,
                     PatternType compType,
                     Ref prec, Ref dep,
                     Ref candPrec, Ref candDep, EdgeMeta edgeMeta) {
            this.isDuplicate = isDuplicate;
            this.direction = direction;
            this.compType = compType;
            this.prec = prec;
            this.dep = dep;
            this.candPrec = candPrec;
            this.candDep = candDep;
            this.edgeMeta = edgeMeta;
        }
    }

    private class CompressInfoComparator implements Comparator<CompressInfo> {

        @Override
        public int compare(CompressInfo infoA, CompressInfo infoB) {
            if (infoA.isDuplicate) return -1;
            else if (infoB.isDuplicate) return 1;
            else {
                int directionResult = infoA.direction.compareTo(infoB.direction);
                if (directionResult != 0) return directionResult;
                else {
                    return infoA.compType.compareTo(infoB.compType);
                }
            }
        }
    }

}
