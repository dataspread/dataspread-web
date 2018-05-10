package org.ds.api;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.impl.sys.NewTableModel;
import org.zkoss.zss.model.CellRegion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@RestController
public class ApiController {
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

    @RequestMapping(value = "/createTable/{book}/{sheet}/{table}/{row1}-{row2}/{col1}-{col2}",
            method = RequestMethod.GET)
    public Boolean createTable(@PathVariable String book,
                               @PathVariable String sheet,
                               @PathVariable String table,
                               @PathVariable int row1,
                               @PathVariable int row2,
                               @PathVariable int col1,
                               @PathVariable int col2){
        CellRegion range = new CellRegion(row1, row2, col1, col2);
        NewTableModel tableModel = new NewTableModel( book, sheet, table);
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
            DBContext context = new DBContext(connection);
            tableModel.createTable(context, range, table, book, sheet);
            context.getConnection().commit();
            context.getConnection().close();
        }
        catch(java.lang.Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @RequestMapping(value = "/linkable/{book}/{sheet}/{table}/{row1}-{row2}/{col1}-{col2}",
            method = RequestMethod.GET)
    public Boolean linkTable(@PathVariable String book,
                               @PathVariable String sheet,
                               @PathVariable String table,
                               @PathVariable int row1,
                               @PathVariable int row2,
                               @PathVariable int col1,
                               @PathVariable int col2){
        CellRegion range = new CellRegion(row1, row2, col1, col2);
        NewTableModel tableModel = new NewTableModel( book, sheet, table);
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
            DBContext context = new DBContext(connection);
            tableModel.linkTable(context, range, table, book, sheet);
            context.getConnection().commit();
            context.getConnection().close();
        }
        catch(java.lang.Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @RequestMapping(value = "/sortTable/{table}/{attribute}/{order}",
            method = RequestMethod.GET)
    public Boolean sortTable(@PathVariable String table,
                             @PathVariable String attribute,
                             @PathVariable String order) {
        String query = "SELECT tableName FROM tables LIMIT 1";
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
        NewTableModel tableModel = new NewTableModel(book, sheet, table);
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
        String query = "SELECT tableName FROM tables LIMIT 1";
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
        NewTableModel tableModel = new NewTableModel(book, sheet, table);
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
            DBContext context = new DBContext(connection);
            tableModel.filterTable(context, table, filter);
        }
        return true;
    }

}