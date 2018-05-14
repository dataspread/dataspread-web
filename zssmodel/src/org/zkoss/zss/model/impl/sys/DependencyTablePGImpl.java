package org.zkoss.zss.model.impl.sys;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of dependency table based on Postgres.
 * @author Mangesh
 * @since 3.5.0
 */
public class DependencyTablePGImpl extends DependencyTableAdv {
	private static final long serialVersionUID = 1L;
	private static final Log _logger = Log.lookup(DependencyTablePGImpl.class.getName());
    private long lastLookupTime;

    public DependencyTablePGImpl() {
        lastLookupTime = 0;
    }

	@Override
	public void setBookSeries(SBookSeries series) {
        //Do nothing.
        //TODO: remove the concept of book series.
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
		String insertQuery = "INSERT INTO dependency VALUES (?,?,?,?,?,?,?)";
		addQuery(insertQuery, dependant, precedent);
		insertQuery = "INSERT INTO full_dependency VALUES (?,?,?,?,?,?,?)";
		addQuery(insertQuery, dependant, precedent);
	}

	public void clear() {
        throw new UnsupportedOperationException();
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
		String selectQuery = "WITH RECURSIVE deps AS (" +
				"  SELECT dep_bookname, dep_sheetname, dep_range::text, must_expand FROM full_dependency" +
				"  WHERE  bookname  = ?" +
				"  AND    sheetname =  ?" +
				"  AND    range && ?" +
				"  UNION " +
				"  SELECT d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand FROM full_dependency d" +
				"    INNER JOIN deps t" +
				"    ON  d.bookname   =  t.dep_bookname" +
				"    AND t.must_expand" +
				"    AND d.sheetname =  t.dep_sheetname" +
				"    AND d.range      && t.dep_range::box)" +
				" SELECT dep_bookname, dep_sheetname, dep_range::box FROM deps";
		return getDependentsQuery(precedent, selectQuery);

	}

    @Override
	public Set<Ref> getDependents(Ref precedent) {
        String selectQuery = "WITH RECURSIVE deps AS (" +
                "  SELECT dep_bookname, dep_sheetname, dep_range::text, must_expand FROM dependency" +
                "  WHERE  bookname  = ?" +
                "  AND    sheetname =  ?" +
                "  AND    range && ?" +
                "  UNION " +
                "  SELECT d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand FROM dependency d" +
                "    INNER JOIN deps t" +
                "    ON  d.bookname   =  t.dep_bookname" +
                "    AND t.must_expand" +
				"    AND d.sheetname =  t.dep_sheetname" +
                "    AND d.range      && t.dep_range::box)" +
                " SELECT dep_bookname, dep_sheetname, dep_range::box FROM deps";
        return getDependentsQuery(precedent, selectQuery);
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
        String selectQuery = "SELECT dep_bookname, dep_sheetname, dep_range  FROM dependency " +
                " WHERE bookname = ? " +
                " AND   sheetname = ? " +
                " AND   range && ?";
        return getDependentsQuery(precedent, selectQuery);
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
        String selectQuery = "SELECT bookname, sheetname, range FROM dependency " +
                " WHERE dep_bookname = ? " +
                " AND   dep_sheetname = ? " +
                " AND   dep_range && ?";
        return getDependentsQuery(dependent, selectQuery);
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
