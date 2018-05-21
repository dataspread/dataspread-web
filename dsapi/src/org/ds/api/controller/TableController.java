package org.ds.api.controller;

import org.ds.api.Cell;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.impl.sys.TableMonitor;
import org.zkoss.zss.model.sys.BookBindings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@RestController
public class TableController {

    final String BOOK_NAME = "book_name";
    final String SCHEMA = "schema";
    final String FILTER = "filter";
    final String ATTRIBUTE_ORDER_PAIR = "attribute_order_pair";
    final String SHEET_NAME = "sheet_name";
    final String ROW_1 = "row_1";
    final String ROW_2 = "row_2";
    final String COL_1 = "col_1";
    final String COL_2 = "col_2";

    final String USER_ID = "user_id";
    final String TABLE_NAME = "table_name";
    final String ROW = "row";
    final String COL = "col";
    final String TABLE_SHEET_ID = "table_sheet_id";
    final String LINK = "link";
    final String MSG = "error_message";
    final String TABLE_CELLS = "table_cells";
    final String COUNT = "count";

   @RequestMapping(value = "/putCell/{book}/{sheet}/{row}/{col}/{value}",
            method = RequestMethod.PUT)
    public void putCells(@PathVariable String book,
                         @PathVariable String sheet,
                         @PathVariable int row,
                         @PathVariable int col,
                         @PathVariable String value) {
        putCells(book, sheet, row, row, col, col, value);
    }

    @RequestMapping(value = "/putCells/{book}/{sheet}/{row1}-{row2}/{col1}-{col2}",
            method = RequestMethod.PUT)
    public void putCells(@PathVariable String book,
                         @PathVariable String sheet,
                         @PathVariable int row1,
                         @PathVariable int row2,
                         @PathVariable int col1,
                         @PathVariable int col2,
                         @RequestBody String value) {

        SBook sbook = BookBindings.getBookByName(book);
        SSheet sSheet = sbook.getSheetByName(sheet);

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            for (int row = row1; row <= row2; row++) {
                for (int col = col1; col <= col2; col++) {
                    sSheet.getCell(row, col).setStringValue(value, connection, true);
                }
            }
        }
    }

    String returnFalse(org.zkoss.json.JSONObject ret, Exception e){
        e.printStackTrace();
        ret.clear();
        ret.put(MSG, e.getMessage());
        return ret.toJSONString();
    }

    @RequestMapping(value = "/createTable",
            method = RequestMethod.PUT)
    public String createTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_NAME);
            String sheet = (String)dict.get(SHEET_NAME);
            String table = (String)dict.get(TABLE_NAME);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            org.zkoss.json.JSONArray json_schema = (org.zkoss.json.JSONArray) dict.get(SCHEMA);
            List<String> schema = new ArrayList<>();
            for (int i = 0; i <= col2 - col1; i++){
                schema.add((String)json_schema.get(i));
            }
            String user_id = (String)dict.get(USER_ID);
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

        return ret.toJSONString();
    }

    @RequestMapping(value = "/linkTable",
            method = RequestMethod.PUT)
    public String linkTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
            String book = (String)dict.get(BOOK_NAME);
            String sheet = (String)dict.get(SHEET_NAME);
            String table = (String)dict.get(TABLE_NAME);
            int row1 = (int)dict.get(ROW_1);
            int row2 = (int)dict.get(ROW_2);
            int col1 = (int)dict.get(COL_1);
            int col2 = (int)dict.get(COL_2);
            String user_id = (String)dict.get(USER_ID);
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

        return ret.toJSONString();
    }

    @RequestMapping(value = "/unlinkTable",method = RequestMethod.PUT)
    public String unlinkTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
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

        return ret.toJSONString();
    }


    @RequestMapping(value = "/getTableCells",method = RequestMethod.PUT)
    public String getTableCells(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
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

        return ret.toJSONString();
    }

    @RequestMapping(value = "/dropTable",method = RequestMethod.PUT)
    public String dropTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
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

        return ret.toJSONString();
    }

    @RequestMapping(value = "/reorderTable",
            method = RequestMethod.PUT)
    public String reorderTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
            String tableSheetId= (String) dict.get(TABLE_SHEET_ID);
            org.zkoss.json.JSONArray attributeOrder= (org.zkoss.json.JSONArray) dict.get(ATTRIBUTE_ORDER_PAIR);
            StringBuilder reorderbuilder = new StringBuilder();
            for (Object object:attributeOrder){
                if (reorderbuilder.length() > 0){
                    reorderbuilder.append(',');
                }
                reorderbuilder.append(((org.zkoss.json.JSONArray)object).get(0)).append(" ").append(((org.zkoss.json.JSONArray)object).get(1));
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

        return ret.toJSONString();
    }

    @RequestMapping(value = "/filterTable",
            method = RequestMethod.PUT)
    public String filterTable(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
            String tableSheetId= (String) dict.get(TABLE_SHEET_ID);
            org.zkoss.json.JSONArray filter= (org.zkoss.json.JSONArray) dict.get(FILTER);
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

        return ret.toJSONString();
    }

    @RequestMapping(value = "/deleteRows",
            method = RequestMethod.PUT)
    public String deleteRows(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
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

        return ret.toJSONString();
    }

    @RequestMapping(value = "/deleteCols",
            method = RequestMethod.PUT)
    public String deleteCols(@RequestBody String value){
        JSONParser paser = new JSONParser();
        org.zkoss.json.JSONObject ret = new org.zkoss.json.JSONObject();
        try {
            org.zkoss.json.JSONObject dict = (org.zkoss.json.JSONObject)paser.parse(value);
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

        return ret.toJSONString();
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