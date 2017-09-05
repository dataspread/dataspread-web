package org.model;

import java.sql.*;

public class AutoRollbackConnection  implements AutoCloseable {
    static int openConnections=0;

    final private Connection connection;
    private boolean comitted;

    /* TODO: get a connection here */
    AutoRollbackConnection(Connection connection)
    {
        this.connection=connection;
        comitted = false;
        synchronized (AutoRollbackConnection.class) {
            openConnections++;
        }
    }

    @Override
    public void close() throws SQLException {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length>2)
            System.out.println("Closing connection at " + stack[2]);
        if (!comitted) {
            // Rollback uncomitted transactions
            connection.rollback();
        }
        connection.close();

        synchronized (AutoRollbackConnection.class) {
            openConnections--;
            System.out.println("Open connections: " + openConnections);
        }

    }

    public void commit() {
        try {
            connection.commit();
            comitted=true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {};
            e.printStackTrace();
        }
    }

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public Array createArrayOf(String tableName, Object[] elements) throws SQLException {
        return connection.createArrayOf(tableName, elements);
    }

    public Connection getInternalConnection() {
        return connection;
    }
}
