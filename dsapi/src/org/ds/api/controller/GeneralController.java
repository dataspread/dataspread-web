package org.ds.api.controller;

import org.ds.api.Cell;
import org.ds.api.JsonWrapper;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.sys.TableMonitor;
import org.zkoss.zss.model.sys.BookBindings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.zkoss.json.*;
import javafx.util.Pair;


@RestController
public class GeneralController {
    // General API
    @Autowired private SimpMessagingTemplate template;

    @RequestMapping(value = "/api/getCells/{bookId}/{sheetName}/{row1}/{col1}/{row2}/{col2}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getCells(@PathVariable String bookId,
                                                @PathVariable String sheetName,
                                                @PathVariable int row1,
                                                @PathVariable int col1,
                                                @PathVariable int row2,
                                                @PathVariable int col2) {
        List<Cell> returnCells = new ArrayList<>();

        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        CellRegion range = new CellRegion(row1, col1, row2, col2);
        HashMap<String, Object> data = new HashMap<>();
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());
        JSONArray tableInfo = null;
        try {
            tableInfo = TableMonitor.getMonitor().getTableInformation(dbContext,range,sheetName,bookId);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
        HashSet<Pair<Integer, Integer>> checked = new HashSet<Pair<Integer, Integer>>();
        if (tableInfo!= null){
            for (int i = 0; i < tableInfo.size(); i++){
                JSONObject table = (JSONObject) tableInfo.get(i);
                JSONObject tableRange = (JSONObject) table.get("range");
                int tableRow1 = Integer.parseInt(String.valueOf(tableRange.get("row1")));
                int tableRow2 = Integer.parseInt(String.valueOf(tableRange.get("row2")));
                int tableCol1 = Integer.parseInt(String.valueOf(tableRange.get("col1")));
                int tableCol2 = Integer.parseInt(String.valueOf(tableRange.get("col2")));
                ArrayList<Object> rows = new ArrayList<>();
                for (int row = tableRow1; row <= tableRow2; row++) {
                    ArrayList<Object> cols = new ArrayList<>();
                    for (int col = tableCol1; col <= tableCol2; col++) {
                        SCell sCell = sheet.getCell(row, col);
                        cols.add(sCell.getValue());
                        checked.add(new Pair<>(row, col));
                    }
                    rows.add(cols);
                }
                table.put("cells", rows);
            }
        }
        for (int row = row1; row <= row2; row++) {
            for (int col = col1; col <= col2; col++) {
                Pair<Integer, Integer> pair = new Pair<>(row, col);
                if (!checked.contains(pair)){
                    SCell sCell = sheet.getCell(row, col);

                    Cell cell = new Cell();
                    cell.row = row;
                    cell.col = col;
                    cell.value = String.valueOf(sCell.getValue());
                    if (sCell.getType() == SCell.CellType.FORMULA)
                        cell.formula = sCell.getFormulaValue();
                    returnCells.add(cell);
                }
            }
        }
        data.put("cells", returnCells);
        data.put("tables", tableInfo);
        return JsonWrapper.generateJson(data);
    }

    @RequestMapping(value = "/api/putCells",
            method = RequestMethod.PUT)
    public HashMap<String, Object> putCells(@RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String bookId = obj.getString("bookId");
        String sheetName = obj.getString("sheetName");
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        org.json.JSONArray cells = obj.getJSONArray("cells");
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            for (Object cell : cells) {
                int row = ((org.json.JSONObject)cell).getInt("row");
                int col = ((org.json.JSONObject)cell).getInt("col");
                String type = ((org.json.JSONObject)cell).getString("type");
                String formula = ((org.json.JSONObject)cell).getString("formula");
                Object value = getValue((org.json.JSONObject) cell, type);
                if (!formula.equals("")) {
                    sheet.getCell(row,col).setFormulaValue(formula, connection, true);
                } else {
                    sheet.getCell(row, col).setValue(value, connection, true);
                }
                String format = ((org.json.JSONObject)cell).getString("format");
            }
            return JsonWrapper.generateJson(null);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
    }

    @RequestMapping(value = "/api/insertRows",
            method = RequestMethod.PUT)
    public HashMap<String, Object> insertRows(@RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int rowIdx = obj.getInt("startRow");
        int lastRowIdx = obj.getInt("endRow");
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.insertRow(rowIdx, lastRowIdx);
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/deleteRows",
            method = RequestMethod.DELETE)
    public HashMap<String, Object> deleteRows(@RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int rowIdx = obj.getInt("startRow");
        int lastRowIdx = obj.getInt("endRow");
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.deleteColumn(rowIdx, lastRowIdx);
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/insertCols",
            method = RequestMethod.PUT)
    public HashMap<String, Object> insertCols(@RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int colIdx = obj.getInt("startCol");
        int lastColIdx = obj.getInt("endCol");
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.insertColumn(colIdx, lastColIdx);
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/deleteCols",
            method = RequestMethod.DELETE)
    public HashMap<String, Object> deleteCols(@RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int colIdx = obj.getInt("startCol");
        int lastColIdx = obj.getInt("endCol");
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.deleteColumn(colIdx, lastColIdx);
        return JsonWrapper.generateJson(null);
    }

    private Object getValue(org.json.JSONObject cell, String type){
        switch (type.toUpperCase()) {
            case "INTEGER":
                return cell.getInt("value");
            case "REAL":
            case "FLOAT":
                return cell.getDouble("value");
            case "DATE":
                try{
                    return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(cell.getString("value"));
                } catch  (Exception ex ){
                    return cell.getString("value");
                }
            case "BOOLEAN":
                return cell.getBoolean("value");
            case "TEXT":
            default:
                return cell.getString("value");
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