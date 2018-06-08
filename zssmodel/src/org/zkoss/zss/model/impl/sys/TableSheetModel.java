package org.zkoss.zss.model.impl.sys;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSemantics;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.*;
import org.zkoss.zss.model.sys.BookBindings;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.zkoss.zss.model.impl.sys.TableMonitor.TABLESHEETLINK;
import static org.zkoss.zss.model.impl.sys.TableMonitor.getRangeFromQueryResult;
import static org.zkoss.zss.model.impl.sys.TableMonitor.setStmtValue;

public class TableSheetModel {

    final static String ATTRIBUTES = "attributes";
    final static String TYPE = "type";
    final static String VALUES = "values";
    final static String TABLE_SHEET_ID = "table_sheet_id";
    final static String LABEL_CELLS = "label_cells";
    final static String FILTER = "filter";
    final static String ORDER = "order";
    final static String NAME = "name";
    final static String RANGE = "range";
    final static String ROW1 = "row1";
    final static String ROW2 = "row2";
    final static String COL1 = "col1";
    final static String COL2 = "col2";
    final static String LINKTABLEID = "linktableid";
    final static String LINK = "link";

    PosMapping rowMapping, colMapping;
    String linkId;
//    String sheetName, String tableName,
    TableSheetModel(DBContext context, String linkId){
//        this.sheetName = sheetName;
//        this.tableName = tableName;
        this.linkId = linkId;
        rowMapping = new CountedBTree(context, "LINK_" + linkId + "_row_idx");
        colMapping = new CountedBTree(context, "LINK_" + linkId + "_col_idx");
    }

    TableSheetModel(DBContext context, String linkId, CellRegion range){
        this.linkId = linkId;
        rowMapping = new CountedBTree(context, "LINK_" + linkId + "_row_idx");
        colMapping = new CountedBTree(context, "LINK_" + linkId + "_col_idx");
        ArrayList<Integer> columnids = new ArrayList<>();
        for (int i = 0; i < range.getColumnCount(); i++){
            columnids.add(i);
        }
        colMapping.insertIDs(context, 0, columnids);
    }

    public void initualizeMapping(DBContext context, ArrayList<Integer> oidList){
        rowMapping.deleteIDs(context, 0, rowMapping.size(context));
        rowMapping.insertIDs(context, 0,oidList);
    }

