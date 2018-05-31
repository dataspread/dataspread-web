package org.ds.api.controller;

import org.ds.api.JsonWrapper;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class UserController {

    @RequestMapping(value = "/api/addUser",
            method = RequestMethod.POST)
    public HashMap<String, Object> addUser(@RequestHeader("auth-token") String authToken,
                                           @RequestBody String userName){
        String query = "INSERT INTO user_account(token, username) VALUES (?, ?);";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, authToken);
            statement.setString(2, userName);
            statement.execute();
        } catch (SQLException e) {
            JsonWrapper.generateError(e.getMessage());
        }
        return JsonWrapper.generateJson(null);
    }
}
