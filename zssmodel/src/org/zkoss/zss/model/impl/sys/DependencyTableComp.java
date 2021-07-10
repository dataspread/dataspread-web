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
import org.zkoss.zss.model.impl.sys.utils.*;
import org.zkoss.zss.model.sys.dependency.Ref;

import org.model.LruCache;

import java.sql.*;
import java.util.*;

import static org.zkoss.zss.model.impl.sys.utils.PatternTools.*;

public class DependencyTableComp extends DependencyTableAdv {

    private final String dependencyTableName = DBHandler.compressDependency;
    private final String logTableName = DBHandler.stagedLog;

    private int CACHE_SIZE = 1000000;
    private final int UPDATE_CACHE_SIZE = 100;

    /** Map<dependant, precedent> */
    protected LruCache<Ref, List<RefWithMeta>> _mapCache = new LruCache<>(CACHE_SIZE);
    protected LruCache<Ref, List<RefWithMeta>> _reverseMapCache = new LruCache<>(CACHE_SIZE);
    private RTree<Ref, Rectangle> _rectToRefCache  = RTree.create();
    private int refNum = 0;
    private int logEntryNum = 0;
    private int insertEntryNum = 0;

    private boolean refreshCacheMode = false;
    private Map<Ref, List<RefWithMeta>> _mapFullCache = new HashMap<>();
    private Map<Ref, List<RefWithMeta>> _reverseMapFullCache = new HashMap<>();

    private CompressInfoComparator compressInfoComparator = new CompressInfoComparator();

    public DependencyTableComp() {}


