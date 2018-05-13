package org.zkoss.zss.model.impl.sys;

import org.model.AutoRollbackConnection;
import org.model.BlockStore;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.poi.ss.formula.atp.DateParser;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSemantics;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.impl.*;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.round;
import static java.sql.JDBCType.NCLOB;


public class NewTableModel {

    private int block_row = 100000;

    BlockStore bs;
    private MetaDataBlock metaDataBlock;
    String newTableName;

    public NewTableModel(String bookName, String sheetName, String tableName){
        this.newTableName = tableName;
        metaDataBlock = new MetaDataBlock();
        metaDataBlock.sheetNames.add(sheetName);
        metaDataBlock.bookNames.add(bookName);
    }

    List<Integer> convertToType(List<String> schema) throws Exception {
        ArrayList<Integer> result = new ArrayList<>();
        for (String s:schema){
            switch (s.toUpperCase()) {
                case "TEXT":
                    result.add(Types.VARCHAR);
                    break;
                case "INTEGER":
                    result.add(Types.INTEGER);
                    break;
                case "REAL":
                case "FLOAT":
                    result.add(Types.FLOAT);
                    break;
                case "DATE":
                    result.add(Types.DATE);
                    break;
                case "BOOLEAN":
                    result.add(Types.BOOLEAN);
                    break;
                default:
                    throw new Exception("Unsupported type");

            }
        }
        return result;
    }

    void setStmtValue(PreparedStatement stmt, int index, String value, List<Integer> schema) throws Exception {
        switch (schema.get(index)) {
            case Types.BOOLEAN:
                stmt.setBoolean(index + 1,Boolean.parseBoolean(value));
                break;
            case Types.BIGINT:
                stmt.setLong(index + 1,Long.parseLong(value));
                break;
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
                stmt.setDouble(index + 1,Double.parseDouble(value));
                break;
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
                stmt.setInt(index + 1, (int) round(Double.parseDouble(value)));
                break;
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.CHAR:
                stmt.setString(index + 1,value);
                break;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            default:
                throw new Exception("Unsupported type");
        }
    }

    public boolean createTable(DBContext context, CellRegion range, String tableName,
                               String bookName, String sheetName,List<String> schema) throws Exception {
        // todo : sync relationship to mem

        newTableName = tableName.toLowerCase();
        /* First create table then create model */
        /* extract table header row */
        CellRegion tableHeaderRow = new CellRegion(range.row, range.column, range.row, range.lastColumn);

        SBook book = BookBindings.getBookByName(bookName);
        SSheet sheet = book.getSheetByName(sheetName);
        List<String> columnList = sheet.getCells(tableHeaderRow)
                .stream()
                .sorted(Comparator.comparingInt(SCell::getColumnIndex))
                .map(SCell::getValue)
                .map(Object::toString)
                .map(e -> e.trim().replaceAll("[^a-zA-Z0-9.\\-;]+", "_"))
                .collect(Collectors.toList());

        if (columnList.size()<tableHeaderRow.getLength())
            throw new Exception("Missing columns names.");

        if (columnList.stream().filter(e->!Character.isLetter(e.charAt(0))).findFirst().isPresent())
            throw new Exception("Column names should start with a letter.");

        final int[] i = {0};
        String createTable = (new StringBuilder())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(newTableName)
                .append(" (")
                .append(columnList.stream().map(e -> e + " " + schema.get(i[0]++)).collect(Collectors.joining(",")))
                .append(") WITH OIDS")
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
        }catch (SQLException e) {
            System.out.println(createTable);
            e.printStackTrace();
        }

        appendTableRows(context,new CellRegion(range.row + 1, range.column, range.lastRow, range.lastColumn),
                tableName,sheet, convertToType(schema));

