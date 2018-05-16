package org.zkoss.zss.model.impl.sys;

import org.junit.Test;
import org.model.AutoRollbackConnection;
import org.model.BlockStore;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
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

        ArrayList<Integer> oidList = appendTableRows(context,new CellRegion(range.row + 1, range.column,
                range.lastRow, range.lastColumn),tableName,sheet, convertToType(schema));

        // todo: uncomment it

        String[] ret = new String[]{insertToTableSheetLink(context, range, bookName, sheetName, tableName),
                insertToTables(context,userId,metaTableName)};

        _models.get(ret[0]).initualizeMapping(context, oidList);


        //deleteCells(context, tableHeaderRow);
        return ret;
    }



    public String[] linkTable(DBContext context, CellRegion range, String userId, String metaTableName,
                              String bookName, String sheetName) throws Exception {
        // todo : sync relationship to mem
        String tableName = getTableName(userId, metaTableName);

        String[] ret = new String[]{insertToTableSheetLink(context, range, bookName, sheetName, tableName),
                getSharedLink(context,userId,metaTableName)};

        initializePosmappingForLinkedTable(context, ret[0]);

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
        _models.get(tableSheetLink).drop(context);
        _models.remove(tableSheetLink);
    }

    public void dropTable(DBContext context, String userId, String metaTableName) throws SQLException {
        String tableName = getTableName(userId, metaTableName);
        AutoRollbackConnection connection = context.getConnection();

        String selectLinkid = (new StringBuilder())
                .append("SELECT linkid")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE tableName = \'" + tableName + "\'")
                .toString();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectLinkid);
            while (rs.next()) {
                unLinkTable(context, rs.getString("linkid"));
            }
        }

        String deleteFromTables = (new StringBuilder())
                .append("DELETE FROM ")
                .append(TABLES)
                .append(" WHERE tablename = ? and userid = ?")
                .toString();
        try(PreparedStatement stmt = connection.prepareStatement(deleteFromTables)){
            stmt.setString(1, metaTableName);
            stmt.setString(2, userId);
            stmt.execute();
        }

        String dropTable = (new StringBuilder())
                .append("DROP TABLE ")
                .append(tableName)
                .toString();
        try(Statement stmt = connection.createStatement()){
            stmt.execute(dropTable);
        }
    }

    public void insertRows(DBContext context, int row, int count){
        //Empty rows?
    }

    public void deleteRows(DBContext context, int row, int count, String bookName, String sheetName
                           ) throws SQLException {
        String select = selectAllFromSheet(sheetName, bookName);
        CellRegion deleteregion = new CellRegion(row,0,row + count - 1,Integer.MAX_VALUE);
        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            while (rs.next()) {
                String tableRange = rs.getString("range");
                String[] stringRowCol = tableRange.split("-");
                Integer[] rowcol = {Integer.parseInt(stringRowCol[0]),
                        Integer.parseInt(stringRowCol[1]),
                        Integer.parseInt(stringRowCol[2]),
                        Integer.parseInt(stringRowCol[3])};

                CellRegion range = new CellRegion(rowcol[0], rowcol[1], rowcol[2], rowcol[3]);
                if (range.overlaps(deleteregion)){
                    CellRegion overlap = range.getOverlap(deleteregion);
                    String linkId = rs.getString("linkid");
                    if (overlap.getRow() == range.getRow()){
                        unLinkTable(context,linkId);
                    }
                    else {
                        overlap = overlap.shiftedRange(-rowcol[0] - 1, -rowcol[1]);
                        _models.get(linkId).deleteRows(context, overlap.getRow(), overlap.getRowCount());
                    }
                }
            }
        }
    }

    public void deleteCols(DBContext context, int col, int count, String sheetName,
                           String bookName) throws SQLException {
        String select = selectAllFromSheet(sheetName, bookName);

        CellRegion deleteregion = new CellRegion(0,col,Integer.MAX_VALUE,col + count - 1);
        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            while (rs.next()) {
                String tableRange = rs.getString("range");
                String[] stringRowCol = tableRange.split("-");
                Integer[] rowcol = {Integer.parseInt(stringRowCol[0]),
                        Integer.parseInt(stringRowCol[1]),
                        Integer.parseInt(stringRowCol[2]),
                        Integer.parseInt(stringRowCol[3])};

                CellRegion range = new CellRegion(rowcol[0], rowcol[1], rowcol[2], rowcol[3]);
                if (range.overlaps(deleteregion)){
                    CellRegion overlap = range.getOverlap(deleteregion);
                    overlap = overlap.shiftedRange(-rowcol[0], -rowcol[1]);
                    String linkId = rs.getString("linkid");
                    _models.get(linkId).deleteCols(context, overlap.getColumn(), overlap.getColumnCount());
                }
            }
        }
    }

    public void reorderTable(DBContext context, String tableSheetId, String order) throws SQLException {

        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append(TABLESHEETLINK)
                .append(" SET ")
                .append("sort = ?")
                .append(" WHERE ")
                .append("linkid = ?")
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(appendToTables)) {
            stmt.setString(1, order);
            stmt.setString(2, tableSheetId);
            stmt.execute();
        }

        initializePosmappingForLinkedTable(context, tableSheetId);


    }

    public void filterTable(DBContext context, String tableSheetId, String filter) throws SQLException {
        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append(TABLESHEETLINK)
                .append(" SET ")
                .append("filter = ?")
                .append(" WHERE ")
                .append("linkid = ?")
           .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(appendToTables)) {
            stmt.setString(1, filter);
            stmt.setString(2, tableSheetId);
            stmt.execute();
        }
        initializePosmappingForLinkedTable(context, tableSheetId);
    }

    public JSONArray getCells(DBContext context, CellRegion fetchRange, String sheetName,
                              String bookName) {

        SBook book = BookBindings.getBookByName(bookName);
        SSheet sheet = book.getSheetByName(sheetName);
        // Reduce Range to bounds
        String select = selectAllFromSheet(sheetName, bookName);

        JSONArray ret = new JSONArray();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(select);
            while(rs.next()){
                String tableRange = rs.getString("range");
                String linkId = rs.getString("linkid");
                String tableName = rs.getString("tablename");
                String filter = rs.getString("filter");
                String order = rs.getString("sort");
                String [] stringRowCol = tableRange.split("-");
                Integer [] rowcol = {Integer.parseInt(stringRowCol[0]),
                                    Integer.parseInt(stringRowCol[1]),
                                    Integer.parseInt(stringRowCol[2]),
                                    Integer.parseInt(stringRowCol[3])};

                CellRegion range = new CellRegion(rowcol[0], rowcol[1], rowcol[2], rowcol[3]);

                if (fetchRange.overlaps(range)) {
                    CellRegion overlap = fetchRange.getOverlap(range);
                    overlap.shiftedRange(-rowcol[0], -rowcol[1]);
                    ret.add(_models.get(linkId).getCells(context, overlap, rowcol[0], rowcol[1],
                            tableName, order, filter));

                }

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String insertToTables(DBContext context, String userId, String metaTableName) throws SQLException {
        // todo: check overlaping point
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
                        + "\'," + "\'\'" + "," + "\'\'" + ") ")
                .toString();


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendRecord);
            _models.put(linkid, new TableSheetModel(context, linkid, range));
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
    private void initializePosmappingForLinkedTable(DBContext context, String linkId) throws SQLException {
        String select = (new StringBuilder())
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE linkid = \'" + linkId + "\'")
                .toString();

        ArrayList<Integer> oid_list = new ArrayList<>();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            if (rs.next()) {
                String tableRange = rs.getString("range");
                String tableName = rs.getString("tablename");
                String [] stringRowCol = tableRange.split("-");
                Integer count = Integer.parseInt(stringRowCol[2]) - Integer.parseInt(stringRowCol[0]);
                String order = rs.getString("sort");
                String filter = rs.getString("filter");
                String query = (new StringBuilder())
                        .append("SELECT oid")
                        .append(" FROM ")
                        .append(tableName) // todo
                        .append(filter.length() > 0?" WHERE " + filter:"")
                        .append(order.length() > 0?" ORDER BY " + order:"")
                        .append(" LIMIT " + count.toString())
                        .toString();

                try(PreparedStatement state = connection.prepareStatement(query)){
                    ResultSet oids = state.executeQuery();
                    while(oids.next()){
                        oid_list.add(oids.getInt(1));
                    }
                }

            }
        }
        _models.get(linkId).initualizeMapping(context, oid_list);
    }

    private String selectAllFromSheet(String sheetName, String bookName){
        StringBuilder select = new StringBuilder()
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE sheetName = \'")
                .append(sheetName)
                .append("\' AND bookName = \'")
                .append(bookName)
                .append("\'");
        return select.toString();
    }

    private void updateRegion(DBContext context, String linkId, CellRegion region){

    }
}
