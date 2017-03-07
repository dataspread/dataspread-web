package org.model;

import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {
    private Connection connection;

    public DBContext(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