    public JSONObject getCellsJSON(DBContext context, CellRegion fetchRegion, CellRegion tableRegion,
                                   String tableName, String order, String filter, String sharedLink){
        int rowOffset = tableRegion.getRow();
        int colOffset = tableRegion.getColumn();

        JSONObject ret = new JSONObject();
        ret.put(TABLE_SHEET_ID, linkId);
        JSONArray attributes = new JSONArray();
        ret.put(ATTRIBUTES, attributes);
        JSONArray labels = new JSONArray();
        ret.put(LABEL_CELLS, labels);

        ArrayList<Integer> rowIds;
        boolean includeHeader = (fetchRegion.getRow() == 0);
        if (includeHeader)
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow(),
                    min(fetchRegion.getLastRow() - fetchRegion.getRow(),
                            rowMapping.size(context) - fetchRegion.getRow()));
        else
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow() - 1,
                    min(fetchRegion.getLastRow() - fetchRegion.getRow() + 1,
                    rowMapping.size(context) - fetchRegion.getRow()));


        HashMap<Integer, Integer> row_map = new HashMap<>(); // Oid -> row number
        int bound = rowIds.size();
        for (int i1 = 0; i1 < bound; i1++) {
            if (rowIds.get(i1) != -1) {
                row_map.put(rowIds.get(i1), fetchRegion.getRow() + i1 + (includeHeader ? 1 : 0));
            }
        }


        StringBuffer select = new StringBuffer("SELECT oid,* ")
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE oid = ANY (?) ");

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            // Array inArrayRow = context.getConnection().createArrayOf(pkColumnType, rowIds);
            /* Assume an int array for now */
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds.toArray());
            stmt.setArray(1, inArrayRow);
            ResultSet rs = stmt.executeQuery();

            if (includeHeader) {
                for (int i = fetchRegion.column; i <= fetchRegion.lastColumn; i++){
                    JSONArray cell = new JSONArray();
                    int index = (int) colMapping.getIDs(context,i,1).get(0);
                    cell.add(rs.getMetaData().getColumnLabel(index + 2));
                    cell.add(rowOffset);
                    cell.add(colOffset + i);
                    labels.add(cell);
                }
            }

            ArrayList<JSONArray> attributeCells = new ArrayList<>();
            List<Integer> schema = new ArrayList<>();



            for (int i = fetchRegion.column; i <= fetchRegion.lastColumn; i++){
                JSONArray attributeCell = new JSONArray();
                attributeCells.add(attributeCell);
                JSONObject column = new JSONObject();
                attributes.add(column);
                int index = (int) colMapping.getIDs(context,i,1).get(0);
                int type = rs.getMetaData().getColumnType(index + 2);
                column.put(TYPE, typeIdToString(type));
                schema.add(type);
                column.put(VALUES, attributeCell);
            }
            while (rs.next()) {
                int oid = rs.getInt(1); /* First column is oid */
                int row = row_map.get(oid);

                for (int i = fetchRegion.column; i <= fetchRegion.lastColumn; i++) {
                    JSONArray cell = new JSONArray();
                    int index = (int) colMapping.getIDs(context,i,1).get(0);
                    cell.add(getValue(rs,index, schema.get(i - fetchRegion.column)));
                    cell.add(rowOffset + row);
                    cell.add(colOffset + i);
                    attributeCells.get(i - fetchRegion.column).add(cell);
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    JSONObject getTableInfomation(DBContext context, String tableName, String order, String filter,
                                  String sharedLink, CellRegion fetchRegion, CellRegion tableRange) throws Exception {

        List<Pair<String, Integer>> tableColumns = getSchema(context, tableName, fetchRegion.column,
                min(fetchRegion.lastColumn + 1, colMapping.size(context)) - fetchRegion.column);

        HashMap<String, String> orderMap = new HashMap<>();

        if (order.length() > 0){
            String [] orders = order.split(",");

            for (String o :orders){
                String[] attributeOrder = o.split(" ");
                if (attributeOrder.length != 2) {
                    for (int i = 0; i < attributeOrder.length;i++){
                        System.out.println(i + " " + attributeOrder[i]);
                    }
                    throw new Exception("Error format for order");
                }
                if (!orderMap.containsKey(attributeOrder[0])){
                    orderMap.put(attributeOrder[0],attributeOrder[1]);
                }
            }
        }

        JSONObject ret = new JSONObject();
        JSONArray attributes = new JSONArray();
        JSONObject range = new JSONObject();

        ret.put(ATTRIBUTES,attributes);
        ret.put(RANGE, range);
        ret.put(FILTER,filter);
        ret.put(LINKTABLEID, linkId);
        ret.put(LINK, sharedLink);

        range.put(ROW1, tableRange.getRow());
        range.put(ROW2, tableRange.getLastRow());
        range.put(COL1, tableRange.getColumn());
        range.put(COL2, tableRange.getLastColumn());

        for (Pair<String, Integer> col:tableColumns){
            JSONObject attribute = new JSONObject();
            attributes.add(attribute);
            attribute.put(NAME, col.getX());
            attribute.put(TYPE, typeIdToString(col.getY()));
            if (orderMap.containsKey(col.getX())){
                attribute.put(ORDER, orderMap.get(col.getX()));
            }
            else {
                attribute.put(ORDER,null);
            }
        }
        return ret;
    }

    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRegion, CellRegion tableRegion,
                                                String tableName) {
        int rowOffset = tableRegion.getRow();
        int colOffset = tableRegion.getColumn();
        Collection<AbstractCellAdv> cells = new ArrayList<>();

        int fixedLastColumn = min(fetchRegion.lastColumn + 1, colMapping.size(context));

        ArrayList<Integer> rowIds;
        boolean includeHeader = (fetchRegion.getRow() == 0);
        if (includeHeader)
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow(),
                    min(fetchRegion.getLastRow() - fetchRegion.getRow(),
                            rowMapping.size(context) - fetchRegion.getRow()));
        else
            rowIds = rowMapping.getIDs(context, fetchRegion.getRow() - 1,
                    min(fetchRegion.getLastRow() - fetchRegion.getRow() + 1,
                            rowMapping.size(context) - fetchRegion.getRow() + 1));


        HashMap<Integer, Integer> row_map = new HashMap<>(); // Oid -> row number
        int bound = rowIds.size();
        for (int i1 = 0; i1 < bound; i1++) {
            if (rowIds.get(i1) != -1) {
                row_map.put(rowIds.get(i1), fetchRegion.getRow() + i1 + (includeHeader ? 1 : 0));
            }
        }


        StringBuffer select = new StringBuffer("SELECT oid,* ")
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE oid = ANY (?) ");

        List<Integer> schema = new ArrayList<>();

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            // Array inArrayRow = context.getConnection().createArrayOf(pkColumnType, rowIds);
            /* Assume an int array for now */
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds.toArray());
            stmt.setArray(1, inArrayRow);
            ResultSet rs = stmt.executeQuery();

            if (includeHeader) {
                for (int i = fetchRegion.column; i < fixedLastColumn ; i++){
                    int index = (int) colMapping.getIDs(context,i,1).get(0);
                    cells.add(newCell(rowOffset,colOffset + i,
                            rs.getMetaData().getColumnLabel(index + 2),connection));
                }
            }

            for (int i = fetchRegion.column; i < fixedLastColumn ; i++){
                int index = (int) colMapping.getIDs(context,i,1).get(0);
                int type = rs.getMetaData().getColumnType(index + 2);
                schema.add(type);
            }
            while (rs.next()) {
                int oid = rs.getInt(1); /* First column is oid */
                int row = row_map.get(oid);

                for (int i = fetchRegion.column; i < fixedLastColumn ; i++) {
                    JSONArray cell = new JSONArray();
                    int index = (int) colMapping.getIDs(context,i,1).get(0);
                    cells.add(newCell(rowOffset + row,colOffset + i,
                            getValue(rs,index, schema.get(i - fetchRegion.column)),connection));
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cells;

    }

    void deleteRows(DBContext context, int row, int count){
        rowMapping.deleteIDs(context, row, count);
    }

    void insertRow(DBContext context, int row, int id){
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(id);
        rowMapping.insertIDs(context,row,ids);
    }

    void insertColumn(DBContext context, int col, int id){
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(id);
        colMapping.insertIDs(context,col,ids);
    }

    void deleteCols(DBContext context, int col, int count){
        colMapping.deleteIDs(context, col, count);
    }

    void updateTableCells(DBContext context, CellRegion updateRegion, JSONArray values) throws Exception {
        ArrayList<String> a = new ArrayList<>();

        if (updateRegion.getLastColumn() >= colMapping.size(context) ||
                updateRegion.getLastRow() >= rowMapping.size(context))
            throw new Exception("Update region exceeds table region");
        ArrayList<Integer> rowIds = rowMapping.getIDs(context, updateRegion.getRow(), updateRegion.getRowCount());
        String tableName = getTableName(context);
        ArrayList<Pair<String, Integer>> schema = getSchema(context, tableName, updateRegion.getColumn(),
                updateRegion.getColumnCount());
        String update = "UPDATE " +
                tableName +
                " SET " +
                IntStream.range(0, schema.size()).mapToObj(e -> schema.get(e).getX() + " = ?")
                        .collect(Collectors.joining(",")) +
                " WHERE oid = ?";

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update)) {
            for (int i = 0; i < rowIds.size(); i ++) {
                int oid = rowIds.get(i);
                JSONArray currentRow  = (JSONArray)(values.get(i));
                for (int j = 0; j < updateRegion.getColumnCount(); j++) {
                    setStmtValue(stmt,j,currentRow.get(j).toString(),schema.get(j).getY());
                }
                stmt.setInt(updateRegion.getColumnCount() + 1, oid);
                stmt.execute();
            }
        }

    }

    String getTableName(DBContext context) throws Exception {
        String select = new StringBuilder()
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE linkid = \'")
                .append(linkId)
                .append("\'").toString();

        AutoRollbackConnection connection = context.getConnection();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            if (rs.next()) {
                return rs.getString("tablename");
            }
            else
                throw new Exception("Wrong tableLinkId.");
        }
    }

    CellRegion getRange(DBContext context) throws Exception {
        String select = new StringBuilder()
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE linkid = \'")
                .append(linkId)
                .append("\'").toString();

        AutoRollbackConnection connection = context.getConnection();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            if (rs.next()) {
                CellRegion range = getRangeFromQueryResult(rs);
                return range;
            }
            else
                throw new Exception("Wrong tableLinkId.");
        }
    }

    SSheet getSheet(DBContext context) throws Exception {
        String select = new StringBuilder()
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE linkid = \'")
                .append(linkId)
                .append("\'").toString();

        AutoRollbackConnection connection = context.getConnection();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            if (rs.next()) {
                String bookId = rs.getString("bookid");
                String sheetName = rs.getString("sheetname");
                return BookBindings.getBookById(bookId).getSheetByName(sheetName);
            }
            else
                throw new Exception("Wrong tableLinkId.");
        }
    }

    int getTotalColumnCount(DBContext context) throws Exception {
        String tableName = getTableName(context);
        String select = "SELECT * FROM " + tableName + " limit 0";

        AutoRollbackConnection connection = context.getConnection();
        int totalColumnCount = 0;
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            ResultSet rs = stmt.executeQuery();
            totalColumnCount = rs.getMetaData().getColumnCount();
        }
        return totalColumnCount;
    }

    ArrayList<Pair<String,Integer>> getSchema(DBContext context) throws Exception {
        String select = new StringBuilder()
                .append("SELECT *")
                .append(" FROM ")
                .append(TABLESHEETLINK)
                .append(" WHERE linkid = \'")
                .append(linkId)
                .append("\'").toString();

        String tableName;

        AutoRollbackConnection connection = context.getConnection();

        int columnCount = 0;

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            if (rs.next()) {
                tableName = rs.getString("tablename");
                CellRegion range = getRangeFromQueryResult(rs);
                columnCount = range.getColumnCount();
            }
            else
                throw new Exception("Wrong tableLinkId.");
            columnCount = max(columnCount, colMapping.size(context));
        }

        return getSchema(context, tableName, 0, columnCount);



    }

    ArrayList<Pair<String,Integer>> getSchema(DBContext context, String tableName,
                                              int column, int columnCount) throws Exception {
        String select = "SELECT * FROM " + tableName + " limit 0";

        ArrayList<Pair<String,Integer>> ret = new ArrayList<Pair<String,Integer>>();

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData schema = rs.getMetaData();
            ArrayList<Integer> columns = colMapping.getIDs(context,column,columnCount);

            for (int index:columns){
                ret.add(new Pair<>(schema.getColumnName(index + 1), schema.getColumnType(index + 1)));
            }

            rs.close();
        }
        return ret;
    }
    private static Object getValue(ResultSet rs, int index, int type) throws Exception {
        switch (type) {
            case Types.BOOLEAN:
                return rs.getBoolean(index + 2);
            case Types.BIGINT:
                return rs.getLong(index + 2);
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
                return rs.getDouble(index + 2);
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
                return rs.getInt(index + 2);
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.CHAR:
                return rs.getString(index + 2);
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            default:
                throw new Exception("getValue: Unsupported type");
        }
    }
    private static CellImpl newCell(int row, int column, Object value, AutoRollbackConnection connection){
        CellImpl cell = new CellImpl(row, column);
        cell.setOutterCellValue(value, connection, false);
        cell.setSemantics(SSemantics.Semantics.TABLE_CONTENT);
        return cell;
    }
    private static String typeIdToString(Integer type) throws Exception {
        switch (type) {
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.BIGINT:
                return "BIGINT";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.FLOAT:
                return "FLOAT";
            case Types.REAL:
                return "REAL";
            case Types.NUMERIC:
                return "NUMERIC";
            case Types.INTEGER:
                return "INTEGER";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.LONGVARCHAR:
                return "LONGVARCHAR";
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.CHAR:
                return "CHAR";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            default:
                throw new Exception("typeIdToString:Unsupported type");
        }
    }
    public void drop(DBContext context){
        rowMapping.dropSchema(context);
        colMapping.dropSchema(context);
    }
}
