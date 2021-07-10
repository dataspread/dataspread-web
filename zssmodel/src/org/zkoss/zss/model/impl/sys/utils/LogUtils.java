package org.zkoss.zss.model.impl.sys.utils;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;

public class LogUtils {

    public static LinkedList<LogEntry> getLogEntries(
            String logTableName,
            String bookName,
            String sheetName,
            boolean isInsertOnly) {
        String selectQuery =
                "SELECT logid, range, dep_range, isInsert FROM " + logTableName +
                        " WHERE dep_bookname = ?    " +
                        " AND dep_sheetname = ?     ";
        if (isInsertOnly) selectQuery += " AND isInsert = TRUE ";
        selectQuery += " ORDER BY logid";

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
                if (range != null) prec = RefUtils.boxToRef(range, bookName, sheetName);
                Ref dep = RefUtils.boxToRef(dep_range, bookName, sheetName);
                boolean isInsert = rs.getBoolean(4);
                result.add(new LogEntry(id, prec, dep, isInsert));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void appendOneLog(String appendLogQuery,
                              int logEntryNum,
                              Ref prec,
                              Ref dep) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(appendLogQuery)) {
            stmt.setInt(1, logEntryNum);
            if (prec != null) {
                stmt.setString(2, prec.getBookName());
                stmt.setString(3, prec.getSheetName());
                stmt.setObject(4, RefUtils.refToPGBox(prec), Types.OTHER);
            } else {
                stmt.setNull(2, Types.NULL);
                stmt.setNull(3, Types.NULL);
                stmt.setNull(4, Types.NULL);
            }
            stmt.setString(5, dep.getBookName());
            stmt.setString(6, dep.getSheetName());
            stmt.setObject(7, RefUtils.refToPGBox(dep), Types.OTHER);
            stmt.execute();

            connection.commit();
        } catch (SQLException e) {

            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void deleteLoadedLogs(DBContext dbContext,
                                        String logTableName,
                                        String bookname,
                                        String sheetname) throws SQLException {
        String query = "DELETE FROM " + logTableName +
                " WHERE dep_bookname = ? " +
                " AND dep_sheetname = ? ";
        PreparedStatement retStmt = dbContext.getConnection().prepareStatement(query);
        retStmt.setString(1, bookname);
        retStmt.setString(2, sheetname);
        retStmt.execute();
    }

    public static void deleteLogEntry(DBContext dbContext,
                                String logTableName, int id) throws SQLException {
        String query = "DELETE FROM " + logTableName +
                " WHERE logid = ?";
        PreparedStatement retStmt = dbContext.getConnection().prepareStatement(query);
        retStmt.setInt(1, id);
        retStmt.execute();
    }
}
