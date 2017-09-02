package org.zkoss.zss.model.impl;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBorder;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.range.impl.StyleUtilNoDB;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class TOM_Model extends Model {

    // Assume table is already present
    private Logger logger = Logger.getLogger(TOM_Model.class.getName());
    private PosMapping rowMapping;
    private PosMapping colMapping;
    private SortedMap<Integer, String> columnNames;

    //Create or load TOM_model.
    TOM_Model(DBContext context, String tableName) {
        rowMapping = new BTree(context, tableName + "_row_idx");
        colMapping = new BTree(context, tableName + "_col_idx");
        this.tableName = tableName;
        loadColumnInfo(context);
    }

    public void loadColumnInfo(DBContext dbContext) {
        columnNames = new TreeMap<>();
        String tableCols = (new StringBuffer())
                .append("SELECT * FROM ")
                .append(tableName)
                .append(" WHERE false")
                .toString();

        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(tableCols.toString());
            int colCount = rs.getMetaData().getColumnCount();
            Integer ids[] = colMapping.getIDs(dbContext, 0, colCount);

            for (int i = 0; i < colCount; i++)
                columnNames.put(ids[i], rs.getMetaData().getColumnName(i + 1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO Make this as a static and get all info to create a table.
    private void createSchema(DBContext dbContext) {
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("(row INT PRIMARY KEY)")
                .toString();

        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteTableRows(DBContext context, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void dropSchema(DBContext context) {
        // Do nothing
    }


    private ArrayList<Integer> getOIDs(DBContext dbContext, String tableName) {
        ArrayList<Integer> oids = new ArrayList<>();

        String getOids = (new StringBuffer())
                .append("SELECT oid FROM ")
                .append(tableName)
                .append(" ORDER BY oid") /* TODO allow custom order */
                .toString();

        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet set = stmt.executeQuery(getOids);

            while (set.next()) {
                oids.add(set.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return oids;
    }

    public void indexOIDs(DBContext context) {
        /* Batch processing */
        if (rowMapping.size(context) == 0) {
            ArrayList<Integer> oids = getOIDs(context, tableName);
            rowMapping.insertIDs(context, 0, oids);
        }
    }

    public void insertOIDs(DBContext context, List<Integer> oids) {
        rowMapping.insertIDs(context, rowMapping.size(context), oids);
    }

    public void insertColMappings(DBContext context, int count) {
        colMapping.insertIDs(context, colMapping.size(context), IntStream.rangeClosed(colMapping.size(context) + 1,
                colMapping.size(context) + count).boxed().collect(Collectors.toList()));
    }

    public void deleteTuples(DBContext context, int row, int count) {
        Integer[] oids = rowMapping.deleteIDs(context, row, count);
        try (PreparedStatement stmt = context.getConnection().prepareStatement(
                "DELETE FROM " + tableName + " WHERE oid = ANY(?)")) {
            Array inArray = context.getConnection().createArrayOf("integer", oids);
            stmt.setArray(1, inArray);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deleteTableColumns(DBContext dbContext, int col, int count) {
        Integer[] colids = colMapping.deleteIDs(dbContext, col, count);
        try (Statement stmt = dbContext.getConnection().createStatement()) {
            for (int colid : colids) {
                StringBuilder deleteColumnsStmt = (new StringBuilder())
                        .append("ALTER TABLE ")
                        .append(tableName)
                        .append(" DROP COLUMN ")
                        .append(columnNames.get(colid));
                stmt.execute(deleteColumnsStmt.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadColumnInfo(dbContext);
    }

    protected void createOIDIndex(DBContext dbContext) {
        /* TODO update query */
        String addOID = (new StringBuffer())
                .append("ALTER TABLE ")
                .append(tableName)
                .append(" SET WITH OIDS")
                .toString();

        String indexOID = (new StringBuffer())
                .append("CREATE UNIQUE INDEX IF NOT EXISTS ")
                .append(tableName)
                .append("_oid_key ON ")
                .append(tableName)
                .append(" (oid)")
                .toString();

        try (Statement stmt = dbContext.getConnection().createStatement()) {
            stmt.executeUpdate(addOID);
            stmt.executeUpdate(indexOID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange) {
        throw new UnsupportedOperationException();
    }

    // Shrink area of TOM Model
    public Collection<AbstractCellAdv> getCellsTOM(DBContext context, SSheet sheet, CellRegion fetchRange) {

        /* TODO: Handle if we do not have enough records  */
        // Reduce Range to bounds
        Collection<AbstractCellAdv> cells = new ArrayList<>();

        CellRegion bounds = getBounds(context);
        if (bounds == null || fetchRange == null)
            return cells;

        CellRegion fetchRegion = bounds.getOverlap(fetchRange);
        if (fetchRegion == null)
            return cells;


        Integer[] rowIds;
        boolean includeHeader = (fetchRegion.getRow() == 0);
        if (includeHeader)
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow(), fetchRegion.getLastRow() - fetchRegion.getRow());
        else
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow() - 1, fetchRegion.getLastRow() - fetchRegion.getRow() + 1);


        Integer[] colIds = colMapping.getIDs(context, fetchRegion.getColumn(), fetchRegion.getLastColumn() - fetchRegion.getColumn() + 1);

        HashMap<Integer, Integer> row_map = new HashMap<>(); // Oid -> row number
        int bound = rowIds.length;
        for (int i1 = 0; i1 < bound; i1++) {
            if (rowIds[i1] != -1) {
                row_map.put(rowIds[i1], fetchRegion.getRow() + i1 + (includeHeader ? 1 : 0));
            }
        }

        HashMap<String, Integer> col_map = new HashMap<>();
        int bound1 = colIds.length;
        for (int i1 = 0; i1 < bound1; i1++) {
            String column = columnNames.get(colIds[i1]);
            col_map.put(column, fetchRegion.getColumn() + i1);
        }

        StringBuffer select = new StringBuffer("SELECT oid, ")
                .append(IntStream.range(0, colIds.length)
                        .mapToObj(i -> columnNames.get(colIds[i]))
                        .collect(Collectors.joining(",")))
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE oid = ANY (?) ");


        try (PreparedStatement stmt = context.getConnection().prepareStatement(select.toString())) {
            // Array inArrayRow = context.getConnection().createArrayOf(pkColumnType, rowIds);
            /* Assume an int array for now */
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds);
            stmt.setArray(1, inArrayRow);

            ResultSet rs = stmt.executeQuery();

            if (includeHeader) {
                for (int i = 0; i < colIds.length; i++) {
                    int col = col_map.get(columnNames.get(colIds[i]));
                    byte[] data = columnNames.get(colIds[i]).getBytes();
                    if (data != null) {
                        AbstractCellAdv cell = CellImpl.fromBytes(sheet, fetchRegion.getRow(), col, data);
                        cells.add(cell);

                        /* Header formatting */
                        StyleUtilNoDB.setBackColor(sheet.getBook(), cell, "#99ccff");
                        StyleUtilNoDB.setFontBoldWeight(sheet.getBook(), cell, SFont.Boldweight.BOLD);
                        StyleUtilNoDB.setBorder(sheet.getBook(), cell, "#000000", SBorder.BorderType.MEDIUM);
                        StyleUtilNoDB.setLocked(sheet.getBook(), cell, true);
                    }
                }
            }
            while (rs.next()) {
                int oid = rs.getInt(1); /* First column is oid */
                int row = row_map.get(oid);

                for (int i = 0; i < colIds.length; i++) {
                    int col = col_map.get(columnNames.get(colIds[i]));
                    byte[] data = rs.getBytes(i + 2);
                    if (data != null) {
                        AbstractCellAdv cell = CellImpl.fromBytes(sheet, row, col, data);
                        cells.add(cell);
                        StyleUtilNoDB.setBackColor(sheet.getBook(), cell, "#99ccff");
                        StyleUtilNoDB.setBorder(sheet.getBook(), cell, "#000000", SBorder.BorderType.MEDIUM);
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

        Integer[] idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        if (groupedCells.size() > 0) {

            StringBuffer sqlColumnNames = new StringBuffer("(");
            for (int i = 0; i < idsCol.length; ++i) {
                sqlColumnNames.append(columnNames.get(idsCol[i]));
                if (i < idsCol.length - 1)
                    sqlColumnNames.append(",");
            }
            sqlColumnNames.append(")");


            StringBuffer sqlValuesPlaceHolders = new StringBuffer("(");
            for (int i = 0; i < idsCol.length; ++i) {
                sqlValuesPlaceHolders.append("?");
                if (i < idsCol.length - 1)
                    sqlValuesPlaceHolders.append(",");
            }
            sqlValuesPlaceHolders.append(")");

            /* Only Update */
            StringBuffer update = new StringBuffer("UPDATE ")
                    .append(tableName)
                    .append(" SET ")
                    .append(sqlColumnNames.toString())
                    .append("=")
                    .append(sqlValuesPlaceHolders.toString())
                    .append(" WHERE oid = ?");

            try (PreparedStatement stmt = context.getConnection().prepareStatement(update.toString())) {
                for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                    // Ignore updates to the first row
                    if (_row.getKey() > 0) {
                        int key = rowMapping.getIDs(context, _row.getKey() - 1, 1)[0];
                        stmt.setInt(idsCol.length + 1, key); //at insert
                        for (int i = 0; i < idsCol.length; i++) {
                            stmt.setString(i + 1,
                                    _row.getValue().get(columnList.first() + i).getValue().toString());
                        }
                        stmt.execute();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        TOM_Mapping.instance.pushUpdates(context, tableName);
    }

    @Override
    public CellRegion getBounds(DBContext context) {
        //rowMapping.size(context)    without -1 to include the header of the table
        return new CellRegion(0, 0, rowMapping.size(context), colMapping.size(context) - 1);
    }

    @Override
    public void insertRows(DBContext context, int row, int count) {
        /* Do nothing */
    }

    @Override
    public void insertCols(DBContext context, int col, int count) {
        /* Do nothing */
    }

    /* Delete row on a spreadsheet */
    @Override
    public void deleteRows(DBContext context, int row, int count) {
        /* Do nothing */
    }

    /* Delete column on a spreadsheet */
    @Override
    public void deleteCols(DBContext context, int col, int count) {
        /* Do nothing */
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
    public void clearCache(DBContext context) {
        rowMapping.clearCache(context);
        colMapping.clearCache(context);
        loadColumnInfo(context);
    }

    @Override
    public void importSheet(Reader reader, char delimiter) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteTableColumns(DBContext dbContext, CellRegion cellRegion) {
        return false;
    }
}