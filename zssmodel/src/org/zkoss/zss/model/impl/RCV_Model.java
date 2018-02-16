package org.zkoss.zss.model.impl;

import com.opencsv.CSVReader;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.model.AutoRollbackConnection;
import org.model.BlockStore;
import org.model.DBContext;
import org.model.DBHandler;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.ModelEvents;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;
import org.zkoss.zss.model.impl.statistic.CombinedStatistic;
import org.zkoss.zss.model.impl.statistic.CountStatistic;
import org.zkoss.zss.model.impl.statistic.KeyStatistic;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;


public class RCV_Model extends Model {
    private static final Logger logger = Logger.getLogger(RCV_Model.class.getName());
    protected PosMapping rowMapping;
    protected PosMapping colMapping;
    private BlockStore bs;
    private MetaDataBlock metaDataBlock;

    //Create or load RCV_model.
    protected RCV_Model(DBContext context, SSheet sheet, String tableName) {
        this.sheet = sheet;
        rowMapping = new CountedBTree(context, tableName + "_row_idx");
        colMapping = new CountedBTree(context, tableName + "_col_idx");
        this.tableName = tableName;
        this.navSbuckets = new ArrayList<Bucket<String>>();
        this.navS = new NavigationStructure(tableName);
        createSchema(context);
        loadMetaData(context);
    }

    protected RCV_Model(DBContext context, SSheet sheet, String tableName, RCV_Model source) {
        this.sheet = sheet;
        rowMapping = source.rowMapping.clone(context, tableName + "_row_idx");
        colMapping = source.colMapping.clone(context, tableName + "_col_idx");
        this.tableName = tableName;
        copySchema(context, source.tableName);
        source.bs.clone(context, tableName + "_rcv_meta");
        loadMetaData(context);
    }

    @Override
    public Model clone(DBContext context, SSheet sheet, String tableName) {
        return new RCV_Model(context, sheet, tableName, this);
    }

    private void loadMetaData(DBContext context) {
        bs = new BlockStore(context, tableName + "_rcv_meta");
        metaDataBlock = bs.getObject(context, 0, MetaDataBlock.class);
        if (metaDataBlock == null) {
            metaDataBlock = new MetaDataBlock();
            bs.putObject(0, metaDataBlock);
            bs.flushDirtyBlocks(context);
        }
    }

