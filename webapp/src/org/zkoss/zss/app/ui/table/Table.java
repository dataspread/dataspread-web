package org.zkoss.zss.app.ui.table;

import org.model.DBHandler;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.app.ui.AppCtrl;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.ListModelList;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
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
        insertRows(table, records, attributes);
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
                String value = null;
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
        builder.append("CREATE TABLE IF NOT EXISTS " + tableName + " (serialNo  SERIAL PRIMARY KEY,");
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

    private void insertRows(String Table, List<List<String>> recs, List<String> attributes) throws SQLException {
        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        String tableName = Table;

        int attrsNo = recs.get(0).size();

        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO " + tableName + "(" + attributes.get(0));

        for (int k = 1; k < attrsNo; k++) {
            builder.append("," + attributes.get(k));
        }
        builder.append(") VALUES (?");

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

        String sql = "SELECT * FROM userTables WHERE bookName='" + book + "' AND rangeRef='" + range + "'";

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
    public void insertUserTable(String tableName, String book, String range) throws SQLException {
        connection = DBHandler.instance.getConnection();

        stmt = connection.createStatement();

        String sql = "INSERT INTO userTables VALUES('" + tableName + "','" + book + "','" + range + "')";

        int x = stmt.executeUpdate(sql);
        connection.commit();

        connection.close();
    }
    public void updateUserTable(String book, String oldRange, String newRange)
    {
        try {
            connection = DBHandler.instance.getConnection();

            stmt = connection.createStatement();
            String sql = "UPDATE userTables SET rangeref='" + newRange + "' WHERE bookname='" + book + "' AND rangeref='" + oldRange + "'";

            int x = stmt.executeUpdate(sql);
            connection.commit();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        String sql = "select DISTINCT tableName from userTables WHERE bookName='" + book + "'";

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

        if (selection.getRow() == selection.getLastRow()) // single row "Header": empty table
        {
            return false;
        }
        return true;
    }

    //Check if selected range overlaps with existing table range
    public String checkOverlap(Spreadsheet ss) throws SQLException {
        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();

        Sheet sheet = ss.getSelectedSheet();
        AreaRef selection = ss.getSelection();

        String bookName = ss.getBook().getBookName();
        String rangeRef = Ranges.getAreaRefString(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());

        String[] temp = rangeRef.split("!");
        String sheetName = temp[0].trim();

        String sql = "SELECT rangeRef FROM userTables WHERE bookName='" + bookName + "' AND rangeRef LIKE '" + sheetName + "%'";

        ResultSet result = stmt.executeQuery(sql);

        boolean checkOverlap = false;
        while (result.next()) {
            String ref = result.getObject("rangeRef").toString();

            String[] temp2 = ref.split("!");
            String areaRef = temp2[1].trim();

            Range range2 = Ranges.range(sheet, areaRef);

            checkOverlap = selection.overlap(range2.getRow(), range2.getColumn(), range2.getLastRow(), range2.getLastColumn());

            if (checkOverlap) {
                connection.close();
                return ref;
            }

        }
        connection.close();
        return null;
    }

    //--------------------------------------------------------------------------------------------------
    public ArrayList<ArrayList<String>> getDisplayTable(String tableName, int columnsCount, int rowsCount) throws SQLException {


        ArrayList<String> columns = getTableColumns(tableName);

        ArrayList<ArrayList<String>> resultArray = new ArrayList<>();

        StringBuilder builder = new StringBuilder();

        if(columns.size()<columnsCount)
        {
            columnsCount=columns.size();
        }
        builder.append("SELECT " + columns.get(0));
        for (int i = 1; i < columnsCount; i++) {
            builder.append("," + columns.get(i));
        }

        rowsCount=rowsCount-1;

        String pk=getPK(tableName);
        if(pk!=null)
        {
            builder.append(" FROM " + tableName + " ORDER BY "+pk+" ASC LIMIT " + rowsCount);
        }else
        {
            builder.append(" FROM " + tableName + " LIMIT " + rowsCount);
        }

        String sql = builder.toString();


        connection = DBHandler.instance.getConnection();
        stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(sql);

        resultArray.add(new ArrayList<>());

        for(int k=0; k<columnsCount; k++)
        {
            resultArray.get(0).add(columns.get(k));
        }

        if (!result.wasNull()) {

            int counter=1;

            while(result.next())
            {
                resultArray.add(new ArrayList<>());
                for (int column = 0; column < columnsCount; column++) {
                    resultArray.get(counter).add(result.getString(column+1));
                }
                counter++;
            }
        }


        connection.close();

        return resultArray;
    }

    private ArrayList<String> getTableColumns(String tableName)  {
        ArrayList<String> columns = new ArrayList<>();
        try {
            connection = DBHandler.instance.getConnection();
            stmt = connection.createStatement();
            String sql = "SELECT column_name FROM information_schema.columns WHERE table_name ='" + tableName + "'";

            ResultSet result = stmt.executeQuery(sql);

            while (result.next()) {

                columns.add(result.getString(1));

            }

            if (columns.get(0).equalsIgnoreCase("serialNo")) {
                columns.remove(0);
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();

        }



        return columns;
    }

    public ArrayList<String> getTableRangeRefs(String tableName, String bookname)
    {
        ArrayList<String> rangeRefs=new ArrayList<>();
        try {
            connection = DBHandler.instance.getConnection();

            stmt = connection.createStatement();
            String sql = "select rangeref from usertables where tablename='"+tableName+"'" +
                    " and bookname='"+bookname+"'";

            ResultSet result = stmt.executeQuery(sql);

            while(result.next())
            {
                rangeRefs.add(result.getString(1));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rangeRefs;
    }

    private String getPK(String tableName)
    {
        String pk=null;
        try {
            connection = DBHandler.instance.getConnection();

            stmt = connection.createStatement();
            String sql = "select column_name from information_schema.key_column_usage " +
                    "where table_name='"+tableName+"'" +
                    " and constraint_name LIKE '%pkey'";

            ResultSet result = stmt.executeQuery(sql);

            if(result.next())
            {
                pk=result.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return pk;
    }

//    public void expand(String tableName, Spreadsheet ss,Sheet sheet, Range src, String type)
    public void expand(Spreadsheet ss, String tableRangeRef, String type) throws SQLException {
        String bookName=ss.getBook().getBookName();
        Sheet sheet = ss.getSelectedSheet();

        String checkedTable = checkUserTable(bookName,tableRangeRef);
        Range src = Ranges.range(sheet,tableRangeRef);

        switch (type)
        {
            case "cols":{
                String newRangeRef=expandColumns(checkedTable,sheet, src);
                if(newRangeRef!=null)
                {
                    updateUserTable(bookName,tableRangeRef,newRangeRef);
                }
                return;}

            case "rows":{
                String newRangeRef=expandRows(checkedTable,sheet,src);
                if(newRangeRef!=null)
                {
                    updateUserTable(bookName,tableRangeRef,newRangeRef);
                }                return;}

            case "all": {
                String newRangeRef=expandColumns(checkedTable,sheet,src);
                if(newRangeRef!=null)
                {
                    updateUserTable(bookName,tableRangeRef,newRangeRef);
                    Range newSrc = Ranges.range(sheet,newRangeRef);
                    String newestRangeRef=expandRows(checkedTable,sheet,newSrc);
                    updateUserTable(bookName,newRangeRef,newestRangeRef);
                }


                return;}

        }

    }
    private String expandRows(String tableName, Sheet sheet, Range src)
    {

        int columnCount=src.getColumnCount();
        int rowsCount=src.getRowCount()-1; // offset

        ArrayList<String> columns=getTableColumns(tableName);
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT " + columns.get(0));
        for (int i = 1; i < columnCount; i++) {
            builder.append("," + columns.get(i));
        }

        String pk=getPK(tableName);
        if(pk!=null)
        {
            builder.append(" FROM " + tableName + " ORDER BY "+pk+" ASC OFFSET " + rowsCount);
        }else
        {
            return null; // can't be done properly
        }

        String sql = builder.toString();

        try {

            connection = DBHandler.instance.getConnection();
            stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery(sql);

//            Range newRange = Ranges.range(sheet, src.getLastRow()+1, src.getColumn(), src.getLastRow()+src.getRowCount(),src.getLastColumn());

            int rowCounter=src.getLastRow();
            int startingCol=src.getColumn();
            while(result.next())
            {
                rowCounter++;
                for (int column = 0; column < columnCount; column++) {

                    Range range = Ranges.range(sheet, rowCounter, column + startingCol);
                    range.setAutoRefresh(false);
                    range.getCellData().setEditText(result.getString(column + 1));
                }
            }
            Range newRange = Ranges.range(sheet, src.getLastRow()+1, src.getColumn(), rowCounter,src.getLastColumn());
            newRange.notifyChange();
            connection.close();

            String ref=Ranges.getAreaRefString(sheet,src.getRow(),src.getColumn(),newRange.getLastRow(),src.getLastColumn());
            CellOperationUtil.applyBorder(newRange, Range.ApplyBorderType.FULL, CellStyle.BorderType.THICK, "#000000");
            CellOperationUtil.applyBackColor(newRange, "#c5f0e7");
            return ref;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;



    }
    private String expandColumns(String tableName,Sheet sheet, Range src)
    {

        ArrayList<String> columns=getTableColumns(tableName);
        int columnCount=src.getColumnCount();
        int rowsCount=src.getRowCount()-1;

        if(columns.size()>columnCount) {
            StringBuilder builder = new StringBuilder();

            builder.append("SELECT " + columns.get(columnCount));
            for (int i = columnCount + 1; i < columns.size(); i++) {
                builder.append("," + columns.get(i));
            }

            String pk = getPK(tableName);
            if (pk != null) {
                builder.append(" FROM " + tableName + " ORDER BY " + pk + " ASC LIMIT " + rowsCount);
            } else {
                builder.append(" FROM " + tableName + " LIMIT " + rowsCount); // problem
            }

            String sql = builder.toString();


            try {

                connection = DBHandler.instance.getConnection();
                stmt = connection.createStatement();
                ResultSet result = stmt.executeQuery(sql);

                int colOffset = src.getLastColumn();
                int newColumnCount = columns.size() - columnCount;

                Range newRange = Ranges.range(sheet, src.getRow(), colOffset + 1, src.getLastRow(), colOffset + newColumnCount);


                for (int k = 0, l = columnCount; k < newColumnCount; k++, l++) {
                    Range range = Ranges.range(sheet, newRange.getRow(), k + newRange.getColumn());
                    range.setAutoRefresh(false);
                    range.getCellData().setEditText(columns.get(l));
                }

                for (int row = 1; row < rowsCount + 1; row++) {

                    if(result.next())
                    {
                        for (int column = 0; column < newColumnCount; column++) {

                            Range range = Ranges.range(sheet, row + newRange.getRow(), column + newRange.getColumn());
                            range.setAutoRefresh(false);
                            range.getCellData().setEditText(result.getString(column + 1));
                        }
                    }else
                    {
                        break;
                    }

                }
                newRange.notifyChange();
                connection.close();

                String ref=Ranges.getAreaRefString(sheet,src.getRow(),src.getColumn(),src.getLastRow(),newRange.getLastColumn());
                CellOperationUtil.applyBorder(newRange, Range.ApplyBorderType.FULL, CellStyle.BorderType.THICK, "#000000");
                CellOperationUtil.applyBackColor(newRange, "#c5f0e7");
                return ref;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;

    }




}


