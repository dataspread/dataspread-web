package org.ds.api;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
public class ApiController {
    @RequestMapping(value = "/getCell/{book}/{sheet}/{row}/{col}",
            method = RequestMethod.GET)
    public String getCells(@PathVariable String book,
                                     @PathVariable String sheet,
                                     @PathVariable int row,
                                     @PathVariable int col) {
        return getCells(book, sheet, row, row, col, col);
    }


    @RequestMapping(value = "/getSheets/{book}",
            method = RequestMethod.GET)
    public String getSheets(@PathVariable String book) {
        List<String> sheetNames = new ArrayList<>();

        SBook sbook = BookBindings.getBookByName(book);
        for (int i = 0; i < sbook.getNumOfSheet(); i++)
            sheetNames.add(sbook.getSheet(i).getSheetName());

        return "{\"sheetNames\":" + sheetNames + "}";
    }

    @RequestMapping(value = "/getBooks",
            method = RequestMethod.GET)
    public String getBooks() {
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
        return "{\"books\":" + books + "}";
    }


    @RequestMapping(value = "/getCells/{book}/{sheet}/{row1}-{row2}/{col1}-{col2}",
            method = RequestMethod.GET)
    public String getCells(@PathVariable String book,
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
        return "{\"getCells\":"+ returnCells + "}";
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
}