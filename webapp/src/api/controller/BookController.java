package api.controller;

import api.Authorization;
import api.JsonWrapper;
import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.zkoss.poi.util.IOUtils;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.BookBindings;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static api.WebSocketConfig.MESSAGE_PREFIX;

@CrossOrigin(origins = "http://localhost:3000")

@RestController
public class BookController {
    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/api/getSyncBooks",
            method = RequestMethod.GET)
    public HashMap<String, Object> getSyncBooks(){
        template.convertAndSend(MESSAGE_PREFIX+"/greetings", "");
        return null;
    }

    public static String getCallbackPath() {
        return new StringBuilder()
                .append(MESSAGE_PREFIX)
                .append("/updateBooks")
                .toString();
    }

    // Books API
    @RequestMapping(value = "/api/getBooks",
            method = RequestMethod.GET)
    public List<Map<String, Object>> getBooks() {
        List<Map<String, Object>> books = new ArrayList<>();
        String query = "SELECT * FROM books";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String bookName = rs.getString("bookname");
                String bookId = rs.getString("booktable");
                Date lastModified = rs.getTimestamp("lastmodified");
                Date createdTime = rs.getTimestamp("createdtime");
                books.add(ImmutableMap.of("text", bookName,
                        "value", bookId,
                        "content", "Last Modified:" + lastModified,
                        "description",
                        ImmutableMap.of(
                                "createdTime", createdTime,
                                "lastModified", lastModified)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }


    @RequestMapping(value = "/api/deleteBook",
            method = RequestMethod.DELETE)
    public HashMap<String, Object> deleteBook(@RequestHeader("auth-token") String authToken,
                                              @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        JSONArray bookIds = (JSONArray) obj.get("bookId");
        String[] clearTables = {"user_books"};
        String[] clearDepTables = {"dependency", "full_dependency"};

        for (int i = 0; i < bookIds.length(); i++) {
            // Run delete logic for each individual book
            String bookId = bookIds.getString(i);
            SBook bookImpl = BookBindings.getBookById(bookId);
            String bookName = bookImpl.getBookName();
            System.out.println(bookName);
            // TODO: Find bookName from book table

            if (!Authorization.ownerBook(bookId, authToken)){
                JsonWrapper.generateError("Permission denied for deleting this book");
            }

            // Clean up sheet table
            while (bookImpl.getNumOfSheet() > 0) {
                bookImpl.deleteSheet(bookImpl.getSheet(0));
            }

            // Delete row from book table
            BookImpl.deleteBook(null, bookId);

            // Delete rows from other tables
            for (String table : clearTables) {
                String query = "DELETE FROM " + table + " WHERE booktable = ?";
                System.out.println(query);
                try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, bookId);
                    statement.execute();
                    connection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return JsonWrapper.generateError(e.getMessage());
                }
            }

            // Delete rows from dependency tables
            // for (String table : clearDepTables) {
            //     String query = "DELETE FROM " + table + " WHERE bookname = ?";
            //     System.out.println(query);
            //     try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
            //          PreparedStatement statement = connection.prepareStatement(query)) {
            //         statement.setString(1, bookName);
            //         statement.execute();
            //         connection.commit();
            //     } catch (SQLException e) {
            //         e.printStackTrace();
            //         return JsonWrapper.generateError(e.getMessage());
            //     }
            // }
        }
        template.convertAndSend(getCallbackPath(), "");
        return JsonWrapper.generateJson(null);
    }



    @RequestMapping(value = "/api/addBook",
            method = RequestMethod.POST)
    public HashMap<String, Object> addBook(@RequestHeader("auth-token") String authToken,
                                           @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String bookName = (String) obj.get("name");
        String query = "SELECT COUNT(*) FROM books WHERE bookname = ?";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, bookName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0)
                    return JsonWrapper.generateError("Duplicated Book Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
        SBook book = BookBindings.getBookByName(bookName);
        book.checkDBSchema();
        query = "INSERT INTO user_books VALUES (?, ?, 'owner')";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, authToken);
            statement.setString(2, book.getId());
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
        template.convertAndSend(getCallbackPath(), bookName);
        return bookWrapper(book.getId(), bookName);
    }


    @RequestMapping(value = "/api/changeBookName",
            method = RequestMethod.PUT)
    public HashMap<String, Object> changeBookName(@RequestHeader("auth-token") String authToken,
                                                  @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String bookId = (String) obj.get("bookId");
        if (!Authorization.authorizeBook(bookId, authToken)){
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        String newBookName = (String) obj.get("newBookName");
        SBook book = BookBindings.getBookById(bookId);
        book.setBookName(newBookName);
        template.convertAndSend(getCallbackPath(), "");
        return bookWrapper(bookId, newBookName);
    }

    @RequestMapping(value = "/api/importBook",
            method = RequestMethod.POST)
    public HashMap<String, Object> importBook(InputStream dataStream){
        //JSONParser parser = new JSONParser();
        //JSONObject dict = (JSONObject)parser.parse(value);

        //get Byte Data
        byte[] processedText = new byte[0];
        try {
            processedText = IOUtils.toByteArray(dataStream);
            //System.out.println(new String(processedText,"UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(processedText);


        //createBook and sheet

        String bookName = null;
        String query = null;
        do {
            Random rand = new Random();
            bookName = "book"+rand.nextInt(10000);
            query = "SELECT COUNT(*) FROM books WHERE bookname = ?";
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, bookName);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    if (rs.getInt(1) > 0)
                        bookName=null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return JsonWrapper.generateError(e.getMessage());
            }
        }
        while (bookName==null);

        SBook book = BookBindings.getBookByName(bookName);
        book.checkDBSchema();
        query = "INSERT INTO user_books VALUES (?, ?, 'owner')";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "guest");
            statement.setString(2, book.getId());
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }

        //import into sheet
        char delimiter = ',';
        try {
            book.getSheetByName("Sheet1").getDataModel().importSheet(new BufferedReader(new InputStreamReader(bais)),delimiter,true);
        } catch (IOException e) {
            return JsonWrapper.generateError(e.getMessage());
        }
        //send message

        //template.convertAndSend(getCallbackPath(), bookName);
        return bookWrapper(book.getId(), bookName);


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
