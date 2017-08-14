package org.zkoss.zss.model.impl;

import org.model.DBContext;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBorder;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.range.impl.StyleUtil;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;


public class TOM_Model extends Model {

    // Assume table is already present
    private Logger logger = Logger.getLogger(TOM_Model.class.getName());
    private PosMapping rowMapping;
    private PosMapping colMapping;

    private SortedMap<Integer, String> columnNames;
    private String pkColumnName;
    private String pkColumnType;


    //Create or load TOM_model.
    TOM_Model(DBContext context, SSheet sheet, String tableName) {
        this.sheet = sheet;
        rowMapping = new BTree(context, tableName + "_row_idx");
        colMapping = new BTree(context, tableName + "_col_idx");
        this.tableName = tableName;
        //sheet.setPassword("000000000"); // Locks the sheet
        // Get columns info
        loadColumnInfo(context);
        getDBPkColumnName(context);
    }

    private void loadColumnInfo(DBContext context) {
        columnNames = new TreeMap<>();
        String tableCols = (new StringBuffer())
                .append("SELECT * FROM ")
                .append(tableName)
                .append(" WHERE false")
                .toString();

        try (Statement stmt = context.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(tableCols.toString());
            int colCount = rs.getMetaData().getColumnCount();
            Integer ids[] = colMapping.getIDs(context, 0, colCount);

            for (int i = 0; i < colCount; i++)
                columnNames.put(ids[i], rs.getMetaData().getColumnName(i+1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Make this as a static and get all info to create a table.
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
        // Since the table id not with a sheet, it shoud not be dropped */
        // TODO: decide the scope of row/col mapping
        rowMapping.dropSchema(context);
        colMapping.dropSchema(context);
    }


    public CellRegion preload(DBContext context, CellRegion range) {
        /* TODO: get dimensions based on mappings */
        /* Batch processing */
        ArrayList<Integer> PKvalues = getPKValues(context);
        Integer ids2[] = rowMapping.createDBIDs(context, 0, PKvalues);

        CellRegion tableSizedRange = new CellRegion(range.getRow(), range.getColumn(),
                range.getRow() + ids2.length, range.getColumn() + columnNames.size() - 1);

        if (range.contains(tableSizedRange)) {
            return tableSizedRange;
        } else {

//            CellRegion returnRange=range.getOverlap(tableSizedRange);
            int lastCol = range.getLastColumn();
            int lastRow = range.getLastRow();

            if (range.getLastColumn() >= tableSizedRange.getLastColumn())
                lastCol = tableSizedRange.getLastColumn();

            if (range.getLastRow() >= tableSizedRange.getLastRow())
                lastCol = tableSizedRange.getLastRow();
            return new CellRegion(range.getRow(), range.getColumn(), lastRow, lastCol);
        }

    }

    private  void getDBPkColumnName(DBContext context) {
        /* TODO update query */
        String findPK = (new StringBuffer())
                .append("SELECT column_name FROM information_schema.key_column_usage ")
                .append("WHERE table_name='")
                .append(tableName)
                .append("' AND constraint_name LIKE '%pkey'")
                .toString();

        try (Statement stmt = context.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(findPK.toString());
            if (rs.next()) {
                pkColumnName = rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Integer> getPKValues(DBContext context) {
        ArrayList<Integer> PKvalues = new ArrayList<>();

        getDBPkColumnName(context);

        String getPKcolVals = (new StringBuffer())
                .append("SELECT " + pkColumnName)
                .append(" FROM " + tableName)
                .append(" ORDER BY " + pkColumnName)
                .toString();

        try (Statement stmt = context.getConnection().createStatement()) {
            ResultSet set = stmt.executeQuery(getPKcolVals);
            pkColumnType = set.getMetaData().getColumnTypeName(1);

            while (set.next()) {
                PKvalues.add(set.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return PKvalues;
    }

    @Override
    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange) {

        // Reduce Range to bounds
        Collection<AbstractCellAdv> cells = new ArrayList<>();

        CellRegion bounds = getBounds(context);
        if (bounds == null || fetchRange == null)
            return cells;

        CellRegion fetchRegion = bounds.getOverlap(fetchRange);
        if (fetchRegion == null)
            return cells;


        Integer[] rowIds;
        int rowCounter;
        boolean includeHeader;
        if (fetchRegion.getRow() == 0) // First Row is the header
        {
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow(), fetchRegion.getLastRow() - fetchRegion.getRow());
            rowCounter = 1;
            includeHeader = true;
        } else {
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow(), fetchRegion.getLastRow() - fetchRegion.getRow() + 1);
            rowCounter = 0;
            includeHeader = false;
        }

        Integer[] colIds = colMapping.getIDs(context, fetchRegion.getColumn(), fetchRegion.getLastColumn() - fetchRegion.getColumn() + 1);

        HashMap<Integer, Integer> row_map = new HashMap<>();
        int bound = rowIds.length;
        for (int i1 = 0; i1 < bound; i1++) {
            row_map.put(rowIds[i1], fetchRegion.getRow() + i1 + rowCounter);
        }

        HashMap<String, Integer> col_map = new HashMap<>();
        int bound1 = colIds.length;
        for (int i1 = 0; i1 < bound1; i1++) {
            String column = columnNames.get(colIds[i1]);
            col_map.put(column, fetchRegion.getColumn() + i1);
        }

        /* TODO: select pk ? */
        StringBuffer select = new StringBuffer("SELECT " + pkColumnName);
        for (int i = 0; i < colIds.length; i++)
            select.append(", ")
                    .append(columnNames.get(colIds[i]));

        /* TODO store strings in pos index */
        select.append(" FROM ")
                .append(tableName)
                .append(" WHERE " + pkColumnName + "::numeric::integer = ANY (?) ");


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
                        StyleUtil.setBackColor(sheet.getBook(),cell,"#99ccff");
                        StyleUtil.setFontBoldWeight(sheet.getBook(),cell, SFont.Boldweight.BOLD);
                        StyleUtil.setBorder(sheet.getBook(),cell, "#000000", SBorder.BorderType.MEDIUM);
                        cell.getCellStyle().setLocked(true);
                    }
                }
            }
            while (rs.next()) {
                int pkVal = rs.getInt(1); /* TODO Change that, first column the PK */
                int row = row_map.get(pkVal);

                for (int i = 0; i < colIds.length; i++) {
                    int col = col_map.get(columnNames.get(colIds[i]));
                    byte[] data = rs.getBytes(i + 2);
                    if (data != null) {
                        AbstractCellAdv cell = CellImpl.fromBytes(sheet, row, col, data);
                        cells.add(cell);
                        StyleUtil.setBackColor(sheet.getBook(),cell,"#99ccff");
                        StyleUtil.setBorder(sheet.getBook(),cell, "#000000", SBorder.BorderType.MEDIUM);
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
//        insert into mytable(id, name, age, color) values (6,'Asmaa','60','brown')
//        on conflict (id)
//        do update set (id,name, age, color) = (6,'Asmaa','60','brown')
//        where mytable.id = 6;


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
                    .append(" WHERE ")
                    .append(pkColumnName)
                    .append("::numeric::integer = ?");

            try (PreparedStatement stmt = context.getConnection().prepareStatement(update.toString())) {
                for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                    int key = rowMapping.getIDs(context, _row.getKey() - 1, 1)[0];
                    stmt.setInt(idsCol.length + 1, key); //at insert
                    for (int i = 0; i < idsCol.length; i++) {
                            stmt.setString(i + 1,
                                    _row.getValue().get(columnList.first() + i).getValue().toString());
                    }
                    stmt.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        TOM_Mapping.instance.pushUpdates(tableName);
    }

    @Override
    public CellRegion getBounds(DBContext context) {
        int rows = rowMapping.size(context);
        int columns = colMapping.size(context);
        if (rows == 0 || columns == 0)
            return null;
        else //rowMapping.size(context)    without -1 to include the header of the table
            return new CellRegion(0, 0, rowMapping.size(context), colMapping.size(context) - 1);
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
            insertColumn.append(" ADD COLUMN ")
                    .append(columnNames.get(ids[i]))
                    .append(" TEXT");
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
    public void clearCache(DBContext context) {
        rowMapping.clearCache(context);
        colMapping.clearCache(context);
    }

    @Override
    public void importSheet(Reader reader, char delimiter) throws IOException {
        throw new UnsupportedOperationException();
    }
}