        //deleteCells(context, tableHeaderRow);
        return insertToTables(context, range, bookName, sheetName, tableName);
    }

    public ArrayList<Integer> appendTableRows(DBContext dbContext, CellRegion range,
                                              String tableName,SSheet sheet, List<Integer> schema) throws Exception {

        ArrayList<Integer> oidList = new ArrayList<>();
        int columnCount = range.getLastColumn() - range.getColumn() + 1;
        String update = new StringBuffer("INSERT INTO ")
                .append(tableName)
                .append(" VALUES (")
                .append(IntStream.range(0, columnCount).mapToObj(e -> "?").collect(Collectors.joining(",")))
                .append(") RETURNING oid;")
                .toString();

        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            for (int i = range.getLastRow() / block_row + 1; i > 0; i--) {
                int min_row = range.getRow() + (i - 1) * block_row;
                int max_row = range.getRow() + i * block_row;
                if (i > range.getLastRow() / block_row) max_row = range.getLastRow();
                CellRegion work_range = new CellRegion(min_row, range.getColumn(), max_row, range.getLastColumn());
                Collection<AbstractCellAdv> cells = sheet.getDataModel().getCells(dbContext, work_range)
                        .stream()
                        .peek(e -> e.translate(-range.getRow(), -range.getColumn())) // Translate
                        .collect(Collectors.toList());

                SortedMap<Integer, SortedMap<Integer, AbstractCellAdv>> groupedCells = new TreeMap<>();
                for (AbstractCellAdv cell : cells) {
                    SortedMap<Integer, AbstractCellAdv> _row;
                    _row = groupedCells.get(cell.getRowIndex());
                    if (_row == null) {
                        _row = new TreeMap<>();
                        groupedCells.put(cell.getRowIndex(), _row);
                    }
                    _row.put(cell.getColumnIndex(), cell);
                }

                for (SortedMap<Integer, AbstractCellAdv> tuple : groupedCells.values()) {
                    for (int j = 0; j < columnCount; j++) {
                        if (tuple.containsKey(j))
                            setStmtValue(stmt,j,tuple.get(j).getValue().toString(),schema);
                        else
                            stmt.setNull(j + 1, schema.get(j));
                    }

                    ResultSet resultSet = stmt.executeQuery();
                    while (resultSet.next())
                        oidList.add(resultSet.getInt(1));
                    resultSet.close();

                }
                // todo: uncomment it:
//                sheet.getDataModel().deleteCells(dbContext, work_range);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return oidList;
    }

    boolean insertToTables(DBContext context, CellRegion range, String bookName,
                                  String sheetName, String tableName){
        /* add the record to the tables table */
        AutoRollbackConnection connection = context.getConnection();
        String tableRange = range.row + "-" + range.column + "-" + range.lastRow + "-" + range.lastColumn;
        String appendRecord = (new StringBuilder())
                .append("INSERT INTO ")
                .append("tables")
                .append(" VALUES ")
                .append(" (\'" + bookName + "\',\'"+ sheetName + "\',\'" + tableRange + "\',\'" + tableName + "\'," + "\'empty\'" + "," + "\'empty\'" + ") ")
                .toString();


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendRecord);
        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void linkTable(DBContext context, CellRegion range,
                                                 String sheetName, String bookName, String tableName) {

//        SBook book = BookBindings.getBookByName(bookName);
//        SSheet sheet = book.getSheetByName(sheetName);
        // Reduce Range to bounds
//        Collection<AbstractCellAdv> cells = new ArrayList<>();
        insertToTables(context,range, bookName,sheetName, tableName);

//        int startRow = 0, endRow = range.lastRow - range.row;
//        int startCol = 0, endCol = range.lastColumn - range.column;
//
//        String query = (new StringBuilder())
//                .append("SELECT")
//                .append(" FROM ")
//                .append(newTableName)
//                .append(" OFFSET "+startRow+" ROWS")
//                .append(" FETCH NEXT "+endRow+" ROWS ONLY")
//                .toString();
//
//        AutoRollbackConnection connection = context.getConnection();
//
//        try(Statement state = connection.createStatement()){
//            ResultSet dataSet = state.executeQuery(query);
//            int row = 0;
//            while(dataSet.next()){
//                int col = startCol;
//                for( int i = 0; i < (endCol - startCol); i++){
//                    byte[] data = dataSet.getBytes(i);
//                    AbstractCellAdv cell = CellImpl.fromBytes(sheet, row, col, data);
//                    cell.setSemantics(SSemantics.Semantics.TABLE_CONTENT);
//                    cells.add(cell);
//                    col++;
//                }
//                row++;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

    }

    public void dropTable(DBContext context){
        String dropTable = (new StringBuilder())
                .append("DROP TABLE ")
                .append(newTableName)
                .toString();
        AutoRollbackConnection connection = context.getConnection();
        try(Statement stmt = connection.createStatement()){
            stmt.execute(dropTable);
        }catch (SQLException e){
            e.printStackTrace();
        }

        String deleteRecords = (new StringBuilder())
                .append("DELETE FROM ")
                .append("tables")
                .append("WHERE tableName = "+newTableName)
                .toString();
        try(Statement stmt = connection.createStatement()){
            stmt.execute(deleteRecords);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void insertRows(DBContext context, int row, int count){
        //Empty rows?
    }

    public void deleteRows(DBContext context, int row, int count){
        //Need pos mapping
    }

    public void deleteTableColumns(DBContext dbContext, int col, int count) {
        //Need pos mapping
    }

    public void sortTable(DBContext context, String tableName, String attribute, String order) {
        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append("tables")
                .append(" SET ")
                .append("order = " + attribute + " " + order)
                .append(" WHERE ")
                .append("tableName == " + tableName)
           .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendToTables);
        }catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void filterTable(DBContext context, String tableName, String filter) {
        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append("tables")
                .append(" SET ")
                .append("filter = " + filter)
                .append(" WHERE ")
                .append("tableName = " + tableName)
           .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendToTables);
        }catch (SQLException e) {
            e.printStackTrace();
        }
  /*change the pos mapping*/

    }

    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange, String sheetName, String bookName) {

        SBook book = BookBindings.getBookByName(bookName);
        SSheet sheet = book.getSheetByName(sheetName);
        // Reduce Range to bounds
        Collection<AbstractCellAdv> cells = new ArrayList<>();
        String select = (new StringBuilder())
                .append("SELECT *")
                .append(" FROM ")
                .append("tables")
                .append(" WHERE sheetName = " + sheet.getSheetName() + " AND bookName = " + book.getBookName())
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(select);
            while(rs.next()){
                Array tableRange = rs.getArray("range");
                int [] rowcol = (int[])tableRange.getArray();
                CellRegion range = new CellRegion(rowcol[0], rowcol[1], rowcol[2], rowcol[3]);
                if(fetchRange.overlaps(range)){
                    String order = rs.getString("order");
                    String filter = rs.getString("filter");
                    CellRegion overlap = fetchRange.getOverlap(range);
                    int startRow = overlap.row - range.row;
                    int endRow = overlap.lastRow - range.lastRow;
                    int startCol = Math.max(overlap.column, range.column);
                    int endCol = Math.min(overlap.lastColumn, range.lastColumn);
                    String query = (new StringBuilder())
                            .append("SELECT")
                            .append(" FROM ")
                            .append(newTableName)
                            .append(" WHERE " + filter)
                            .append(" ORDER BY " + order)
                            .append(" OFFSET "+startRow+" ROWS")
                            .append(" FETCH NEXT "+endRow+" ROWS ONLY")
                            .toString();

                    try(PreparedStatement state = connection.prepareStatement(select.toString())){
                        ResultSet dataSet = state.executeQuery(query);
                        int row = startRow;
                        while(dataSet.next()){
                            int col = startCol;
                            for( int i = 0; i < (endCol - startCol); i++){
                                byte[] data = dataSet.getBytes(i);
                                AbstractCellAdv cell = CellImpl.fromBytes(sheet, row, col, data);
                                cell.setSemantics(SSemantics.Semantics.TABLE_CONTENT);
                                cells.add(cell);
                                col++;
                            }
                            row++;
                        }
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return cells;
    }

    private static class MetaDataBlock {
        List<String> sheetNames;
        List<String> bookNames;
        List<CellRegion> tableRanges;

        MetaDataBlock() {
            sheetNames = new ArrayList();
            bookNames = new ArrayList();
            tableRanges = new ArrayList();
        }
    }
}
