package org.zkoss.zss.app.ui.table;

import org.model.DBHandler;

import java.sql.*;
import java.util.List;

/**
 * Created by Albatool on 4/16/2017.
 */
public class Table {


    private String url = "jdbc:postgresql://localhost:5432/test";
    private String user = "postgres";
    private String password = "illinois";


    Statement stmt = null;
    Connection connection = null;

    //-----------------------------------------------------------------------Table-as-Unit
    protected void create(String tableName, List<String> attrs) throws SQLException {

        connection = connect();
        stmt = connection.createStatement();
//        connection = DBHandler.instance.getConnection();
//        String tableName = "test";

        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (int i = 0; i < attrs.size(); i++) {
            builder.append(attrs.get(i) + " TEXT,");
        }
        builder.deleteCharAt(builder.length() - 1); // to delete the last comma
        builder.append(")");

        String sql = builder.toString();

        stmt.executeUpdate(sql);

        connection.close();

    }

    public void drop(String tableName, String range) throws SQLException {

        //        Connection connection = DBHandler.instance.getConnection();

        connection = connect();
        stmt = connection.createStatement();

        String sql = "DROP TABLE IF EXISTS " + tableName;

        int x = stmt.executeUpdate(sql);

        connection.close();

//        rangeToTable(range);

    }

    private void rename() throws SQLException {

        /*
        TODO

        input: name of range of table to be renamed, new Name

        1) rename table in DB
        2) rename range

         */

    }

    //-----------------------------------------------------------------------Columns
    private void addCol() throws SQLException {

    }

    private void dropCol() throws SQLException {
        /*
        TODO:

        input: Column name

        1) drop column from DB
        2) clear content of column in the spreadsheet
        3) apply range border changes
        4) changes to the named range??


         */
    }

    private void setDataType() throws SQLException {

    }

    private void setDataFormat() throws SQLException {

    }

    private void setConstraint() throws SQLException {

    }
    //-----------------------------------------------------------------------Rows: Table Content

    protected void insertRows(String tableName, List<List<String>> recs) throws SQLException {
//        connection = DBHandler.instance.getConnection();

        connection = connect();
        stmt = connection.createStatement();

//        String tableName = "test";

        int attrsNo = recs.get(0).size();

        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO " + tableName + " VALUES (?");
        for (int i = 1; i < attrsNo; i++) {
            builder.append(",?");
        }
        builder.append(")");

        String sql = builder.toString();

        PreparedStatement pStmt = connection.prepareStatement(sql);

        for (int i = 0; i < recs.size(); i++) {
            List<String> record = recs.get(i);
            for (int j = 0; j < attrsNo; j++) {
                String value = record.get(j);

                pStmt.setString(j + 1, value);

            }
            pStmt.addBatch();

            if (i % 100 == 0 || i == attrsNo - 1) {
                pStmt.executeBatch();
            }
        }
        connection.close();
    }

    private void deleteRows() // Delete in Batches
    {

    }

    //-----------------------------------------------------------------------rangeToTable Referencing
    public String checkRangeToTable(String range) throws SQLException {

        connection = connect();
        stmt = connection.createStatement();

        String sql = "SELECT * FROM rangeToTable WHERE range='" + range + "'";

        ResultSet result = stmt.executeQuery(sql);

        if (result.next()) {
            String name = result.getObject("tableName").toString();
            connection.close();
            return name;
        }
        connection.close();
        return null;
    }

    public void insertRangeToTable(String tableName, String range) throws SQLException {

        connection = connect();
        stmt = connection.createStatement();

        String sql = "INSERT INTO rangeToTable VALUES('" + tableName + "','" + range + "')";

        int x = stmt.executeUpdate(sql);

        connection.close();
    }

    public void deleteRangeToTable(String range) throws SQLException {

        connection = connect();
        stmt = connection.createStatement();

        String sql = "DELETE FROM rangeToTable WHERE range='" + range + "'";

        int x = stmt.executeUpdate(sql);

        connection.close();
    }

    //-----------------------------------------------------------------------

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

}


