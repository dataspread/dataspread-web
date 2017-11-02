package org.zkoss.zss.model.impl;

import com.opencsv.CSVReader;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.jdbc.PgConnection;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class ROM_Model extends Model {
    private static final Logger logger = Logger.getLogger(ROM_Model.class.getName());
    private PosMapping rowMapping;
    private PosMapping colMapping;
    private List<Bucket<String>> navSbuckets;
    private ArrayList<String> recordList;
    private int kHisto = 5;

    //Create or load RCV_model.
    ROM_Model(DBContext context, SSheet sheet, String tableName) {
        this.sheet = sheet;
        rowMapping = new BTree(context, tableName + "_row_idx");
        colMapping = new BTree(context, tableName + "_col_idx");
        this.tableName = tableName;
        this.navSbuckets = new ArrayList<Bucket<String>>();
        this.recordList = new ArrayList<String>();
        createSchema(context);
    }

    private void createSchema(DBContext context) {
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("(row INT PRIMARY KEY)")
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dropSchema(DBContext context) {
        String dropTable = (new StringBuffer())
                .append("DROP TABLE ")
                .append(tableName)
                .toString();
        try (Statement stmt = context.getConnection().createStatement()) {
            stmt.execute(dropTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        rowMapping.dropSchema(context);
        colMapping.dropSchema(context);
    }


    @Override
    public void insertRows(DBContext context, int row, int count) {
        rowMapping.createIDs(context, row, count);
    }

    @Override
    public void insertCols(DBContext context, int col, int count) {
        StringBuffer insertColumn = (new StringBuffer())
                .append("ALTER TABLE ")
                .append(tableName);
        Integer ids[] = colMapping.createIDs(context, col, count);
        for (int i = 0; i < ids.length; i++) {
            insertColumn.append(" ADD COLUMN col_")
                    .append(ids[i])
                    .append(" BYTEA");
            if (i < ids.length - 1)
                insertColumn.append(",");
        }
        try (Statement stmt = context.getConnection().createStatement()) {
            stmt.execute(insertColumn.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteRows(DBContext context, int row, int count) {
        Integer[] ids = rowMapping.deleteIDs(context, row, count);

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM " + tableName + " WHERE row = ANY(?)")) {
            Array inArray = context.getConnection().createArrayOf("integer", ids);
            stmt.setArray(1, inArray);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCols(DBContext context, int col, int count) {
        StringBuffer deleteColumn = (new StringBuffer())
                .append("ALTER TABLE ")
                .append(tableName);
        Integer[] ids = colMapping.deleteIDs(context, col, count);
        for (int i = 0; i < ids.length - 1; i++)
            deleteColumn.append(" DROP COLUMN col_")
                    .append(ids[i])
                    .append(",");
        deleteColumn.append(" DROP COLUMN col_")
                .append(ids[ids.length - 1]);

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(deleteColumn.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateCells(DBContext context, Collection<AbstractCellAdv> cells) {
        if (cells.size() == 0)
            return;

        // Gather cells of same row together
        SortedMap<Integer, SortedMap<Integer, AbstractCellAdv>> groupedCells = new TreeMap<>();
        SortedSet<Integer> columnList = new TreeSet<>();
        for (AbstractCellAdv cell : cells) {
            SortedMap<Integer, AbstractCellAdv> _row = groupedCells.get(cell.getRowIndex());
            if (_row == null) {
                _row = new TreeMap<>();
                groupedCells.put(cell.getRowIndex(), _row);
            }
            _row.put(cell.getColumnIndex(), cell);
            columnList.add(cell.getColumnIndex());
        }

        if (columnList.last() >= colMapping.size(context))
            insertCols(context, colMapping.size(context), columnList.last() - colMapping.size(context) + 1);


        Integer[] idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        StringBuffer update = new StringBuffer("WITH upsert AS (UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int i = 0; i < idsCol.length; ++i) {
            update.append("col_")
                    .append(idsCol[i])
                    .append("=COALESCE(?,")
                    .append("col_")
                    .append(idsCol[i])
                    .append(")");
            if (i < idsCol.length - 1)
                update.append(",");
        }

        update.append(" WHERE row = ? RETURNING *) ")
                .append("INSERT INTO ")
                .append(tableName)
                .append("(row,");

        for (int i = 0; i < idsCol.length; ++i) {
            update.append("col_")
                    .append(idsCol[i]);
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(") SELECT ");
        for (int i = 0; i < idsCol.length; ++i)
            update.append("?,");
        update.append("? WHERE NOT EXISTS (SELECT * FROM upsert)");
        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update.toString())) {
            for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                int rowId = rowMapping.getIDs(context, _row.getKey(), 1)[0];
                stmt.setInt(idsCol.length + 1, rowId);
                stmt.setInt(idsCol.length + 2, rowId);
                for (int i = 0; i < idsCol.length; ++i) {
                    if (_row.getValue().get(columnList.first() + i) == null) {
                        stmt.setNull(i + 1, Types.BINARY);
                        stmt.setNull(i + idsCol.length + 3, Types.BINARY);
                    } else {
                        stmt.setBytes(i + 1,
                                _row.getValue().get(columnList.first() + i).toBytes());
                        stmt.setBytes(i + idsCol.length + 3,
                                _row.getValue().get(columnList.first() + i).toBytes());
                    }
                }
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCells(DBContext context, CellRegion range) {
        StringBuffer delete = new StringBuffer("UPDATE ")
                .append(tableName)
                .append(" SET ");
        Integer[] colIds = colMapping.getIDs(context, range.getColumn(), range.getLastColumn() - range.getColumn() + 1);
        for (int i = 0; i < colIds.length - 1; i++) {
            delete.append("col_")
                    .append(colIds[i])
                    .append("=null,");
        }
        delete.append("col_")
                .append(colIds[colIds.length - 1])
                .append("=null");
        delete.append(" WHERE row = ANY (?) ");

        Integer[] rowIds = rowMapping.getIDs(context, range.getRow(), range.getLastRow() - range.getRow() + 1);

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(delete.toString())) {
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds);
            stmt.setArray(1, inArrayRow);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCells(DBContext context, Collection<AbstractCellAdv> cells) {
        // Gather cells of same row together
        SortedMap<Integer, SortedMap<Integer, AbstractCellAdv>> groupedCells = new TreeMap<>();
        SortedSet<Integer> columnList = new TreeSet<>();
        for (AbstractCellAdv cell : cells) {
            SortedMap<Integer, AbstractCellAdv> _row = groupedCells.get(cell.getRowIndex());
            if (_row == null) {
                _row = new TreeMap<>();
                groupedCells.put(cell.getRowIndex(), _row);
            }
            _row.put(cell.getColumnIndex(), cell);
            columnList.add(cell.getColumnIndex());
        }

        if (columnList.last() >= colMapping.size(context))
            insertCols(context, colMapping.size(context), columnList.last() - colMapping.size(context) + 1);


        Integer[] idsCol = colMapping.getIDs(context, columnList.first(),
                columnList.last() - columnList.first() + 1);

        StringBuffer update = (new StringBuffer())
                .append("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (int i = 0; i < idsCol.length; ++i) {
            update.append("col_")
                    .append(idsCol[i])
                    .append("= CASE WHEN ? IS NULL THEN NULL ELSE ")
                    .append("col_")
                    .append(idsCol[i])
                    .append(" END");
            if (i < idsCol.length - 1)
                update.append(",");
        }
        update.append(" WHERE row = ?");

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(update.toString())) {
            for (Map.Entry<Integer, SortedMap<Integer, AbstractCellAdv>> _row : groupedCells.entrySet()) {
                int rowId = rowMapping.getIDs(context, _row.getKey(), 1)[0];
                stmt.setInt(idsCol.length + 1, rowId);
                for (int i = 0; i < idsCol.length; ++i) {
                    if (_row.getValue().get(i) == null)
                        stmt.setInt(i + 1, 1);
                    else
                        stmt.setNull(i + 1, Types.INTEGER);
                }
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        Integer[] rowIds = rowMapping.getIDs(context, fetchRegion.getRow(), fetchRegion.getLastRow() - fetchRegion.getRow() + 1);
        Integer[] colIds = colMapping.getIDs(context, fetchRegion.getColumn(), fetchRegion.getLastColumn() - fetchRegion.getColumn() + 1);
        HashMap<Integer, Integer> row_map = IntStream.range(0, rowIds.length)
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(rowIds[i], fetchRegion.getRow() + i), null);
        HashMap<Integer, Integer> col_map = IntStream.range(0, colIds.length)
                .collect(HashMap<Integer, Integer>::new, (map, i) -> map.put(colIds[i], fetchRegion.getColumn() + i), null);

        StringBuffer select = new StringBuffer("SELECT row");
        for (int i = 0; i < colIds.length; i++)
            select.append(",col_")
                    .append(colIds[i]);

        select.append(" FROM ")
                .append(tableName)
                .append(" WHERE row = ANY (?) ");

        AutoRollbackConnection connection = context.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(select.toString())) {
            Array inArrayRow = context.getConnection().createArrayOf("integer", rowIds);
            stmt.setArray(1, inArrayRow);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int row_id = rs.getInt(1);
                int row = row_map.get(row_id);

                for (int i = 0; i < colIds.length; i++) {
                    int col = col_map.get(colIds[i]);
                    byte[] data = rs.getBytes(i + 2);
                    if (data!=null) {
                        AbstractCellAdv cell = CellImpl.fromBytes(sheet, row, col, data);
                        cells.add(cell);
                    }
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cells;
    }

    @Override
    public boolean deleteTableRows(DBContext context, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }


    @Override
    public CellRegion getBounds(DBContext context) {
        int rows = rowMapping.size(context);
        int columns = colMapping.size(context);
        if (rows==0 || columns ==0)
            return null;
        else
            return new CellRegion(0, 0, rowMapping.size(context) - 1, colMapping.size(context) - 1);
    }

    @Override
    public void clearCache(DBContext context) {
        rowMapping.clearCache(context);
        colMapping.clearCache(context);
    }

    public void createNavSchema(String firstRow,String indexCol)
    {
        StringBuffer createTable = new StringBuffer("CREATE TABLE IF NOT EXISTS ");
        createTable.append(tableName+" ");

        createTable.append(firstRow);

        StringBuffer indexTable = new StringBuffer("CREATE INDEX col_index ON ");
        indexTable.append(tableName+" ("+indexCol+")");
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
        Statement createStmt = connection.createStatement();
        Statement indexStmt = connection.createStatement()) {
            createStmt.executeUpdate(createTable.toString());
            indexStmt.execute(indexStmt.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importSheet(Reader reader, char delimiter) throws IOException {
        final int COMMIT_SIZE_BYTES = 8 * 1000 * 1000;
        CSVReader csvReader = new CSVReader(reader, delimiter);
        String[] nextLine;
        int importedRows = 0;
        int importedColumns = 0;
        logger.info("Importing sheet");


        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            Connection rawConn = ((DelegatingConnection) connection.getInternalConnection()).getInnermostDelegate();
            CopyManager cm = ((PgConnection) rawConn).getCopyAPI();
            CopyIn cpIN = null;

            StringBuffer sb = new StringBuffer();
            while ((nextLine = csvReader.readNext()) != null)
            {
                ++importedRows;
                if (cpIN == null)
                {
                    // Use the first line to fix the number of columns
                    importedColumns = nextLine.length;
                    insertCols(dbContext, 0, importedColumns);
                    StringBuffer copyCommand = new StringBuffer("COPY ");
                    copyCommand.append(tableName);
                    copyCommand.append("(row");
                    for (int i = 1; i <= importedColumns; i++)
                        copyCommand.append(", col_")
                                .append(i);
                    copyCommand.append(") FROM STDIN WITH DELIMITER '|'");
                    cpIN = cm.copyIn(copyCommand.toString());

                }

                sb.append(importedRows);
                for (int col = 0; col < importedColumns; col++)
                    sb.append('|').append(nextLine[col]);
                sb.append('\n');

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
            insertRows(dbContext, 0, importedRows);
            logger.info("Import done: " + importedRows + " rows and "
                    + importedColumns + " columns imported");
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void importNavSheet(Reader reader, char delimiter) throws IOException {


        BufferedReader br = new BufferedReader(reader);
        String line;
        String headerString = "";
        String indexString = "";
        String valuesString = "";
        int selectedCol = 0;
        int sampleSize = getSampleSize();

        CSVReader csvReader = new CSVReader(reader, delimiter);
        String[] nextLine;
        int importedRows = 0;
        int importedColumns = 0;
        int insertedRows = 0;
        logger.info("Importing sheet");

        //create table schema and index
        try{

            while((line=br.readLine())!=null)
            {
                String [] columns = line.split(String.valueOf(delimiter));
                importedColumns = columns.length;
                StringBuffer str = new StringBuffer();
                StringBuffer header = new StringBuffer("(");
                StringBuffer values = new StringBuffer("?");

                str.append(columns[0]+" TEXT");
                header.append(columns[0]);
                for (int i = 1; i < columns.length; i++) {
                    str.append(", ")
                            .append(columns[i] + " TEXT");
                    header.append(", ")
                            .append(columns[i]);
                    values.append(",?");
                }
                str.append(")");

                headerString = "("+header.toString()+")";
                valuesString = values.toString();
                indexString = columns[selectedCol];
                createNavSchema(str.toString(),indexString);
                break;
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }




        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);

            StringBuffer sb = new StringBuffer();
            while ((nextLine = csvReader.readNext()) != null)
            {
                if(importedRows==0)
                {
                    ++importedRows;
                    continue;
                }

                sb.append("INSERT into "+tableName+" "+headerString+" values("+valuesString+")");

                PreparedStatement pst = null;

                pst = connection.prepareStatement(sb.toString());

                for(int i=0;i<importedColumns;i++)
                    pst.setString(i+1,nextLine[i]);

                pst.executeUpdate();

                ++importedRows;

                if ((importedRows-1)% sampleSize ==0 && importedRows!=1) {
                    connection.commit();

                    createNavS(headerString,indexString,insertedRows==0?true:false);

                    insertRows(dbContext, insertedRows, importedRows-1);
                    insertedRows += (importedRows-1);

                    logger.info((importedRows-1) + " rows imported ");
                }


            }

            insertRows(dbContext, 0, importedRows);
            logger.info("Import done: " + importedRows + " rows and "
                    + importedColumns + " columns imported");
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void createNavS(String headerString,String indexString,boolean isFirst) {
        //load sorted data from table
        recordList =  new ArrayList<String>();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT "+indexString+" FROM " + tableName+" ORDER by "+indexString);


            while (rs.next())
                recordList.add(rs.getString(1));

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //create nav data structure

        navSbuckets = getBucketsNoOverlap(1,recordList.size());


    }


    private int getSampleSize() {
        return 10;
    }

    @Override
    public boolean deleteTableColumns(DBContext dbContext, CellRegion cellRegion) {
        throw new UnsupportedOperationException();
    }

    public List<Bucket<String>> getBucketsNoOverlap(int startPos, int endPos)
    {
        List<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();

        if (recordList.size()>0) {

            int bucketSize = (endPos-startPos+1) / kHisto;
            int boundary_change = 0;
            int element_count = 0;
            int startIndex=startPos;
            for (int i = 0; i < kHisto && startIndex < endPos; i++) {
                Bucket bucket = new Bucket();
                bucket.minValue = recordList.get(startIndex);
                bucket.startPos = startIndex;
                if(startIndex+bucketSize-1 < endPos) {
                    bucket.maxValue = recordList.get(startIndex + bucketSize - 1);
                    bucket.endPos = startIndex + bucketSize - 1;
                }
                else
                {
                    bucket.maxValue = recordList.get(endPos-1);
                    bucket.endPos = endPos - 1;

                }

                /*
                * if the value next to maxValue is same as maxValue, we need to increase bucket boundary
                * Search where max value ends: binary search in maxValue index+bucketSize
                * Search where maxValue stated in current bucket
                * if count maxValue in current bucket > count maxValue in next bucket. Merge the two else update current
                * bucket boundary to be the index just before the maxValue in current bucket
                * */
                int bounday_inc = 0;//count maxValue in next bucket
                int bounday_dec = 0;//count maxValue in current bucket

                if(recordList.size()-1-startIndex+1 < bucketSize)
                    bucketSize = recordList.size()-1-startIndex; //forcefully set bucket 1 size smaller to pass through next if else

                // System.out.println("startIndex: "+startIndex+", bucketSize: "+bucketSize+", subList.size(): "+subList.size());


                if((startIndex + bucketSize) < recordList.size()) //if not end of list
                {
                    String boundary_value = recordList.get(startIndex + bucketSize);

                    if(boundary_value.equals(bucket.maxValue))
                    {
                        bounday_inc++;
                        //Search where max value ends in next bucket
                        for(int j=startIndex + bucketSize+1 ; j<endPos;j++)
                        {
                            boundary_value = recordList.get(j);
                            if(boundary_value.equals(bucket.maxValue)) {
                                bounday_inc++;
                            }
                            else
                            {
                                break;
                            }
                        }

                        //count maxValue in current bucket
                        for(int j=startIndex + bucketSize-1;j>startIndex -1;j--)
                        {
                            boundary_value = recordList.get(j);
                            if(boundary_value.equals(bucket.maxValue)) {
                                bounday_dec++;
                            }
                            else
                            {
                                break;
                            }
                        }

                        if(bounday_dec>bounday_inc)//keep everything in current bucket
                        {
                            bucket.maxValue = recordList.get(startIndex + bucketSize - 1+bounday_inc);
                            bucket.endPos = startIndex + bucketSize - 1+bounday_inc;
                            boundary_change = bounday_inc;
                        }
                        else
                        {
                            bucket.maxValue = recordList.get(startIndex + bucketSize - 1-bounday_dec);
                            bucket.endPos = startIndex + bucketSize - 1-bounday_dec;
                            boundary_change = -bounday_dec;
                        }
                    }

                }


                bucket.size = bucketSize+boundary_change;
                bucket.childCount = kHisto;
                if(bucket.size>0) {
                    startIndex += bucket.size;
                    bucketList.add(bucket);

                }


                if(bounday_dec < bounday_inc) //create new bucket as the current bucket is shrinked
                {
                    Bucket bucketSplit = new Bucket();
                    bucketSplit.minValue = recordList.get(startIndex);
                    bucketSplit.maxValue = recordList.get(startIndex+bounday_dec+bounday_inc-1);
                    bucketSplit.size = bounday_dec+bounday_inc;
                    bucketList.add(bucketSplit);

                    if(bucket.size >0)
                        i++;
                    startIndex += bucketSplit.size;

                }

            }

            if(startIndex<endPos)
            {
                Bucket bucket = new Bucket();
                bucket.minValue = recordList.get(startIndex);
                bucket.maxValue = recordList.get(endPos-1);
                bucket.size = endPos-1-startIndex+1;
                bucketList.add(bucket);
            }

        }

        // printBuckets(bucketList);
        return bucketList;
    }

    public void createJSON()
    {

    }

    static class Bucket<T> {
        T minValue;
        T maxValue;
        int startPos;
        int endPos;
        int childCount;
        int size;


        @Override
        public String toString() {
            if (minValue==null || maxValue==null)
                return null;

            if(minValue.toString().equals(maxValue.toString()))
                return minValue.toString();
            return minValue.toString() + " to " + maxValue.toString();
        }
    }

}