package org.zkoss.zss.model.impl.sys;

import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import org.postgresql.geometric.PGbox;
import com.google.common.cache.*;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.SBookSeries;
import org.zkoss.util.logging.Log;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.*;

public class DependencyTablePGImplCacheRTree extends DependencyTableAdv {

    private static final long serialVersionUID = 1L;
    private static final Log _logger = Log.lookup(DependencyTablePGImpl.class.getName());
    private long lastLookupTime;

    private static int CACHE_SIZE = 1000000;

    // These R-Trees allow us to get the dependents / precedents of a cell that
    // lies within a cached range.
    private RTree<Set<Ref>, Rectangle> depToPrcRTree = RTree.create();
    private RTree<Set<Ref>, Rectangle> prcToDepRTree = RTree.create();

    // When an eviction occurs, the corresponding R-Tree entry is also deleted.
    private RemovalListener<Rectangle, Set<Ref>> depToPrcListener = removalNotification -> {
        if (removalNotification.wasEvicted() && removalNotification.getCause() == RemovalCause.SIZE) {
            depToPrcRTree.delete(removalNotification.getValue(), removalNotification.getKey());
        }
    };
    private RemovalListener<Rectangle, Set<Ref>> prcToDepListener = removalNotification -> {
        if (removalNotification.wasEvicted() && removalNotification.getCause() == RemovalCause.SIZE) {
            depToPrcRTree.delete(removalNotification.getValue(), removalNotification.getKey());
        }
    };

    // These caches are mainly used to keep the size of the R-Tree restricted to
    // cache size defined above.
    private final Cache<Rectangle, Set<Ref>> depToPrcCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).removalListener(depToPrcListener).build();
    private final Cache<Rectangle, Set<Ref>> prcToDepCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).removalListener(prcToDepListener).build();

    public DependencyTablePGImplCacheRTree() {
        lastLookupTime = 0;
    }

    /**
     *
     * Applies `func` to `src` and all nodes reachable from `src`.
     *
     * @param src The node to start DFS from.
     * @param neighbors A function that takes a node as input and returns a set of
     * neighbors to explore.
     * @param func A function that accepts a node as input and performs some operation
     * on it.
     *
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

    /**
     *
     * Converts a cell range to a rectangle object.
     *
     * @param ref The cell range to convert to a rectangle.
     * @return A Rectangle instance that can be used for R-Tree queries.
     */
    private Rectangle refToRectangle (Ref ref) {
        return RectangleFloat.create(
                ref.getRow(),
                ref.getColumn(),
                (float) 0.5 + ref.getLastRow(),
                (float) 0.5 + ref.getLastColumn()
        );
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

        // Update the DB
        String insertQuery = "INSERT INTO dependency VALUES (?,?,?,?,?,?,?)";
        addQuery(insertQuery, dependant, precedent);
        insertQuery = "INSERT INTO full_dependency VALUES (?,?,?,?,?,?,?)";
        addQuery(insertQuery, dependant, precedent);

        // Refresh the caches and R-trees for the cell references above
        Rectangle dependantRect = this.refToRectangle(dependant);
        Iterator<Entry<Set<Ref>, Rectangle>> precedents = this.depToPrcRTree.search(dependantRect).toBlocking().getIterator();
        if (precedents.hasNext()) {
            this.depToPrcRTree.delete(this.depToPrcCache.getIfPresent(dependantRect), dependantRect);
            this.depToPrcCache.invalidate(dependantRect);
        }
        Rectangle precedentRect = this.refToRectangle(precedent);
        Iterator<Entry<Set<Ref>, Rectangle>> dependents = this.prcToDepRTree.search(precedentRect).toBlocking().getIterator();
        if (dependents.hasNext()) {
            this.prcToDepRTree.delete(this.prcToDepCache.getIfPresent(precedentRect), precedentRect);
            this.prcToDepCache.invalidate(precedentRect);
        }
        this.getDirectPrecedents(dependant);
        this.getDirectDependents(precedent);

    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearDependents(Ref dependant) {

        // Update the caches
        Rectangle rect = this.refToRectangle(dependant);
        this.depToPrcRTree.delete(this.depToPrcCache.getIfPresent(rect), rect);
        this.depToPrcCache.invalidate(rect);
        this.prcToDepRTree.delete(this.prcToDepCache.getIfPresent(rect), rect);
        this.prcToDepCache.invalidate(rect);

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
        this.dfs(precedent, this::getDirectDependents, (n) -> { if (!precedent.equals(n)) dependents.add(n); });
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

        Rectangle precedentRect = this.refToRectangle(precedent);
        Iterator<Entry<Set<Ref>, Rectangle>> dependents = this.prcToDepRTree.search(precedentRect).toBlocking().getIterator();
        if (dependents.hasNext()) {
            Set<Ref> result = new HashSet<>();
            while (dependents.hasNext()) result.addAll(dependents.next().value());
            return result;
        }

        String selectQuery = "SELECT dep_bookname, dep_sheetname, dep_range  FROM dependency " +
                " WHERE bookname = ? " +
                " AND   sheetname = ? " +
                " AND   range && ?";

        Set<Ref> refs = getDependentsQuery(precedent, selectQuery);
        this.prcToDepCache.put(precedentRect, refs);
        this.prcToDepRTree = this.prcToDepRTree.add(Entries.entry(refs, precedentRect));
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

        Rectangle dependentRect = this.refToRectangle(dependent);
        Iterator<Entry<Set<Ref>, Rectangle>> precedents = this.depToPrcRTree.search(dependentRect).toBlocking().getIterator();
        if (precedents.hasNext()) {
            Set<Ref> result = new HashSet<>();
            while (precedents.hasNext()) result.addAll(precedents.next().value());
            return result;
        }

        String selectQuery = "SELECT bookname, sheetname, range FROM dependency " +
                " WHERE dep_bookname = ? " +
                " AND   dep_sheetname = ? " +
                " AND   dep_range && ?";

        Set<Ref> refs = getDependentsQuery(dependent, selectQuery);
        this.depToPrcCache.put(dependentRect, refs);
        this.depToPrcRTree = this.depToPrcRTree.add(Entries.entry(refs, dependentRect));
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
