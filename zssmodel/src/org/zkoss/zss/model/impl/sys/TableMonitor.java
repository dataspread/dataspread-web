package org.zkoss.zss.model.impl.sys;


import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.FormulaClearContext;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.round;


public class TableMonitor {

    private final static Random        _random        = new Random(System.currentTimeMillis());
    private final static AtomicInteger _tableCount    = new AtomicInteger();
    final static String        TABLES         = "tables";
    final static String        TABLESHEETLINK = "sheet_table_link";
    final static String        DISPLAY_NAME = "displayName";
    static final String TABLE_NAME           = "tableName";
    static final String LINK                 = "link";
    HashMap<String, TableSheetModel> _models;

    private static TableMonitor _tableMonitor = null;

    private TableMonitor(){
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

    public static TableMonitor getMonitor(){
        if (_tableMonitor == null){
            _tableMonitor = new TableMonitor();
        }
        return _tableMonitor;
    }

    public String[] createTable(DBContext context, CellRegion range, String userId, String metaTableName,
                               String bookId, String sheetName,List<String> schema) throws Exception {
        // todo : sync relationship to mem

        String tableName = formatTableName(userId, metaTableName);

        /* First create table then create model */
        /* extract table header row */
        CellRegion tableHeaderRow = new CellRegion(range.row, range.column, range.row, range.lastColumn);

        SBook book = BookBindings.getBookById(bookId);
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

        String[] ret = new String[]{insertToTableSheetLink(context, range, bookId, sheetName, tableName),
                insertToTables(context,userId,metaTableName)};

        _models.get(ret[0]).initualizeMapping(context, oidList);

        sheet.getDataModel().deleteCells(context,range);

        clearCache(sheet);

        return ret;
    }



    public String[] linkTable(DBContext context, CellRegion range, String tableName,
                              String bookId, String sheetName) throws Exception {
        // todo : sync relationship to mem

        String[] ret = new String[]{insertToTableSheetLink(context, range, bookId, sheetName, tableName),

                getSharedLink(context,tableName)};

        initializePosmappingForLinkedTable(context, ret[0]);

        // todo: check if it is overlaped with current linked table

        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        // todo: check empty
//        sheet.clearCell(range);
        sheet.getDataModel().deleteCells(context,range);
        clearCache(sheet);
        return ret;
    }

    public void referenceTable(DBContext context, String userId, String displayTableName, String sharedLink) throws Exception {
        AutoRollbackConnection connection = context.getConnection();
        String appendRecord = (new StringBuilder())
                .append("INSERT INTO ")
                .append(TABLES)
                .append(" (sharelink, tablename, userid, displayName, role) ")
                .append(" VALUES (?, ?, ?, ?, ?)")
                .toString();

        try (PreparedStatement stmt = connection.prepareStatement(appendRecord)) {
            stmt.setString(1,sharedLink);
            stmt.setString(2, getTableNameFromSharedLink(context,sharedLink));
            stmt.setString(3, userId);
            stmt.setString(4, displayTableName);
            stmt.setString(5, "collaborator");
            stmt.execute();
        }
    }

    public void unLinkTable(DBContext context,String tableSheetLink) throws Exception {
        TableSheetModel tableSheetModel = _models.get(tableSheetLink);
        SSheet sheet = tableSheetModel.getSheet(context);
        Model model = sheet.getDataModel();
        String tableName = tableSheetModel.getTableName(context);
        CellRegion range = tableSheetModel.getRange(context);
        model.updateCells(context,tableSheetModel.getCells(context,range.shiftedRange(-range.row,-range.column),range, tableName));
        if (model == null){
            throw new Exception("Data model doesn't exist in this sheet.");
        }
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
        tableSheetModel.drop(context);
        _models.remove(tableSheetLink);
        clearCache(sheet);
    }

    public void dropTable(DBContext context, String userId, String tableName) throws Exception {
        AutoRollbackConnection connection = context.getConnection();
        String[] tmp = tableName.split("_",3);
        if (!userId.equals(tmp[1])){
            throw new Exception("Only creater can drop the table.");
        }

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
                .append(" WHERE tablename = ?")
                .toString();
        try(PreparedStatement stmt = connection.prepareStatement(deleteFromTables)){
            stmt.setString(1, tableName);
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

    public void insertRows(DBContext context, String linkTableId, int row, JSONArray values) throws Exception {
        TableSheetModel model = _models.get(linkTableId);
        ArrayList<Pair<String,Integer>> schema = model.getSchema(context);
        String tableName = model.getTableName(context);
        int columnCount = schema.size();

        String update = "INSERT INTO " +
                tableName +
                " (" +
                IntStream.range(0, columnCount).mapToObj(e -> schema.get(e).getX()).collect(Collectors.joining(",")) +
                ") VALUES (" +
                IntStream.range(0, columnCount).mapToObj(e -> "?").collect(Collectors.joining(",")) +
                ") RETURNING oid;";

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            for (int j = 0; j < columnCount; j++)
                setStmtValue(stmt,j,values.get(j).toString(),schema.get(j).getY());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                int oid = resultSet.getInt(1);
                model.insertRow(context, row, oid);

            } else
                throw new Exception("Insert failed");
            resultSet.close();
        }
        clearCache(model.getSheet(context));
    }

    public void insertColumn(DBContext context, String linkTableId, int column, String columnName, String columnType) throws Exception {
        TableSheetModel model = _models.get(linkTableId);
        String tableName = model.getTableName(context);
        String update = "ALTER TABLE " + tableName + " ADD " + columnName +" " + columnType;
        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            stmt.execute();
            model.insertColumn(context, column, model.getTotalColumnCount(context) - 1);
            clearCache(model.getSheet(context));
        }
    }

