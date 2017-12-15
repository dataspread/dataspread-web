package org.model;

import java.sql.*;
import java.util.logging.Logger;

public class DBListener  extends Thread {
    private static final Logger logger = Logger.getLogger(DBListener.class.getName());
    private boolean keepRunning;

    public DBListener()
    {
        keepRunning=true;
    }

    @Override
    public void run() {
        createEventTable();
        try {
            listenEvents();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void stopListener() {
        keepRunning=false;
    }

    private void listenEvents() throws InterruptedException, SQLException {
        // Assume session id 0 is created
        logger.info("Listening for Events");
        AutoRollbackConnection connection = DBHandler.instance.getConnection();

        PreparedStatement listen_events = connection.prepareStatement(
                "DELETE FROM db_events "
                        + "RETURNING action, data");

        while (keepRunning) {
            boolean no_records = true;
            ResultSet rs = listen_events.executeQuery();
            while (rs.next()) {
                no_records = false;
                logger.info("Got message " + rs.getString(1) + " " + rs.getString(2));
                handleEvent(rs.getInt(1), rs.getString(2));
                connection.commit();
            }
            rs.close();
            connection.commit();
            if (no_records)
                Thread.sleep(200L);
        }
    }

    private void handleEvent(int event, String data) {
        switch (event)
        {
            case 1: // Table refresh
                break;
            default:

        }
    }

    private void createEventTable()
    {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             Statement stmt = connection.createStatement())
        {
            String createTable = "CREATE TABLE  IF NOT  EXISTS  db_events (" +
                    "action INTEGER NOT NULL," +
                    "data TEXT);";
            stmt.execute(createTable);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
