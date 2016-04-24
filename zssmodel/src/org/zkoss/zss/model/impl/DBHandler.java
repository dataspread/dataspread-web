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

        try {
            loadBooks();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private void loadBooks() throws SQLException
    {
        try (
                Connection connection = DBHandler.instance.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from emp");
                ResultSet resultSet = statement.executeQuery();
        ) {
            while (resultSet.next()) {
                System.out.println(resultSet.getString(0) + " " + resultSet.getString(1) );
            }
        }

    }

    /*
    try (
    Connection connection = database.getConnection();
    PreparedStatement statement = connection.prepareStatement("SELECT id, name, value FROM Biler");
    ResultSet resultSet = statement.executeQuery();
    ) {
        while (resultSet.next()) {
            Biler biler = new Biler();
            biler.setId(resultSet.getLong("id"));
            biler.setName(resultSet.getString("name"));
            biler.setValue(resultSet.getInt("value"));
            bilers.add(biler);
        }
    } */



}