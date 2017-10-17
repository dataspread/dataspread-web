package org.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class GraphCompressor extends Thread {
    private static final Logger logger = Logger.getLogger(GraphCompressor.class.getName());
    private boolean keepRunning;

    public GraphCompressor()
    {
        keepRunning=true;
    }

    @Override
    public void run() {
        try {
            compressDependency();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void stopListener() {
        keepRunning=false;
    }

    private void compressDependency() throws InterruptedException, SQLException {
        // Assume session id 0 is created
        logger.info("Starting Graph Compressor");
        AutoRollbackConnection connection = DBHandler.instance.getConnection();

        PreparedStatement toCompress = connection
                .prepareStatement("WITH RECURSIVE deps AS (" +
                        "  SELECT oid, dep_bookname, dep_sheetname, dep_range::text, must_expand " +
                        "  FROM dependency" +
                        "  WHERE must_expand" +
                        "  UNION" +
                        "  SELECT d.oid, d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand " +
                        "  FROM dependency d" +
                        "    INNER JOIN deps t" +
                        "    ON  d.bookname   =  t.dep_bookname" +
                        "    AND t.must_expand" +
                        "    AND d.sheetname =  t.dep_sheetname" +
                        "    AND d.range      && t.dep_range::box)" +
                        " SELECT oid FROM deps;");

        PreparedStatement compress1 = connection.prepareStatement(
                "INSERT INTO dependency" +
                        " WITH RECURSIVE deps AS (SELECT" +
                        "      bookname, sheetname, range::text, dep_bookname, dep_sheetname, dep_range::text" +
                        "    FROM dependency" +
                        "    WHERE oid = ?" +
                        "    UNION" +
                        "    SELECT" +
                        "      d.bookname, d.sheetname, d.range::text, t.dep_bookname, t.dep_sheetname, t.dep_range::text" +
                        "    FROM dependency d INNER JOIN deps t" +
                        "        ON d.dep_bookname = t.bookname" +
                        "           AND d.dep_sheetname = t.sheetname" +
                        "           AND d.dep_range && t.range::box)" +
                        " SELECT bookname, sheetname, range::box, dep_bookname, dep_sheetname, dep_range::box, FALSE" +
                        " FROM (SELECT * FROM deps d2" +
                        "   WHERE NOT EXISTS (" +
                        "     SELECT 1" +
                        "     FROM dependency d3" +
                        "     WHERE d3.bookname      =  d2.bookname" +
                        "     AND   d3.sheetname     =  d2.sheetname" +
                        "     AND   d3.range         && d2.range::box" +
                        "     AND   d3.dep_bookname  =  d2.dep_bookname" +
                        "     AND   d3.dep_sheetname =  d2.dep_sheetname" +
                        "     AND   d3.dep_range     && d2.dep_range::box)) as deps2");

        PreparedStatement compress2 = connection.prepareStatement(
                "UPDATE dependency SET must_expand = FALSE WHERE oid = ?");
        while (keepRunning) {
            ResultSet resultSet = toCompress.executeQuery();
            while (resultSet.next())
            {
                int oid = resultSet.getInt(1);
                logger.info("Compressing oid = " + oid + ".");
                compress1.setInt(1, oid);
                int insertedRecords = compress1.executeUpdate();
                logger.info("insertedRecords = " + insertedRecords + ".");
                compress2.setInt(1, oid);
                compress2.execute();
            }
            resultSet.close();
            connection.commit();
            Thread.sleep(2000L);
        }
    }
}
