package org.zkoss.zss.model.impl.sys;

import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.model.LruCache;

import org.zkoss.util.Pair;
import org.zkoss.zss.model.impl.sys.utils.LogUtils;
import org.zkoss.zss.model.impl.sys.utils.RefUtils;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.SBookSeries;
import org.postgresql.geometric.PGbox;
import org.zkoss.util.logging.Log;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.*;

public class DependencyTablePGImplCacheRTreeV2 extends DependencyTableAdv {

    private static final long serialVersionUID = 1L;
    private static final Log _logger = Log.lookup(DependencyTablePGImpl.class.getName());

    private final String logTableName = DBHandler.stagedLog;

    private static int CACHE_SIZE = 1000000;
    private static int MAX_CACHE = 100000000;
    private LruCache<Ref, Set<Ref>> depToPrcCache = new LruCache<>(CACHE_SIZE);
    private LruCache<Ref, Set<Ref>> prcToDepCache = new LruCache<>(CACHE_SIZE);
    private RTree<Ref, Rectangle> rectToRefCache  = RTree.create();
    private int refNum = 0;
    private int logEntryNum = 0;
    private int insertEntryNum = 0;

    public DependencyTablePGImplCacheRTreeV2() {}

    /**
     *
     * @param src The node to start DFS from.
     * @param neighbors A function that takes a node as input and returns a set of
     * neighbors to explore.
     * @param func A function that accepts a node as input and performs some operation
     * on it.
     *
     * Applies `func` to `src` and all nodes reachable from `src`.
     */
    private void dfs (Ref src, Function<Ref, Set<Ref>> neighbors, Consumer<Ref> func) {
        Set<Ref> visitedSet = new HashSet<>();
        Deque<Ref> frontier = new ArrayDeque<>(Collections.singleton(src));
        while (frontier.size() > 0) {
            Ref curr = frontier.poll();
            if (!visitedSet.contains(curr)) {
                visitedSet.add(curr);
                func.accept(curr);
                frontier.addAll(neighbors.apply(curr));
            }
        }
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

    private Iterator<Ref> findOverlappingRefsFromDB(Ref updateRef) {
        String selectQueryA =
                "  SELECT range::box from dependency" +
                        "  WHERE  bookname  = ?" +
                        "  AND    sheetname =  ?" +
                        "  AND    range && ?";

        String selectQueryB =
                "  SELECT dep_range::box from dependency" +
                        "  WHERE  dep_bookname  = ?" +
                        "  AND    dep_sheetname =  ?" +
                        "  AND    dep_range && ?";

        HashSet<Ref> resultSet = new HashSet<>();
        findOverlappingRefsHelper(resultSet, selectQueryA, updateRef);
        findOverlappingRefsHelper(resultSet, selectQueryB, updateRef);
        return resultSet.iterator();
    }

    /**
     *
     * @param ref
     * @return All refs that overlap with ref.
     */
    private Set<Ref> getNeighbors(Ref ref) {
        Iterator<Entry<Ref, Rectangle>> rTreeIter = this.rectToRefCache.search(RefUtils.refToRect(ref)).toBlocking().getIterator();
        Set<Ref> neighbors = new HashSet<>();
        if (rTreeIter.hasNext()) {
            while (rTreeIter.hasNext()) {
                neighbors.add(rTreeIter.next().value());
            }
        } else {
            Iterator<Ref> dbIter = this.findOverlappingRefsFromDB(ref);
            while (dbIter.hasNext()) {
                neighbors.add(dbIter.next());
            }
        }
        return neighbors;
    }

    @Override
    public void configDepedencyTable(int cacheSize, int compConstant) {
        CACHE_SIZE = cacheSize;
        depToPrcCache = new LruCache<>(CACHE_SIZE);
        prcToDepCache = new LruCache<>(CACHE_SIZE);
    }

    @Override
    public void setBookSeries(SBookSeries series) {
        //Do nothing.
        //TODO: remove the concept of book series.
    }

    @Override
    public void addPreDep(Ref precedent, Set<Ref> dependent) {
        throw new UnsupportedOperationException();
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
    public void addBatch(String bookName, String sheetName, List<Pair<Ref, Ref>> edgeBatch) {
        long start = System.currentTimeMillis();

        String insertQuery = "INSERT INTO dependency VALUES (?,?,?,?,?,?,?)";
        // AutoRollbackConnection connection = DBHandler.instance.getConnection();

        edgeBatch.forEach(pair -> {
            Ref prec = pair.getX();
            Ref dep = pair.getY();

            performOneInsert(prec, dep);
            // try {
            //     PreparedStatement stmt = connection.prepareStatement(insertQuery);
            //     stmt.setString(1, prec.getBookName());
            //     stmt.setString(2, prec.getSheetName());
            //     stmt.setObject(3, new PGbox(prec.getRow(),
            //             prec.getColumn(), prec.getLastRow(),
            //             prec.getLastColumn()), Types.OTHER);
            //     stmt.setString(4, dep.getBookName());
            //     stmt.setString(5, dep.getSheetName());
            //     stmt.setObject(6, new PGbox(dep.getRow(),
            //             dep.getColumn(), dep.getLastRow(),
            //             dep.getLastColumn()), Types.OTHER);
            //     stmt.setBoolean(7, true);
            //     stmt.execute();
            // } catch (SQLException e) {
            //     e.printStackTrace();
            // }

        });

        // connection.commit();
        // connection.close();

        addBatchTime = System.currentTimeMillis() - start;
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

        // String selectQuery =
        //         "  SELECT range::box, dep_range::box " +
        //                 "  FROM " + DBHandler.dependency +
        //                 "  WHERE  bookname  = ?" +
        //                 "  AND    sheetname =  ?";

        // String deleteQuery =
        //         " DELETE FROM " + DBHandler.dependency +
        //                 "  WHERE  bookname  = ?" +
        //                 "  AND    sheetname =  ?";

        // List<Pair<Ref, Ref>> result = new LinkedList<>();
        // try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
        //      PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
        //     stmt.setString(1, bookName);
        //     stmt.setString(2, sheetName);

        //     ResultSet rs =  stmt.executeQuery();
        //     while(rs.next())
        //     {
        //         PGbox range = (PGbox) rs.getObject(1);
        //         Ref prec = RefUtils.boxToRef(range, bookName, sheetName);
        //         PGbox dep_range = (PGbox) rs.getObject(2);
        //         Ref dep = RefUtils.boxToRef(dep_range, bookName, sheetName);
        //         result.add(new Pair<>(prec, dep));
        //     }

        //     PreparedStatement delStmt = connection.prepareStatement(deleteQuery);
        //     delStmt.setString(1, bookName);
        //     delStmt.setString(2, sheetName);
        //     delStmt.execute();

        //     connection.commit();
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }

        // return result;
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

    @Override
    public void refreshCache(String bookName, String sheetName) {
        long start = System.currentTimeMillis();

        boolean isInsertOnly = false;
        LogUtils.getLogEntries(logTableName, bookName, sheetName, isInsertOnly)
                .forEach(logEntry -> {
                    if (logEntry.isInsert) performOneInsert(logEntry.prec, logEntry.dep);
                    else performOneDelete(logEntry.dep);
                });
        deleteAllLogs(bookName, sheetName);

        if (refNum > CACHE_SIZE) { // discard _rectToRefCache
            rectToRefCache = RTree.create();
        } else if (rectToRefCache.isEmpty() && refNum > 0) { // rebuild the cache if necessary
            rebuildRectToRefCache(bookName, sheetName);
        }

        refreshCacheTime = System.currentTimeMillis() - start;
    }

    @Override
    public void add(Ref dependent, Ref precedent) {
        String insertQuery = "INSERT INTO " + logTableName
                + " VALUES (?,?,?,?,?,?,?,TRUE)";
        LogUtils.appendOneLog(insertQuery, logEntryNum, precedent, dependent);
        logEntryNum += 1;
        insertEntryNum += 1;
    }

    private void performOneInsert(Ref precedent, Ref dependant) {

        // Update the DB and ref count
        String insertQuery = "INSERT INTO dependency VALUES (?,?,?,?,?,?,?)";
        addQuery(insertQuery, dependant, precedent);
        // insertQuery = "INSERT INTO full_dependency VALUES (?,?,?,?,?,?,?)";
        // addQuery(insertQuery, dependant, precedent);
        this.refNum += 2;

        // Update the caches
        Set<Ref> deps = this.prcToDepCache.get(precedent);
        if (deps != null) {
            // deps.add(precedent);
            deps.add(dependant);
            this.prcToDepCache.put(precedent, deps);
        } else {
            HashSet<Ref> newDepSet = new HashSet<>();
            newDepSet.add(dependant);
            this.prcToDepCache.put(precedent, newDepSet);
        }
        Set<Ref> precs = this.depToPrcCache.get(dependant);
        if (precs != null) {
            // precs.add(dependant);
            precs.add(precedent);
            this.depToPrcCache.put(dependant, precs);
        } else {
            HashSet<Ref> newPrecSet = new HashSet<>();
            newPrecSet.add(precedent);
            this.depToPrcCache.put(dependant, newPrecSet);
        }

        // Update the RTree if it exists
        // if (this.refNum > CACHE_SIZE) {
        //     if (!this.rectToRefCache.isEmpty()) {
        //         this.rectToRefCache = RTree.create();
        //     }
        // } else {
        //     this.rectToRefCache = this.rectToRefCache.add(precedent, RefUtils.refToRect(precedent));
        //     this.rectToRefCache = this.rectToRefCache.add(dependant, RefUtils.refToRect(dependant));
        // }

        if (!this.rectToRefCache.isEmpty()) {
            this.rectToRefCache = this.rectToRefCache.add(precedent, RefUtils.refToRect(precedent));
            this.rectToRefCache = this.rectToRefCache.add(dependant, RefUtils.refToRect(dependant));
        }
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    private void rebuildRectToRefCache(String bookName, String sheetName) {
        String selectQuery =
                " SELECT range, dep_range FROM dependency" +
                        " WHERE bookname = ?    " +
                        " AND sheetname = ?     ";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setString(1, bookName);
            stmt.setString(2, sheetName);
            ResultSet rs =  stmt.executeQuery();
            while(rs.next()) {
                PGbox range = (PGbox) rs.getObject(1);
                this.rectToRefCache = this.rectToRefCache.add(RefUtils.boxToRef(range, bookName, sheetName),
                        RefUtils.boxToRect(range));

                PGbox dep_range = (PGbox) rs.getObject(2);
                this.rectToRefCache = this.rectToRefCache.add(RefUtils.boxToRef(dep_range, bookName, sheetName),
                        RefUtils.boxToRect(dep_range));
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearDependents(Ref dependent) {
        String deleteQuery = "INSERT INTO " + logTableName
                + " VALUES (?,?,?,?,?,?,?,FALSE)";
        LogUtils.appendOneLog(deleteQuery, logEntryNum, null, dependent);
        logEntryNum += 1;
    }

    private void performOneDelete(Ref dependant) {
        String deleteQuery = "DELETE FROM dependency" +
                " WHERE dep_bookname  = ?" +
                " AND   dep_sheetname =  ?" +
                " AND   dep_range && ?";

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setString(1, dependant.getBookName());
            stmt.setString(2, dependant.getSheetName());
            stmt.setObject(3, new PGbox(dependant.getRow(),
                    dependant.getColumn(), dependant.getLastRow(),
                    dependant.getLastColumn()), Types.OTHER);
            stmt.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Maintain cache
        Set<Ref> precs = this.depToPrcCache.get(dependant);
        if (precs != null) {
            precs.forEach((prc) -> {
                Set<Ref> depSet = prcToDepCache.get(prc);
                if (depSet != null) {
                    depSet.remove(dependant);
                    if (depSet.isEmpty()) {
                        prcToDepCache.remove(prc);
                        this.refNum--;
                        if (!this.rectToRefCache.isEmpty()) {
                            this.rectToRefCache = this.rectToRefCache.delete(prc, RefUtils.refToRect(prc));
                        }
                    }
                }
            });
        }
        this.depToPrcCache.remove(dependant);

        // Update ref count
        this.refNum--;
        if (!this.rectToRefCache.isEmpty()) {
            this.rectToRefCache = this.rectToRefCache.delete(dependant, RefUtils.refToRect(dependant));
        }
        // Rebuild RTree if necessary
        // if (this.refNum < CACHE_SIZE) {
        //     if (this.rectToRefCache.isEmpty() && this.refNum > 0) {
        //         this.rebuildRectToRefCache(dependant.getBookName(), dependant.getSheetName());
        //     } else {
        //         this.rectToRefCache = this.rectToRefCache.delete(dependant, RefUtils.refToRect(dependant));
        //     }
        // }
    }

    @Override
    public Set<Ref> getActualDependents(Ref precedent) {
        if (precedent == null) { return new HashSet<>(); }
        Set<Ref> dependents = new HashSet<>();
        this.dfs(precedent, this::getNeighbors, (n) -> { if (!precedent.equals(n)) dependents.add(n); });
        return dependents;
    }

    @Override
    public Set<Ref> getDependents(Ref precedent) {
        // return this.getActualDependents(precedent);

        LinkedHashSet<Ref> result = new LinkedHashSet<>();

        if (RefUtils.isValidRef(precedent)) {
            long start = System.currentTimeMillis();

            if (CACHE_SIZE == 0) {
                String selectQuery = "WITH RECURSIVE deps AS (" +
                        "  SELECT dep_bookname, dep_sheetname, dep_range::text, must_expand FROM dependency" +
                        "  WHERE  bookname  = ?" +
                        "  AND    sheetname =  ?" +
                        "  AND  range && ?" +
                        "  UNION " +
                        "  SELECT d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand FROM dependency d" +
                        "    INNER JOIN deps t" +
                        "    ON  d.bookname   =  t.dep_bookname" +
                        "    AND t.must_expand" +
                        "    AND d.sheetname =  t.dep_sheetname" +
                        "    AND d.range      && t.dep_range::box)" +
                        " SELECT dep_bookname, dep_sheetname, dep_range::box FROM deps";

                result.addAll(getDependentsQuery(precedent, selectQuery));

            } else {
                RTree<Ref, Rectangle> rectToRefResult = RTree.create();
                Queue<Ref> updateQueue = new LinkedList<>();
                updateQueue.add(precedent);
                while (!updateQueue.isEmpty()) {
                    Ref updateRef = updateQueue.remove();
                    for (Ref precRef : getNeighbors(updateRef)) {
                        for (Ref depRef : getDirectDependents(precRef)) {
                            if (!isContained(rectToRefResult, depRef)) {
                                rectToRefResult = rectToRefResult.add(depRef, RefUtils.refToRect(depRef));
                                result.add(depRef);
                                updateQueue.add(depRef);
                            }
                        }
                    }
                }
            }

            lookupTime = System.currentTimeMillis() - start;
        }

        return result;
    }

    private boolean isContained(RTree<Ref, Rectangle> rectToRefResult, Ref input) {
        boolean isContained = false;
        Iterator<Entry<Ref, Rectangle>> matchIter =
                rectToRefResult.search(RefUtils.refToRect(input)).toBlocking().getIterator();
        while (matchIter.hasNext()) {
            if (isSubsume(matchIter.next().value(), input)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    private boolean isSubsume(Ref large, Ref small) {
        if (large.getOverlap(small) == null) return false;
        return large.getOverlap(small).equals(small);
    }

    private Set<Ref> getDependentsQuery(Ref precedent, String selectQuery) {
        Set<Ref> result = new LinkedHashSet<>();
        // long startTime = System.currentTimeMillis();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
            stmt.setString(1, precedent.getBookName());
            stmt.setString(2, precedent.getSheetName());
            stmt.setObject(3, new PGbox(precedent.getRow(),
                    precedent.getColumn(), precedent.getLastRow(),
                    precedent.getLastColumn()), Types.OTHER);

            ResultSet rs =  stmt.executeQuery();
            while(rs.next())
            {
                PGbox range = (PGbox) rs.getObject(3);
                // The order of points is based on how postgres stores them
                result.add(
                        new RefImpl(rs.getString(1),
                                rs.getString(2),
                                (int) range.point[1].x,
                                (int) range.point[1].y,
                                (int) range.point[0].x,
                                (int) range.point[0].y));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // lookupTime += System.currentTimeMillis() - startTime;
        return result;
    }

    @Override
    public Set<Ref> getDirectDependents(Ref precedent) {

        if (this.prcToDepCache.containsKey(precedent)) {
            return this.prcToDepCache.get(precedent);
        }

        if (CACHE_SIZE >= MAX_CACHE) return new HashSet<>();

        String selectQuery = "SELECT dep_bookname, dep_sheetname, dep_range  FROM dependency " +
                " WHERE bookname = ? " +
                " AND   sheetname = ? " +
                " AND   range && ?";

        Set<Ref> refs = getDependentsQuery(precedent, selectQuery);
        this.prcToDepCache.put(precedent, refs);
        return refs;

    }

    @Override
    public void merge(DependencyTableAdv dependencyTable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Ref> searchPrecedents(RefFilter filter){
        throw new UnsupportedOperationException();
    }

    //ZSS-648
    @Override
    public Set<Ref> getDirectPrecedents(Ref dependent) {

        if (this.depToPrcCache.containsKey(dependent)) {
            return this.depToPrcCache.get(dependent);
        }

        String selectQuery = "SELECT bookname, sheetname, range FROM dependency " +
                " WHERE dep_bookname = ? " +
                " AND   dep_sheetname = ? " +
                " AND   dep_range && ?";

        Set<Ref> refs = getDependentsQuery(dependent, selectQuery);
        this.depToPrcCache.put(dependent, refs);
        return refs;

    }

    //ZSS-815
    @Override
    public void adjustSheetIndex(String bookName, int index, int size) {
        // do nothing
    }

    //ZSS-820
    @Override
    public void moveSheetIndex(String bookName, int oldIndex, int newIndex) {
        // do nothing
    }
}
