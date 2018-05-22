package org.ds.api.controller;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.BookBindings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.json.JSONObject;

@RestController
public class BookController {
    // Books API
    @RequestMapping(value = "/api/getBooks",
            method = RequestMethod.GET)
    public HashMap<String, Object> getBooks() {
        List<HashMap<String, Object>> books = new ArrayList<>();
        String query = "SELECT * FROM books";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String bookName = rs.getString("bookname");
                String bookId = rs.getString("booktable");
                Date lastModified = rs.getTimestamp("lastmodified");
                Date createdTime = rs.getTimestamp("createdtime");
                String link = JsonWrapper.encode(bookId);
                HashMap<String, Object> book = new HashMap<>();
                book.put("name", bookName);
                book.put("id", bookId);
                book.put("link", link);
                book.put("lastModified", lastModified);
                book.put("createdTime", createdTime);
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!books.isEmpty()) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("books", books);
            return JsonWrapper.generateJson(data);
        }
        return JsonWrapper.generateJson(null);
    }


    @RequestMapping(value = "/api/deleteBook",
            method = RequestMethod.DELETE)
    public HashMap<String, Object> deleteBook(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String bookId = (String) obj.get("bookId");
        BookImpl.deleteBook(null, bookId);
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/addBook",
            method = RequestMethod.POST)
    public HashMap<String, Object> addBook(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String bookName = (String) obj.get("name");
        String query = "SELECT COUNT(*) FROM books WHERE bookname = ?";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, bookName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return JsonWrapper.generateError("Duplicated Book Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
        SBook book = BookBindings.getBookByName(bookName);
        book.checkDBSchema();
        return bookWrapper(book.getId(), bookName);
    }

    @RequestMapping(value = "/api/changeBookName",
            method = RequestMethod.PUT)
    public HashMap<String, Object> changeBookName(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String bookId = (String) obj.get("bookId");
        String newBookName = (String) obj.get("newBookName");
        SBook book = BookBindings.getBookById(bookId);
        book.setBookName(newBookName);
        return bookWrapper(bookId, newBookName);
    }

    private HashMap<String, Object> bookWrapper(String bookId, String bookName) {
        HashMap<String, Object> bookJson = new HashMap<>();
        bookJson.put("name", bookName);
        bookJson.put("id", bookId);
        bookJson.put("link", JsonWrapper.encode(bookId));
        String query = "SELECT lastmodified, createdtime FROM books WHERE booktable = ?";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, bookId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                bookJson.put("lastModified", rs.getTimestamp("lastmodified"));
                bookJson.put("createdTime", rs.getTimestamp("createdtime"));
            } else {
                bookJson.put("lastModified", null);
                bookJson.put("createdTime", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("book", bookJson);
        return JsonWrapper.generateJson(data);
    }
}