    //Copy the table
    private void copySchema(DBContext dbContext, String sourceTable) {
        createSchema(dbContext);
        String copyTable = (new StringBuffer())
                .append("INSERT INTO ")
                .append(tableName)
                .append(" SELECT * FROM ")
                .append(sourceTable)
                .toString();
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(copyTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Create a table from the database
    private void createSchema(DBContext dbContext) {
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("(row INT, col INT, data BYTEA)")
                .toString();
        String createIndex = (new StringBuffer())
                .append("CREATE INDEX IF NOT EXISTS ")
                .append(tableName)
                .append("_row_col ON ")
                .append(tableName)
                .append("(row, col)")
                .toString();
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
            stmt.execute(createIndex);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Bucket<String>> createNavS(String bucketName, int start, int count) {
        //load sorted data from table
        ArrayList<String> recordList =  new ArrayList<String>();

        StringBuffer select = null;

        select = new StringBuffer("SELECT COUNT(*)");
        select.append(" FROM ")
                .append(tableName+"_2")
                .append(" WHERE row !=1");
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.navS.setTotalRows(count+1);
        if(this.indexString==null)
        {
            ArrayList<Bucket<String>> newList = this.navS.getUniformBuckets(0,count);
            return newList;
        }

        select = new StringBuffer("SELECT row, "+indexString);

        select.append(" FROM ")
                .append(tableName+"_2")
                .append(" WHERE row !=1 ORDER BY "+indexString);

        ArrayList<Integer> ids = new ArrayList<Integer>();

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            DBContext context = new DBContext(connection);
            ResultSet rs = stmt.executeQuery();
            ids.add(1);
            while (rs.next()) {
                int row = rs.getInt(1);
                ids.add(row);
                recordList.add(new String(rs.getBytes(2),"UTF-8"));
            }
            rs.close();
            stmt.close();

            Hybrid_Model hybrid_model = (Hybrid_Model) this;
            ROM_Model rom_model = (ROM_Model) hybrid_model.tableModels.get(0).y;

            rom_model.rowMapping.dropSchema(context);
            rom_model.rowMapping = new CountedBTree(context, tableName + "_row_idx");
            rom_model.rowMapping.insertIDs(context,start,ids);

            connection.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }




        //create nav data structure
        this.navS.setRecordList(recordList);
        ArrayList<Bucket<String>> newList = this.navS.getNonOverlappingBuckets(0,recordList.size()-1);//getBucketsNoOverlap(0,recordList.size()-1,true);

        //addByCount(context, pos + i, ids[i], false);
        return newList;

    }


    @Override
    public ArrayList<Bucket<String>> createNavS(SSheet currentSheet, int start, int count) {
        //load sorted data from table
        ArrayList<String> recordList =  new ArrayList<String>();


        StringBuffer select = null;

        if(this.indexString==null)
        {
            /*trueOrder = new HashMap<Integer,Integer>();

            for(int i=1;i<currentSheet.getEndRowIndex()+2;i++)
                trueOrder.put(i,i);*/

            ArrayList<Bucket<String>> newList = this.navS.getUniformBuckets(0,currentSheet.getEndRowIndex());
            return newList;
        }

        Hybrid_Model hybrid_model = (Hybrid_Model) this;
        ROM_Model rom_model = (ROM_Model) hybrid_model.tableModels.get(0).y;

        /*int columnIndex = Integer.parseInt(indexString.split("_")[1])-1;
        CellRegion tableRegion =  new CellRegion(1, columnIndex,//100000,20);
                currentSheet.getEndRowIndex(),columnIndex);

        ArrayList<SCell> result = (ArrayList<SCell>) currentSheet.getCells(tableRegion);

        Collections.sort(result, Comparator.comparing(SCell::getStringValue));*/

        int columnIndex = Integer.parseInt(indexString.split("_")[1])-1;

        CellRegion tableRegion =  new CellRegion(1, columnIndex,//100000,20);
                currentSheet.getEndRowIndex(),columnIndex);

        ArrayList<SCell> result = (ArrayList<SCell>) currentSheet.getCells(tableRegion);



        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext context = new DBContext(connection);

            ArrayList<Integer> rowIds=null;

            if(rom_model.rowOrderTable.keySet().isEmpty()) {
                rowIds = rom_model.rowMapping.getIDs(context, tableRegion.getRow(), tableRegion.getLastRow() - tableRegion.getRow() + 1);
            }
            else
            {
                CombinedStatistic startRow = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(tableRegion.getRow()-1));//-1 to acount for the header which is not inserted
                rowIds = rom_model.rowOrderTable.get(hybrid_model.tableModels.get(0).y.indexString).getIDs(context, startRow, tableRegion.getLastRow() - tableRegion.getRow() + 1,AbstractStatistic.Type.COUNT);
            }

            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<CombinedStatistic> statistics = new ArrayList<>();

            for(int i = 0; i < rowIds.size(); i++) {
                ids.add(rowIds.get(i));
                statistics.add(new CombinedStatistic(new KeyStatistic(result.get(i).getStringValue())));
                recordList.add(result.get(i).getStringValue());
            }



            /*for(int i=1;i<ids.size();i++)
                trueOrder.put(i+1,ids.get(i));


            rom_model.rowCombinedTree.dropSchema(context);
            rom_model.rowCombinedTree = new CombinedBTree(context, tableName + "_row_com_idx");
            rom_model.rowCombinedTree.insertIDs(context,statistics,ids);*/

            if(rom_model.rowOrderTable.containsKey(indexString))
                rom_model.rowCombinedTree = rom_model.rowOrderTable.get(indexString);
            else{
                CombinedBTree newOrder = new CombinedBTree(context, tableName + "_row_com_"+indexString+"_idx");
                newOrder.insertIDs(context,statistics,ids);

                rom_model.rowOrderTable.put(indexString,newOrder);

                rom_model.rowCombinedTree = newOrder;

                hybrid_model.tableModels.get(0).y.indexString = indexString;


            }
            connection.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }




        //create nav data structure
        this.navS.setRecordList(recordList);
        ArrayList<Bucket<String>> newList = this.navS.getNonOverlappingBuckets(0,recordList.size()-1);//getBucketsNoOverlap(0,recordList.size()-1,true);

        //addByCount(context, pos + i, ids[i], false);
        return newList;

    }

