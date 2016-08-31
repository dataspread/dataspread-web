package org.zkoss.zss.model.impl;

import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {
    private Connection connection;

    DBContext(Connection connection) {
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
