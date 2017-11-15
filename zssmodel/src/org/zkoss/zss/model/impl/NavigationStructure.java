package org.zkoss.zss.model.impl;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sajjadur on 11/14/2017.
 */
public class NavigationStructure {
    private ArrayList<String> recordList;
    private int kHisto;
    private int selectedColumn;
    private String tableName;

    public NavigationStructure(String tableName)
    {
        this.tableName = tableName;
        this.recordList = new ArrayList<String>();
        this.kHisto = 10;
    }

    public int getSampleSize() {
        return 100;
    }

    public List<Bucket<String>> createNavS(String headerString,String indexString,boolean isFirst) {
        //load sorted data from table
        recordList =  new ArrayList<String>();
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();

             Statement statement = connection.createStatement()) {

            ResultSet rs = statement.executeQuery("SELECT "+indexString+" FROM " + tableName+" WHERE row != 1 ORDER by "+indexString);


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

        return getNonOverlappingBuckets(0,recordList.size()-1,true);//getBucketsNoOverlap(0,recordList.size()-1,true);

        //  printBuckets(navSbuckets);

    }

    private List<Bucket<String>> getNonOverlappingBuckets(int startPos, int endPos, boolean initBucket)
    {
        if(recordList.get(startPos).equals(recordList.get(endPos)))
            return getOverlappingBuckets(startPos,endPos,false);

        List<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();
        int bucketSize = (endPos-startPos+1) / kHisto;

        System.out.println("(start,end): ("+startPos+","+endPos+"), BUCKET Size: "+bucketSize);

        if (bucketSize > 0) {
            int startIndex=startPos;
            for (int i = 0; i < kHisto && startIndex < endPos+1; i++) {
                System.out.println("---------------BUCKET NO: "+i);
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
                    bucket.setChildren(getOverlappingBuckets(bucket.startPos,bucket.endPos,false));
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
                    bucket.setChildren(getNonOverlappingBuckets(bucket.startPos,bucket.endPos,false));
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
                if(bucket.maxValue.equals(bucket.minValue))
                    bucket.setChildren(getOverlappingBuckets(bucket.startPos,bucket.endPos,false));
                else
                    bucket.setChildren(getNonOverlappingBuckets(bucket.startPos,bucket.endPos,false));
                bucketList.add(bucket);
            }

        }

        // printBuckets(bucketList);
        return bucketList;
    }

    private List<Bucket<String>> getOverlappingBuckets(int startPos, int endPos, boolean b) {
        List<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();
        int bucketSize = (endPos-startPos+1) / kHisto;

        System.out.println("(start,end): ("+startPos+","+endPos+"), BUCKET Size: "+bucketSize);

        if (bucketSize > 0) {
            int startIndex=startPos;
            for (int i = 0; i < kHisto && startIndex < endPos+1; i++) {
                System.out.println("---------------BUCKET NO: " + i);
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
                bucket.setChildren(getOverlappingBuckets(bucket.startPos, bucket.endPos, false));
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

                bucket.setChildren(getOverlappingBuckets(bucket.startPos,bucket.endPos,false));
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
        }
    }

    public void setSelectedColumn(int selectedColumn) {
        this.selectedColumn = selectedColumn;
    }
}
