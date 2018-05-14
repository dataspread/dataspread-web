package org.zkoss.zss.model.impl.sys;

import org.junit.Test;
import org.model.AutoRollbackConnection;
import org.model.BlockStore;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSemantics;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.CountedBTree;
import org.zkoss.zss.model.impl.PosMapping;
import org.zkoss.zss.model.sys.BookBindings;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.round;


public class TableController {

    private final static Random        _random        = new Random(System.currentTimeMillis());
    private final static AtomicInteger _tableCount    = new AtomicInteger();
    private final static String        TABLES         = "tables";
    private final static String        TABLESHEETLINK = "sheet_table_link";
    HashMap<String, TableSheetModel> _models;

    private static TableController _tableController = null;

    private TableController(){
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
            DBContext context = new DBContext(connection);
            ArrayList<String> tableSheetLinks = getAllTableSheetLinks(context);
            _models= new HashMap<>();
            for (String links : tableSheetLinks){
                _models.put(links, new TableSheetModel(context, links));
            }
            context.getConnection().commit();
        }
        catch(java.lang.Exception e){
            e.printStackTrace();
        }
    }

    public static TableController getController(){
        if (_tableController == null){
            _tableController = new TableController();
        }
        return _tableController;
    }

    public String[] createTable(DBContext context, CellRegion range, String userId, String metaTableName,
                               String bookName, String sheetName,List<String> schema) throws Exception {
        // todo : sync relationship to mem

        String tableName = getTableName(userId, metaTableName);

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
                .append(tableName)
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

        // todo: uncomment it

        //deleteCells(context, tableHeaderRow);
        return new String[]{insertToTableSheetLink(context, range, bookName, sheetName, tableName),
                            insertToTables(context,userId,metaTableName)};
    }



    public String[] linkTable(DBContext context, CellRegion range, String userId, String metaTableName,
                              String bookName, String sheetName) throws Exception {
        // todo : sync relationship to mem
        String tableName = getTableName(userId, metaTableName);

        // todo: uncomment it

        //deleteCells(context, range);
        return new String[]{insertToTableSheetLink(context, range, bookName, sheetName, tableName),
                getSharedLink(context,userId,metaTableName)};
    }

    public void unLinkTable(DBContext context,String tableSheetLink) throws SQLException {

        String deleteRecords = (new StringBuilder())
                .append("DELETE FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE linkid = ?")
                .toString();
        AutoRollbackConnection connection = context.getConnection();
        try(PreparedStatement stmt = connection.prepareStatement(deleteRecords)){
            stmt.setString(1, tableSheetLink);
            stmt.execute();
        }
    }

    public void dropTable(DBContext context, String user_id, String metaTableName){
        String tableName = getTableName(user_id, metaTableName);
        String dropTable = (new StringBuilder())
                .append("DROP TABLE ")
                .append(tableName)
                .toString();
        AutoRollbackConnection connection = context.getConnection();
        try(Statement stmt = connection.createStatement()){
            stmt.execute(dropTable);
        }catch (SQLException e){
            e.printStackTrace();
        }

        String deleteRecords = (new StringBuilder())
                .append("DELETE FROM ")
                .append(TABLESHEETLINK)
                .append("WHERE tableName = " + tableName)
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

    public void reorderTable(DBContext context, String tableName, String attribute, String order) {
        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append(TABLESHEETLINK)
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
                .append(TABLESHEETLINK)
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

    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange, String sheetName,
                                                String bookName) {

        SBook book = BookBindings.getBookByName(bookName);
        SSheet sheet = book.getSheetByName(sheetName);
        // Reduce Range to bounds
        Collection<AbstractCellAdv> cells = new ArrayList<>();
        String select = (new StringBuilder())
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
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
                            .append("todo") // todo
                            .append(" WHERE " + filter)
                            .append(" ORDER BY " + order)
                            .append(" OFFSET "+startRow+" ROWS")
                            .append(" FETCH NEXT "+endRow+" ROWS ONLY")
                            .toString();

                    try(PreparedStatement state = connection.prepareStatement(select)){
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

    private String insertToTables(DBContext context, String userId, String metaTableName) throws SQLException {
        AutoRollbackConnection connection = context.getConnection();
        StringBuilder sharedLinkBuilder = new StringBuilder().append((char)('a'+_random.nextInt(26)))
                .append(Long.toString(System.currentTimeMillis()+_tableCount.getAndIncrement(), Character.MAX_RADIX));
        for (int i = 0; i < 10; i++){
            sharedLinkBuilder.append((char)('A'+_random.nextInt(26)));
        }
        String sharedLink =  sharedLinkBuilder.toString();
        String appendRecord = (new StringBuilder())
                .append("INSERT INTO ")
                .append(TABLES)
                .append(" VALUES ")
                .append(" (\'" + sharedLink + "\',\'" + metaTableName + "\',\'"
                        + userId + "\') ")
                .toString();


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendRecord);
        }
        return sharedLink;
    }

    private String getSharedLink(DBContext context, String userId, String metaTableName) throws SQLException {
        AutoRollbackConnection connection = context.getConnection();
        String select = (new StringBuilder())
                .append("SELECT sharelink")
                .append(" FROM ")
                .append(TABLES)
                .append(" WHERE userid = \'" + userId + "\' AND tablename = \'" + metaTableName + "\'")
                .toString();
        String sharedLink = "";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            if (rs.next()) {
               sharedLink = rs.getString("sharelink");
            }
        }
        return sharedLink;
    }

    private String insertToTableSheetLink(DBContext context, CellRegion range, String bookName,
                                          String sheetName, String tableName) throws SQLException {
        /* add the record to the tables table */
        AutoRollbackConnection connection = context.getConnection();
        String tableRange = range.row + "-" + range.column + "-" + range.lastRow + "-" + range.lastColumn;
        String linkid = ((char)('a'+_random.nextInt(26))) +
                Long.toString(System.currentTimeMillis()+_tableCount.getAndIncrement(), Character.MAX_RADIX);
        String appendRecord = (new StringBuilder())
                .append("INSERT INTO ")
                .append(TABLESHEETLINK)
                .append(" VALUES ")
                .append(" (\'" + linkid + "\',\'" + bookName + "\',\'"
                        + sheetName + "\',\'" + tableRange + "\',\'" + tableName
                        + "\'," + "\'empty\'" + "," + "\'empty\'" + ") ")
                .toString();


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendRecord);
            _models.put(linkid, new TableSheetModel(context, linkid));
        }
        return linkid;
    }

    private List<Integer> convertToType(List<String> schema) throws Exception {
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

    private void setStmtValue(PreparedStatement stmt, int index, String value, List<Integer> schema) throws Exception {
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

    private String getTableName(String userId, String metaTableName){
        return "_" + userId + "_" + metaTableName;
    }
    private ArrayList<Integer> appendTableRows(DBContext dbContext, CellRegion range,
                                               String tableName, SSheet sheet, List<Integer> schema) throws Exception {

        ArrayList<Integer> oidList = new ArrayList<>();
        int columnCount = range.getLastColumn() - range.getColumn() + 1;
        String update = "INSERT INTO " +
                tableName +
                " VALUES (" +
                IntStream.range(0, columnCount).mapToObj(e -> "?").collect(Collectors.joining(",")) +
                ") RETURNING oid;";

        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            int block_row = 100000;
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
                    _row = groupedCells.computeIfAbsent(cell.getRowIndex(), k -> new TreeMap<>());
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

    private ArrayList<String> getAllTableSheetLinks(DBContext context){
        ArrayList<String> ret = new ArrayList<>();
        String select = (new StringBuilder())
                .append("SELECT linkid")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            while (rs.next()) {
                String linkId = rs.getString("linkid");
                ret.add(linkId);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return ret;
    }
}
