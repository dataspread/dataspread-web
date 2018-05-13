package org.ds.api;

import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.impl.sys.TableController;
import org.zkoss.zss.model.CellRegion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class ApiController {

    final String BOOK_NAME = "book_name";
    final String SCHEMA = "schema";
    final String FILTER = "filter";
    final String ATTRIBUTE_ORDER_PAIR = "attribute_order_pair";
    final String BOOK_ID = "book_id";
    final String SHEET_NAME = "sheet_name";
    final String ROW_1 = "row_1";
    final String ROW_2 = "row_2";
    final String COL_1 = "col_1";
    final String COL_2 = "col_2";
    final String ATTRIBUTES = "attributes";
    final String TYPE = "type";
    final String VALUES = "values";
    final String DATA = "data";
    final String USER_ID = "user_id";
    final String TABLE_NAME = "table_name";
    final String ROW = "row";
    final String COL = "col";
    final String TABLE_SHEET_ID = "table_sheet_id";
    final String LINK = "link";
    final String MSG = "error_message";

    @RequestMapping(value = "/getCell/{book}/{sheet}/{row}/{col}",
            method = RequestMethod.GET)
    public HashMap<String, List<Cell>> getCells(@PathVariable String book,
                                     @PathVariable String sheet,
                                     @PathVariable int row,
                                     @PathVariable int col) {
        return getCells(book, sheet, row, row, col, col);
    }


    @RequestMapping(value = "/getSheets/{book}",
            method = RequestMethod.GET)
    public HashMap<String, List<String>> getSheets(@PathVariable String book) {
        List<String> sheetNames = new ArrayList<>();

        SBook sbook = BookBindings.getBookByName(book);
        for (int i = 0; i < sbook.getNumOfSheet(); i++)
            sheetNames.add(sbook.getSheet(i).getSheetName());
        HashMap<String, List<String>> result = new HashMap<>();
        result.put("sheets", sheetNames);
        return result;
    }


    @RequestMapping(value = "/getBooks",
            method = RequestMethod.GET)
    public HashMap<String, List<String>> getBooks() {
        List<String> books = new ArrayList<>();
        String query = "SELECT bookname FROM books";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next())
                books.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HashMap<String, List<String>> result = new HashMap<>();
        result.put("books", books);
        return result;
    }


    @RequestMapping(value = "/getCells/{book}/{sheet}/{row1}-{row2}/{col1}-{col2}",
            method = RequestMethod.GET)
    public HashMap<String, List<Cell>> getCells(@PathVariable String book,
                                     @PathVariable String sheet,
                                     @PathVariable int row1,
                                     @PathVariable int row2,
                                     @PathVariable int col1,
                                     @PathVariable int col2) {
        List<Cell> returnCells = new ArrayList<>();

        SBook sbook = BookBindings.getBookByName(book);
        SSheet sSheet = sbook.getSheetByName(sheet);

        for (int row = row1; row <= row2; row++) {
            for (int col = col1; col <= col2; col++) {
                SCell sCell = sSheet.getCell(row, col);

                Cell cell = new Cell();
                cell.row = row;
                cell.col = col;
                cell.value = sCell.getStringValue();
                if (sCell.getType() == SCell.CellType.FORMULA)
                    cell.formula = sCell.getFormulaValue();
                returnCells.add(cell);
            }
        }
        HashMap<String, List<Cell>> result = new HashMap<>();
        result.put("getCells", returnCells);
        return result;
    }

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

    String returnFalse(JSONObject ret, Exception e){
        e.printStackTrace();
        ret.clear();
        ret.put(MSG, e.getMessage());
        return ret.toJSONString();
    }

    @RequestMapping(value = "/createTable",
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
            String user_id = (String)dict.get(USER_ID);
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableController tableModel = TableController.getController();
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

    @RequestMapping(value = "/linkable",
            method = RequestMethod.GET)
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
            String user_id = (String)dict.get(USER_ID);
            CellRegion range = new CellRegion(row1, col1, row2, col2);
            TableController tableModel = TableController.getController();
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

    @RequestMapping(value = "/sortTable/{table}/{attribute}/{order}",
            method = RequestMethod.GET)
    public Boolean sortTable(@PathVariable String table,
                             @PathVariable String attribute,
                             @PathVariable String order) {
        String query = "SELECT tableName FROM sheet_table_link LIMIT 1";
        String book = "";
        String sheet = "";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                book = rs.getString("bookname");
                sheet = rs.getString("sheetname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        TableController tableModel = TableController.getController();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
            DBContext context = new DBContext(connection);
            tableModel.sortTable(context, table, attribute, order);
            context.getConnection().commit();
            context.getConnection().close();
        }
        return true;
    }

    @RequestMapping(value = "/filterTable/{table}/{filter}/{row1}-{row2}/{col1}-{col2}",
            method = RequestMethod.GET)
    public Boolean filterTable(  @PathVariable String table,
                                 @PathVariable String filter) {
        String query = "SELECT tableName FROM sheet_table_link LIMIT 1";
        String book = "";
        String sheet = "";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
             while (rs.next()) {
                 book = rs.getString("bookName");
                sheet = rs.getString("sheetName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        TableController tableModel = TableController.getController();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
            DBContext context = new DBContext(connection);
            tableModel.filterTable(context, table, filter);
        }
        return true;
    }

}