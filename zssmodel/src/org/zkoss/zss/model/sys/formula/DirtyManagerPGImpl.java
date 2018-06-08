package org.zkoss.zss.model.sys.formula;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.sql.*;

/* Simple in-db implementation for DirtyManager */
public class DirtyManagerPGImpl extends DirtyManager {

    DirtyManagerPGImpl()
    {
        createTable();
    }

    @Override
    public int getDirtyTrxId(Ref region) {
        int trxId = -1;
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement getTrxId = connection.prepareStatement(
                     "SELECT MAX(trxid) FROM dirty_regions" +
                             " WHERE bookname = ?" +
                             " AND   sheetname = ?" +
                             " AND   range && ?")) {

            getTrxId.setString(1, region.getBookName());
            getTrxId.setString(2, region.getSheetName());
            getTrxId.setObject(3, new PGbox(region.getRow(),
                    region.getColumn(), region.getLastRow(),
                    region.getLastColumn()), Types.OTHER);
            ResultSet rs = getTrxId.executeQuery();
            if (rs.next())
                trxId = rs.getInt(1);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trxId;
    }

    @Override
    public void addDirtyRegion(Ref region, int trxId) {
        if (region.getType()== Ref.RefType.AREA || region.getType()== Ref.RefType.CELL) {

            try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
                 PreparedStatement addRegion = connection.prepareStatement(
                         "INSERT INTO dirty_regions VALUES (?, ?, ?, ?, ?)")) {
                addRegion.setString(1, region.getBookName());
                addRegion.setString(2, region.getSheetName());
                addRegion.setObject(3, new PGbox(region.getRow(),
                        region.getColumn(), region.getLastRow(),
                        region.getLastColumn()), Types.OTHER);
                addRegion.setInt(4, trxId);
                addRegion.setString(5, "W");
                addRegion.execute();
                // W - waiting
                // P - processing
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void removeDirtyRegion(Ref region, int trxId) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement removeRegion = connection.prepareStatement(
                     "DELETE FROM dirty_regions" +
                             " WHERE bookname = ?" +
                             " AND   sheetname = ?" +
                             " AND   range && ?" +
                             " AND   trxid = ?")) {

            removeRegion.setString(1, region.getBookName());
            removeRegion.setString(2, region.getSheetName());
            removeRegion.setObject(3, new PGbox(region.getRow(),
                    region.getColumn(), region.getLastRow(),
                    region.getLastColumn()), Types.OTHER);
            removeRegion.setInt(4, trxId);
            int deletedRecords = removeRegion.executeUpdate();
            if (deletedRecords==0)
                System.out.println("Error deleting dirty records");
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DirtyRecord getDirtyRegionFromQueue() {
        return getDirtyRegionFromQueue();
    }

    @Override
    public DirtyRecord getDirtyRegionFromQueue(long waitTime) {
        //TODO - We can modify the query to select based on priority.
        DirtyRecord dirtyRecord = null;
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement getRegion = connection.prepareStatement(
                     "UPDATE dirty_regions" +
                             " SET status = 'P' WHERE oid IN (" +
                             "   SELECT oid FROM dirty_regions" +
                             "   WHERE status = 'W' LIMIT 1" +
                             "   FOR UPDATE" +
                             "   SKIP LOCKED)" +
                             " RETURNING bookname, sheetname, range, trxid");
             ResultSet rs = getRegion.executeQuery()) {
            if (rs.next()) {
                dirtyRecord = new DirtyRecord();
                PGbox range = (PGbox) rs.getObject(3);
                dirtyRecord.region = new RefImpl(rs.getString(1),
                        rs.getString(2),
                        (int) range.point[0].x,
                        (int) range.point[0].y,
                        (int) range.point[1].x,
                        (int) range.point[1].y);
                dirtyRecord.trxId = rs.getInt(4);
            }
            rs.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (dirtyRecord==null) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return dirtyRecord;
    }

    private void createTable()
    {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             Statement stmt = connection.createStatement())
        {
            String createTable = "CREATE TABLE  IF NOT  EXISTS  dirty_regions (" +
                    "bookname      TEXT    NOT NULL," +
                    "sheetname     TEXT    NOT NULL," +
                    "range         BOX     NOT NULL," +
                    "trxid         INTEGER NOT NULL," +
                    "status        CHAR(1)," +
                    "FOREIGN KEY (bookname, sheetname) REFERENCES sheets (bookname, sheetname)" +
                    " ON DELETE CASCADE ON UPDATE CASCADE," +
                    " UNIQUE (oid) ) WITH oids";
            stmt.execute(createTable);
            String resetRecords = "UPDATE dirty_regions" +
                    " SET trxid = 1, status = 'W'";
            stmt.execute(resetRecords);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