    public void changeTableColumnType(DBContext context, String linkTableId, int column, String columnType) throws Exception {
        TableSheetModel model = _models.get(linkTableId);
        ArrayList<Pair<String, Integer>> schema = model.getSchema(context);
        String oldColumnName = schema.get(column).getX();
        String tableName = model.getTableName(context);
        String update = "ALTER TABLE "+ tableName +" ALTER COLUMN " + oldColumnName +" TYPE " + columnType;
        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            stmt.execute();
            clearCache(model.getSheet(context));
        }
    }

    public void changeTableColumnName(DBContext context, String linkTableId, int column, String columnName) throws Exception {
        TableSheetModel model = _models.get(linkTableId);
        ArrayList<Pair<String,Integer>> schema = model.getSchema(context);
        String oldColumnName = schema.get(column).getX();
        String tableName = model.getTableName(context);
        String update = "ALTER TABLE "+ tableName + " RENAME COLUMN " + oldColumnName + " TO " + columnName;
        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            stmt.execute();
            clearCache(model.getSheet(context));
        }
    }

    public void deleteRows(DBContext context, int row, int count, String linkId) throws Exception {
        // todo: delete actual row
        TableSheetModel model = _models.get(linkId);
        model.deleteRows(context, row, count);
        clearCache(model.getSheet(context));
    }

    public void deleteCols(DBContext context, int col, int count, String linkId) throws Exception {
        // todo: delete actual column
        TableSheetModel model = _models.get(linkId);
        model.deleteCols(context,col,count);
        clearCache(model.getSheet(context));
    }

    public void shiftRow(DBContext context, String sheetName, String bookId, int row, int rowShift) {
        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append(TABLESHEETLINK)
                .append(" SET row1 = row1 + ?, row2 = row2 + ?")
                .append(" WHERE bookid = ? and sheetname = ? and row1 >= ?")
                .toString();
        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(appendToTables)) {
            stmt.setInt(1, rowShift);
            stmt.setInt(2, rowShift);
            stmt.setString(3, bookId);
            stmt.setString(4, sheetName);
            stmt.setInt(5, row);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void shiftColumn(DBContext context, String sheetName, String bookId, int col, int colShift){
        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append(TABLESHEETLINK)
                .append(" SET col1 = col1 + ?, col2 = col2 + ?")
                .append(" WHERE bookid = ? and sheetname = ? and col1 >= ?")
                .toString();
        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(appendToTables)) {
            stmt.setInt(1, colShift);
            stmt.setInt(2, colShift);
            stmt.setString(3, bookId);
            stmt.setString(4, sheetName);
            stmt.setInt(5, col);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reorderTable(DBContext context, String tableSheetId, String order) throws Exception {

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

        clearCache(_models.get(tableSheetId).getSheet(context));

    }

    public void filterTable(DBContext context, String tableSheetId, String filter) throws Exception {
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

        clearCache(_models.get(tableSheetId).getSheet(context));
    }

    public JSONArray getCells(DBContext context, CellRegion fetchRange, String sheetName,
                              String bookId) {

        String select = selectAllFromSheet(sheetName, bookId);
        JSONArray ret = new JSONArray();
        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(select);
            while(rs.next()){
                String linkId = rs.getString("linkid");
                String tableName = rs.getString("tablename");
                String sharedLink = getSharedLink(context, tableName);
                String filter = rs.getString("filter");
                String order = rs.getString("sort");

                CellRegion range = getRangeFromQueryResult(rs);

                if (fetchRange.overlaps(range)) {
                    CellRegion overlap = fetchRange.getOverlap(range);
                    overlap = overlap.shiftedRange(-range.getRow(), -range.getColumn());
                    ret.add(_models.get(linkId).getCellsJSON(context, overlap, range,
                            tableName, order, filter, sharedLink));

                }

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public JSONArray getTableInformation(DBContext context, CellRegion fetchRange, String sheetName,
                              String bookId) throws Exception {

        String select = selectAllFromSheet(sheetName, bookId);
        JSONArray ret = new JSONArray();
        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(select);
            while(rs.next()){
                String linkId = rs.getString("linkid");
                String tableName = rs.getString("tablename");
                String sharedLink = getSharedLink(context,tableName);
                String filter = rs.getString("filter");
                String order = rs.getString("sort");

                CellRegion range = getRangeFromQueryResult(rs);

                if (fetchRange.overlaps(range)) {
                    CellRegion overlap = fetchRange.getOverlap(range);
                    overlap = overlap.shiftedRange(-range.getRow(), -range.getColumn());
                    ret.add(_models.get(linkId).getTableInfomation(context, tableName,
                            order,filter,sharedLink, overlap, range));

                }

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public Collection<AbstractCellAdv> getTableCells(DBContext context, CellRegion fetchRange, String sheetName,
                              String bookId) {

        Collection<AbstractCellAdv> cells = new ArrayList<>();

        String select = selectAllFromSheet(sheetName, bookId);
        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(select);
            while(rs.next()){
                String linkId = rs.getString("linkid");
                String tableName = rs.getString("tablename");
                CellRegion range = getRangeFromQueryResult(rs);
                if (fetchRange.overlaps(range)) {
                    CellRegion overlap = fetchRange.getOverlap(range);
                    overlap = overlap.shiftedRange(-range.getRow(), -range.getColumn());
                    Collection<AbstractCellAdv> tableCells = _models.get(linkId).getCells(context, overlap, range,
                            tableName);
                    cells.addAll(tableCells);

                }

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return cells;
    }


    public void updateTableCells(DBContext context, String linkTableId, int row1, int row2, int col1, int col2,
                                 JSONArray values) throws Exception {
        CellRegion updateRegion = new CellRegion(row1, col1, row2, col2);
        TableSheetModel model = _models.get(linkTableId);
        model.updateTableCells(context, updateRegion, values);
        clearCache(model.getSheet(context));
    }

    public JSONArray getTables(DBContext context, String userId) throws Exception {
        AutoRollbackConnection connection = context.getConnection();
        JSONArray ret = new JSONArray();
        String select = (new StringBuilder())
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLES)
                .append(" WHERE userid = ?")
                .toString();
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject table = new JSONObject();
                ret.add(table);
                table.put(DISPLAY_NAME, rs.getString("displayname"));
                table.put(TABLE_NAME, rs.getString("tablename"));
                table.put(LINK, rs.getString("sharelink"));
            }
        }
        return ret;
    }

    public static CellRegion getRangeFromQueryResult(ResultSet rs) throws SQLException {
        Integer [] rowcol = {
                rs.getInt("row1"),
                rs.getInt("col1"),
                rs.getInt("row2"),
                rs.getInt("col2")
        };
        return new CellRegion(rowcol[0], rowcol[1], rowcol[2], rowcol[3]);
    }

    public String getTableName(DBContext context, String linkeTableId) throws Exception {
        return _models.get(linkeTableId).getTableName(context);
    }


    public SSheet getSheet(DBContext context, String linkeTableId) throws Exception {
        return _models.get(linkeTableId).getSheet(context);
    }

    public ArrayList<SSheet> getSheets(DBContext context, String tableName) throws Exception {
        ArrayList<SSheet> ret = new ArrayList<>();
        for (String linkedTableId:getAllTableSheetLinksFromTable(context, tableName)){
            ret.add(getSheet(context,linkedTableId));
        }
        return ret;
    }

    private String insertToTables(DBContext context, String userId, String displayTableName) throws SQLException {
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
                .append(" (sharelink, tablename, userid, displayName, role) ")
                .append(" VALUES (?, ?, ?, ?, ?)")
                .toString();


        try (PreparedStatement stmt = connection.prepareStatement(appendRecord)) {
            stmt.setString(1,sharedLink);
            stmt.setString(2,formatTableName(userId,displayTableName));
            stmt.setString(3, userId);
            stmt.setString(4, displayTableName);
            stmt.setString(5, "creater");
            stmt.execute();
        }
        return sharedLink;
    }

    private String getSharedLink(DBContext context, String tableName) throws SQLException {
        AutoRollbackConnection connection = context.getConnection();
        String select = (new StringBuilder())
                .append("SELECT sharelink")
                .append(" FROM ")
                .append(TABLES)
                .append(" WHERE tablename = \'" + tableName + "\'")
                .append(" LIMIT 1")
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
    private String getTableNameFromSharedLink(DBContext context, String sharedLink) throws Exception {
        AutoRollbackConnection connection = context.getConnection();
        String select = (new StringBuilder())
                .append("SELECT tablename")
                .append(" FROM ")
                .append(TABLES)
                .append(" WHERE sharelink = ?")
                .toString();
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, sharedLink);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tablename");
            }
            else
                throw new Exception("Invalid shared link.");

        }
    }

    private String insertToTableSheetLink(DBContext context, CellRegion range, String bookId,
                                          String sheetName, String tableName) throws SQLException {

        // todo:check overlap
        /* add the record to the tables table */
        AutoRollbackConnection connection = context.getConnection();
        String tableRange = range.row + "," + range.column + "," + range.lastRow + "," + range.lastColumn;
        String linkid = ((char)('a'+_random.nextInt(26))) +
                Long.toString(System.currentTimeMillis()+_tableCount.getAndIncrement(), Character.MAX_RADIX);
        String appendRecord = (new StringBuilder())
                .append("INSERT INTO ")
                .append(TABLESHEETLINK)
                .append(" VALUES ")
                .append(" (\'" + linkid + "\',\'" + bookId + "\',\'"
                        + sheetName + "\'," + tableRange + ",\'" + tableName
                        + "\'," + "\'\'" + "," + "\'\'" + ") ")
                .toString();


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendRecord);
            _models.put(linkid, new TableSheetModel(context, linkid, range));
        }
        return linkid;
    }

    private static List<Integer> convertToType(List<String> schema) throws Exception {
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

    static void setStmtValue(PreparedStatement stmt, int index, String value, int type) throws Exception {
        switch (type) {
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

    public static String formatTableName(String userId, String metaTableName){
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
                Collection<SCell> cells = sheet.getCells(work_range)
                        .stream()
                        .peek(e -> (e).translate(-range.getRow(), -range.getColumn())) // Translate
                        .collect(Collectors.toList());

                SortedMap<Integer, SortedMap<Integer, SCell>> groupedCells = new TreeMap<>();
                for (SCell cell : cells) {
                    SortedMap<Integer, SCell> _row;
                    _row = groupedCells.computeIfAbsent(cell.getRowIndex(), k -> new TreeMap<>());
                    _row.put(cell.getColumnIndex(), cell);
                }

                for (SortedMap<Integer, SCell> tuple : groupedCells.values()) {
                    for (int j = 0; j < columnCount; j++) {
                        if (tuple.containsKey(j))
                            setStmtValue(stmt,j,tuple.get(j).getValue().toString(),schema.get(j));
                        else
                            stmt.setNull(j + 1, schema.get(j));
                    }

                    ResultSet resultSet = stmt.executeQuery();
                    while (resultSet.next())
                        oidList.add(resultSet.getInt(1));
                    resultSet.close();

                }
            }
        }
        return oidList;
    }

    private ArrayList<String> getAllTableSheetLinks(DBContext context){
        String select = (new StringBuilder())
                .append("SELECT linkid")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .toString();
        return getTableSheetLinks(context,select);
    }

    private ArrayList<String> getAllTableSheetLinksFromTable(DBContext context, String tableName){
        String select = (new StringBuilder())
                .append("SELECT linkid")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" where tablename = ")
                .append(tableName)
                .toString();
        return getTableSheetLinks(context,select);
    }

    private ArrayList<String> getTableSheetLinks(DBContext context, String select){
        ArrayList<String> ret = new ArrayList<>();
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
                String tableName = rs.getString("tablename");
                CellRegion range = getRangeFromQueryResult(rs);
                Integer count = range.getRowCount();
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

    private String selectAllFromSheet(String sheetName, String bookId){
        StringBuilder select = new StringBuilder()
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE sheetName = \'")
                .append(sheetName)
                .append("\' AND bookId = \'")
                .append(bookId)
                .append("\'");
        return select.toString();
    }



    private void clearCache(SSheet sheet){
        sheet.clearCache();
        EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(sheet));
    }
}