    @Override
    public Set<Ref> getDependents(Ref precedent) {
        final boolean isDirectDep = false;
        LinkedHashSet<Ref> result = new LinkedHashSet<>();

        if (RefUtils.isValidRef(precedent)) {
            long start = System.currentTimeMillis();
            getDependentsInternal(precedent, result, isDirectDep);

            if (insertEntryNum != 0) {
                final boolean isInsertOnly = true;
                LogUtils.getLogEntries(logTableName, precedent.getBookName(), precedent.getSheetName(), isInsertOnly)
                        .forEach(logEntry -> {
                            getDependentsInternal(logEntry.prec, result, isDirectDep);
                            getDependentsInternal(logEntry.dep, result, isDirectDep);
                        });
            }
            lookupTime = System.currentTimeMillis() - start;
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
        //  refreshCache(precedent.getBookName(), precedent.getSheetName());

        //  boolean isDirectDep = true;
        //  LinkedHashSet<Ref> result = new LinkedHashSet<>() ;
        //  if (RefUtils.isValidRef(precedent)) getDependentsInternal(precedent, result, isDirectDep);
        // return result;
        return new LinkedHashSet<>();
    }

    @Override
    public void add(Ref dependent, Ref precedent) {
        String insertQuery = "INSERT INTO " + logTableName
                + " VALUES (?,?,?,?,?,?,?,TRUE)";
        LogUtils.appendOneLog(insertQuery, logEntryNum, precedent, dependent);
        logEntryNum += 1;
        insertEntryNum += 1;
    }

    @Override
    public void addPreDep(Ref precedent, Set<Ref> dependent) {

    }

    @Override
    public void clearDependents(Ref dependent) {
        String deleteQuery = "INSERT INTO " + logTableName
                + " VALUES (?,?,?,?,?,?,?,FALSE)";
        LogUtils.appendOneLog(deleteQuery, logEntryNum, null, dependent);
        logEntryNum += 1;
    }

    @Override
    public Set<Ref> searchPrecedents(RefFilter filter) {
        return null;
    }

    @Override
    public void addBatch(String bookName, String sheetName, List<Pair<Ref, Ref>> edgeBatch) {
        long start = System.currentTimeMillis();

        _rectToRefCache = RTree.create();
        rebuildRectToRefCache(bookName, sheetName);

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            LinkedList<EdgeUpdate> updateCache = new LinkedList<>();
            boolean isInsert = true;
            edgeBatch.forEach(oneEdge -> {
                Ref prec = oneEdge.getX();
                Ref dep = oneEdge.getY();

                fastModify(prec, dep, isInsert, updateCache, dbContext);
            });

            flushUpdateCache(updateCache, dbContext);
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        maintainRectToRefCache(bookName, sheetName);

        addBatchTime = System.currentTimeMillis() - start;
    }

    // Flush the update cache
    private void flushUpdateCache(LinkedList<EdgeUpdate> updateCache,
                                  DBContext dbContext) {
        updateCache.forEach(edgeUpdate -> {
            if (edgeUpdate.hasUpdate()) {
                try {
                    deleteDBEntry(dbContext, edgeUpdate.oldPrec, edgeUpdate.oldDep, edgeUpdate.oldEdgeMeta);
                    insertDBEntry(dbContext, edgeUpdate.newPrec, edgeUpdate.newDep, edgeUpdate.newEdgeMeta.patternType,
                            edgeUpdate.newEdgeMeta.startOffset, edgeUpdate.newEdgeMeta.endOffset);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fastModify(Ref prec, Ref dep, boolean isInsert,
                            LinkedList<EdgeUpdate> updateCache,
                            DBContext dbContext) {
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
                    if (evicted != null) {
                        deleteDBEntry(dbContext, evicted.oldPrec, evicted.oldDep, evicted.oldEdgeMeta);
                        insertDBEntry(dbContext, evicted.newPrec, evicted.newDep, evicted.newEdgeMeta.patternType,
                                evicted.newEdgeMeta.startOffset, evicted.newEdgeMeta.endOffset);
                    }
                } else {
                    EdgeMeta noTypeEdgeMeta = new EdgeMeta(PatternType.NOTYPE,
                            Offset.noOffset, Offset.noOffset);
                    EdgeUpdate evicted = addToUpdateCache(updateCache, prec, dep, noTypeEdgeMeta);
                    if (evicted != null) {
                        deleteDBEntry(dbContext, evicted.oldPrec, evicted.oldDep, evicted.oldEdgeMeta);
                        insertDBEntry(dbContext, evicted.newPrec, evicted.newDep, evicted.newEdgeMeta.patternType,
                                evicted.newEdgeMeta.startOffset, evicted.newEdgeMeta.endOffset);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void configDepedencyTable(int cacheSize, int compConstant) {
        CACHE_SIZE = cacheSize/3;
    }

    @Override
    public List<Pair<Ref, Ref>> getLoadedBatch(String bookName, String sheetName) {
        boolean isInsertOnly = false;
        LinkedList<Pair<Ref, Ref>> loadedBatch = new LinkedList<>();
        LogUtils.getLogEntries(logTableName, bookName, sheetName, isInsertOnly).forEach(oneLog -> {
            Pair<Ref, Ref> oneEdge = new Pair<>(oneLog.prec, oneLog.dep);
            loadedBatch.addLast(oneEdge);
        });

        deleteAllLogs(bookName, sheetName);
        return loadedBatch;
    }

    @Override
    public void refreshCache(String bookName, String sheetName) {
        long start = System.currentTimeMillis();

        refreshCacheMode = true;

        loadEverything(bookName, sheetName);
        boolean isInsertOnly = false;
        LogUtils.getLogEntries(logTableName, bookName, sheetName, isInsertOnly)
                .forEach(logEntry -> {
                    if (logEntry.isInsert) performOneInsert(logEntry.prec, logEntry.dep);
                    else performOneDelete(logEntry.dep);
                });

        updateDBFromCache(bookName, sheetName);
        updateCache();
        deleteAllLogs(bookName, sheetName);
        refreshCacheMode = false;

        maintainRectToRefCache(bookName, sheetName);

        refreshCacheTime = System.currentTimeMillis() - start;
    }

    private void loadEverything(String bookName, String sheetName) {
        _rectToRefCache = RTree.create();
        String selectQuery =
                "  SELECT range::box, dep_range::box, pattern_type, offsetRange::box" +
                        "  FROM " + dependencyTableName +
                        "  WHERE  bookname  = ?" +
                        "  AND    sheetname =  ?";

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setString(1, bookName);
            stmt.setString(2, sheetName);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next())
            {
                PGbox range = (PGbox) rs.getObject(1);
                Ref prec = RefUtils.boxToRef(range, bookName, sheetName);
                PGbox dep_range = (PGbox) rs.getObject(2);
                Ref dep = RefUtils.boxToRef(dep_range, bookName, sheetName);
                PatternType patternType = PatternType.values()[rs.getInt(3)];
                PGbox offsetRange = (PGbox) rs.getObject(4);
                Offset startOffset = new Offset((int) offsetRange.point[0].x,
                        (int) offsetRange.point[0].y);
                Offset endOffset = new Offset((int) offsetRange.point[1].x,
                        (int) offsetRange.point[1].y);
                EdgeMeta edgeMeta =
                        new EdgeMeta(patternType, startOffset, endOffset);
                insertMemEntry(prec, dep, edgeMeta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDBFromCache(String bookname, String sheetname) {
        String deleteQuery =
                "DELETE FROM " + dependencyTableName +
                        " WHERE  bookname  = ?" +
                        " AND    sheetname =  ?";

        String insertQuery = "INSERT INTO " + dependencyTableName +
                " VALUES (?,?,?,?,?,?,?,?,?)";

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            PreparedStatement delStmt = connection.prepareStatement(deleteQuery);
            delStmt.setString(1, bookname);
            delStmt.setString(2, sheetname);
            delStmt.execute();

            _mapFullCache.forEach((Ref prec, List<RefWithMeta> depList) -> depList.forEach((RefWithMeta depWithMeta) -> {
                Ref dep = depWithMeta.getRef();
                PatternType patternType = depWithMeta.getPatternType();
                Offset startOffset = depWithMeta.getEdgeMeta().startOffset;
                Offset endOffset = depWithMeta.getEdgeMeta().endOffset;

                PreparedStatement retStmt = null;
                try {
                    retStmt = connection.prepareStatement(insertQuery);
                    retStmt.setString(1, bookname);
                    retStmt.setString(2, sheetname);
                    retStmt.setObject(3, RefUtils.refToPGBox(prec), Types.OTHER);
                    retStmt.setString(4, bookname);
                    retStmt.setString(5, sheetname);
                    retStmt.setObject(6, RefUtils.refToPGBox(dep), Types.OTHER);
                    retStmt.setBoolean(7, true);
                    retStmt.setInt(8, patternType.ordinal());
                    retStmt.setObject(9, RefUtils.offsetToPGBox(startOffset, endOffset));
                    retStmt.execute();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }));

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCache() {
        _mapCache = new LruCache<>(CACHE_SIZE);
        _mapFullCache.forEach((Ref prec, List<RefWithMeta> depList) -> _mapCache.put(prec, depList));
        _mapFullCache = new HashMap<>();

        _reverseMapCache = new LruCache<>(CACHE_SIZE);
        _reverseMapFullCache.forEach((Ref dep, List<RefWithMeta> precList) -> _reverseMapCache.put(dep, precList));
        _reverseMapFullCache = new HashMap<>();
    }

    private void deleteAllLogs(String bookName, String sheetName) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            LogUtils.deleteLoadedLogs(dbContext, logTableName, bookName, sheetName);
            logEntryNum = 0;
            insertEntryNum = 0;
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                    if (!isContained(result, depUpdateRef)) {
                        result.add(depUpdateRef);
                        if (!isDirectDep) updateQueue.add(depUpdateRef);
                    }
                });
            }
        }
    }

    private boolean isContained(LinkedHashSet<Ref> result, Ref input) {
        return result.stream().anyMatch(ref -> isSubsume(ref, input));
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

    private void performOneInsert(Ref prec, Ref dep) {
        LinkedList<CompressInfo> compressInfoList = findCompressInfo(prec, dep);

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
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
        deleteDBEntry(dbContext, selectedInfo.candPrec, selectedInfo.candDep, selectedInfo.edgeMeta);

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
                startOffset = RefUtils.refToOffset(prec, dep, true);
                endOffset = RefUtils.refToOffset(prec, dep, false);
                break;
            case TYPETWO:
                startOffset = RefUtils.refToOffset(prec, dep, true);
                endOffset = Offset.noOffset;
                break;
            case TYPETHREE:
                startOffset = Offset.noOffset;
                endOffset = RefUtils.refToOffset(prec, dep, false);
                break;
            default: // TYPEFOUR
                startOffset = Offset.noOffset;
                endOffset = Offset.noOffset;
        }

        return new Pair<>(startOffset, endOffset);
    }

    private void deleteDBEntry(DBContext dbContext,
                               Ref prec,
                               Ref dep,
                               EdgeMeta edgeMeta) throws SQLException {
        deleteMemEntry(prec, dep, edgeMeta);

        if (!refreshCacheMode) {
            String query = "DELETE FROM " + dependencyTableName +
                    " WHERE  bookname  = ?" +
                    " AND    sheetname =  ?" +
                    " AND    range ~= ?" +
                    " AND    dep_range ~= ?" +
                    " AND    offsetRange ~= ?";
            PreparedStatement retStmt = dbContext.getConnection().prepareStatement(query);
            retStmt.setString(1, prec.getBookName());
            retStmt.setString(2, prec.getSheetName());
            retStmt.setObject(3, RefUtils.refToPGBox(prec), Types.OTHER);
            retStmt.setObject(4, RefUtils.refToPGBox(dep), Types.OTHER);
            retStmt.setObject(5, RefUtils.offsetToPGBox(edgeMeta.startOffset, edgeMeta.endOffset), Types.OTHER);
            retStmt.execute();
        }

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

        if (!refreshCacheMode) {
            String query = "INSERT INTO " + dependencyTableName +
                    " VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement retStmt = dbContext.getConnection().prepareStatement(query);
            retStmt.setString(1, newPrec.getBookName());
            retStmt.setString(2, newPrec.getSheetName());
            retStmt.setObject(3, RefUtils.refToPGBox(newPrec), Types.OTHER);
            retStmt.setString(4, newDep.getBookName());
            retStmt.setString(5, newDep.getSheetName());
            retStmt.setObject(6, RefUtils.refToPGBox(newDep), Types.OTHER);
            retStmt.setBoolean(7, true);
            retStmt.setInt(8, patternType.ordinal());
            retStmt.setObject(9, RefUtils.offsetToPGBox(startOffset, endOffset));
            retStmt.execute();
        }

        refNum += 1;
    }

    private void deleteMemEntry(Ref prec,
                                Ref dep,
                                EdgeMeta edgeMeta) {
        if (refreshCacheMode) {
            List<RefWithMeta> depList = _mapFullCache.get(prec);
            if (depList != null) {
                depList.remove(new RefWithMeta(dep, edgeMeta));
                if (depList.isEmpty()) _mapFullCache.remove(prec);
            }

            List<RefWithMeta> precList = _reverseMapFullCache.get(dep);
            if (precList != null) {
                precList.remove(new RefWithMeta(prec, edgeMeta));
                if (precList.isEmpty()) _reverseMapFullCache.remove(dep);
            }
        } else { // This is not very efficient, but it is correct
            _mapCache.remove(prec);
            _reverseMapCache.remove(dep);
        }

        _rectToRefCache = _rectToRefCache.delete(prec, RefUtils.refToRect(prec));
        _rectToRefCache = _rectToRefCache.delete(dep, RefUtils.refToRect(dep));
    }

    private void insertMemEntry(Ref prec,
                                Ref dep,
                                EdgeMeta edgeMeta) {
        if (refreshCacheMode) {
            List<RefWithMeta> depList = _mapFullCache.getOrDefault(prec, new LinkedList<>());
            depList.add(new RefWithMeta(dep, edgeMeta));
            _mapFullCache.put(prec, depList);

            List<RefWithMeta> precList = _reverseMapFullCache.getOrDefault(dep, new LinkedList<>());
            precList.add(new RefWithMeta(prec, edgeMeta));
            _reverseMapFullCache.put(dep, precList);

        } else {
            if (_mapCache.containsKey(prec)) {
                List<RefWithMeta> depList = _mapCache.get(prec);
                depList.add(new RefWithMeta(dep, edgeMeta));
                _mapCache.put(prec, depList);
            }

            if (_reverseMapCache.containsKey(dep)) {
                List<RefWithMeta> precList = _reverseMapCache.get(dep);
                precList.add(new RefWithMeta(prec, edgeMeta));
                _reverseMapCache.put(dep, precList);
            }
        }

        _rectToRefCache = _rectToRefCache.add(prec, RefUtils.refToRect(prec));
        _rectToRefCache = _rectToRefCache.add(dep, RefUtils.refToRect(dep));
    }

    private void performOneDelete(Ref delDep) {
        assert (delDep.getRow() == delDep.getLastRow() &&
                delDep.getColumn() == delDep.getLastColumn());

        AutoRollbackConnection connection = DBHandler.instance.getConnection();
        DBContext dbContext = new DBContext(connection);
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
                    if (newDep.getType() == Ref.RefType.CELL) {
                        performOneInsert(newPrec, newDep);
                    } else  {
                        try {
                            insertDBEntry(dbContext, newPrec, newDep, newEdgeMeta.patternType,
                                    newEdgeMeta.startOffset, newEdgeMeta.endOffset);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
        });
        connection.commit();
        connection.close();
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
                refList.add(RefUtils.coordToRef(dep, firstRow, firstCol, lastRow, delCol - 1));
            if (delCol != lastCol)
                refList.add(RefUtils.coordToRef(dep, firstRow, delCol + 1, lastRow, lastCol));
        } else { // Column range
            if (delRow != firstRow)
                refList.add(RefUtils.coordToRef(dep, firstRow, firstCol, delRow - 1, lastCol));
            if (delRow != lastRow)
                refList.add(RefUtils.coordToRef(dep, delRow + 1, firstCol, lastRow, lastCol));
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
                " SELECT range, dep_range FROM " + dependencyTableName +
                " WHERE bookname = ?    " +
                " AND sheetname = ?     ";

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {

            stmt.setString(1, bookName);
            stmt.setString(2, sheetName);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next()) {
                PGbox range = (PGbox) rs.getObject(1);
                _rectToRefCache = _rectToRefCache.add(RefUtils.boxToRef(range, bookName, sheetName),
                        RefUtils.boxToRect(range));

                PGbox dep_range = (PGbox) rs.getObject(2);
                _rectToRefCache = _rectToRefCache.add(RefUtils.boxToRef(dep_range, bookName, sheetName),
                        RefUtils.boxToRect(dep_range));
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        Ref lastCandPrec = findLastPrec(candPrec, candDep, metaData, direction);
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
            if (isCompressibleTypeZero(prec, dep, lastCandPrec))
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
        if (large.getOverlap(small) == null) return false;
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
                                })
                );

        return res;
    }

    private Rectangle getRectangleFromRef(Ref ref)
    {
        return RectangleFloat.create(ref.getRow(),ref.getColumn(),
                (float) 0.5 + ref.getLastRow(), (float) 0.5 + ref.getLastColumn());
    }

    private Iterable<RefWithMeta> findPrecs(Ref dep) {
        List<RefWithMeta> precIter;
        if (refreshCacheMode) {
            precIter = _reverseMapFullCache.getOrDefault(dep, new LinkedList<>());
        } else {
            precIter = _reverseMapCache.get(dep);
            if (precIter == null || precIter.isEmpty()) {
                precIter = findPrecsFromDB(dep);
                _reverseMapCache.put(dep, precIter);
            }
        }
        return precIter;
    }

    private Iterable<RefWithMeta> findDeps(Ref prec) {
        List<RefWithMeta> depIter = null;
        if (refreshCacheMode) {
            depIter = _mapFullCache.getOrDefault(prec, new LinkedList<>());
        } else {
            depIter = _mapCache.get(prec);
            if (depIter == null || depIter.isEmpty()) {
                depIter = findDepsFromDB(prec);
                _mapCache.put(prec, depIter);
            }
        }
        return depIter;
    }

    private List<RefWithMeta> findDepsFromDB(Ref prec) {
        String selectQuery =
                "  SELECT dep_range::box, pattern_type, offsetRange::box" +
                "  FROM " + dependencyTableName +
                "  WHERE  bookname  = ?" +
                "  AND    sheetname =  ?" +
                "  AND    range ~= ?";
        return findRefsFromDB(prec, selectQuery);
    }

    private List<RefWithMeta> findPrecsFromDB(Ref dep) {
        String selectQuery =
                "  SELECT range::box, pattern_type, offsetRange::box" +
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
            stmt.setObject(3, RefUtils.refToPGBox(ref), Types.OTHER);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next())
            {
                PGbox range = (PGbox) rs.getObject(1);
                Ref newRef = RefUtils.boxToRef(range, ref.getBookName(), ref.getSheetName());
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

        if (!_rectToRefCache.isEmpty()) {
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
        String selectQueryA =
                "  SELECT range::box from " + dependencyTableName +
                "  WHERE  bookname  = ?" +
                "  AND    sheetname =  ?" +
                "  AND    range && ?";

        String selectQueryB =
                "  SELECT dep_range::box from " + dependencyTableName +
                "  WHERE  dep_bookname  = ?" +
                "  AND    dep_sheetname =  ?" +
                "  AND    dep_range && ?";

       HashSet<Ref> resultSet = new HashSet<>();
       findOverlappingRefsHelper(resultSet, selectQueryA, updateRef);
       findOverlappingRefsHelper(resultSet, selectQueryB, updateRef);

       return resultSet.iterator();
    }

    private void findOverlappingRefsHelper(HashSet<Ref> resultSet,
                                           String selectQuery,
                                           Ref updateRef) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setString(1, updateRef.getBookName());
            stmt.setString(2, updateRef.getSheetName());
            stmt.setObject(3, RefUtils.refToPGBox(updateRef), Types.OTHER);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next())
            {
                PGbox range = (PGbox) rs.getObject(1);
                Ref resRef = RefUtils.boxToRef(range, updateRef.getBookName(), updateRef.getSheetName());
                resultSet.add(resRef);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        boolean hasUpdate() {
            return !(oldPrec.equals(newPrec) &&
                     oldDep.equals(oldPrec) &&
                     oldEdgeMeta.equals(newEdgeMeta));
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
