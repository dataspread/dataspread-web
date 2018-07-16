package org.zkoss.zss.model.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Created by Sajjadur on 11/14/2017.
 */
public class NavigationStructure {
    /**
     * Navigation bucket tree structure. Lazily constructed upon user clicking. The Model object has a reference to this Bucket Tree structure {@link Model#navSbuckets}. TODO: This link would break by reassigning in method init*. HOW TO FIX???
     */
    private ArrayList<Bucket<String>> navBucketTree;
    /**
     * Temporarily store the computed bucket for user call. To be serialized by calling {@link #getSerializedBuckets}.
     */
    private ReturnBuffer returnBuffer;

    class ReturnBuffer {
        public ArrayList<Bucket<String>> buckets;
        /**
         * Indicating whether the returned subgroups are clickable/expandable or not.
         */
        public boolean clickable;
    }

    /**
     * Total number of data rows. Does not include the header line.
     */
    private int totalRows;
    private String indexString;
    private Set<Integer> typeCheckedColumns;

    public void setCurrentSheet(SSheet currentSheet) {
        this.currentSheet = currentSheet;
    }

    private SSheet currentSheet;

    /**
     * Must be set externally for the bucket generator to work.
     */
    private ArrayList<Object> recordList;
    private int kHist;
    private String tableName;
    private Kryo kryo;
    private boolean useKryo;

    public NavigationStructure(String tableName) {
        this.tableName = tableName;
        this.kHist = 9;    // max # of histograms is kHist + 1
        kryo = new Kryo();
        this.useKryo = true;
        returnBuffer = new ReturnBuffer();
        typeCheckedColumns = new HashSet<>();
    }

    /**
     * Convert a given column from string to semantic type. If done then do nothing.
     *
     * @param col
     * @return true: performance conversion; false: nothing was done.
     */
    public boolean typeConvertColumnIfHavent(AutoRollbackConnection connection, int col) {
        if (typeCheckedColumns.contains(col))
            return false;
        typeCheckedColumns.add(col);
        System.out.println("Type converting column " + col);
        CellRegion tableRegion = new CellRegion(0, col, totalRows, col);
        ArrayList<SCell> result = (ArrayList<SCell>) currentSheet.getCells(tableRegion);
        result.forEach(x -> x.updateCellTypeFromString(connection, false));
        Collection<AbstractCellAdv> castCells = new ArrayList<>();
        result.forEach(x -> castCells.add((AbstractCellAdv) x));
        currentSheet.getDataModel().updateCells(new DBContext(connection), castCells);
        connection.commit();
        return true;
    }

