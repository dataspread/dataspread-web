package org.model;

import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {
    private AutoRollbackConnection connection;

    public DBContext(AutoRollbackConnection connection) {
        this.connection = connection;
    }

    public AutoRollbackConnection getConnection() {
        return connection;
    }

}
