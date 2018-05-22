package org.zkoss.zss.model.impl.sys;

import javafx.util.Pair;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSemantics;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.CountedBTree;
import org.zkoss.zss.model.impl.PosMapping;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.zkoss.zss.model.impl.sys.TableMonitor.TABLESHEETLINK;

public class TableSheetModel {

    final static String ATTRIBUTES = "attributes";
    final static String TYPE = "type";
    final static String VALUES = "values";
    final static String TABLE_SHEET_ID = "table_sheet_id";
    final static String LABEL_CELLS = "label_cells";
    final static String FILTER = "order";
    final static String ORDER = "filter";

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

    public JSONObject getCells(DBContext context, CellRegion fetchRegion, int rowOffset,
                               int colOffset, String tableName, String order, String filter){

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

        int columnNum = 0;

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(select);
            if (rs.next()) {
                String tableRange = rs.getString("range");
                tableName = rs.getString("tablename");
                String [] stringRowCol = tableRange.split("-");
                Integer [] rowcol = {Integer.parseInt(stringRowCol[0]),
                        Integer.parseInt(stringRowCol[1]),
                        Integer.parseInt(stringRowCol[2]),
                        Integer.parseInt(stringRowCol[3])};

                CellRegion range = new CellRegion(rowcol[0], rowcol[1], rowcol[2], rowcol[3]);
                columnNum = range.getColumnCount();
            }
            else
                throw new Exception("Wrong tableLinkId.");
        }

        columnNum = max(columnNum, rowMapping.size(context));

        select = "SELECT * FROM " + tableName + " limit 0";

        ArrayList<Pair<String,Integer>> ret = new ArrayList<Pair<String,Integer>>();

        try (PreparedStatement stmt = connection.prepareStatement(select)) {
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData schema = rs.getMetaData();
            ArrayList<Integer> columns = colMapping.getIDs(context,0,columnNum);

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
