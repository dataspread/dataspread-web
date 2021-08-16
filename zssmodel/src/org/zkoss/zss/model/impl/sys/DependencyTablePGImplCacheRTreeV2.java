package org.zkoss.zss.model.impl.sys;

import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.model.LruCache;

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
    private long lastLookupTime;

    private static int CACHE_SIZE = 1000000;
    private final LruCache<Ref, Set<Ref>> depToPrcCache = new LruCache<>(CACHE_SIZE);
    private final LruCache<Ref, Set<Ref>> prcToDepCache = new LruCache<>(CACHE_SIZE);
    private RTree<Ref, Rectangle> rectToRefCache  = RTree.create();
    private int refNum = 0;

    public DependencyTablePGImplCacheRTreeV2() {
        lastLookupTime = 0;
    }

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
    public void add(Ref dependant, Ref precedent) {

        // Update the DB and ref count
        String insertQuery = "INSERT INTO dependency VALUES (?,?,?,?,?,?,?)";
        addQuery(insertQuery, dependant, precedent);
        insertQuery = "INSERT INTO full_dependency VALUES (?,?,?,?,?,?,?)";
        addQuery(insertQuery, dependant, precedent);
        this.refNum += 2;

        // Update the caches
        Set<Ref> deps = this.prcToDepCache.get(precedent);
        if (deps != null) {
            deps.add(precedent);
            this.prcToDepCache.put(precedent, deps);
        } else {
            this.prcToDepCache.put(precedent, new HashSet<>());
        }
        Set<Ref> precs = this.depToPrcCache.get(dependant);
        if (precs != null) {
            precs.add(dependant);
            this.depToPrcCache.put(dependant, precs);
        } else {
            this.depToPrcCache.put(dependant, new HashSet<>());
        }

        // Update the RTree if it exists
        if (this.refNum > CACHE_SIZE) {
            if (!this.rectToRefCache.isEmpty()) {
                this.rectToRefCache = RTree.create();
            }
        } else {
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
    public void clearDependents(Ref dependant) {
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

        // Update ref count
        this.refNum--;

        // Maintain cache
        this.depToPrcCache.remove(dependant);
        Set<Ref> precs = this.depToPrcCache.get(dependant);
        if (precs != null) {
            precs.forEach((prc) -> {
                Set<Ref> depSet = prcToDepCache.get(prc);
                depSet.remove(dependant);
                if (depSet.isEmpty()) {
                    prcToDepCache.remove(prc);
                }
            });
        }

        // Rebuild RTree if necessary
        if (this.refNum < CACHE_SIZE) {
            if (this.rectToRefCache.isEmpty() && this.refNum > 0) {
                this.rebuildRectToRefCache(dependant.getBookName(), dependant.getSheetName());
            } else {
                this.rectToRefCache = this.rectToRefCache.delete(dependant, RefUtils.refToRect(dependant));
            }
        }

    }

    @Override
    public long getLastLookupTime() {
        try {
            return lastLookupTime;
        } finally {
            lastLookupTime = 0;
        }
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
        return this.getActualDependents(precedent);
    }

    private Set<Ref> getDependentsQuery(Ref precedent, String selectQuery) {
        Set<Ref> result = new LinkedHashSet<>();
        long startTime = System.currentTimeMillis();
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
        lastLookupTime += System.currentTimeMillis() - startTime;
        return result;
    }

    @Override
    public Set<Ref> getDirectDependents(Ref precedent) {

        if (this.prcToDepCache.containsKey(precedent)) {
            return this.prcToDepCache.get(precedent);
        }

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