    public ArrayList<Bucket<String>> createNavSOnDemand(String bucketName, int start, int count) {
        //load sorted data from table
        ArrayList<String> recordList =  new ArrayList<String>();

        AutoRollbackConnection connection = DBHandler.instance.getConnection();
        DBContext context = new DBContext(connection);
        StringBuffer select = null;
        if(bucketName==null)
        {
            select = new StringBuffer("SELECT COUNT(*)");
            select.append(" FROM ")
                    .append(tableName+"_2")
                    .append(" WHERE row !=1");
            try (PreparedStatement stmt = connection.prepareStatement(select.toString())) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(this.indexString==null)
        {
            return this.navS.getUniformBuckets(0,count);
        }

        ArrayList<Integer> rowIds = rowMapping.getIDs(context,start,count);

        select = new StringBuffer("SELECT row, "+indexString);

        select.append(" FROM ")
                .append(tableName+"_2")
                .append(" WHERE row = ANY (?) AND row !=1");

        try (PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds.toArray());
            stmt.setArray(1, inArrayRow);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recordList.add(new String(rs.getBytes(2),"UTF-8"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //create nav data structure
        this.navS.setRecordList(recordList);
        ArrayList<Bucket<String>> newList = this.navS.getNonOverlappingBuckets(0,recordList.size()-1);//getBucketsNoOverlap(0,recordList.size()-1,true);

        if(bucketName==null)
        {
            return newList;
        }
        //addByCount(context, pos + i, ids[i], false);
        return this.navS.recomputeNavS(bucketName,this.navSbuckets,newList);
        //  printBuckets(navSbuckets);

    }

    @Override
    public ArrayList<String> getHeaders()
    {
        ArrayList<String> headers = new ArrayList<String>();


        StringBuffer select = null;
        select = new StringBuffer("SELECT *");
        select.append(" FROM ")
                .append(tableName+"_2")
                .append(" WHERE row =1");
        try (
                AutoRollbackConnection connection = DBHandler.instance.getConnection();

                PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            DBContext context = new DBContext(connection);
            ResultSet rs = stmt.executeQuery();
            int i=2;
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                for(;i<=columnCount;i++)
                    headers.add(new String(rs.getBytes(i),"UTF-8"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return headers;

    }

    @Override
    public void setIndexString(String str) {
        this.indexString = str;
    }


    @Override
    public void dropSchema(DBContext context) {
        String dropTable = (new StringBuffer())
                .append("DROP TABLE ")
                .append(tableName)
                .toString();
        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dropTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        bs.dropSchemaAndClear(context);
        rowMapping.dropSchema(context);
        colMapping.dropSchema(context);
    }


    @Override
    public void insertRows(DBContext context, int row, int count) {
        rowMapping.createIDs(context, row, count);
    }

    @Override
    public void insertCols(DBContext context, int col, int count) {
        colMapping.createIDs(context, col, count);
    }

    @Override
    public void deleteRows(DBContext dbContext, int row, int count) {
        ArrayList<Integer> ids = rowMapping.deleteIDs(dbContext, row, count);

        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM " + tableName + " WHERE row = ANY(?)")) {
            Array inArray = dbContext.getConnection().createArrayOf("integer", ids.toArray());
            stmt.setArray(1, inArray);
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCols(DBContext context, int col, int count) {
        ArrayList<Integer> ids = colMapping.deleteIDs(context, col, count);

        metaDataBlock.deletedColumns.addAll(ids);
        // simplified conversion
        /*
        for (int id : ids)
            metaDataBlock.deletedColumns.add(id);
        */

        bs.putObject(0, metaDataBlock);
        bs.flushDirtyBlocks(context);

        /* Do delete lazy
        try (PreparedStatement stmt = context.getConnection().prepareStatement(
                "DELETE FROM " + tableName + " WHERE col = ANY(?)")) {
            Array inArray = context.getConnection().createArrayOf("integer", ids);
            stmt.setArray(1, inArray);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } */
    }

    public void executeLazyDelete(DBContext dbContext) {
        Integer[] ids = (Integer[]) metaDataBlock.deletedColumns.toArray();

        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM " + tableName + " WHERE col = ANY(?)")) {
            Array inArray = connection.createArrayOf("integer", ids);
            stmt.setArray(1, inArray);
            stmt.execute();
            metaDataBlock.deletedColumns.clear();
            bs.putObject(0, metaDataBlock);
            bs.flushDirtyBlocks(dbContext);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void updateCells(DBContext context, Collection<AbstractCellAdv> cells) {

        StringBuffer update = new StringBuffer("WITH upsert AS ( UPDATE ")
                .append(tableName)
                .append(" SET data = ? WHERE row = ? AND col = ? RETURNING *) INSERT INTO ")
                .append(tableName)
                .append(" (row,col,data) SELECT ?,?,? WHERE NOT EXISTS (SELECT * FROM upsert)");

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update.toString())) {
            for (AbstractCellAdv cell : cells) {
                // Extend sheet
                ArrayList<Integer> idsRow = rowMapping.getIDs(context, cell.getRowIndex(), 1);
                int row = idsRow.get(0);
                ArrayList<Integer> idsCol = colMapping.getIDs(context, cell.getColumnIndex(), 1);
                int col = idsCol.get(0);
                stmt.setBytes(1, cell.toBytes());
                stmt.setInt(2, row);
                stmt.setInt(3, col);
                stmt.setInt(4, row);
                stmt.setInt(5, col);
                stmt.setBytes(6, cell.toBytes());
                stmt.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCells(DBContext dbContext, CellRegion range) {

        ArrayList<Integer> rowIds = rowMapping.getIDs(dbContext, range.getRow(), range.getLastRow() - range.getRow() + 1);
        ArrayList<Integer> colIds = colMapping.getIDs(dbContext, range.getColumn(), range.getLastColumn() - range.getColumn() + 1);

        String delete = new StringBuffer("DELETE FROM ")
                .append(tableName)
                .append(" WHERE row = ANY (?) AND col = ANY (?)").toString();


        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(delete)) {

            Array inArrayRow = dbContext.getConnection().createArrayOf("integer", rowIds.toArray());
            stmt.setArray(1, inArrayRow);

            Array inArrayCol = dbContext.getConnection().createArrayOf("integer", colIds.toArray());
            stmt.setArray(2, inArrayCol);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deleteCells(DBContext dbContext, Collection<AbstractCellAdv> cells) {

        String delete = new StringBuffer("DELETE FROM ")
                .append(tableName)
                .append(" WHERE row = ? AND col = ?").toString();

        AutoRollbackConnection connection = dbContext.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(delete)) {
            for (AbstractCellAdv cell : cells) {
                ArrayList<Integer> idsRow = rowMapping.getIDs(dbContext, cell.getRowIndex(), 1);
                int row = idsRow.get(0);
                ArrayList<Integer> idsCol = colMapping.getIDs(dbContext, cell.getColumnIndex(), 1);
                int col = idsCol.get(0);
                stmt.setObject(1, row);
                stmt.setObject(2, col);
                stmt.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteTableRows(DBContext context, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange) {
        // Reduce Range to bounds
        Collection<AbstractCellAdv> cells = new ArrayList<>();

        CellRegion bounds =  getBounds(context);
        if (bounds==null || fetchRange==null)
            return cells;

        CellRegion fetchRegion = bounds.getOverlap(fetchRange);
        if (fetchRegion == null)
            return cells;

        /*Hybrid_Model hybrid_model = (Hybrid_Model) this;
        ROM_Model rom_model = (ROM_Model) hybrid_model.tableModels.get(0).y;

        CombinedStatistic startRow = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(fetchRegion.getRow()));
        CombinedStatistic startCol = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(fetchRegion.getColumn()));

        ArrayList<Integer> rowIds = rom_model.rowCombinedTree.getIDs(context, startRow, fetchRegion.getLastRow() - fetchRegion.getRow() + 1,AbstractStatistic.Type.COUNT);
        ArrayList<Integer> colIds = rom_model.colCombinedTree.getIDs(context, startCol, fetchRegion.getLastColumn() - fetchRegion.getColumn() + 1,AbstractStatistic.Type.COUNT);

        */

        ArrayList<Integer> rowIds = rowMapping.getIDs(context, fetchRegion.getRow(), fetchRegion.getLastRow() - fetchRegion.getRow() + 1);
        ArrayList<Integer> colIds = colMapping.getIDs(context, fetchRegion.getColumn(), fetchRegion.getLastColumn() - fetchRegion.getColumn() + 1);
        HashMap<Integer, Integer> row_map = IntStream.range(0, rowIds.size())
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(rowIds.get(i), fetchRegion.getRow() + i),
                        (map1, map2) -> map1.putAll(map2));

        HashMap<Integer, Integer> col_map = IntStream.range(0, colIds.size())
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(colIds.get(i), fetchRegion.getColumn() + i),
                        (map1, map2) -> map1.putAll(map2));


        String select = new StringBuffer("SELECT row, col, data FROM ")
                .append(tableName)
                .append(" WHERE row = ANY (?) AND col = ANY (?)").toString();


        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(select)) {

            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds.toArray());
            stmt.setArray(1, inArrayRow);

            Array inArrayCol = context.getConnection().createArrayOf("integer", colIds.toArray());
            stmt.setArray(2, inArrayCol);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int row_id = rs.getInt(1);
                int col_id = rs.getInt(2);
                AbstractCellAdv cell = CellImpl.fromBytes(sheet, row_map.get(row_id),
                        col_map.get(col_id), rs.getBytes(3));
                cells.add(cell);
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cells;
    }

    @Override
    public CellRegion getBounds(DBContext context) {
        int rows = rowMapping.size(context);
        int columns = colMapping.size(context);
        if (rows==0 || columns ==0)
            return new CellRegion(0,0,0,0);
        else
            return new CellRegion(0, 0, rowMapping.size(context) - 1, colMapping.size(context) - 1);
    }

    @Override
    public void clearCache(DBContext context) {
        rowMapping.clearCache(context);
        colMapping.clearCache(context);
    }

    @Override
    public void importSheet(Reader reader, char delimiter, boolean useNav) throws IOException {
        final int COMMIT_SIZE_BYTES = 8 * 1000 * 1000;
        CSVReader csvReader = new CSVReader(reader, delimiter);
        String[] nextLine;
        int importedRows = 0;
        int importedColumns = 0;
        logger.info("Importing sheet");


        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            Connection rawConn = ((DelegatingConnection) connection.getInternalConnection()).getInnermostDelegate();
            CopyManager cm = ((PgConnection) rawConn).getCopyAPI();

            CopyIn cpIN = cm.copyIn("COPY " + tableName + " (row,col,data)" +
                    " FROM STDIN WITH DELIMITER '|'");

            StringBuffer sb = new StringBuffer();
            while ((nextLine = csvReader.readNext()) != null) {
                ++importedRows;
                if (importedColumns < nextLine.length)
                    importedColumns = nextLine.length;
                for (int col = 0; col < nextLine.length; col++) {
                    sb.append(importedRows).append('|');
                    sb.append(col+1).append('|');
                    sb.append(nextLine[col]).append('\n');
                }

                if (sb.length() >= COMMIT_SIZE_BYTES) {
                    cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
                    sb = new StringBuffer();
                    logger.info(importedRows + " rows imported ");
                }
            }
            if (sb.length() > 0)
                cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
            cpIN.endCopy();
            rawConn.commit();
            DBContext dbContext = new DBContext(connection);
            insertRows(dbContext, 0, importedRows);
            insertCols(dbContext, 0, importedColumns);
            logger.info("Import done: " + importedRows + " rows and "
                    + importedColumns + " columns imported");
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteTableColumns(DBContext dbContext, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }

    private static class MetaDataBlock {
        List<Integer> deletedColumns;

        MetaDataBlock() {
            deletedColumns = new ArrayList<>();
        }
    }
}
