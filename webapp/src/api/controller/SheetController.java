package api.controller;

import api.Authorization;
import api.JsonWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;

import java.util.*;

import org.json.JSONObject;

import static api.WebSocketConfig.MESSAGE_PREFIX;

@RestController
public class SheetController {
    @Autowired
    private SimpMessagingTemplate template;

    private String getCallbackPath(String bookId) {
        return new StringBuilder()
                .append(MESSAGE_PREFIX)
                .append("updateSheets/")
                .append(bookId)
                .toString();
    }

    // Sheets API
    @RequestMapping(value = "/api/getSheets/{bookId}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getSheets(@PathVariable String bookId) {
       // if (!Authorization.authorizeBook(bookId, authToken)){
       //     JsonWrapper.generateError("Permission denied for accessing this book");
       // }
        SBook book = BookBindings.getBookById(bookId);
        return sheetWrapper(book);
    }

    @RequestMapping(value = "/api/deleteSheet",
            method = RequestMethod.DELETE)
    public HashMap<String, Object> deleteSheet(@RequestHeader("auth-token") String authToken,
                                               @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String bookId = (String) obj.get("bookId");
        String sheetName = (String) obj.get("sheetName");
        if (!Authorization.authorizeBook(bookId, authToken)){
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet ssheet = book.getSheetByName(sheetName);
        book.deleteSheet(ssheet);
        template.convertAndSend(getCallbackPath(bookId), "");
        return sheetWrapper(book);
    }

    @RequestMapping(value = "/api/addSheet",
            method = RequestMethod.POST)
    public HashMap<String, Object> addSheet(@RequestHeader("auth-token") String authToken,
                                            @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String sheetName = (String) obj.get("sheetName");
        String bookId = (String) obj.get("bookId");
        if (!Authorization.authorizeBook(bookId, authToken)){
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        book.createSheet(sheetName);
        template.convertAndSend(getCallbackPath(bookId), "");
        return sheetWrapper(book);
    }

    @RequestMapping(value = "/api/changeSheetName",
            method = RequestMethod.PUT)
    public HashMap<String, Object> changeSheetName(@RequestHeader("auth-token") String authToken,
                                                   @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String oldSheetName = (String) obj.get("oldSheetName");
        String newSheetName = (String) obj.get("newSheetName");
        String bookId = (String) obj.get("bookId");
        if (!Authorization.authorizeBook(bookId, authToken)){
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet ssheet = book.getSheetByName(oldSheetName);
        book.setSheetName(ssheet, newSheetName);
        template.convertAndSend(getCallbackPath(bookId), "");
        return sheetWrapper(book);
    }

    @RequestMapping(value = "/api/copySheet",
            method = RequestMethod.POST)
    public HashMap<String, Object> copySheet(@RequestHeader("auth-token") String authToken,
                                             @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String sheetName = (String) obj.get("sheetName");
        String bookId = (String) obj.get("bookId");
        if (!Authorization.authorizeBook(bookId, authToken)){
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        int num = 1;
        String newSheetName = null;
        for(int i = 0, length = book.getNumOfSheet(); i <= length; i++) {
            String n = sheetName + " (" + ++num + ")";
            if(book.getSheetByName(n) == null) {
                newSheetName = n;
                break;
            }
        }
        book.createSheet(newSheetName, sheet);
        template.convertAndSend(getCallbackPath(bookId), "");
        return sheetWrapper(book);
    }

    @RequestMapping(value = "/api/moveSheet",
            method = RequestMethod.PUT)
    public HashMap<String, Object> shiftSheets(@RequestHeader("auth-token") String authToken,
                                               @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String sheetName = (String) obj.get("sheetName");
        String bookId = (String) obj.get("bookId");
        int newPos = (int) obj.get("newPos");
        if (!Authorization.authorizeBook(bookId, authToken)){
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        book.moveSheetTo(book.getSheetByName(sheetName), newPos);
        template.convertAndSend(getCallbackPath(bookId), "");
        return sheetWrapper(book);
    }

    @RequestMapping(value = "/api/clearSheet",
            method = RequestMethod.PUT)
    public HashMap<String, Object> clearSheet(@RequestHeader("auth-token") String authToken,
                                              @RequestBody String json) {
        JSONObject obj = new JSONObject(json);
        String sheetName = (String) obj.get("sheetName");
        String bookId = (String) obj.get("bookId");
        if (!Authorization.authorizeBook(bookId, authToken)){
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        book.deleteSheet(sheet);
        book.createSheet(sheetName);
        template.convertAndSend(GeneralController.getCallbackPath(bookId, sheetName), "");
        return sheetWrapper(book);
    }

    private HashMap<String, Object> sheetWrapper(SBook sbook) {
        List<Object> sheets = new ArrayList<>();
        for (int i = 0; i < sbook.getNumOfSheet(); i++) {
            SSheet sheet = sbook.getSheet(i);
            HashMap<String, Object> sheetJson = new HashMap<>();
            sheetJson.put("name", sheet.getSheetName());
            sheetJson.put("numRow", sheet.getEndRowIndex());
            sheetJson.put("numCol", sheet.getEndColumnIndex());
            sheets.add(sheetJson);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("sheets", sheets);
        return JsonWrapper.generateJson(data);
    }
}
