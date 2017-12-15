package Navigation;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.impl.BTree;
import org.zkoss.zss.model.impl.PosMapping;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class NavIndex {
    // Data sorted by String
    private ArrayList<AbstractMap.SimpleImmutableEntry<Integer, String>>  data;
    private boolean complete;
    //private BTree<String, String> rowMapping;

    NavIndex()
    {
        data = new ArrayList<>();
        complete = false;
    }

    public List<Bucket<String>> getBuckets(int count,boolean overlap)
    {
        synchronized (this) {
            if(!overlap)
                return getBucketsNoOverlap(0, data.size()-1, count);
            else
                return getBuckets(0, data.size(), count);
        }
    }


    public List<Bucket<String>> getBuckets(String minValue, String maxValue, int count)
    {
        synchronized (this) {
            int startPos = Collections.binarySearch(data,
                    new AbstractMap.SimpleImmutableEntry<Integer, String>(null, minValue),
                    Comparator.comparing(Map.Entry::getValue));

            int endPos = Collections.binarySearch(data,
                    new AbstractMap.SimpleImmutableEntry<Integer, String>(null, maxValue),
                    Comparator.comparing(Map.Entry::getValue));
            return getBuckets(startPos, endPos, count);
        }
    }

    public List<Bucket<String>> getBuckets(Bucket parent, int count,boolean overlap)
    {
        synchronized (this) {

            if(!overlap) {
                int startPos = Collections.binarySearch(data,
                        new AbstractMap.SimpleImmutableEntry<Integer, String>(null, (String) parent.minValue),
                        Comparator.comparing(Map.Entry::getValue));
                while (startPos>0 && data.get(startPos).getValue().equals(data.get(startPos-1).getValue())) startPos--;

                int endPos = Collections.binarySearch(data,
                        new AbstractMap.SimpleImmutableEntry<Integer, String>(null, (String) parent.maxValue),
                        Comparator.comparing(Map.Entry::getValue));
                while (endPos < data.size()-1 && data.get(endPos).getValue().equals(data.get(endPos+1).getValue())) endPos++;

                return getBucketsNoOverlap(startPos, endPos, count);
            }
            else
            {
                int startPos = Collections.binarySearch(data,
                        new AbstractMap.SimpleImmutableEntry<Integer, String>(null, (String) parent.minValue),
                        Comparator.comparing(Map.Entry::getValue));

                int endPos = Collections.binarySearch(data,
                        new AbstractMap.SimpleImmutableEntry<Integer, String>(null, (String) parent.maxValue),
                        Comparator.comparing(Map.Entry::getValue));
                return getBuckets(startPos, endPos, count);//
            }
        }
    }

    public List<Bucket<String>> getBuckets(int startPos, int endPos, int count)
    {
        List<Bucket<String>> bucketList = new ArrayList<>(count);
            List<AbstractMap.SimpleImmutableEntry<Integer, String>> subList = data.subList(startPos, endPos);
            if (subList.size()>0) {
                int bucketSize = subList.size() / count;
                for (int i = 0; i < count; i++) {
                    Bucket bucket = new Bucket();
                    bucket.minValue = subList.get(i * bucketSize).getValue();
                    bucket.maxValue = subList.get((i + 1) * bucketSize - 1).getValue();
                    bucket.size = bucketSize;
                    bucketList.add(bucket);
                }
            }
        printBuckets(bucketList);
        return bucketList;
    }

    private void printBuckets(List<Bucket<String>> bucketList) {
        for(int i=0;i<bucketList.size();i++)
        {
            System.out.println("Bucket "+(i+1));
            System.out.println("Max: "+bucketList.get(i).maxValue);
            System.out.println("Min: "+bucketList.get(i).minValue);
            System.out.println("Size: "+bucketList.get(i).size);
        }
    }

    public List<Bucket<String>> getBucketsNoOverlap(int startPos, int endPos, int count)
    {
        List<Bucket<String>> bucketList = new ArrayList<>(count);
        List<AbstractMap.SimpleImmutableEntry<Integer, String>> subList = data;//.subList(startPos, endPos);
        if (subList.size()>0) {

            int bucketSize = (endPos-startPos+1) / count;
            int boundary_change = 0;
            int element_count = 0;
            int startIndex=startPos;
            for (int i = 0; i < count && startIndex < endPos; i++) {
                Bucket bucket = new Bucket();
                bucket.minValue = subList.get(startIndex).getValue();
                if(startIndex+bucketSize-1 < endPos) {
                    bucket.maxValue = subList.get(startIndex + bucketSize - 1).getValue();

                }
                else
                {
                    bucket.maxValue = subList.get(endPos-1).getValue();

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

                if(subList.size()-1-startIndex+1 < bucketSize)
                    bucketSize = subList.size()-1-startIndex; //forcefully set bucket 1 size smaller to pass through next if else

               // System.out.println("startIndex: "+startIndex+", bucketSize: "+bucketSize+", subList.size(): "+subList.size());


                if((startIndex + bucketSize) < subList.size()) //if not end of list
                {
                    String boundary_value = subList.get(startIndex + bucketSize).getValue();

                    if(boundary_value.equals(bucket.maxValue))
                    {
                        bounday_inc++;
                        //Search where max value ends in next bucket
                        for(int j=startIndex + bucketSize+1 ; j<endPos;j++)
                        {
                            boundary_value = subList.get(j).getValue();
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
                            boundary_value = subList.get(j).getValue();
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
                            bucket.maxValue = subList.get(startIndex + bucketSize - 1+bounday_inc).getValue();
                            boundary_change = bounday_inc;
                        }
                        else
                        {
                            bucket.maxValue = subList.get(startIndex + bucketSize - 1-bounday_dec).getValue();
                            boundary_change = -bounday_dec;
                        }
                    }

                }


                bucket.size = bucketSize+boundary_change;
                if(bucket.size>0) {
                    startIndex += bucket.size;
                    bucketList.add(bucket);

                }


                if(bounday_dec < bounday_inc) //create new bucket as the current bucket is shrinked
                {
                    Bucket bucketSplit = new Bucket();
                    bucketSplit.minValue = subList.get(startIndex).getValue();
                    bucketSplit.maxValue = subList.get(startIndex+bounday_dec+bounday_inc-1).getValue();
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
                bucket.minValue = subList.get(startIndex).getValue();
                bucket.maxValue = subList.get(endPos-1).getValue();
                bucket.size = endPos-1-startIndex+1;
                bucketList.add(bucket);
            }

        }

       // printBuckets(bucketList);
        return bucketList;
    }

    public void addRecords(List<AbstractMap.SimpleImmutableEntry<Integer, String>>  newData)
    {
        synchronized (this) {
            data.addAll(newData);
            data.sort(Comparator.comparing(Map.Entry::getValue));
          // System.out.println(" Records 5" + data.get(6));
        }

    }

   /* public void insertIntoDB(ArrayList<String>  fullData, String tableName)
    {
        String url = "jdbc:postgresql://127.0.0.1:5432/datasets";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        synchronized (this) {



                DBContext context = new DBContext(connection);
                rowMapping = new BTree<String, String>(context, tableName + "_row_idx");

                ArrayList<String> sortedData = new ArrayList<String>();

                for(int i=0;i<data.size();i++)
                    sortedData.add(fullData.get(data.get(i).getKey()-1));

                rowMapping.insertIDsByValue(context,sortedData,sortedData);

            } catch (Exception e) {
                e.printStackTrace();
            }
            connection.commit();
            connection.close();

        }

    }*/

    public void setComplete() {
        this.complete = true;
        //this.fetch1k();
    }

    /*public void fetch1k()
    {
        String url = "jdbc:postgresql://127.0.0.1:5432/datasets";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        synchronized (this) {

            // Get tables
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
            ) {

                DBContext context = new DBContext(connection);
                System.out.println("Printing first 1k data");
                ArrayList<String> vals = rowMapping.getIDsByCount(context,0,1000);

                for(int i = 0; i<vals.size();i++)
                {
                    System.out.println(vals.get(i));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
    public int size()
    {
        return data.size();
    }

    public List<Integer> getOids(int fromRowIndex, int toRowIndex, int estimatedTableCount) {
        ArrayList<Integer> ret = new ArrayList<>();
        synchronized (this)
        {
            double multiplier = (double) data.size()/estimatedTableCount;
            if (multiplier>1.0)
                multiplier = 1.0;

            int scaledFromRowIndex = (int) (fromRowIndex * multiplier);
            int scaledToRowIndex = (int) (toRowIndex * multiplier);
            ret.add(data.get(scaledFromRowIndex).getKey());
        }
        return ret;
    }

    static class Bucket<T> {
        T minValue;
        T maxValue;
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

    public boolean isComplete() {
        return complete;
    }
}
