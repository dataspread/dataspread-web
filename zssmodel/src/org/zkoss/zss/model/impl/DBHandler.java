package org.zkoss.zss.model.impl;

import org.zkoss.util.logging.Log;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by Mangesh Bendre on 4/22/2016.
 */
public class DBHandler implements ServletContextListener {
    public static DBHandler instance;
    private DataSource ds;
    private static final Log _logger = Log.lookup(DBHandler.class);

    public Connection getConnection()
    {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cacheDS() throws Exception
    {
        InitialContext cxt = new InitialContext();
        if ( cxt == null ) {
            throw new Exception("No context!");
        }

        ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/ibd" );
        if ( ds == null ) {
            throw new Exception("Data source not found!");
        }
    }


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        instance = this;
        try {
            cacheDS();
        } catch (Exception e) {
            System.err.println("Unable to connect to a Database");
            e.printStackTrace();
        }
        createBookTable();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private void createBookTable()
    {
        try (Connection connection = DBHandler.instance.getConnection();
             Statement stmt = connection.createStatement())
        {
            String createTable = "CREATE TABLE  IF NOT  EXISTS  books (" +
                    "bookname  TEXT NOT NULL," +
                    "booktable TEXT NOT NULL," +
                    "PRIMARY KEY (bookname)" +
                    ");";
            stmt.execute(createTable);
            connection.commit();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}