    /**
     * Serialize the newly created buckets to JSON format.
     *
     * @return
     */
    public String getSerializedBuckets() {
        class ScrollingProtocol {
            public boolean clickable;
            public ArrayList<BucketGroup> data;

            class BucketGroup {
                public String name;
                public int[] rowRange;
                public int value;
                public int rate;

                /**
                 * Needed for serialization
                 */
                public BucketGroup() {
                }

                public BucketGroup(Bucket<String> item) {
                    name = item.name;
                    rowRange = new int[]{item.startPos, item.endPos};
                    value = item.size;
                    rate = 10;
                }
            }

            /**
             * Needed for serialization
             */
            public ScrollingProtocol() {
            }

            public ScrollingProtocol(ReturnBuffer ret) {
                data = new ArrayList<>();
                clickable = ret.clickable;
                for (int i = 0; i < ret.buckets.size(); i++) {
                    data.add(new BucketGroup(ret.buckets.get(i)));
                }
            }
        }

        ScrollingProtocol obj = new ScrollingProtocol(returnBuffer);
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Called upon importing a new table. Compute the top level groups based on row number.
     *
     * @param totalRows
     */
    public void initRowNumberBucket(int totalRows) {
        this.totalRows = totalRows;
        navBucketTree = getUniformBuckets(1, totalRows);
    }

    /**
     * Called when user select an index to start navigation. This function must be called after {@link #sortSetRecordList}.
     */
    public void initIndexedBucket(int totalRows) {
        this.totalRows = totalRows;
        navBucketTree = getNonOverlappingBuckets(1, this.totalRows - 1);
    }

    /**
     * Compute or fetch computed bucket list based on the input paths and store the result in {@link #returnBuffer}. Expect at least one level in the paths. This function assumes the target is expandable, i.e. it's user's job not to call this function on an end-node.
     */
    public void computeOnDemandBucket(int[] paths) {
        if (paths.length == 0) {
            navBucketTree = getNonOverlappingBuckets(1, this.totalRows - 1);
            return;
        }
        Bucket<String> subRoot = getSubRootBucket(paths);
        if (indexString == null)
            subRoot.setChildren(getUniformBuckets(subRoot.startPos, subRoot.endPos));
        else
            subRoot.setChildren(getNonOverlappingBuckets(subRoot.startPos, subRoot.endPos));
    }

    /**
     * Get a ArrayList of start/end row ID for each group of the path.
     *
     * @param paths navigation paths from root to selected sub-root.
     * @return The length of the list will be two times the number of buckets.
     */
    public List<Integer> getGroupStartEndIndex(int[] paths) {
        ArrayList<Bucket<String>> subRoot = navBucketTree;
        for (int i = 0; i < paths.length; i++) {
            subRoot = subRoot.get(paths[i]).children;
        }
        List<Integer> ret = new ArrayList<>();
        for (Bucket<String> group : subRoot) {
            ret.add(group.startPos);
            ret.add(group.endPos);
        }
        return ret;
    }

    /**
     * Find the bucket given the paths. Currently doesn't support empty paths. i.e. will return null. TODO: construct a root-level navBucketTree.
     *
     * @param paths
     * @return
     */
    public Bucket<String> getSubRootBucket(int[] paths) {
        if (paths.length == 0) return null;
        Bucket<String> subRoot = this.navBucketTree.get(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            subRoot = subRoot.getChildren().get(paths[i]);
        }
        return subRoot;
    }

    public void setNavBucketTree(ArrayList<Bucket<String>> navBucketTree) {
        this.navBucketTree = navBucketTree;
    }

    public void setIndexString(String str) {
        this.indexString = str;
    }

    /**
     * Set the RecordList of navigation structure. Assume input list is a list of Comparable and does not contain the header row. The function will add a dummy header row to align the index with the back-end row positional mapping.
     * @param ls Input Arraylist<Comparable> excluding the header info.
     */
    public void setRecordList(ArrayList<Object> ls) {
        this.recordList = ls;
        recordList.add(0,null);
    }

    public void setTotalRows(int rows) {
        this.totalRows = rows;
    }

    public int getTotalRows() {
        return this.totalRows;
    }

    public ArrayList<Bucket<String>> getNonOverlappingBuckets(int startPos, int endPos) {

        ArrayList<Bucket<String>> bucketList = new ArrayList<>();
        int bucketSize = (endPos - startPos + 1) / kHist;

        boolean subgroupClickable = false;
        if (bucketSize == 0) {
            bucketSize = 1;
        }
        int startIndex = startPos;

        for (int i = 0; i < kHist && startIndex < endPos + 1; i++) {
            //System.out.println("---------------BUCKET NO: "+i);
            Bucket bucket = new Bucket();
            bucket.minValue = recordList.get(startIndex);
            bucket.startPos = startIndex;
            if (startIndex + bucketSize - 1 < endPos + 1) {
                bucket.maxValue = recordList.get(startIndex + bucketSize - 1);
                bucket.endPos = startIndex + bucketSize - 1;
            } else {
                bucket.maxValue = recordList.get(endPos);
                bucket.endPos = endPos;

            }
            int[] start_end;
            start_end = getStartEnd(bucket.maxValue, bucket.startPos, bucket.endPos);
            int boundary_inc = 0;//count maxValue in next bucket
            int boundary_dec = 0;//count maxValue in current bucket
            boundary_dec = bucket.endPos - start_end[0] + 1;
            boundary_inc = start_end[1] - bucket.endPos + 1;
            if (bucket.maxValue.equals(bucket.minValue)) {
                bucket.maxValue = recordList.get(start_end[1]);
                bucket.endPos = start_end[1];
                bucket.size = bucket.endPos - bucket.startPos + 1;
                startIndex += bucket.size;
                bucket.setName(false);
                bucket.setId();
                bucketList.add(bucket);
            } else {
                if (boundary_dec >= boundary_inc)//keep everything in current bucket
                {
                    bucket.maxValue = recordList.get(start_end[1]);
                    bucket.endPos = start_end[1];
                } else {
                    bucket.maxValue = recordList.get(start_end[0] - 1);
                    bucket.endPos = start_end[0] - 1;
                }

                bucket.size = bucket.endPos - bucket.startPos + 1;
                startIndex += bucket.size;
                bucket.setName(false);
                bucket.setId();
                bucketList.add(bucket);
            }
            if (!subgroupClickable && !(bucket.maxValue.equals(bucket.minValue)))
                subgroupClickable = true;
        }

        if (startIndex < endPos + 1) {
            Bucket bucket = new Bucket();
            bucket.minValue = recordList.get(startIndex);
            bucket.maxValue = recordList.get(endPos);
            bucket.startPos = startIndex;
            bucket.endPos = endPos;
            bucket.size = endPos - startIndex + 1;
            bucket.setName(false);
            bucket.setId();
            bucketList.add(bucket);
            if (!subgroupClickable && !(bucket.maxValue.equals(bucket.minValue)))
                subgroupClickable = true;
        }
        returnBuffer.clickable = subgroupClickable;
        returnBuffer.buckets = bucketList;
        return bucketList;
    }

    /**
     * Return the position-based buckets.
     *
     * @param startPos location of starting row. First data row corresponds to startPos=1
     * @param endPos   Same as above. Last row is totalRows.
     * @return Bucket list.
     */
    public ArrayList<Bucket<String>> getUniformBuckets(int startPos, int endPos) {
        ArrayList<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();
        int bucketSize = (endPos - startPos + 1) / kHist;

        //System.out.println("(start,end): ("+startPos+","+endPos+"), BUCKET Size: "+bucketSize);
        boolean subgroupClickable = false;

        if (bucketSize > 0) {
            int startIndex = startPos;
            for (int i = 0; i < kHist && startIndex < endPos + 1; i++) {
                //System.out.println("---------------BUCKET NO: " + i);
                Bucket bucket = new Bucket();
                //bucket.minValue = recordList.get(startIndex);
                bucket.startPos = startIndex;
                if (startIndex + bucketSize - 1 < endPos + 1) {
                    //bucket.maxValue = recordList.get(startIndex + bucketSize - 1);
                    bucket.endPos = startIndex + bucketSize - 1;
                } else {
                    //bucket.maxValue = recordList.get(endPos);
                    bucket.endPos = endPos;

                }

                bucket.size = bucket.endPos - bucket.startPos + 1;
                startIndex += bucket.size;
                bucket.setName(true);
                bucket.setId();
                bucketList.add(bucket);
                if (!subgroupClickable && (bucket.endPos - bucket.startPos + 1) / kHist > 0) subgroupClickable = true;
            }

            if (startIndex < endPos + 1) {
                Bucket bucket = new Bucket();
                //bucket.minValue = recordList.get(startIndex);
                //bucket.maxValue = recordList.get(endPos);
                bucket.startPos = startIndex;
                bucket.endPos = endPos;
                bucket.size = endPos - startIndex + 1;
                bucket.setName(true);
                bucket.setId();

                bucketList.add(bucket);
                if (!subgroupClickable && (bucket.endPos - bucket.startPos + 1) / kHist > 0) subgroupClickable = true;
            }
        }
        returnBuffer.clickable = subgroupClickable;
        returnBuffer.buckets = bucketList;
        return bucketList;
    }

    private int[] getStartEnd(Object maxValue, int startPos, int currentPos) {
        int[] indexes = new int[2];

        if (currentPos == 0)
            indexes[0] = currentPos;
        else if (recordList.get(currentPos - 1).equals(maxValue)) {
            int i = currentPos - 1;
            while (i > startPos && recordList.get(i - 1).equals(maxValue)) {
                i--;
            }
            indexes[0] = i;
        } else
            indexes[0] = currentPos;

        if (currentPos == recordList.size() - 1)
            indexes[1] = currentPos;
        else if (recordList.get(currentPos + 1).equals(maxValue)) {
            int i = currentPos + 1;
            while (i < recordList.size() - 1 && recordList.get(i + 1).equals(maxValue)) {
                i++;
            }
            indexes[1] = i;
        } else
            indexes[1] = currentPos;


        return indexes;
    }

    public void printBuckets(List<Bucket<String>> bucketList) {
        for (int i = 0; i < bucketList.size(); i++) {
            System.out.println("Bucket " + (i + 1));
            System.out.println("Max: " + bucketList.get(i).maxValue);
            System.out.println("Min: " + bucketList.get(i).minValue);
            System.out.println("start: " + bucketList.get(i).startPos);
            System.out.println("end: " + bucketList.get(i).endPos);
            System.out.println("Size: " + bucketList.get(i).size);
            System.out.println("children: " + bucketList.get(i).getChildrenCount());
            printBuckets(bucketList.get(i).getChildren());
        }
    }

    public void writeJavaObject(ArrayList<Bucket<String>> object_ls) { // TODO: serialize newly created structures.
        String className = object_ls.get(0).getClass().getName();

        PreparedStatement pstmt = null;

        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName + "_nav")
                .append("(id INT PRIMARY KEY,data BYTEA)")
                .toString();

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(createTable.toString());
            connection.commit();
            // set input parameters
            StringBuffer navSB = new StringBuffer();
            for (int i = 0; i < object_ls.size(); i++) {
                navSB.append("INSERT into " + tableName + "_nav (id,data) values(?,?)");

                pstmt = connection.prepareStatement(navSB.toString());
                pstmt.setInt(1, i);

                if (useKryo) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    Output out = new Output(byteArrayOutputStream);
                    kryo.writeObject(out, object_ls.get(i));
                    pstmt.setBytes(2, out.toBytes());
                    out.close();
                    byteArrayOutputStream.close();
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    Object o = object_ls.get(i);
                    String value = mapper.writeValueAsString(o);
                    pstmt.setBytes(2, value.getBytes());
                }

                pstmt.executeUpdate();
                navSB = new StringBuffer();

            }

