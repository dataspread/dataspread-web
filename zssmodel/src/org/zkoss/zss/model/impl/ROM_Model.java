package org.zkoss.zss.model.impl;

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
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class ROM_Model extends Model {
    private static final Logger logger = Logger.getLogger(ROM_Model.class.getName());
    private PosMapping rowMapping;
    private PosMapping colMapping;


    //Create or load RCV_model.
    ROM_Model(DBContext context, SSheet sheet, String tableName) {
        this.sheet = sheet;
        rowMapping = new BTree<Integer,Integer>(context, tableName + "_row_idx");
        colMapping = new BTree<Integer,Integer>(context, tableName + "_col_idx");
        this.tableName = tableName;
        createSchema(context);
    }

    private void createSchema(DBContext context) {
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("(row INT PRIMARY KEY)")
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dropSchema(DBContext context) {
        String dropTable = (new StringBuffer())
                .append("DROP TABLE ")
                .append(tableName)
                .toString();
        try (Statement stmt = context.getConnection().createStatement()) {
            stmt.execute(dropTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rowMapping.dropSchema(context);
        colMapping.dropSchema(context);
    }


    @Override
    public void insertRows(DBContext context, int row, int count) {
        rowMapping.createIDs(context, row, count);
    }

    @Override
    public void insertCols(DBContext context, int col, int count) {
        StringBuffer insertColumn = (new StringBuffer())
                .append("ALTER TABLE ")
                .append(tableName);
        ArrayList<Integer> ids = colMapping.createIDs(context, col, count);
        for (int i = 0; i < ids.size(); i++) {
            insertColumn.append(" ADD COLUMN col_")
                    .append(ids.get(i))
                    .append(" BYTEA");
            if (i < ids.size() - 1)
                insertColumn.append(",");
        }
        try (Statement stmt = context.getConnection().createStatement()) {
            stmt.execute(insertColumn.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteRows(DBContext context, int row, int count) {
        ArrayList<Integer> ids = rowMapping.deleteIDs(context, row, count);

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM " + tableName + " WHERE row = ANY(?)")) {
            Array inArray = context.getConnection().createArrayOf("integer", ids.toArray());
            stmt.setArray(1, inArray);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCols(DBContext context, int col, int count) {
        StringBuffer deleteColumn = (new StringBuffer())
                .append("ALTER TABLE ")
                .append(tableName);
        ArrayList<Integer> ids = colMapping.deleteIDs(context, col, count);
        for (int i = 0; i < ids.size() - 1; i++)
            deleteColumn.append(" DROP COLUMN col_")
                    .append(ids.get(i))
                    .append(",");
        deleteColumn.append(" DROP COLUMN col_")
                .append(ids.get(ids.size() - 1));

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(deleteColumn.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateCells(DBContext context, Collection<AbstractCellAdv> cells) {
        if (cells.size() == 0)
            return;

        // Gather cells of same row together
        SortedMap<Integer, SortedMap<Integer, AbstractCellAdv>> groupedCells = new TreeMap<>();
        SortedSet<Integer> columnList = new TreeSet<>();
        for (AbstractCellAdv cell : cells) {
            SortedMap<Integer, AbstractCellAdv> _row = groupedCells.get(cell.getRowIndex());
            if (_row == null) {
                _row = new TreeMap<>();
                groupedCells.put(cell.getRowIndex(), _row);
            }
            _row.put(cell.getColumnIndex(), cell);
            columnList.add(cell.getColumnIndex());
        }

        if (columnList.last() >= colMapping.size(context))
            insertCols(context, colMapping.size(context), columnList.last() - colMapping.size(context) + 1);


        ArrayList<Integer> idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        StringBuffer update = new StringBuffer("WITH upsert AS (UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int i = 0; i < idsCol.size(); ++i) {
            update.append("col_")
                    .append(idsCol.get(i))
                    .append("=COALESCE(?,")
                    .append("col_")
                    .append(idsCol.get(i))
                    .append(")");
            if (i < idsCol.size() - 1)
                update.append(",");
        }

        update.append(" WHERE row = ? RETURNING *) ")
                .append("INSERT INTO ")
                .append(tableName)
                .append("(row,");

        for (int i = 0; i < idsCol.size(); ++i) {
            update.append("col_")
                    .append(idsCol.get(i));
            if (i < idsCol.size() - 1)
                update.append(",");
        }
        update.append(") SELECT ");
        for (int i = 0; i < idsCol.size(); ++i)
            update.append("?,");
        update.append("? WHERE NOT EXISTS (SELECT * FROM upsert)");
        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update.toString())) {
            for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                int rowId = (int) rowMapping.getIDs(context, _row.getKey(), 1).get(0);
                stmt.setInt(idsCol.size() + 1, rowId);
                stmt.setInt(idsCol.size() + 2, rowId);
                for (int i = 0; i < idsCol.size(); ++i) {
                    if (_row.getValue().get(columnList.first() + i) == null) {
                        stmt.setNull(i + 1, Types.BINARY);
                        stmt.setNull(i + idsCol.size() + 3, Types.BINARY);
                    } else {
                        stmt.setBytes(i + 1,
                                _row.getValue().get(columnList.first() + i).toBytes());
                        stmt.setBytes(i + idsCol.size() + 3,
                                _row.getValue().get(columnList.first() + i).toBytes());
                    }
                }
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCells(DBContext context, CellRegion range) {
        StringBuffer delete = new StringBuffer("UPDATE ")
                .append(tableName)
                .append(" SET ");
        ArrayList<Integer> colIds = colMapping.getIDs(context, range.getColumn(), range.getLastColumn() - range.getColumn() + 1);
        for (int i = 0; i < colIds.size() - 1; i++) {
            delete.append("col_")
                    .append(colIds.get(i))
                    .append("=null,");
        }
        delete.append("col_")
                .append(colIds.get(colIds.size() - 1))
                .append("=null");
        delete.append(" WHERE row = ANY (?) ");

        ArrayList<Integer> rowIds = rowMapping.getIDs(context, range.getRow(), range.getLastRow() - range.getRow() + 1);

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(delete.toString())) {
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds.toArray());
            stmt.setArray(1, inArrayRow);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCells(DBContext context, Collection<AbstractCellAdv> cells) {
        // Gather cells of same row together
        SortedMap<Integer, SortedMap<Integer, AbstractCellAdv>> groupedCells = new TreeMap<>();
        SortedSet<Integer> columnList = new TreeSet<>();
        for (AbstractCellAdv cell : cells) {
            SortedMap<Integer, AbstractCellAdv> _row = groupedCells.get(cell.getRowIndex());
            if (_row == null) {
                _row = new TreeMap<>();
                groupedCells.put(cell.getRowIndex(), _row);
            }
            _row.put(cell.getColumnIndex(), cell);
            columnList.add(cell.getColumnIndex());
        }

        if (columnList.last() >= colMapping.size(context))
            insertCols(context, colMapping.size(context), columnList.last() - colMapping.size(context) + 1);


        ArrayList<Integer> idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        StringBuffer update = (new StringBuffer())
                .append("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int i = 0; i < idsCol.size(); ++i) {
            update.append("col_")
                    .append(idsCol.get(i))
                    .append("= CASE WHEN ? IS NULL THEN NULL ELSE ")
                    .append("col_")
                    .append(idsCol.get(i))
                    .append(" END");
            if (i < idsCol.size() - 1)
                update.append(",");
        }
        update.append(" WHERE row = ?");

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update.toString())) {
            for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                int rowId = (int) rowMapping.getIDs(context, _row.getKey(), 1).get(0);
                stmt.setInt(idsCol.size() + 1, rowId);
                for (int i = 0; i < idsCol.size(); ++i) {
                    if (_row.getValue().get(i) == null)
                        stmt.setInt(i + 1, 1);
                    else
                        stmt.setNull(i + 1, Types.INTEGER);
                }
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange) {
        // Reduce Range to bounds
        Collection<AbstractCellAdv> cells = new ArrayList<>();

        CellRegion bounds =  getBounds(context);
        if (bounds==null || fetchRange==null)
            return cells;

        CellRegion fetchRegion = bounds.getOverlap(fetchRange);
        if (fetchRegion == null)
            return cells;

        ArrayList<Integer> rowIds = rowMapping.getIDs(context, fetchRegion.getRow(), fetchRegion.getLastRow() - fetchRegion.getRow() + 1);
        ArrayList<Integer> colIds = colMapping.getIDs(context, fetchRegion.getColumn(), fetchRegion.getLastColumn() - fetchRegion.getColumn() + 1);
        HashMap<Integer, Integer> row_map = IntStream.range(0, rowIds.size())
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(rowIds.get(i), fetchRegion.getRow() + i), null);
        HashMap<Integer, Integer> col_map = IntStream.range(0, colIds.size())
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(colIds.get(i), fetchRegion.getColumn() + i), null);

        StringBuffer select = new StringBuffer("SELECT row");
        for (int i = 0; i < colIds.size(); i++)
            select.append(",col_")
                    .append(colIds.get(i));

        select.append(" FROM ")
                .append(tableName)
                .append(" WHERE row = ANY (?) ");

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds.toArray());
            stmt.setArray(1, inArrayRow);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int row_id = rs.getInt(1);
                int row = row_map.get(row_id);

                for (int i = 0; i < colIds.size(); i++) {
                    int col = col_map.get(colIds.get(i));
                    byte[] data = rs.getBytes(i + 2);
                    if (data!=null) {
                        AbstractCellAdv cell = CellImpl.fromBytes(sheet, row, col, data);
                        cells.add(cell);
                    }
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cells;
    }

    @Override
    public boolean deleteTableRows(DBContext context, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }


    @Override
    public CellRegion getBounds(DBContext context) {
        int rows = rowMapping.size(context);
        int columns = colMapping.size(context);
        if (rows==0 || columns ==0)
            return null;
        else
            return new CellRegion(0, 0, rowMapping.size(context) - 1, colMapping.size(context) - 1);
    }

    @Override
    public void clearCache(DBContext context) {
        rowMapping.clearCache(context);
        colMapping.clearCache(context);
    }

    @Override
    public void importSheet(Reader reader, char delimiter) throws IOException {
        final int COMMIT_SIZE_BYTES = 8 * 1000 * 1000;
        CSVReader csvReader = new CSVReader(reader, delimiter);
        String[] nextLine;
        int importedRows = 0;
        int importedColumns = 0;
        logger.info("Importing sheet");


        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            Connection rawConn = ((DelegatingConnection) connection.getInternalConnection()).getInnermostDelegate();
            CopyManager cm = ((PgConnection) rawConn).getCopyAPI();
            CopyIn cpIN = null;

            StringBuffer sb = new StringBuffer();
            while ((nextLine = csvReader.readNext()) != null)
            {
                ++importedRows;
                if (cpIN == null)
                {
                    // Use the first line to fix the number of columns
                    importedColumns = nextLine.length;
                    insertCols(dbContext, 0, importedColumns);
                    StringBuffer copyCommand = new StringBuffer("COPY ");
                    copyCommand.append(tableName);
                    copyCommand.append("(row");
                    for (int i = 1; i <= importedColumns; i++)
                        copyCommand.append(", col_")
                                .append(i);
                    copyCommand.append(") FROM STDIN WITH DELIMITER '|'");
                    cpIN = cm.copyIn(copyCommand.toString());

                }

                sb.append(importedRows);
                for (int col = 0; col < importedColumns; col++)
                    sb.append('|').append(nextLine[col]);
                sb.append('\n');

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
            insertRows(dbContext, 0, importedRows);
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