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
public class bookController {
    // Books API
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


    @RequestMapping(value = "/deleteBook",
            method = RequestMethod.PUT)
    public HashMap<String, HashMap> deleteBook(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String book_id = (String) obj.get("book_id");
        BookImpl.deleteBook(book_id, book_id);
        SBook sbook = BookBindings.remove(book_id);
        HashMap<String, HashMap> result = new HashMap<>();
        HashMap<String, String> book = new HashMap<>();
        book.put("name", book_id);
        book.put("id", sbook.getId());
        book.put("link", encode(sbook.getId()));
        result.put("book", book);
        return result;
    }

    @RequestMapping(value = "/addBook",
            method = RequestMethod.POST)
    public HashMap<String, HashMap> addBook(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String book_name = (String) obj.get("name");
        SBook book = new BookImpl(book_name);
        book.checkDBSchema();
        HashMap<String, HashMap> result = new HashMap<>();
        HashMap<String, String> book_json = new HashMap<>();
        book_json.put("name", book_name);
        book_json.put("id", book.getId());
        book_json.put("link", encode(book.getId()));
        result.put("book", book_json);
        return result;
    }

    @RequestMapping(value = "/changeBookName",
            method = RequestMethod.PUT)
    public HashMap<String, HashMap> changeBookName(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String book_id = (String) obj.get("book_id");
        String book_name = (String) obj.get("new_book_name");
        SBook book = BookBindings.get(book_id);
        book.setBookName(book_name);
        BookBindings.remove(book_id);
        BookBindings.put(book_name, book);
        HashMap<String, HashMap> result = new HashMap<>();
        HashMap<String, String> book_json = new HashMap<>();
        book_json.put("name", book_name);
        book_json.put("id", book_id);
        book_json.put("link", encode(book_id));
        result.put("book", book_json);
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
