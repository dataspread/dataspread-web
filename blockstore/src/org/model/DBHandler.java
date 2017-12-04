package org.model;

import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Created by Mangesh Bendre on 4/22/2016.
 */
public class DBHandler implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DBHandler.class.getName());
    public static DBHandler instance;
    private DataSource ds;
    DBListener dbListener;
    GraphCompressor graphCompressor;

    public static void connectToDB(String url, String driver, String userName, String password) {
        DBHandler.instance = new DBHandler();
        PoolProperties p = new PoolProperties();
        p.setUrl(url);
        p.setDriverClassName(driver);
        p.setDefaultAutoCommit(false);
        p.setUsername(userName);
        p.setPassword(password);
        org.apache.tomcat.jdbc.pool.DataSource dataSource
                = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setPoolProperties(p);
        instance.ds = dataSource;
        instance.initApplication();
    }

    public AutoRollbackConnection getConnection()
    {
        try {
            return new AutoRollbackConnection(ds.getConnection());
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


    private void initApplication()
    {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            createBookTable(dbContext);
            createUserAccountTable(dbContext);
            createUserTable(dbContext);
            createTableOrders(dbContext);
            createDependencyTable(dbContext);
            connection.commit();
            //dbListener = new DBListener();
            //dbListener.start();
            //TODO - fix issues with the graph compressor
            //graphCompressor = new GraphCompressor();
            //graphCompressor.start();
        } catch (Exception e) {
            e.printStackTrace();
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
        initApplication();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        dbListener.stopListener();
    }

    private void createBookTable(DBContext dbContext)
    {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement())
        {
            String createBooksTable = "CREATE TABLE  IF NOT  EXISTS  books (" +
                    "bookname  TEXT NOT NULL," +
                    "booktable TEXT NOT NULL UNIQUE," +
                    "lastopened timestamp," +
                    "PRIMARY KEY (bookname))";
            stmt.execute(createBooksTable);


            String createSheetsTable = "CREATE TABLE IF NOT EXISTS sheets (" +
                    "  booktable     TEXT REFERENCES books(booktable) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "  sheetid       INTEGER," +
                    "  sheetindex    INTEGER," +
                    "  bookname      TEXT REFERENCES books(bookname) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "  sheetname     TEXT," +
                    "  modelname     TEXT," +
                    "  PRIMARY KEY (booktable, sheetid)," +
                    "  UNIQUE (bookname,sheetname))";
            stmt.execute(createSheetsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUserTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "username  TEXT NOT NULL," +
                    "booktable   TEXT NOT NULL" +
                    ");";
            stmt.execute(createTable);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void createUserAccountTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS user_account (" +
                    "username  TEXT NOT NULL," +
                    "password   TEXT NOT NULL" +
                    ");";
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableOrders(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            String createTable = "CREATE TABLE  IF NOT  EXISTS  tableorders (" +
                    "tablename  TEXT NOT NULL," +
                    "ordername TEXT NOT NULL," +
                    "rowIdxTable TEXT, " +
                    "colIdxTable TEXT, " +
                    "PRIMARY KEY (tablename, ordername)," +
                    "UNIQUE (oid)" +
                    ") WITH oids";
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDependencyTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS  btree_gist");

            String createTable = "CREATE TABLE  IF NOT  EXISTS  dependency (" +
                    "bookname      TEXT    NOT NULL," +
                    "sheetname     TEXT    NOT NULL," +
                    "range         BOX     NOT NULL," +
                    "dep_bookname  TEXT    NOT NULL," +
                    "dep_sheetname TEXT    NOT NULL," +
                    "dep_range     BOX     NOT NULL," +
                    "must_expand   BOOLEAN NOT NULL," +
                    "FOREIGN KEY (bookname, sheetname) REFERENCES sheets (bookname, sheetname)" +
                    " ON DELETE CASCADE ON UPDATE CASCADE," +
                    "FOREIGN KEY (dep_bookname, dep_sheetname) REFERENCES sheets (bookname, sheetname)" +
                    " ON DELETE CASCADE ON UPDATE CASCADE," +
                    " UNIQUE (oid) ) WITH oids";
            stmt.execute(createTable);

            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx1 " +
                    " ON dependency using GIST (bookname, sheetname, range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx2 " +
                    "ON dependency using GIST (dep_bookname, dep_sheetname, dep_range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx3 " +
                    "ON dependency (must_expand)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}