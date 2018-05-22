package org.ds.api.controller;

import org.ds.api.Cell;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
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
public class GeneralController {
    // General API

    @RequestMapping(value = "/getCells/{book}/{sheet}/{row1}/{row2}/{col1}/{col2}",
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

    public static String encode(final String clearText) {
        try {
            return new String(
                    Base64.getEncoder().encode(MessageDigest.getInstance("SHA-256").digest(clearText.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return clearText;
        }
    }
}