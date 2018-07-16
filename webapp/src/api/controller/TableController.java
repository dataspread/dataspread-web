package api.controller;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.sys.TableMonitor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.zkoss.json.*;

import static api.WebSocketConfig.MESSAGE_PREFIX;
import static org.zkoss.zss.model.impl.sys.TableMonitor.formatTableName;

@RestController
public class TableController {

    @Autowired
    private SimpMessagingTemplate template;

    static final String BOOK_ID              = "bookId";
    static final String SCHEMA               = "schema";
    static final String FILTER               = "filter";
    static final String ATTRIBUTE_ORDER_PAIR = "attributeOrderPair";
    static final String ATTRIBUTE_NAME       = "attributeName";
    static final String ORDER                = "order";
    static final String SHEET_NAME           = "sheetName";
    static final String ROW_1                = "row1";
    static final String ROW_2                = "row2";
    static final String COL_1                = "col1";
    static final String COL_2                = "col2";
    static final String TABLE_NAME           = "tableName";
    static final String DISPLAY_NAME         = "displayName";
    static final String ROW                  = "row";
    static final String COL                  = "col";
    static final String LINK_TABLE_ID        = "linkTableId";
    static final String LINK                 = "link";
    static final String MSG                  = "message";
    static final String DATA                 = "data";
    static final String STATUS               = "status";
    static final String SUCCESS              = "success";
    static final String FAILED               = "failed";
    static final String TABLE_CELLS          = "tableCells";
    static final String DEFAULT_USER_ID      = "DataspreadUser";
    static final String VALUE                = "value";
    static final String COLUMN_TYPE          = "columnType";
    static final String COLUMN_NAME          = "columnName";
    static final String COLUMN               = "column";
    static final String VALUES               = "values";
    final static String        TABLES         = "tables";


