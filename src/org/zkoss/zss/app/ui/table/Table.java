package org.zkoss.zss.app.ui.table;

import org.model.DBHandler;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.app.ui.AppCtrl;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.ListModelList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Albatool on 4/16/2017.
 */
public class Table {


    Statement stmt = null;
    Connection connection = null;

    //-----------------------------------------------------------------------CreateTable

    public boolean createTable(Spreadsheet ss, String tableName) throws SQLException {
        String bookName = ss.getBook().getBookName();
        Sheet sheet = ss.getSelectedSheet();
        AreaRef selection = ss.getSelection();
        String rangeRef = Ranges.getAreaRefString(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());

        List<String> attributes = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();

        attributes = extractAttributes(sheet, selection, attributes);
        records = extractRecords(sheet, selection, records);

        String table = create(tableName, attributes);
        insertUserTable(table, bookName, rangeRef);
        insertRows(table, records);
        return true;
    }

    private List<String> extractAttributes(Sheet sheet, AreaRef selection, List<String> attributes) {

        Range src = Ranges.range(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());


        for (int i = 0; i < src.getColumnCount(); i++) {
            Range attribute = Ranges.range(sheet, selection.getRow(), selection.getColumn() + i);
            String attributeName = attribute.getCellEditText();
            if (attributeName.isEmpty()) {
                attributeName = "Column" + (i + 1);
                attribute.setCellEditText("Column" + (i + 1));
            }
            attributes.add(attributeName);
        }
        return attributes;

    }

    private List<List<String>> extractRecords(Sheet sheet, AreaRef selection, List<List<String>> records) {

        Range src = Ranges.range(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
        int rows = src.getRowCount();
        int cols = src.getColumnCount();

        int record = src.getRow();

        int counter = 1;
        while (counter < rows) {
            record++;
            List<String> values = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                Range cell = Ranges.range(sheet, record, selection.getColumn() + j);
//                String value = cell.getCellData().getValue().toString();
                String value= null;
                boolean nullValue = cell.getCellData().isBlank();
                if (!nullValue) {
                    value = cell.getCellValue().toString();
                }
                values.add(value);
            }
            records.add(values);
            counter++;
        }

        return records;

    }

    private String create(String table, List<String> attrs) throws SQLException {

        connection = DBHandler.instance.getConnection();
//        String tableName = "test";
//        String tableName = "table" + next();
        String tableName = table.trim();
        stmt = connection.createStatement();

        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (int i = 0; i < attrs.size(); i++) {
            builder.append(attrs.get(i) + " TEXT,");
        }
        builder.deleteCharAt(builder.length() - 1); // to delete the last comma
        builder.append(")");

        String sql = builder.toString();

        int x = stmt.executeUpdate(sql);
        connection.commit();
        connection.close();
        return tableName;
    }

    private void insertRows(String Table, List<List<String>> recs) throws SQLException {
        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        String tableName = Table;

        int attrsNo = recs.get(0).size();

        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO " + tableName + " VALUES (?");
        for (int i = 1; i < attrsNo; i++) {
            builder.append(",?");
        }
        builder.append(")");

        String sql = builder.toString();

        PreparedStatement pStmt = connection.prepareStatement(sql);
        final int batchSize = 1000;
        int count = 0;

        for (int i = 0; i < recs.size(); i++) {
            List<String> record = recs.get(i);
            for (int j = 0; j < attrsNo; j++) {
                String value = record.get(j);

                pStmt.setString(j + 1, value);

            }
            pStmt.addBatch();

            if (++count % batchSize == 0) {
                pStmt.executeBatch();
            }
        }
        pStmt.executeBatch();
        connection.commit();

        connection.close();
    }
    //-----------------------------------------------------------------------DeleteTable

    public void deleteTable(String tableName) throws SQLException {
        drop(tableName);
        deleteUserTable(tableName);
    }

    public void drop(String tableName) throws SQLException {

        Connection connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        String sql = "DROP TABLE IF EXISTS " + tableName;

        int x = stmt.executeUpdate(sql);
        connection.commit();
        connection.close();
    }
    //-----------------------------------------------------------------------

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

    //-----------------------------------------------------------------------rangeToTable Referencing
    public String checkUserTable(String book, String range) throws SQLException {

        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        String sql = "SELECT * FROM userTables WHERE bookName='"+book+"' AND rangeRef='" + range + "'";

        ResultSet result = stmt.executeQuery(sql);

        if (result.next()) {
            String name = result.getObject("tableName").toString();

            connection.close();
            return name;
        }
        connection.close();
        return null;
    }

    // When table is created, maintain related reference
    private void insertUserTable(String tableName, String book, String range) throws SQLException {
        connection = DBHandler.instance.getConnection();

        stmt = connection.createStatement();

        String sql = "INSERT INTO userTables VALUES('" + tableName + "','" + book + "','" + range + "')";

        int x = stmt.executeUpdate(sql);
        connection.commit();

        connection.close();
    }

    // When table is dropped, delete related reference
    public void deleteUserTable(String table) throws SQLException {

        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        String sql = "DELETE FROM userTables WHERE tableName='" + table + "'";

        int x = stmt.executeUpdate(sql);
        connection.commit();
        connection.close();
    }

    public ListModelList<String> userTables(String book) throws SQLException {
        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        String sql = "select tableName from userTables WHERE bookName='" + book + "'";

        ResultSet result = stmt.executeQuery(sql);

        ListModelList<String> tables = new ListModelList<>();

        while (result.next()) {
            tables.add(result.getString("tableName"));
        }

        return tables;

    }
    //-----------------------------------------------------------------------CheckedSelectedRangeIsOK

    //Check if selected range is one row only, including single cell case
    public boolean checkArea(Spreadsheet ss) {

        Sheet sheet = ss.getSelectedSheet();
        AreaRef selection = ss.getSelection();

        if (selection.getRow()==selection.getLastRow()) // single row "Header": empty table
        {
            return false;
        }
        return true;
    }

    //Check if selected range overlaps with existing table range
    public boolean checkOverlap(Spreadsheet ss) throws SQLException {
        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        Sheet sheet = ss.getSelectedSheet();
        AreaRef selection = ss.getSelection();

        String bookName = ss.getBook().getBookName();
        String rangeRef = Ranges.getAreaRefString(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());

        String[] temp=rangeRef.split("!");
        String sheetName=temp[0].trim();

        String sql = "SELECT rangeRef FROM userTables WHERE bookName='"+bookName+"' AND rangeRef LIKE '" + sheetName + "%'";

        ResultSet result = stmt.executeQuery(sql);

        boolean checkOverlap=false;
        if (result.next()) {
            String ref = result.getObject("rangeRef").toString();

            String[] temp2=rangeRef.split("!");
            String areaRef=temp[1].trim();

            Range range2 = Ranges.range(sheet, areaRef);

            checkOverlap=selection.overlap(range2.getRow(),range2.getColumn(),range2.getLastRow(),range2.getLastColumn());

            if(checkOverlap)
            {
                connection.close();
                return true;
            }

        }
        connection.close();
        return false;
    }
}


