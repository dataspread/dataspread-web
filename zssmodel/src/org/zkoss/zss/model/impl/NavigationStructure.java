package org.zkoss.zss.model.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sajjadur on 11/14/2017.
 */
public class NavigationStructure{
    private ArrayList<String> recordList;
    private int kHisto;
    private int selectedColumn;
    private String tableName;
    private String headerString;
    private String indexString;
    private Kryo kryo;
    private boolean useKryo;


    public NavigationStructure(String tableName)
    {
        this.tableName = tableName;
        this.recordList = new ArrayList<String>();
        this.kHisto = 10;
        kryo = new Kryo();
        this.useKryo = true;
    }

    public int getSampleSize() {
        return 1000;
    }

    public void setHeaderString(String str)
    {
        this.headerString = str;
    }

    public void setIndexString(String str)
    {
        this.indexString = str;
    }

    public void setRecordList(ArrayList<String> ls)
    {
        this.recordList = ls;
    }

    public ArrayList<Bucket<String>> createNavS(Bucket<String> bkt) {
        //load sorted data from table
        recordList =  new ArrayList<String>();


        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();

            Statement statement = connection.createStatement()) {

            ResultSet rs = null;
            if(bkt == null)
                rs = statement.executeQuery("SELECT "+indexString+" FROM " + tableName+" WHERE row != 1 ORDER by "+indexString);
            else
                rs = statement.executeQuery("SELECT "+indexString+" FROM " + tableName+" WHERE row != 1 AND row >= "+(bkt.startPos+2)+" AND row <= "+(bkt.endPos+2)+" ORDER by "+indexString);


            while (rs.next()) {
                recordList.add(new String(rs.getBytes(selectedColumn+1),"UTF-8"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //create nav data structure

        return getNonOverlappingBuckets(0,recordList.size()-1);//getBucketsNoOverlap(0,recordList.size()-1,true);

        //  printBuckets(navSbuckets);

    }

    public ArrayList<Bucket<String>> getNonOverlappingBuckets(int startPos, int endPos)
    {
        if(recordList.get(startPos).equals(recordList.get(endPos)))
            return getUniformBuckets(startPos,endPos,false);

        ArrayList<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();
        int bucketSize = (endPos-startPos+1) / kHisto;

        //System.out.println("(start,end): ("+startPos+","+endPos+"), BUCKET Size: "+bucketSize);

        if (bucketSize > 0) {
            int startIndex=startPos;
            for (int i = 0; i < kHisto && startIndex < endPos+1; i++) {
                //System.out.println("---------------BUCKET NO: "+i);
                Bucket bucket = new Bucket();
                bucket.minValue = recordList.get(startIndex);
                bucket.startPos = startIndex;
                if(startIndex+bucketSize-1 < endPos+1) {
                    bucket.maxValue = recordList.get(startIndex + bucketSize - 1);
                    bucket.endPos = startIndex + bucketSize - 1;
                }
                else
                {
                    bucket.maxValue = recordList.get(endPos);
                    bucket.endPos = endPos;

                }

                int [] start_end = new int[2];

                start_end = getStartEnd(bucket.maxValue,bucket.startPos,bucket.endPos);

                int boundary_inc = 0;//count maxValue in next bucket
                int boundary_dec = 0;//count maxValue in current bucket

                boundary_dec = bucket.endPos-start_end[0]+1;
                boundary_inc = start_end[1]-bucket.endPos+1;

                if(bucket.maxValue.equals(bucket.minValue))
                {
                    bucket.maxValue = recordList.get(start_end[1]);
                    bucket.endPos = start_end[1];
                    bucket.size = bucket.endPos-bucket.startPos+1;
                    startIndex += bucket.size;
                    bucket.setName(false);
                    bucket.setId();
                    //bucket.setChildren(getUniformBuckets(bucket.startPos,bucket.endPos,false));
                    bucketList.add(bucket);
                }
                else {
                    if (boundary_dec >= boundary_inc)//keep everything in current bucket
                    {
                        bucket.maxValue = recordList.get(start_end[1]);
                        bucket.endPos = start_end[1];
                    } else {
                        bucket.maxValue = recordList.get(start_end[0] - 1);
                        bucket.endPos = start_end[0] - 1;
                    }

                    bucket.size = bucket.endPos-bucket.startPos+1;
                    startIndex += bucket.size;
                    bucket.setName(false);
                    bucket.setId();
                    //bucket.setChildren(getNonOverlappingBuckets(bucket.startPos,bucket.endPos,false));
                    bucketList.add(bucket);
                }

            }

            if(startIndex<endPos+1)
            {
                Bucket bucket = new Bucket();
                bucket.minValue = recordList.get(startIndex);
                bucket.maxValue = recordList.get(endPos);
                bucket.startPos = startIndex;
                bucket.endPos = endPos;
                bucket.size = endPos-startIndex+1;
                bucket.setName(false);
                bucket.setId();
                /*if(bucket.maxValue.equals(bucket.minValue))
                    bucket.setChildren(getUniformBuckets(bucket.startPos,bucket.endPos,false));
                else
                    bucket.setChildren(getNonOverlappingBuckets(bucket.startPos,bucket.endPos,false));*/
                bucketList.add(bucket);
            }

        }

        // printBuckets(bucketList);
        return bucketList;
    }

    public ArrayList<Bucket<String>> getUniformBuckets(int startPos, int endPos, boolean b) {
        ArrayList<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();
        int bucketSize = (endPos-startPos+1) / kHisto;

        //System.out.println("(start,end): ("+startPos+","+endPos+"), BUCKET Size: "+bucketSize);

        if (bucketSize > 0) {
            int startIndex=startPos;
            for (int i = 0; i < kHisto && startIndex < endPos+1; i++) {
                //System.out.println("---------------BUCKET NO: " + i);
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

                bucket.size = bucket.endPos - bucket.startPos + 1;
                startIndex += bucket.size;
                bucket.setName(true);
                bucket.setId();
                //bucket.setChildren(getUniformBuckets(bucket.startPos, bucket.endPos, false));
                bucketList.add(bucket);
            }

            if(startIndex<endPos+1)
            {
                Bucket bucket = new Bucket();
                bucket.minValue = recordList.get(startIndex);
                bucket.maxValue = recordList.get(endPos);
                bucket.startPos = startIndex;
                bucket.endPos = endPos;
                bucket.size = endPos-startIndex+1;
                bucket.setName(true);
                bucket.setId();

                //bucket.setChildren(getUniformBuckets(bucket.startPos,bucket.endPos,false));
                bucketList.add(bucket);
            }
        }

        // printBuckets(bucketList);
        return bucketList;
    }

    private int[] getStartEnd(Object maxValue,int startPos, int currentPos) {
        int [] indexes = new int[2];

        if(currentPos==0)
            indexes[0] = currentPos;
        else if(recordList.get(currentPos-1).equals(maxValue))
        {
            int i = currentPos-1;
            while(i> startPos && recordList.get(i-1).equals(maxValue))
            {
                i--;
            }
            indexes[0] = i;
        }
        else
            indexes[0] = currentPos;

        if(currentPos==recordList.size()-1)
            indexes[1] = currentPos;
        else if(recordList.get(currentPos+1).equals(maxValue))
        {
            int i = currentPos+1;
            while(i< recordList.size()-1 && recordList.get(i+1).equals(maxValue))
            {
                i++;
            }
            indexes[1] = i;
        }
        else
            indexes[1] = currentPos;


        return indexes;
    }

    public void printBuckets(List<Bucket<String>> bucketList) {
        for(int i=0;i<bucketList.size();i++)
        {
            System.out.println("Bucket "+(i+1));
            System.out.println("Max: "+bucketList.get(i).maxValue);
            System.out.println("Min: "+bucketList.get(i).minValue);
            System.out.println("start: "+bucketList.get(i).startPos);
            System.out.println("end: "+bucketList.get(i).endPos);
            System.out.println("Size: "+bucketList.get(i).size);
            System.out.println("children: "+bucketList.get(i).getChildrenCount());
            printBuckets(bucketList.get(i).getChildren());
        }
    }

    public void setSelectedColumn(int selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

    public void writeJavaObject(ArrayList<Bucket<String>> object_ls){
        String className = object_ls.get(0).getClass().getName();

        PreparedStatement pstmt = null;

        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName+"_nav")
                .append("(id INT PRIMARY KEY,data BYTEA)")
                .toString();

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute(createTable.toString());
            connection.commit();
            // set input parameters
            StringBuffer navSB = new StringBuffer();
            for(int i=0;i<object_ls.size();i++) {
                navSB.append("INSERT into "+tableName+"_nav (id,data) values(?,?)");

                pstmt = connection.prepareStatement(navSB.toString());
                pstmt.setInt(1,i);

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

    public ArrayList<Bucket<String>> readJavaObject(String dataTable){

        ArrayList<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();

        PreparedStatement pstmt = null;
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();) {

            StringBuffer navSB = new StringBuffer();
            navSB.append("SELECT data from "+dataTable+"_nav ORDER BY id");
            pstmt = connection.prepareStatement(navSB.toString());

            ResultSet rs = pstmt.executeQuery();
            try {
                while(rs.next()) {
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
                        object = mapper.readValue(value,Bucket.class);
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

        for(int i=0;i<newNavs.size();i++)
        {
            Bucket<String> newParent = updateParentBucket(bucketName,newNavs.get(i),newList);
            if(newParent!=null)
            {
                newNavs.remove(i);
                newNavs.add(i,newParent);
                return newNavs;
            }
        }

        return newNavs;

    }

    private Bucket<String> updateParentBucket(String bucketName, Bucket<String> parent, ArrayList<Bucket<String>> children) {

        if(parent.getName().equals(bucketName))
        {
            parent.setChildren(children);
            return parent;
        }
        for(int i=0;i<parent.children.size();i++)
        {
            Bucket<String> newParent = updateParentBucket(bucketName,parent.children.get(i),children);
            if(newParent!=null) {
                parent.children.remove(i);
                parent.children.add(i,newParent);
                return parent;
            }
        }
        return null;
    }
}
