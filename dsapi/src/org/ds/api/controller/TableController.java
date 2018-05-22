package org.ds.api.controller;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.sys.TableMonitor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.zkoss.json.*;

@RestController
public class TableController {

    static final String BOOK_NAME = "bookName";
    static final String SCHEMA = "schema";
    static final String FILTER = "filter";
    static final String ATTRIBUTE_ORDER_PAIR = "attributeOrderPair";
    static final String SHEET_NAME = "sheetName";
    static final String ROW_1 = "row1";
    static final String ROW_2 = "row2";
    static final String COL_1 = "col1";
    static final String COL_2 = "col2";

    static final String USER_ID = "userId";
    static final String TABLE_NAME = "tableName";
    static final String ROW = "row";
    static final String COL = "col";
    static final String TABLE_SHEET_ID = "tableSheetId";
    static final String LINK = "link";
    static final String MSG = "message";
    static final String DATA = "data";
    static final String STATUS = "status";
    static final String SUCCESS = "success";
    static final String FAILED = "failed";
    static final String TABLE_CELLS = "tableCells";
    static final String COUNT = "count";

    static final String DEFAULT_USER_ID = "DataspreadUser";



    String returnFalse(JSONObject result, Exception e){
        e.printStackTrace();
        result.clear();
        result.put(MSG, e.getMessage());
        result.put(STATUS,FAILED);
        result.put(DATA,null);
        return result.toJSONString();
    }

    String returnTrue(Object data){
        JSONObject result = new JSONObject();
        result.put(MSG,null);
        result.put(STATUS,SUCCESS);
        result.put(DATA,data);
        return result.toJSONString();
    }

    @RequestMapping(value = "/api/createTable",
            method = RequestMethod.PUT)
    public String createTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_NAME);
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
            String user_id = DEFAULT_USER_ID;
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                String[] links = tableModel.createTable(context, range, user_id, table, book, sheet,schema);
                context.getConnection().commit();
                ret.put(TABLE_SHEET_ID, links[0]);
                ret.put(LINK, links[1]);
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/linkTable",
            method = RequestMethod.PUT)
    public String linkTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_NAME);
            String sheet = (String)dict.get(SHEET_NAME);
            String table = (String)dict.get(TABLE_NAME);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            String user_id = DEFAULT_USER_ID;
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                String[] links = tableModel.linkTable(context, range, user_id, table, book, sheet);
                context.getConnection().commit();
                ret.put(TABLE_SHEET_ID, links[0]);
                ret.put(LINK, links[1]);
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/unlinkTable",method = RequestMethod.PUT)
    public String unlinkTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String tableSheetLink = (String) dict.get(TABLE_SHEET_ID);

            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.unLinkTable(context, tableSheetLink);
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }


    @RequestMapping(value = "/api/getTableCells",method = RequestMethod.PUT)
    public String getTableCells(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_NAME);
            String sheet = (String)dict.get(SHEET_NAME);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                ret.put(TABLE_CELLS,tableModel.getCells(context, range, sheet, book));
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/dropTable",method = RequestMethod.PUT)
    public String dropTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String userId= (String) dict.get(USER_ID);
            String tableName= (String) dict.get(TABLE_NAME);

            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.dropTable(context, userId, tableName);
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/reorderTable",
            method = RequestMethod.PUT)
    public String reorderTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String tableSheetId= (String) dict.get(TABLE_SHEET_ID);
            JSONArray attributeOrder= (JSONArray) dict.get(ATTRIBUTE_ORDER_PAIR);
            StringBuilder reorderbuilder = new StringBuilder();
            for (Object object:attributeOrder){
                if (reorderbuilder.length() > 0){
                    reorderbuilder.append(',');
                }
                reorderbuilder.append(((JSONArray)object).get(0)).append(" ").append(((JSONArray)object).get(1));
            }

            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.reorderTable(context, tableSheetId, reorderbuilder.toString());
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/filterTable",
            method = RequestMethod.PUT)
    public String filterTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String tableSheetId= (String) dict.get(TABLE_SHEET_ID);
            JSONArray filter= (JSONArray) dict.get(FILTER);
            StringBuilder filterbuilder = new StringBuilder();
            for (Object object:filter){
                if (filterbuilder.length() > 0){
                    filterbuilder.append(" AND ");
                }
                filterbuilder.append(object);
            }


            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.filterTable(context, tableSheetId, filterbuilder.toString());
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/deleteRows",
            method = RequestMethod.PUT)
    public String deleteRows(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_NAME);
            String sheet = (String)dict.get(SHEET_NAME);
            int row = (int)dict.get(ROW);
            int count = (int)dict.get(COUNT);
            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.deleteRows(context,row, count, book,sheet);
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
    }

    @RequestMapping(value = "/api/deleteCols",
            method = RequestMethod.PUT)
    public String deleteCols(@RequestBody String value){
        JSONParser paser = new JSONParser();
        JSONObject ret = new JSONObject();
        try {
            JSONObject dict = (JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_NAME);
            String sheet = (String)dict.get(SHEET_NAME);
            int col = (int)dict.get(COL);
            int count = (int)dict.get(COUNT);
            TableMonitor tableModel = TableMonitor.getController();
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
                DBContext context = new DBContext(connection);
                tableModel.deleteCols(context,col, count, book,sheet);
                context.getConnection().commit();
            }
            catch(java.lang.Exception e){
                return returnFalse(ret,e);
            }

        }
        catch (java.lang.Exception e){
            return returnFalse(ret,e);
        }

        return returnTrue(ret);
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