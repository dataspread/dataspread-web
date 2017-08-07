package org.zkoss.zss.model.impl;

import com.opencsv.CSVReader;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class TOM_Model extends Model {

    private Logger logger = Logger.getLogger(TOM_Model.class.getName());
    private PosMapping rowMapping;
    private PosMapping colMapping;
    private SortedMap<Integer, String> cols;
    private String pkColumnName = "empno"; /*TODO remove the hardcoding */
    private String pkColumnType;
    //private int startingRow;
    //private int startingCol;

    //Create or load TOM_model.
    TOM_Model(DBContext context, String tableName) {
        rowMapping = new BTree(context, tableName + "_row_idx");
        colMapping = new BTree(context, tableName + "_col_idx");
        this.tableName = tableName;
        createSchema(context);
        cols = new TreeMap<>();


        ArrayList<String> columns = tableColumns(context);
        // Integer ids[] = colMapping.createIDs(context, 0, columns.size());

        Integer ids[] = colMapping.getIDs(context, 0, columns.size());
        for (int i = 0; i < ids.length; i++) {
            cols.put(ids[i], columns.get(i));
        }

    }

    private void createSchema(DBContext context) {
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (rowNo SERIAL PRIMARY KEY)")
//                .append(" ()")
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

    public CellRegion preload(DBContext context, CellRegion range) {

        ArrayList<String> columns = tableColumns(context);
       // Integer ids[] = colMapping.createIDs(context, 0, columns.size());

        Integer ids[] = colMapping.getIDs(context, 0, columns.size());
        for (int i = 0; i < ids.length; i++) {
            cols.put(ids[i], columns.get(i));
        }

        /* Batch processing */
        ArrayList<Integer> PKvalues = PKvalues(context);
        Integer ids2[] = rowMapping.createDBIDs(context, 0, PKvalues);

        CellRegion newRange= new CellRegion(range.getRow(), range.getColumn(), range.getRow()+ids2.length, range.getColumn()+ids.length);

        if(range.contains(newRange))
        {
            return newRange;
        }
        else{
            return range;
        }

    }

    public ArrayList<String> tableColumns(DBContext context) {
        ArrayList<String> columns = new ArrayList<>();

        String tableCols = (new StringBuffer())
                .append("SELECT * FROM ")
                .append(tableName)
                .append(" WHERE false")
                .toString();

        try (Statement stmt = context.getConnection().createStatement()) {
            ResultSet set = stmt.executeQuery(tableCols.toString());

            int colCount = set.getMetaData().getColumnCount();

            for (int i = 0; i < colCount; i++) {
                columns.add(set.getMetaData().getColumnName(i + 1));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }

    public ArrayList<Integer> PKvalues(DBContext context) {
        ArrayList<Integer> PKvalues = new ArrayList<>();

        String findPK = (new StringBuffer())
                .append("SELECT column_name FROM information_schema.key_column_usage ")
                .append("WHERE table_name='")
                .append(tableName)
                .append("' AND constraint_name LIKE '%pkey'")
                .toString();

        try (Statement stmt = context.getConnection().createStatement()) {
            ResultSet set = stmt.executeQuery(findPK.toString());

            if (set.next()) {
                pkColumnName = set.getString(1);
                String getPKcolVals = (new StringBuffer())
                        .append("SELECT " + pkColumnName)
                        .append(" FROM " + tableName)
                        .toString();

                set = stmt.executeQuery(getPKcolVals);

                pkColumnType = set.getMetaData().getColumnTypeName(1);

                while (set.next()) {
                    PKvalues.add(set.getInt(1));
                }
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
//            row_map.put(rowIds[i1], fetchRegion.getRow() + i1);
            row_map.put(rowIds[i1], fetchRegion.getRow() + i1 + rowCounter);
        }

        HashMap<String, Integer> col_map = new HashMap<>();
        int bound1 = colIds.length;
        for (int i1 = 0; i1 < bound1; i1++) {
            String column = cols.get(colIds[i1]);
            col_map.put(column, fetchRegion.getColumn() + i1);
        }

        /* TODO: select pk ? */
        StringBuffer select = new StringBuffer("SELECT " + cols.get(colIds[0]));
        for (int i = 1; i < colIds.length; i++)
            select.append(", ")
                    .append(cols.get(colIds[i]));

        select.append(" FROM ")
                .append(tableName)
                .append(" WHERE " + pkColumnName + " = ANY (?) ");


        try (PreparedStatement stmt = context.getConnection().prepareStatement(select.toString())) {
           // Array inArrayRow = context.getConnection().createArrayOf(pkColumnType, rowIds);
            /* Assume an int array for now */
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds);

            stmt.setArray(1, inArrayRow);

            ResultSet rs = stmt.executeQuery();

            if (includeHeader) {
                for (int i = 0; i < colIds.length; i++) {
                    int col = col_map.get(cols.get(colIds[i]));
                    byte[] data = cols.get(colIds[i]).getBytes();
                    if (data != null) {
                        AbstractCellAdv cell = CellImpl.fromBytes(fetchRegion.getRow(), col, data);
                        cells.add(cell);
                    }
                }
            }
            while (rs.next()) {
                int pkVal = rs.getInt(1); /* TODO why is the first column the PK */
                int row = row_map.get(pkVal);

                for (int i = 0; i < colIds.length; i++) {
                    int col = col_map.get(cols.get(colIds[i]));
                    byte[] data = rs.getBytes(i + 1);
                    if (data != null) {
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

        boolean headerModified = false;
        SortedMap<Integer, AbstractCellAdv> headerRow=null;
        if (groupedCells.get(0) != null)// if table header is modified
        {
            headerRow = groupedCells.get(0);
            groupedCells.remove(0);
            headerModified = true;
            /*
            remove that row from the groupedCells
            boolean columnModified=true;
            at the end of Update.. call function that alter table columns' names
             */
        }
//        if (columnList.last() >= colMapping.size(context))
//            insertCols(context, colMapping.size(context), columnList.last() - colMapping.size(context) + 1);


        Integer[] idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        Boolean PKincluded = false;
        int pkIndex=-1;
        for (int i = 0; i < idsCol.length; i++) {
            String value = cols.get(idsCol[i]);
            if (value == pkColumnName) {
                PKincluded = true;
                pkIndex=i;
                break;
            }
        }
//        insert into mytable(id, name, age, color) values (6,'Asmaa','60','brown')
//        on conflict (id)
//        do update set (id,name, age, color) = (6,'Asmaa','60','brown')
//        where mytable.id = 6;

        StringBuffer sqlColumnNames= new StringBuffer("(");
        for (int i = 0; i < idsCol.length; ++i) {
            sqlColumnNames.append(cols.get(idsCol[i]));
            if (i < idsCol.length - 1)
                sqlColumnNames.append(",");
        }
        if(!PKincluded) {
            sqlColumnNames.append(",")
                    .append(pkColumnName);
        }
        sqlColumnNames.append(")");


        StringBuffer sqlValuesPlaceHolders=new StringBuffer("(");
        for (int i = 0; i < idsCol.length; ++i) {
            sqlValuesPlaceHolders.append("?");
            if (i < idsCol.length - 1)
                sqlValuesPlaceHolders.append(",");
        }
        if(!PKincluded)        {
            sqlValuesPlaceHolders.append(",?");
        }
        sqlValuesPlaceHolders.append(")");

        StringBuffer update = new StringBuffer("INSERT INTO ")
                .append(tableName)
                .append(sqlColumnNames.toString())
                .append(" VALUES ")
                .append(sqlValuesPlaceHolders.toString())
                .append(" ON CONFLICT(")
                .append(pkColumnName)
                .append(") DO UPDATE SET ")
                .append(sqlColumnNames.toString())
                .append("=")
                .append(sqlValuesPlaceHolders.toString())
                .append(") WHERE ")
                .append(tableName)
                .append("." + pkColumnName)
                .append("=?");

//        insert into mytable(name, age, color,id) values (?,?,?,?)
//        on conflict (id)
//        do update set (name, age, color,id) = (?,?,?,?)
//        where mytable.id = ?;

        try (PreparedStatement stmt = context.getConnection().prepareStatement(update.toString())) {
            for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {

                int PKvalue = rowMapping.getIDs(context, _row.getKey(), 1)[0];
                int index2;

                if (!PKincluded) {
                    stmt.setInt(idsCol.length+1, PKvalue); //at insert
                    stmt.setInt((idsCol.length * 2) +2, PKvalue); // at update
                    stmt.setInt((idsCol.length * 2) +3, PKvalue); // at where

                    index2=idsCol.length+2;

                } else {

                    if (_row.getValue().get(columnList.first() + pkIndex) != null)
                    {
                        stmt.setInt(pkIndex + 1,
                                _row.getValue().get(columnList.first() + pkIndex).getNumberValue().intValue()); //at insert
                        stmt.setInt(idsCol.length + pkIndex + 1,
                                _row.getValue().get(columnList.first() + pkIndex).getNumberValue().intValue()); // at update

                        // Need to update the Keys!! Missing

                    }

                    stmt.setInt((idsCol.length * 2)+1, PKvalue); //at where

                    index2=idsCol.length+1;
                }

                for (int i = 0; i < idsCol.length; i++) {
                    if(pkIndex>-1 && i==pkIndex)
                        continue;

                    if (_row.getValue().get(columnList.first() + i) == null) {
                        stmt.setNull(i + 1, Types.NULL);
                        stmt.setNull(i + index2, Types.NULL);
                    }else {
//                        String type=_row.getValue().get(columnList.first() + i).getType().name();
//                        if (type.equalsIgnoreCase("NUMBER")) {
//                            stmt.setInt(i + 1,
//                                    _row.getValue().get(columnList.first() + i).getNumberValue().intValue());
//                            stmt.setInt(i + index2,
//                                    _row.getValue().get(columnList.first() + i).getNumberValue().intValue());
//                        }else
//                        {
                            stmt.setString(i + 1,
                                    _row.getValue().get(columnList.first() + i).getStringValue());
                            stmt.setString(i + index2,
                                    _row.getValue().get(columnList.first() + i).getStringValue());
//                        }
                    }
                }
                stmt.execute();
            }

            if(headerModified) {
                Statement HeaderStmt = context.getConnection().createStatement();
                for (int i = 0; i < idsCol.length; i++) {
                    if (headerRow.get(columnList.first() + i) != null)
                    {
                        StringBuffer sql= new StringBuffer("ALTER TABLE "+tableName +"  RENAME COLUMN ")
                                .append(cols.get(idsCol[i]))
                                .append(" TO ")
                                .append(headerRow.get(columnList.first() + i).getValue().toString());
                        HeaderStmt.execute(sql.toString());

                        // Update the Columns Sorted Set
                        cols.replace(idsCol[i],cols.get(idsCol[i]),headerRow.get(columnList.first() + i).getValue().toString());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public CellRegion getBounds(DBContext context) {
        int rows = rowMapping.size(context);
        int columns = colMapping.size(context);
        if (rows == 0 || columns == 0)
            return null;
        else //startingRow+rowMapping.size(context)    without -1 to include the header of the table
            return new CellRegion(0, 0,  rowMapping.size(context),  colMapping.size(context) - 1);
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
            while ((nextLine = csvReader.readNext()) != null) {
                ++importedRows;
                if (cpIN == null) {
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
        /*
        StringBuffer update = new StringBuffer("WITH upsert AS (UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int i = 0; i < idsCol.length; ++i) {
            update.append(cols.get(idsCol[i]))
                    .append("=COALESCE(?,")
                    .append(cols.get(idsCol[i]))
                    .append(")");
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(" WHERE "+pkColumnName+"= ? RETURNING *) ")
                .append("INSERT INTO ")
                .append(tableName)
                .append("( ");
        for (int i = 0; i < idsCol.length; ++i) {
            update.append(cols.get(idsCol[i]));
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") SELECT ");
        for (int i = 0; i < idsCol.length; ++i) {
            update.append("?,");
            if (i < idsCol.length - 1)
                update.append(",");
        }

        update.append(" WHERE NOT EXISTS (SELECT * FROM upsert)");
*/


        /*
        StringBuffer update = new StringBuffer("INSERT INTO ")
                .append(tableName)
                .append("(");

        if(PKincluded) {
            update.append(pkColumnName)
                .append(",");
        }
        for (int i = 0; i < idsCol.length; ++i) {
            update.append(cols.get(idsCol[i]));
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") VALUES(");
        if(PKincluded) {
            update.append("?,");
        }
        for (int i = 0; i < idsCol.length; ++i) {
            update.append("?");
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") ON CONFLICT(")
                .append(pkColumnName)
                .append(") DO UPDATE SET(");

        for (int i = 0; i < idsCol.length; ++i) {
            if(idsCol[i]==pkColID)
                continue;

            update.append(cols.get(idsCol[i]));
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") = (");
        int length=idsCol.length;
        if(!PKincluded)
        {
            length=idsCol.length-1;
        }
        for (int i = 0; i < length; ++i) {
            update.append("?");
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") WHERE ")
                .append(tableName)
                .append("."+pkColumnName)
                .append("=?");




        for (int i = 0; i < idsCol.length; ++i) {
            String column=cols.get(idsCol[i]);
            if(column==pkColumnName)
                continue;
            update.append(cols.get(idsCol[i]));
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") VALUES(");
        if (!PKincluded) {
            update.append("?,");
        }
        for (int i = 0; i < idsCol.length; ++i) {
            update.append("?");
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") ON CONFLICT(")
                .append(pkColumnName)
                .append(") DO UPDATE SET(");

        for (int i = 0; i < idsCol.length; ++i) {
            String column=cols.get(idsCol[i]);
            if(column==pkColumnName)
                continue;
            update.append(cols.get(idsCol[i]));
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") = (");
        int length = idsCol.length;
        if (PKincluded) {
            length = idsCol.length - 1;
        }
        for (int i = 0; i < length; ++i) {
            update.append("?");
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") WHERE ")
                .append(tableName)
                .append("." + pkColumnName)
                .append("=?");





















         */