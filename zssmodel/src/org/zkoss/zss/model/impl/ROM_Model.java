package org.zkoss.zss.model.impl;

import com.opencsv.CSVReader;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.zkoss.zss.model.CellRegion;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class ROM_Model extends Model {
    private Logger logger = Logger.getLogger(ROM_Model.class.getName());
    private PosMapping rowMapping;
    private PosMapping colMapping;


    //Create or load RCV_model.
    ROM_Model(DBContext context, String tableName) {
        rowMapping = new BTree(context, tableName + "_row_idx");
        colMapping = new BTree(context, tableName + "_col_idx");
        this.tableName = tableName;
        createSchema(context);
    }

    private void createSchema(DBContext context) {
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("(row INT PRIMARY KEY)")
                .toString();

        try (Statement stmt = context.getConnection().createStatement()) {
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
        Integer ids[] = colMapping.createIDs(context, col, count);
        for (int i = 0; i < ids.length; i++) {
            insertColumn.append(" ADD COLUMN col_")
                    .append(ids[i])
                    .append(" BYTEA");
            if (i < ids.length - 1)
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
        Integer[] ids = rowMapping.deleteIDs(context, row, count);

        try (PreparedStatement stmt = context.getConnection().prepareStatement(
                "DELETE FROM " + tableName + " WHERE row = ANY(?)")) {
            Array inArray = context.getConnection().createArrayOf("integer", ids);
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
        Integer[] ids = colMapping.deleteIDs(context, col, count);
        for (int i = 0; i < ids.length - 1; i++)
            deleteColumn.append(" DROP COLUMN col_")
                    .append(ids[i])
                    .append(",");
        deleteColumn.append(" DROP COLUMN col_")
                .append(ids[ids.length - 1]);

        try (Statement stmt = context.getConnection().createStatement()) {
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


        Integer[] idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        StringBuffer update = new StringBuffer("WITH upsert AS (UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int i = 0; i < idsCol.length; ++i) {
            update.append("col_")
                    .append(idsCol[i])
                    .append("=COALESCE(?,")
                    .append("col_")
                    .append(idsCol[i])
                    .append(")");
            if (i < idsCol.length - 1)
                update.append(",");
        }

        update.append(" WHERE row = ? RETURNING *) ")
                .append("INSERT INTO ")
                .append(tableName)
                .append("(row,");

        for (int i = 0; i < idsCol.length; ++i) {
            update.append("col_")
                    .append(idsCol[i]);
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") SELECT ");
        for (int i = 0; i < idsCol.length; ++i)
            update.append("?,");
        update.append("? WHERE NOT EXISTS (SELECT * FROM upsert)");
        try (PreparedStatement stmt = context.getConnection().prepareStatement(update.toString())) {
            for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                int rowId = rowMapping.getIDs(context, _row.getKey(), 1)[0];
                stmt.setInt(idsCol.length + 1, rowId);
                stmt.setInt(idsCol.length + 2, rowId);
                for (int i = 0; i < idsCol.length; ++i) {
                    if (_row.getValue().get(columnList.first() + i) == null) {
                        stmt.setNull(i + 1, Types.BINARY);
                        stmt.setNull(i + idsCol.length + 3, Types.BINARY);
                    } else {
                        stmt.setBytes(i + 1,
                                _row.getValue().get(columnList.first() + i).toBytes());
                        stmt.setBytes(i + idsCol.length + 3,
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
        Integer[] colIds = colMapping.getIDs(context, range.getColumn(), range.getLastColumn() - range.getColumn() + 1);
        for (int i = 0; i < colIds.length - 1; i++) {
            delete.append("col_")
                    .append(colIds[i])
                    .append("=null,");
        }
        delete.append("col_")
                .append(colIds[colIds.length - 1])
                .append("=null");
        delete.append(" WHERE row = ANY (?) ");

        Integer[] rowIds = rowMapping.getIDs(context, range.getRow(), range.getLastRow() - range.getRow() + 1);

        try (PreparedStatement stmt = context.getConnection().prepareStatement(delete.toString())) {
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds);
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


        Integer[] idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        StringBuffer update = (new StringBuffer())
                .append("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int i = 0; i < idsCol.length; ++i) {
            update.append("col_")
                    .append(idsCol[i])
                    .append("= CASE WHEN ? IS NULL THEN NULL ELSE ")
                    .append("col_")
                    .append(idsCol[i])
                    .append(" END");
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(" WHERE row = ?");

        try (PreparedStatement stmt = context.getConnection().prepareStatement(update.toString())) {
            for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                int rowId = rowMapping.getIDs(context, _row.getKey(), 1)[0];
                stmt.setInt(idsCol.length + 1, rowId);
                for (int i = 0; i < idsCol.length; ++i) {
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

        Integer[] rowIds = rowMapping.getIDs(context, fetchRegion.getRow(), fetchRegion.getLastRow() - fetchRegion.getRow() + 1);
        Integer[] colIds = colMapping.getIDs(context, fetchRegion.getColumn(), fetchRegion.getLastColumn() - fetchRegion.getColumn() + 1);
        HashMap<Integer, Integer> row_map = IntStream.range(0, rowIds.length)
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(rowIds[i], fetchRegion.getRow() + i), null);
        HashMap<Integer, Integer> col_map = IntStream.range(0, colIds.length)
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(colIds[i], fetchRegion.getColumn() + i), null);

        StringBuffer select = new StringBuffer("SELECT row");
        for (int i = 0; i < colIds.length; i++)
            select.append(",col_")
                    .append(colIds[i]);

        select.append(" FROM ")
                .append(tableName)
                .append(" WHERE row = ANY (?) ");

        try (PreparedStatement stmt = context.getConnection().prepareStatement(select.toString())) {
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds);
            stmt.setArray(1, inArrayRow);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int row_id = rs.getInt(1);
                int row = row_map.get(row_id);

                for (int i = 0; i < colIds.length; i++) {
                    int col = col_map.get(colIds[i]);
                    byte[] data = rs.getBytes(i + 2);
                    if (data!=null) {
                        AbstractCellAdv cell = CellImpl.fromBytes(row, col, data);
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
        final int COMMIT_SIZE_BYTES = 8 * 1000;
        CSVReader csvReader = new CSVReader(reader, delimiter);
        String[] nextLine;
        int importedRows = 0;
        int importedColumns = 0;

        try (Connection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            Connection rawConn = ((DelegatingConnection) connection).getInnermostDelegate();
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
                }
            }
            if (sb.length() > 0)
                cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
            cpIN.endCopy();
            rawConn.commit();
            insertRows(dbContext, 0, importedRows);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}