            connection.commit();
            pstmt.close();

            System.out.println("writeJavaObject: done serializing: " + className);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public ArrayList<Bucket<String>> readJavaObject(String dataTable) {

        ArrayList<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();

        PreparedStatement pstmt = null;
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {

            StringBuffer navSB = new StringBuffer();
            navSB.append("SELECT data from " + dataTable + "_nav ORDER BY id");
            pstmt = connection.prepareStatement(navSB.toString());

            ResultSet rs = pstmt.executeQuery();
            try {
                while (rs.next()) {
                    if (useKryo) {
                        Input in = new Input(rs.getBytes(1));
                        Bucket<String> object = new Bucket<String>();
                        object = kryo.readObject(in, object.getClass());
                        in.close();
                        bucketList.add(object);
                    } else {
                        ObjectMapper mapper = new ObjectMapper();
                        Bucket<String> object = new Bucket<String>();
                        String value = new String(rs.getBytes(1));
                        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                        object = mapper.readValue(value, Bucket.class);
                        bucketList.add(object);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            rs.close();
            pstmt.close();
            //String className = bucketList.get(0).getClass().getName();
            System.out.println("readJavaObject: done de-serializing: ");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //printBuckets(bucketList);
        return bucketList;
    }

    public ArrayList<Bucket<String>> recomputeNavS(String bucketName, ArrayList<Bucket<String>> navSbuckets, ArrayList<Bucket<String>> newList) {
        ArrayList<Bucket<String>> newNavs = navSbuckets;

        for (int i = 0; i < newNavs.size(); i++) {
            Bucket<String> newParent = updateParentBucket(bucketName, newNavs.get(i), newList);
            if (newParent != null) {
                newNavs.remove(i);
                newNavs.add(i, newParent);
                return newNavs;
            }
        }

        return newNavs;

    }

    private Bucket<String> updateParentBucket(String bucketName, Bucket<String> parent, ArrayList<Bucket<String>> children) {
        System.out.println("Calling: " + parent.getName());
        if (parent.getName().equals(bucketName)) {
            parent.setChildren(children);
            return parent;
        }

        if (parent.children == null)
            return null;

        for (int i = 0; i < parent.children.size(); i++) {
            Bucket<String> newParent = updateParentBucket(bucketName, parent.children.get(i), children);
            if (newParent != null) {
                parent.children.remove(i);
                parent.children.add(i, newParent);
                return parent;
            }
        }
        return null;
    }
}