    @RequestMapping(value = "/api/createTable",
            method = RequestMethod.POST)
    public JSONObject createTable(@RequestHeader("auth-token") String userId, @RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_ID);
            String sheet = (String)dict.get(SHEET_NAME);
            String table = (String)dict.get(TABLE_NAME);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            JSONArray json_schema = (JSONArray) dict.get(SCHEMA);
            List<String> schema = new ArrayList<>();
            for (int i = 0; i <= col2 - col1; i++){
                schema.add((String)json_schema.get(i));
            }
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                String[] links = tableModel.createTable(context, range, userId, table, book, sheet,schema);
                context.getConnection().commit();
                ret.put(LINK_TABLE_ID, links[0]);
                ret.put(LINK, links[1]);
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }
            notifyUpdate(book,sheet);
        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }


        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/linkTable",
            method = RequestMethod.POST)
    public JSONObject linkTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_ID);
            String sheet = (String)dict.get(SHEET_NAME);
            String table = (String)dict.get(TABLE_NAME);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                String[] links = tableModel.linkTable(context, range, table, book, sheet);
                context.getConnection().commit();
                ret.put(LINK_TABLE_ID, links[0]);
                ret.put(LINK, links[1]);
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }
            notifyUpdate(book,sheet);

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/referenceTable",
            method = RequestMethod.POST)
    public JSONObject referenceTable(@RequestHeader("auth-token") String userId,@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String displayName = (String)dict.get(DISPLAY_NAME);
            String linkedTableId = (String)dict.get(LINK_TABLE_ID);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.referenceTable(context, userId, displayName, linkedTableId);
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }
        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/getTables",
            method = RequestMethod.POST)
    public JSONObject getTables(@RequestHeader("auth-token") String userId){
        JSONObject ret = new JSONObject();
        try {
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                ret.put(TABLES,tableModel.getTables(context, userId));
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }
        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/unlinkTable",method = RequestMethod.POST)
    public JSONObject unlinkTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String tableLinkedId = (String) dict.get(LINK_TABLE_ID);

            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                SSheet sheet = tableModel.getSheet(context, tableLinkedId);
                tableModel.unLinkTable(context, tableLinkedId);
                context.getConnection().commit();
                notifyUpdate(sheet);
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }


        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }


    @RequestMapping(value = "/api/getTableCells",method = RequestMethod.PUT)
    public JSONObject getTableCells(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_ID);
            String sheet = (String)dict.get(SHEET_NAME);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                ret.put(TABLE_CELLS,tableModel.getCells(context, range, sheet, book));
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/dropTable",method = RequestMethod.DELETE)
    public JSONObject dropTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String userId= DEFAULT_USER_ID;
            String tableName= (String) dict.get(TABLE_NAME);

            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                List<SSheet> sheets = tableModel.getSheets(context, formatTableName(userId,tableName));
                tableModel.dropTable(context, userId, tableName);
                context.getConnection().commit();
                notifyUpdates(sheets);
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }


        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }


        return returnTrue(null);
    }

    @RequestMapping(value = "/api/reorderTable",
            method = RequestMethod.PUT)
    public JSONObject reorderTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkedTableId= (String) dict.get(LINK_TABLE_ID);
            JSONArray attributeOrder= (JSONArray) dict.get(ATTRIBUTE_ORDER_PAIR);
            StringBuilder reorderbuilder = new StringBuilder();
            for (Object object:attributeOrder){
                if (reorderbuilder.length() > 0){
                    reorderbuilder.append(',');
                }
                reorderbuilder.append(((JSONObject)object).get(ATTRIBUTE_NAME))
                        .append(" ")
                        .append(((JSONObject)object).get(ORDER));
            }

            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.reorderTable(context, linkedTableId, reorderbuilder.toString());
                context.getConnection().commit();
                notifyUpdate(context,linkedTableId);
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/filterTable",
            method = RequestMethod.PUT)
    public JSONObject filterTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkedTableId= (String) dict.get(LINK_TABLE_ID);
            String filter= (String) dict.get(FILTER);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.filterTable(context, linkedTableId, filter);
                context.getConnection().commit();
                notifyUpdate(context,linkedTableId);
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/deleteTableRow",
            method = RequestMethod.DELETE)
    public JSONObject deleteTableRow(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkId = (String)dict.get(LINK_TABLE_ID);
            int row = (int)dict.get(ROW);
            int count = 1;//(int)dict.get(COUNT);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.deleteRows(context,row, count, linkId);
                context.getConnection().commit();
                notifyUpdates(context,tableModel.getTableName(context,linkId));
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/deleteTableColumn",
            method = RequestMethod.DELETE)
    public JSONObject deleteTableColumn(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkId = (String)dict.get(LINK_TABLE_ID);
            int col = (int)dict.get(COL);
            int count = 1;//(int)dict.get(COUNT);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.deleteCols(context,col, count, linkId);
                context.getConnection().commit();
                notifyUpdates(context,tableModel.getTableName(context,linkId));
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/insertTableRow",
            method = RequestMethod.POST)
    public JSONObject insertTableRow(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkTableId = (String)dict.get(LINK_TABLE_ID);
            int row = (int)dict.get(ROW);
            JSONArray values = (JSONArray)dict.get(VALUE);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.insertRows(context,linkTableId,row,values);
                context.getConnection().commit();
                notifyUpdates(context,tableModel.getTableName(context,linkTableId));
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/insertTableColumn",
            method = RequestMethod.POST)
    public JSONObject insertTableColumn(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkTableId = (String)dict.get(LINK_TABLE_ID);
            int column = (int)dict.get(COLUMN);
            String columnType = (String)dict.get(COLUMN_TYPE);
            String columnName = (String)dict.get(COLUMN_NAME);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.insertColumn(context,linkTableId,column,columnName,columnType);
                context.getConnection().commit();
                notifyUpdates(context,tableModel.getTableName(context,linkTableId));
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/changeTableColumnType",
            method = RequestMethod.PUT)
    public JSONObject changeTableColumnType(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkTableId = (String)dict.get(LINK_TABLE_ID);
            int column = (int)dict.get(COLUMN);
            String columnType = (String)dict.get(COLUMN_TYPE);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.changeTableColumnType(context, linkTableId, column, columnType);
                context.getConnection().commit();
                notifyUpdates(context,tableModel.getTableName(context,linkTableId));
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/changeTableColumnName",
            method = RequestMethod.PUT)
    public JSONObject changeTableColumnName(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkTableId = (String)dict.get(LINK_TABLE_ID);
            int column = (int)dict.get(COLUMN);
            String columnName = (String)dict.get(COLUMN_NAME);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.changeTableColumnName(context, linkTableId, column, columnName);
                context.getConnection().commit();
                notifyUpdates(context,tableModel.getTableName(context,linkTableId));
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    @RequestMapping(value = "/api/updateTableCells",
            method = RequestMethod.PUT)
    public JSONObject updateTableCells(@RequestBody String value){
        JSONParser paser = new JSONParser();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String linkTableId = (String)dict.get(LINK_TABLE_ID);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            JSONArray values = (JSONArray)dict.get(VALUES);
            TableMonitor tableModel = TableMonitor.getMonitor();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.updateTableCells(context,linkTableId,row1,row2,col1,col2,values);
                context.getConnection().commit();
                notifyUpdates(context,tableModel.getTableName(context,linkTableId));
            }
            catch(java.lang.Exception e){
                return returnFalse(e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(e);
        }

        return returnTrue(null);
    }

    JSONObject returnFalse(Exception e){
        JSONObject result = new JSONObject();
        e.printStackTrace();
        result.clear();
        if (e.getMessage() == null)
            result.put(MSG, e.toString());
        else
            result.put(MSG, e.getMessage());
        result.put(STATUS,FAILED);
        result.put(DATA,null);
        return result;
    }

    JSONObject returnTrue(Object data){
        JSONObject result = new JSONObject();
        result.put(MSG,null);
        result.put(STATUS,SUCCESS);
        result.put(DATA,data);
        return result;
    }

    String getCallbackPath(String bookId, String sheetName) {
        return new StringBuilder()
                .append(MESSAGE_PREFIX)
                .append("updateCells/")
                .append(bookId)
                .append("/")
                .append(sheetName)
                .toString();
    }

    void notifyUpdate(DBContext context, String linkedTableId){
        try {
            notifyUpdate(
                TableMonitor.getMonitor().getSheet(context, linkedTableId)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void notifyUpdates(DBContext context, String tableName){
        try {
            notifyUpdates(
                    TableMonitor.getMonitor().getSheets(context, tableName)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void notifyUpdate(String bookId, String sheetName){
        template.convertAndSend(getCallbackPath(bookId,sheetName), "");
    }



    void notifyUpdate(SSheet sheet){
        template.convertAndSend(getCallbackPath(sheet.getBook().getId(), sheet.getSheetName()), "");
    }

    void notifyUpdates(List<SSheet> sheets){
        for (SSheet sheet:sheets){
            notifyUpdate(sheet);
        }
    }








    public static String encode(final String clearText) {
        try {
            return new String(
                    Base64.getEncoder().encode(MessageDigest.getInstance("SHA-256").digest(clearText.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return clearText;
        }
    }
}