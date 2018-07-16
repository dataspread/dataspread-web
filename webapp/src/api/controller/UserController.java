package api.controller;

import api.JsonWrapper;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class UserController {
    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/api/addUser",
            method = RequestMethod.POST)
    public HashMap<String, Object> addUser(@RequestHeader("auth-token") String authToken,
                                           @RequestBody String json){
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String userName = obj.getString("username");
        String query = "INSERT INTO user_account(authtoken, username) VALUES (?, ?);";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, authToken);
            statement.setString(2, userName);
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            return JsonWrapper.generateError(e.getMessage());
        }
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/addShareBook",
            method = RequestMethod.POST)
    public HashMap<String, Object> addShareBook(@RequestHeader("auth-token") String authToken,
                                           @RequestBody String json){
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String link = obj.getString("link");
        String query = "INSERT INTO user_books VALUES " +
                "(?, (SELECT booktable FROM books WHERE link = ? LIMIT 1), 'share') " +
                "RETURNING booktable";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, authToken);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                connection.commit();
            } else {
                return JsonWrapper.generateError("Shared book can not found!");
            }
        } catch (SQLException e) {
            return JsonWrapper.generateError(e.getMessage());
        }
        return JsonWrapper.generateJson(null);
    }
}
