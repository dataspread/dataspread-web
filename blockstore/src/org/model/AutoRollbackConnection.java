package org.model;

import java.sql.*;
import java.util.logging.Logger;

public class AutoRollbackConnection  implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(DBHandler.class.getName());
    static int openConnections=0;
    private String connectionOpenedAt;

    private Connection connection;
    private boolean committed;

    /* TODO: get a connection here */
    AutoRollbackConnection(Connection connection)
    {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        //if (stack.length>3)
        //    logger.info("Opening connection at " + stack[3]);
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
        //if (stack.length>2)
        //    logger.info("Closing connection at " + stack[2]);
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
            //logger.info("Open connections: " + openConnections);
        }

    }

    public void commit() {
        try {
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {}
            e.printStackTrace();
        }
    }

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return connection.createArrayOf(typeName, elements);
    }

    public Connection getInternalConnection() {
        return connection;
    }

    public void finalize()
    {
        if (connection!=null) {
            logger.warning("Database Connection left open at " + connectionOpenedAt);
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection=null;
        }
    }

}
