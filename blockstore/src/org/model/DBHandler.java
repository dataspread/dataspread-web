package org.model;

import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Created by Mangesh Bendre on 4/22/2016.
 */
public class DBHandler {
    private static final Logger logger = Logger.getLogger(DBHandler.class.getName());
    public static DBHandler instance;
    private DataSource ds;
    DBListener dbListener;

    public static final String dependency = "dependency";
    public static final String fullDependency = "full_dependency";
    public static final String compressDependency = "compress_dependency";
    public static final String stagedLog = "staged_log";
    public static final String userBooks = "user_books";

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

    public AutoRollbackConnection getConnection() {
        try {
            return new AutoRollbackConnection(ds.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void cacheDS() throws Exception {
        InitialContext cxt = new InitialContext();
        if (cxt == null) {
            throw new Exception("No context!");
        }

        ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/ibd");
        if (ds == null) {
            throw new Exception("Data source not found!");
        }
    }

    public static void initDBHandler() {
        instance = new DBHandler();
    }

    public void initApplication() {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            createBookTable(dbContext);
            createUserAccountTable(dbContext);
            createUserBooksTable(dbContext);
            createTableOrders(dbContext);
            createDependencyTable(dbContext);
            createFullDependencyTable(dbContext);
            createTypeConversionTable(dbContext);
            createCompressDependencyTable(dbContext);
            createStagedLog(dbContext);
            connection.commit();
            //dbListener = new DBListener();
            //dbListener.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdownApplication() {
        dbListener.stopListener();
    }


    private void createBookTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            String createBooksTable = "CREATE TABLE  IF NOT  EXISTS  books (" +
                    "bookname  TEXT NOT NULL," +
                    "booktable TEXT NOT NULL UNIQUE," +
                    "lastmodified timestamp," +
                    "createdtime timestamp," +
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

            String createDataTableSheetLink = "CREATE TABLE  IF NOT  EXISTS  sheet_table_link (" +
                    "linkid  TEXT NOT NULL," +
                    "bookid  TEXT NOT NULL," +
                    "sheetname  TEXT NOT NULL," +
                    "row1  INTEGER NOT NULL," +
                    "col1  INTEGER NOT NULL," +
                    "row2  INTEGER NOT NULL," +
                    "col2  INTEGER NOT NULL," +
                    "tablename  TEXT NOT NULL," +
                    "filter  TEXT NOT NULL," +
                    "sort TEXT NOT NULL," +
                    "PRIMARY KEY (linkid))";
            stmt.execute(createDataTableSheetLink);


            String createDataTable = "CREATE TABLE  IF NOT  EXISTS  tables (" +
                    "sharelink  TEXT NOT NULL," +
                    "tablename  TEXT NOT NULL," +
                    "userid  TEXT NOT NULL," +
                    "displayName  TEXT NOT NULL," +
                    "role TEXT NOT NULL)";
            stmt.execute(createDataTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void createTypeConversionTable(DBContext dbContext) {


        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS type_converted_books (" +
                    "bookid  TEXT NOT NULL," +
                    "sheetname  TEXT NOT NULL," +
                    "cols   TEXT NOT NULL" +
                    ");";
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUserBooksTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS " + userBooks + " (" +
                    "authtoken  TEXT NOT NULL," +
                    "booktable  TEXT NOT NULL," +
                    "role   TEXT NOT NULL" +
                    ");";
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUserAccountTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS user_account (" +
                    "authtoken  TEXT NOT NULL UNIQUE," +
                    "username   TEXT NOT NULL," +
                    "PRIMARY KEY (authtoken));";
            stmt.execute(createTable);
            String initializeUser = "INSERT INTO user_account VALUES ('guest', 'guest')" +
                    "ON CONFLICT (authtoken) DO NOTHING;";
            stmt.execute(initializeUser);
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

            String createTable = "CREATE TABLE  IF NOT  EXISTS " + dependency + " (" +
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

            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx1 ON "
                            + dependency + " using GIST (bookname, sheetname, range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx2 ON "
                            + dependency + " using GIST (dep_bookname, dep_sheetname, dep_range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx3 ON "
                            + dependency + " (must_expand)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createFullDependencyTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS  btree_gist");

            String createTable = "CREATE TABLE  IF NOT  EXISTS " + fullDependency + " (" +
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

            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx1 ON " +
                    fullDependency + " using GIST (bookname, sheetname, range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx2 ON " +
                    fullDependency + " using GIST (dep_bookname, dep_sheetname, dep_range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx3 ON " +
                    fullDependency + " (must_expand)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCompressDependencyTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS  btree_gist");

            String createTable = "CREATE TABLE  IF NOT  EXISTS " + compressDependency + " (" +
                    "bookname      TEXT    NOT NULL," +
                    "sheetname     TEXT    NOT NULL," +
                    "range         BOX     NOT NULL," +
                    "dep_bookname  TEXT    NOT NULL," +
                    "dep_sheetname TEXT    NOT NULL," +
                    "dep_range     BOX     NOT NULL," +
                    "must_expand   BOOLEAN NOT NULL," +
                    "pattern_type  INTEGER NOT NULL," +
                    "offsetRange   BOX     NOT NULL," +
                    "FOREIGN KEY (bookname, sheetname) REFERENCES sheets (bookname, sheetname)" +
                    " ON DELETE CASCADE ON UPDATE CASCADE," +
                    "FOREIGN KEY (dep_bookname, dep_sheetname) REFERENCES sheets (bookname, sheetname)" +
                    " ON DELETE CASCADE ON UPDATE CASCADE," +
                    " UNIQUE (oid) ) WITH oids";
            stmt.execute(createTable);

            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx1 ON " +
                    compressDependency + " using GIST (bookname, sheetname, range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx2 ON " +
                    compressDependency + " using GIST (dep_bookname, dep_sheetname, dep_range)");
            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx3 ON " +
                    compressDependency + " (must_expand)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createStagedLog(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS  btree_gist");

            String createTable = "CREATE TABLE  IF NOT  EXISTS " + stagedLog + " (" +
                    "logID         INTEGER NOT NULL," +
                    "bookname      TEXT    ," +
                    "sheetname     TEXT    ," +
                    "range         BOX     ," +
                    "dep_bookname  TEXT    NOT NULL," +
                    "dep_sheetname TEXT    NOT NULL," +
                    "dep_range     BOX     NOT NULL," +
                    "isInsert      BOOLEAN NOT NULL," +
                    "FOREIGN KEY (dep_bookname, dep_sheetname) REFERENCES sheets (bookname, sheetname)" +
                    " ON DELETE CASCADE ON UPDATE CASCADE," +
                    " UNIQUE (oid) ) WITH oids";
            stmt.execute(createTable);

            stmt.execute("CREATE INDEX IF NOT EXISTS dependency_idx1 ON "
                    + stagedLog + " using GIST (bookname, sheetname, range)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}