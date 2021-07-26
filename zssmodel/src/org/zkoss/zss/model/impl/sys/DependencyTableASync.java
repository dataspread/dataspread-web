package org.zkoss.zss.model.impl.sys;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.model.LruCache;
import org.postgresql.geometric.PGbox;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.impl.sys.utils.*;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class DependencyTableASync extends DependencyTableAdv {
    private static final long serialVersionUID = 1L;

    private final String dependencyTableName = DBHandler.dependency;
    private final String fullDependencyTblName = DBHandler.fullDependency;
    private final String logTableName = DBHandler.stagedLog;

    private int CACHE_SIZE = 1000000;
    private int compressionConst = 1;

    protected LruCache<Ref, List<Ref>> _mapCache = new LruCache<>(CACHE_SIZE);
    private int logEntryNum = 0;
    private int insertEntryNum = 0;

    public DependencyTableASync() {}

    @Override
    public long getLastLookupTime() {
        return 0;
    }

    @Override
    public Set<Ref> getDependents(Ref precedent) {
        LinkedHashSet<Ref> result = new LinkedHashSet<>();

        if (RefUtils.isValidRef(precedent)) {
            long start = System.currentTimeMillis();
            findDeps(precedent).forEach(result::add);

            if (insertEntryNum != 0) {
                final boolean isInsertOnly = true;
                LogUtils.getLogEntries(logTableName, precedent.getBookName(),
                        precedent.getSheetName(), isInsertOnly)
                        .forEach(logEntry -> {
                            findDeps(logEntry.prec).forEach(result::add);
                            findDeps(logEntry.dep).forEach(result::add);
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
        // throw new RuntimeException("ASync: not support getting direct dependents yet");
        return new HashSet<>();
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
        Map<Ref, List<Ref>> compressedGraph =
                ASyncCompressorUtils.buildCompressedGraph(edgeBatch, compressionConst);
        insertCompressedGraph(compressedGraph);
        insertFullGraph(edgeBatch);
        addBatchTime = System.currentTimeMillis() - start;
    }

    @Override
    public void configDepedencyTable(int cacheSize, int compConstant) {
        CACHE_SIZE = cacheSize;
        compressionConst = compConstant;
    }

    public List<Pair<Ref, Ref>> getLoadedBatch(String bookName, String sheetName) {
        boolean isInsertOnly = false;
        LinkedList<Pair<Ref, Ref>> loadedBatch = new LinkedList<>();
        LogUtils.getLogEntries(logTableName, bookName, sheetName, isInsertOnly).forEach(oneLog -> {
            Pair<Ref, Ref> oneEdge = new Pair<>(oneLog.prec, oneLog.dep);
            loadedBatch.addLast(oneEdge);
        });
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            LogUtils.deleteLoadedLogs(dbContext, logTableName, bookName, sheetName);
            logEntryNum -= loadedBatch.size();
            insertEntryNum -= loadedBatch.size();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loadedBatch;
    }

    @Override
    public void refreshCache(String bookName, String sheetName) {
        long start = System.currentTimeMillis();

        boolean isInsertOnly = false;
        LogUtils.getLogEntries(logTableName, bookName, sheetName, isInsertOnly).forEach(oneLog -> {
            if (oneLog.isInsert) performOneInsert(oneLog.id, oneLog.prec, oneLog.dep);
            else performOneDelete(oneLog.id, oneLog.dep);
        });

        deleteCompressedGraph(bookName, sheetName);
        Map<Ref, List<Ref>> compressedGraph =
                ASyncCompressorUtils.buildCompressedGraph(
                        getFullDependency(bookName, sheetName), compressionConst);
        insertCompressedGraph(compressedGraph);

        refreshCacheTime = System.currentTimeMillis() - start;
    }

    private void performOneInsert(int logId, Ref prec, Ref dep) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            LogUtils.deleteLogEntry(dbContext, logTableName, logId);
            logEntryNum -= 1;
            insertEntryNum -= 1;

            String insertQuery = "INSERT INTO " + fullDependencyTblName + " VALUES (?,?,?,?,?,?,?)";
            addQuery(insertQuery, dep, prec);

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performOneDelete(int logId, Ref dep) {
        AutoRollbackConnection connection = DBHandler.instance.getConnection();
        DBContext dbContext = new DBContext(connection);
        try {
            LogUtils.deleteLogEntry(dbContext, logTableName, logId);
            logEntryNum -= 1;

            String deleteQuery = "DELETE FROM " + fullDependencyTblName +
                    " WHERE dep_bookname  = ?" +
                    " AND   dep_sheetname =  ?" +
                    " AND   dep_range && ?";

            PreparedStatement stmt = connection.prepareStatement(deleteQuery);
            stmt.setString(1, dep.getBookName());
            stmt.setString(2, dep.getSheetName());
            stmt.setObject(3, new PGbox(dep.getRow(),
                    dep.getColumn(), dep.getLastRow(),
                    dep.getLastColumn()), Types.OTHER);

            stmt.execute();

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection.close();
    }

    private List<Pair<Ref, Ref>> getFullDependency(String bookName,
                                                   String sheetName) {
        List<Pair<Ref, Ref>> result = new LinkedList<>();
        String selectQuery = "  SELECT range::box, dep_range::box " +
                        "  FROM " + fullDependencyTblName +
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
                PGbox dep_range = (PGbox) rs.getObject(2);
                Ref prec = RefUtils.boxToRef(range, bookName, sheetName);
                Ref dep = RefUtils.boxToRef(dep_range, bookName, sheetName);
                result.add(new Pair<>(prec, dep));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private Iterable<Ref> findDeps(Ref prec) {
        List<Ref> depIter = _mapCache.get(prec);
        if (depIter == null || depIter.isEmpty()) {
            depIter = findDepsFromDB(prec);
            _mapCache.put(prec, depIter);
        }
        return depIter;
    }

    private List<Ref> findDepsFromDB(Ref prec) {
        String selectQuery =
                "  SELECT dep_range::box " +
                        "  FROM " + dependencyTableName +
                        "  WHERE  bookname  = ?" +
                        "  AND    sheetname =  ?" +
                        "  AND    range ~= ?";
        return findRefsFromDB(prec, selectQuery);
    }

    private List<Ref> findRefsFromDB(Ref ref, String selectQuery) {
        List<Ref> result = new LinkedList<>();
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
                result.add(newRef);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void deleteCompressedGraph(String bookName, String sheetName) {
        String deleteQuery = "DELETE FROM " + dependencyTableName +
                " WHERE dep_bookname  = ?" +
                " AND   dep_sheetname =  ?";

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setString(1, bookName);
            stmt.setString(2, sheetName);

            stmt.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertCompressedGraph(Map<Ref, List<Ref>> compressedGraph) {
        String insertQuery = "INSERT INTO " + dependencyTableName + " VALUES (?,?,?,?,?,?,?)";
        compressedGraph.forEach((prec, dependants) -> {
            dependants.forEach(dep -> addQuery(insertQuery, dep, prec));
            _mapCache.put(prec, dependants);
        });
    }

    private void insertFullGraph(List<Pair<Ref, Ref>> fullGraph) {
        String insertQuery = "INSERT INTO " + fullDependencyTblName + " VALUES (?,?,?,?,?,?,?)";
        fullGraph.forEach(edge -> {
            Ref prec = edge.getX();
            Ref dep = edge.getY();
            addQuery(insertQuery, dep, prec);
        });
    }

    private void addQuery(String insertQuery, Ref dependant, Ref precedent) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setString(1, precedent.getBookName());
            stmt.setString(2, precedent.getSheetName());
            stmt.setObject(3, new PGbox(precedent.getRow(),
                    precedent.getColumn(), precedent.getLastRow(),
                    precedent.getLastColumn()), Types.OTHER);
            stmt.setString(4, dependant.getBookName());
            stmt.setString(5, dependant.getSheetName());
            stmt.setObject(6, new PGbox(dependant.getRow(),
                    dependant.getColumn(), dependant.getLastRow(),
                    dependant.getLastColumn()), Types.OTHER);
            stmt.setBoolean(7, true);
            stmt.execute();
            connection.commit();
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
}

