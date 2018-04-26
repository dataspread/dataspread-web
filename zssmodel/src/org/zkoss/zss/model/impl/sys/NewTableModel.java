package org.zkoss.zss.model.impl.sys;

import org.model.AutoRollbackConnection;
import org.model.BlockStore;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSemantics;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.CellImpl;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class NewTableModel {
    BlockStore bs;
    private String newTableName;

    public boolean createTable(DBContext context, CellRegion range, String tableName, String bookName, SSheet sheet) throws Exception {

        newTableName = tableName.toLowerCase();
        /* First create table then create model */
        /* extract table header row */
        CellRegion tableHeaderRow = new CellRegion(range.row, range.column, range.row, range.lastColumn);
        List<String> columnList = getCells(context, range, sheet)
                .stream()
                .sorted(Comparator.comparingInt(SCell::getRowIndex))
                .map(AbstractCellAdv::getValue)
                .map(Object::toString)
                .map(e -> e.trim().replaceAll("[^a-zA-Z0-9.\\-;]+", "_"))
                .collect(Collectors.toList());

        if (columnList.size()<tableHeaderRow.getLength())
            throw new Exception("Missing columns names.");

        if (columnList.stream().filter(e->!Character.isLetter(e.charAt(0))).findFirst().isPresent())
            throw new Exception("Column names should start with a letter.");


        String createTable = (new StringBuilder())
                .append("CREATE TABLE ")
                .append(newTableName)
                .append(" (")
                .append(columnList.stream().map(e -> e + " TEXT").collect(Collectors.joining(",")))
                .append(") WITH OIDS")
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
        }

   /* add the record to the tables table */
        String appendRecord = (new StringBuilder())
                .append("INSERT INTO ")
                .append("tables")
                .append(" VALUES ")
                .append(" (" + bookName + "，"+ sheet.getSheetName() + "，" + range + "，" + tableName + "，" + "" + "，" + "" + ") ")
           .toString();


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendRecord);
        }

        //deleteCells(context, tableHeaderRow);
        return true;
    }

    public void sortTable(DBContext context, String tableName, String order) {
        String appendtoTables = (new StringBuilder())
                .append("UPDATE ")
                .append("tables")
                .append(" SET ")
                .append("order = " + order)
                .append(" WHERE ")
                .append("tableName == " + tableName)
           .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.prepareStatement(appendtoTables)) {

        }catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void filterTable(DBContext context, String tableName, String filter) {
        String appendToTables = (new StringBuilder())
                .append("UPDATE ")
                .append("tables")
                .append(" SET ")
                .append("filter = " + filter)
                .append(" WHERE ")
                .append("tableName = " + tableName)
           .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(appendToTables);
        }catch (SQLException e) {
            e.printStackTrace();
        }
  /*change the pos mapping*/

    }

    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange, SSheet sheet) {

        // Reduce Range to bounds
        Collection<AbstractCellAdv> cells = new ArrayList<>();
        String select = (new StringBuilder())
                .append("SELECT")
                .append(" FROM ")
                .append("tables")
                .append(" WHERE sheetName = " + sheet.getSheetName())
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(select);
            while(rs.next()){
                Array tableRange = rs.getArray("range");
                int [] rowcol = (int[])tableRange.getArray();
                CellRegion range = new CellRegion(rowcol[0], rowcol[1], rowcol[2], rowcol[3]);
                if(fetchRange.overlaps(range)){
                    String order = rs.getString("order");
                    String filter = rs.getString("filter");
                    CellRegion overlap = fetchRange.getOverlap(range);
                    int startRow = overlap.row - range.row;
                    int endRow = overlap.lastRow - range.lastRow;
                    int startCol = Math.max(overlap.column, range.column);
                    int endCol = Math.min(overlap.lastColumn, range.lastColumn);
                    String query = (new StringBuilder())
                            .append("SELECT")
                            .append(" FROM ")
                            .append(newTableName)
                            .append(" WHERE " + filter)
                            .append(" ORDER BY " + order)
                            .append(" OFFSET "+startRow+" ROWS")
                            .append(" FETCH NEXT "+endRow+" ROWS ONLY")
                            .toString();

                    try(PreparedStatement state = connection.prepareStatement(select.toString())){
                        ResultSet dataSet = state.executeQuery(query);
                        int row = startRow;
                        while(dataSet.next()){
                            int col = startCol;
                            for( int i = 0; i < (endCol - startCol); i++){
                                byte[] data = dataSet.getBytes(i);
                                AbstractCellAdv cell = CellImpl.fromBytes(sheet, row, col, data);
                                cell.setSemantics(SSemantics.Semantics.TABLE_CONTENT);
                                cells.add(cell);
                                col++;
                            }
                            row++;
                        }
                    }
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return cells;
    }


}
