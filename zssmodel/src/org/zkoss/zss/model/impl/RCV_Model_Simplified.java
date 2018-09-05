package org.zkoss.zss.model.impl;

/* Simplified version of RCV, that does not use positional indexing */

import com.opencsv.CSVReader;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;


public class RCV_Model_Simplified extends Model {
    private static final Logger logger = Logger.getLogger(RCV_Model_Simplified.class.getName());

    //Create or load RCV_model.
    protected RCV_Model_Simplified(DBContext context, SSheet sheet, String tableName) {
        this.sheet = sheet;
        this.tableName = tableName;
        createSchema(context);
    }

    protected RCV_Model_Simplified(DBContext context, SSheet sheet, String tableName, RCV_Model_Simplified source) {
        this.sheet = sheet;
        this.tableName = tableName;
        copySchema(context, source.tableName);
    }

    @Override
    public Model clone(DBContext context, SSheet sheet, String tableName) {
        return new RCV_Model_Simplified(context, sheet, tableName, this);
    }

    @Override
    public ArrayList<String> getHeaders() {
        return null;
    }

    @Override
    public void setIndexString(String str) {

    }

    //Copy the table
    private void copySchema(DBContext dbContext, String sourceTable) {
        createSchema(dbContext);
        String copyTable = (new StringBuffer())
                .append("INSERT INTO ")
                .append(tableName)
                .append(" SELECT * FROM ")
                .append(sourceTable)
                .toString();
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(copyTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Create a table from the database
    private void createSchema(DBContext dbContext) {
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                //.append("(row INT, col INT, data BYTEA, PRIMARY KEY(row, col))")
                .append("(row INT, col INT, data BYTEA)")
                .toString();
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public ArrayList<Bucket> createNavS(String bucketName, int start, int count) {
        return null;
    }

    @Override
    public void dropSchema(DBContext context) {
        String dropTable = (new StringBuffer())
                .append("DROP TABLE ")
                .append(tableName)
                .toString();
        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dropTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void insertRows(DBContext context, int row, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertCols(DBContext context, int col, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRows(DBContext dbContext, int row, int count) {
    }

    @Override
    public void deleteCols(DBContext context, int col, int count) {

    }


    @Override
    public void updateCells(DBContext context, Collection<AbstractCellAdv> cells) {

        StringBuffer update = new StringBuffer("WITH upsert AS ( UPDATE ")
                .append(tableName)
                .append(" SET data = ? WHERE row = ? AND col = ? RETURNING *) INSERT INTO ")
                .append(tableName)
                .append(" (row,col,data) SELECT ?,?,? WHERE NOT EXISTS (SELECT * FROM upsert)");

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update.toString())) {
            for (AbstractCellAdv cell : cells) {
                stmt.setBytes(1, cell.toBytes());
                stmt.setInt(2, cell.getRowIndex());
                stmt.setInt(3, cell.getColumnIndex());
                stmt.setInt(4, cell.getRowIndex());
                stmt.setInt(5, cell.getColumnIndex());
                stmt.setBytes(6, cell.toBytes());
                stmt.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCells(DBContext dbContext, CellRegion range) {
        String delete = new StringBuffer("DELETE FROM ")
                .append(tableName)
                .append(" WHERE row BETWEEN ? AND ? AND col BETWEEN ? AND ?").toString();


        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(delete)) {

            stmt.setInt(1, range.getRow());
            stmt.setInt(2, range.getLastRow());
            stmt.setInt(3, range.getColumn());
            stmt.setInt(4, range.getLastColumn());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deleteCells(DBContext dbContext, Collection<AbstractCellAdv> cells) {

        String delete = new StringBuffer("DELETE FROM ")
                .append(tableName)
                .append(" WHERE row = ? AND col = ?").toString();

        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(delete)) {
            for (AbstractCellAdv cell : cells) {
                stmt.setObject(1, cell.getRowIndex());
                stmt.setObject(2, cell.getColumnIndex());
                stmt.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteTableRows(DBContext context, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange) {
        Collection<AbstractCellAdv> cells = new ArrayList<>();
        String select = new StringBuffer("SELECT row, col, data FROM ")
                .append(tableName)
                .append(" WHERE row BETWEEN ? AND ? AND col BETWEEN ? AND ?").toString();


        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setInt(1, fetchRange.getRow());
            stmt.setInt(2, fetchRange.getLastRow());
            stmt.setInt(3, fetchRange.getColumn());
            stmt.setInt(4, fetchRange.getLastColumn());


            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int row = rs.getInt(1);
                int col = rs.getInt(2);
                AbstractCellAdv cell = CellImpl.fromBytes(sheet, row,
                        col, rs.getBytes(3));
                cells.add(cell);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cells;
    }

    @Override
    public CellRegion getBounds(DBContext context) {
        int rows = 0;
        int columns = 0;
        String select = new StringBuffer("SELECT max(row), max(col) FROM ")
                .append(tableName).toString();

        AutoRollbackConnection connection = context.getConnection();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(select)) {
            if (rs.next()) {
                 rows = rs.getInt(1);
                columns = rs.getInt(2);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

            return new CellRegion(0,0,rows,columns);
    }

    @Override
    public void clearCache(DBContext context) {
    }

    @Override
    public void importSheet(Reader reader, char delimiter, boolean useNav) throws IOException {
        final int COMMIT_SIZE_BYTES = 8 * 1000 * 1000;
        CSVReader csvReader = new CSVReader(reader, delimiter);
        String[] nextLine;
        int importedRows = 0;
        int importedColumns = 0;
        logger.info("Importing sheet");


        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            Connection rawConn = ((DelegatingConnection) connection.getInternalConnection()).getInnermostDelegate();
            CopyManager cm = ((PgConnection) rawConn).getCopyAPI();

            CopyIn cpIN = cm.copyIn("COPY " + tableName + " (row,col,data)" +
                    " FROM STDIN WITH DELIMITER '|'");

            StringBuffer sb = new StringBuffer();
            while ((nextLine = csvReader.readNext()) != null) {
                if (importedColumns < nextLine.length)
                    importedColumns = nextLine.length;
                for (int col = 0; col < nextLine.length; col++) {
                    sb.append(importedRows).append('|');
                    sb.append(col).append('|');
                    sb.append(nextLine[col]).append('\n');
                }
                ++importedRows;

                if (sb.length() >= COMMIT_SIZE_BYTES) {
                    cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
                    sb = new StringBuffer();
                    logger.info(importedRows + " rows imported ");
                }
            }
            if (sb.length() > 0)
                cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
                cpIN.endCopy();
            rawConn.commit();
            logger.info("Import done: " + importedRows + " rows and "
                    + importedColumns + " columns imported");
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteTableColumns(DBContext dbContext, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }

}
