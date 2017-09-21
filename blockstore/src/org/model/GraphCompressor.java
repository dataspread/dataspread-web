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

        PreparedStatement compresslevel1 = connection.prepareStatement(
                "INSERT INTO dependency\n" +
                        "WITH RECURSIVE deps AS (  SELECT\n" +
                        "                            bookname,\n" +
                        "                            sheetname,\n" +
                        "                            range::text,\n" +
                        "                            dep_bookname,\n" +
                        "                            dep_sheetname,\n" +
                        "                            dep_range::text\n" +
                        "                          FROM dependency\n" +
                        "                          WHERE must_expand\n" +
                        "                          UNION\n" +
                        "                          SELECT\n" +
                        "                            d.bookname,\n" +
                        "                            d.sheetname,\n" +
                        "                            d.range::text,\n" +
                        "                            t.dep_bookname,\n" +
                        "                            t.dep_sheetname,\n" +
                        "                            t.dep_range::text\n" +
                        "                          FROM dependency d INNER JOIN deps t\n" +
                        "                              ON d.dep_bookname = t.bookname\n" +
                        "                                 AND d.dep_sheetname = t.sheetname\n" +
                        "                                 AND d.dep_range ??# t.range::box)\n" +
                        "SELECT bookname,\n" +
                        "  sheetname,\n" +
                        "  range::box,\n" +
                        "  dep_bookname,\n" +
                        "  dep_sheetname,\n" +
                        "  dep_range::box, FALSE FROM (\n" +
                        "SELECT * from deps\n" +
                        "EXCEPT\n" +
                        "SELECT bookname,\n" +
                        "  sheetname,\n" +
                        "  range::text,\n" +
                        "  dep_bookname,\n" +
                        "  dep_sheetname,\n" +
                        "  dep_range::text FROM  dependency) as deps2");

        PreparedStatement compresslevel2 = connection.prepareStatement(
                "UPDATE dependency SET must_expand = FALSE");
        while (keepRunning) {
            int recordsAdded = compresslevel1.executeUpdate();
            if (recordsAdded>0)
                logger.info(recordsAdded + " records added for level compress.");
            compresslevel2.execute();
            connection.commit();
            Thread.sleep(2000L);
        }
    }
}
