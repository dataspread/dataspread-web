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

        return getBucketsNoOverlap(0,recordList.size()-1,true);

        //  printBuckets(navSbuckets);

    }


    private List<Bucket<String>> getBucketsNoOverlap(int startPos, int endPos, boolean initBucket)
    {
        int parentBucketEnd = endPos+1;
        if(initBucket)
            parentBucketEnd = recordList.size();

        if(startPos==20 && endPos==29)
            System.out.println("Gotcha");

        List<Bucket<String>> bucketList = new ArrayList<Bucket<String>>();
        int bucketSize = (endPos-startPos+1) / kHisto;

        System.out.println("(start,end): ("+startPos+","+endPos+"), BUCKET Size: "+bucketSize);

        if (bucketSize > 0) {


            int boundary_change = 0;
            int element_count = 0;
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

                /*
                * if the value next to maxValue is same as maxValue, we need to increase bucket boundary
                * Search where max value ends: binary search in maxValue index+bucketSize
                * Search where maxValue stated in current bucket
                * if count maxValue in current bucket > count maxValue in next bucket. Merge the two else update current
                * bucket boundary to be the index just before the maxValue in current bucket
                * */
                int bounday_inc = 0;//count maxValue in next bucket
                int bounday_dec = 0;//count maxValue in current bucket

                if(parentBucketEnd-1-startIndex+1 < bucketSize)
                    bucketSize = parentBucketEnd-1-startIndex; //forcefully set bucket 1 size smaller to pass through next if else

                // System.out.println("startIndex: "+startIndex+", bucketSize: "+bucketSize+", subList.size(): "+subList.size());


                if((startIndex + bucketSize) < parentBucketEnd) //if not end of list
                {
                    String boundary_value = recordList.get(startIndex + bucketSize);

                    if(boundary_value.equals(bucket.maxValue))
                    {
                        bounday_inc++;
                        //Search where max value ends in next bucket
                        for(int j=startIndex + bucketSize+1 ; j<endPos+1;j++)
                        {
                            // System.out.println("i: "+i+", j:  "+j);
                            boundary_value = recordList.get(j);
                            if(boundary_value.equals(bucket.maxValue)) {
                                bounday_inc++;
                            }
                            else
                            {
                                break;
                            }
                        }
                        //System.out.println("----------From boundary inc to dec----------");
                        //count maxValue in current bucket
                        for(int j=startIndex + bucketSize-1;j>startIndex -1;j--)
                        {
                            // System.out.println("i: "+i+", j:  "+j);
                            boundary_value = recordList.get(j);
                            if(boundary_value.equals(bucket.maxValue)) {
                                bounday_dec++;
                            }
                            else
                            {
                                break;
                            }
                        }

                        if(bounday_dec>=bounday_inc)//keep everything in current bucket
                        {
                            bucket.maxValue = recordList.get(startIndex + bucketSize - 1+bounday_inc);
                            bucket.endPos = startIndex + bucketSize - 1+bounday_inc;
                            boundary_change = bounday_inc;
                        }
                        else
                        {
                            bucket.maxValue = recordList.get(startIndex + bucketSize -bounday_dec);
                            bucket.endPos = startIndex + bucketSize - bounday_dec;
                            boundary_change = -bounday_dec;
                        }
                    }

                }


                bucket.size = bucketSize+boundary_change;
                if(bucket.size>0) {
                    startIndex += bucket.size;

                    bucket.setChildren(getBucketsNoOverlap(bucket.startPos,bucket.endPos,false));
                    bucketList.add(bucket);

                }


                if(bounday_dec < bounday_inc) //create new bucket as the current bucket is shrinked
                {
                    Bucket bucketSplit = new Bucket();
                    bucketSplit.minValue = recordList.get(startIndex);
                    bucketSplit.maxValue = recordList.get(startIndex+bounday_dec+bounday_inc-1);
                    bucketSplit.startPos = startIndex;
                    bucketSplit.endPos = startIndex+bounday_dec+bounday_inc-1;

                    bucketSplit.size = bounday_dec+bounday_inc;

                    bucketSplit.setChildren(getBucketsNoOverlap(bucketSplit.startPos,bucketSplit.endPos,false));

                    bucketList.add(bucketSplit);

                    if(bucket.size >0)
                        i++;
                    startIndex += bucketSplit.size;

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

                bucket.setChildren(getBucketsNoOverlap(bucket.startPos,bucket.endPos,false));
                bucketList.add(bucket);
            }

        }

        // printBuckets(bucketList);
        return bucketList;
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
