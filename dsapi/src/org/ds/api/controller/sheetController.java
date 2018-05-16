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
public class sheetController {
    // Sheets API
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

    @RequestMapping(value = "/deleteSheet",
            method = RequestMethod.PUT)
    public HashMap<String, List<String>> deleteSheet(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String book_id = (String) obj.get("book_id");
        String name = (String) obj.get("sheet");
        SBook sbook = BookBindings.getBookByName(book_id);
        SSheet ssheet = sbook.getSheetByName(name);
        sbook.deleteSheet(ssheet);
        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < sbook.getNumOfSheet(); i++)
            sheetNames.add(sbook.getSheet(i).getSheetName());
        HashMap<String, List<String>> result = new HashMap<>();
        result.put("sheets", sheetNames);
        return result;
    }

    @RequestMapping(value = "/addSheet",
            method = RequestMethod.POST)
    public HashMap<String, List<String>> addSheet(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String name = (String) obj.get("sheet");
        String book_id = (String) obj.get("book_id");
        SBook sbook = BookBindings.getBookByName(book_id);
        SSheet ssheet = sbook.createSheet(name);
        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < sbook.getNumOfSheet(); i++)
            sheetNames.add(sbook.getSheet(i).getSheetName());
        HashMap<String, List<String>> result = new HashMap<>();
        result.put("sheets", sheetNames);
        return result;
    }

    @RequestMapping(value = "/changeSheetName",
            method = RequestMethod.PUT)
    public HashMap<String, List<String>> changeSheetName(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String name = (String) obj.get("sheet");
        String book_id = (String) obj.get("book_id");
        SBook sbook = BookBindings.getBookByName(book_id);
        SSheet ssheet = sbook.getSheetByName(name);
        sbook.setSheetName(ssheet, name);
        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < sbook.getNumOfSheet(); i++)
            sheetNames.add(sbook.getSheet(i).getSheetName());
        HashMap<String, List<String>> result = new HashMap<>();
        result.put("sheets", sheetNames);
        return result;
    }

    @RequestMapping(value = "/copySheet",
            method = RequestMethod.POST)
    public HashMap<String, HashMap> copySheet(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String book_name = (String) obj.get("name");
        SBook book = new BookImpl(book_name);
        book.checkDBSchema();
        HashMap<String, HashMap> result = new HashMap<>();
        HashMap<String, String> book_json = new HashMap<>();
        book_json.put("name", book_name);
        book_json.put("id", book.getId());
        book_json.put("link", encode(book.getId()));
        result.put("sheets", book_json);
        return result;
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
