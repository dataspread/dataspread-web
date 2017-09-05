package org.model;

import java.sql.*;

public class AutoRollbackConnection  implements AutoCloseable {
    static int openConnections=0;
    private String connectionOpenedAt;

    private Connection connection;
    private boolean committed;

    /* TODO: get a connection here */
    AutoRollbackConnection(Connection connection)
    {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length>3)
            System.out.println("Opening connection at " + stack[3]);
        connectionOpenedAt = stack[3].toString();
        this.connection=connection;
        committed = false;
        synchronized (AutoRollbackConnection.class) {
            openConnections++;
        }
    }

    @Override
    public void close(){
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length>2)
            System.out.println("Closing connection at " + stack[2]);
        if (!committed) {
            // Rollback uncomitted transactions
            try {
                connection.rollback();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        try {
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        connection = null;

        synchronized (AutoRollbackConnection.class) {
            openConnections--;
            System.out.println("Open connections: " + openConnections);
        }

    }

    public void commit() {
        try {
            connection.commit();
            committed = true;
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

    public void finalize()
    {
        if (connection!=null) {
            System.err.println("Database Connection left open at " + connectionOpenedAt);
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection=null;
        }
    }

}
