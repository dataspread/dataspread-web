import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.impl.CountedBTree;
import org.zkoss.zss.model.impl.KeyBTree;
import org.zkoss.zss.model.impl.CombinedBTree;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;
import org.zkoss.zss.model.impl.statistic.CombinedStatistic;
import org.zkoss.zss.model.impl.statistic.CountStatistic;
import org.zkoss.zss.model.impl.statistic.KeyStatistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

public class BTreeTest {

    public static void main(String[] args) {
       // deepTest();

        //generateDist(1000000);
       // reBalancingVConstructionTest();

        fixedFillFactorTest();
    }

    private static void fixedFillFactorTest() {

        String url = "jdbc:postgresql://127.0.0.1:5432/postgres";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext context = new DBContext(DBHandler.instance.getConnection());


        //load data in to an array list and get unique values
        ArrayList<Integer> ls = new ArrayList<Integer>();
        ArrayList<Integer> unique = new ArrayList<Integer>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("uniform_1m.csv"));

            String line = "";

            int element = 0;

            while((line = br.readLine())!=null)
            {
                element = Integer.parseInt(line.trim());
                ls.add(element);

                if(!unique.contains(element))
                    unique.add(element);
            }

            br.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        int [] sampleSize = {500,1000,5000,10000,20000};

        int fillFactor = 5;
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter("fixed_ff_totalTimes.csv"));
            bw.write("Fill Factor, Sample Size, Batch Insert (ms), Batch Lookup (ms), All Insert(ms), All Lookup (ms)\n");
            for(;fillFactor<=100;fillFactor+=5)
            {
                for(int i=0;i<sampleSize.length;i++) {

                    long [] batchTotalIL = ff_batch_test(context,ls,unique,sampleSize[i],fillFactor);

                    bw.write(fillFactor+","+sampleSize[i]+","+batchTotalIL[0]+","+batchTotalIL[1]+",-,-\n");

                }
                long [] allTotalIL = ff_All_test(context,ls,unique,fillFactor);

                bw.write(fillFactor+",-,-,-,"+allTotalIL[0]+","+allTotalIL[1]+"\n");
            }

            bw.close();
        }
        catch(Exception e)
        {

        }

        updatable_ff_batch_test(context,ls,fillFactor);

    }

    private static long[] ff_All_test(DBContext context, ArrayList<Integer> ls, ArrayList<Integer> unique,  int fillFactor) {
        String tableName = "ff_all_"+fillFactor;

        long [] insertLookUpTime = new long[2];

        //insert values regular

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();

        CombinedBTree testTree = new CombinedBTree(context, tableName,true);
        testTree.setBlockSize(fillFactor);


        for(int i = 0; i < ls.size(); i++) {
            ids.add(i);
            statistics.add(new CombinedStatistic(new KeyStatistic(ls.get(i))));
        }

        long totalInsertTime = 0;

        int insertIndex=0;


        long startTime = System.currentTimeMillis();
        testTree.insertIDs(context, statistics, ls);
        long elapsedTime = System.currentTimeMillis()-startTime;

        totalInsertTime+=elapsedTime;

        //randomly look up 1 values

        long totalLookUpTime = 0;
        Random rand = new Random();
        for(int i=0;i<1000;i++)
        {
            int lookUpIndex = rand.nextInt(ls.size()-2);
            CombinedStatistic start = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(lookUpIndex));

            startTime = System.currentTimeMillis();
            testTree.getIDs(context, start, 1, AbstractStatistic.Type.COUNT);
            elapsedTime = System.currentTimeMillis()-startTime;

            totalLookUpTime+=elapsedTime;
        }

        insertLookUpTime[0] = totalInsertTime;
        insertLookUpTime[1] = totalLookUpTime/1000;

        return  insertLookUpTime;

    }

    private static long [] ff_batch_test(DBContext context, ArrayList<Integer> ls, ArrayList<Integer> unique, int sampleSize, int fillFactor) {
        String tableName = "ff_batch_"+sampleSize+"_"+fillFactor;

        long [] insertLookUpTime = new long[2];

        //insert values regular

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();

        CombinedBTree testTree = new CombinedBTree(context, tableName,true);
        testTree.setBlockSize(fillFactor);

        ArrayList<Long> insertTime = new ArrayList<Long>();
        long totalInsertTime = 0;

        for(int i=0;i<ls.size();i++)
        {
            ids.add(i);
            statistics.add(new CombinedStatistic(new KeyStatistic(ls.get(i))));
            if(ids.size()==sampleSize) {
                long startTime = System.currentTimeMillis();
                testTree.insertIDs(context, statistics, ids);
                long elapsedTime = System.currentTimeMillis()-startTime;

                insertTime.add(elapsedTime);
                totalInsertTime+=elapsedTime;

                ids.clear();
                statistics.clear();
            }
        }

        if(ids.size() > 0) {
            long startTime = System.currentTimeMillis();
            testTree.insertIDs(context, statistics, ids);
            long elapsedTime = System.currentTimeMillis()-startTime;

            insertTime.add(elapsedTime);
            totalInsertTime+=elapsedTime;
            ids.clear();
            statistics.clear();

        }

        writeResults(sampleSize,fillFactor,insertTime,"insert_ff_batch");

        //randomly look up 1 values

        long totalLookUpTime = 0;
        Random rand = new Random();
        for(int i=0;i<1000;i++)
        {
            int lookUpIndex = rand.nextInt(ls.size()-2);
            CombinedStatistic start = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(lookUpIndex));

            long startTime = System.currentTimeMillis();
            testTree.getIDs(context, start, 1, AbstractStatistic.Type.COUNT);
            long elapsedTime = System.currentTimeMillis()-startTime;

            totalLookUpTime+=elapsedTime;
        }

        insertLookUpTime[0] = totalInsertTime;
        insertLookUpTime[1] = totalLookUpTime/1000;

        return  insertLookUpTime;
    }


    private static void updatable_ff_batch_test(DBContext context, ArrayList<Integer> ls, int fillFactor) {
        String tableName = "ff_updatable_batch";

        int sampleSize = (int) (0.01*ls.size());

        long [] insertLookUpTime = new long[2];

        //insert values regular

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();

        CombinedBTree testTree = new CombinedBTree(context, tableName,true);
        testTree.setBlockSize(fillFactor);

        ArrayList<FillObject> insertTime = new ArrayList<FillObject>();

        ArrayList<FillObject> lookUpTime = new ArrayList<FillObject>();
        long totalInsertTime = 0;

        int insertedSoFar = 0;
        for(int i=0;i<ls.size();i++)
        {
            ids.add(i);
            statistics.add(new CombinedStatistic(new KeyStatistic(ls.get(i))));
            if(ids.size()==sampleSize) {
                long startTime = System.currentTimeMillis();
                testTree.insertIDs(context, statistics, ids);
                long elapsedTime = System.currentTimeMillis()-startTime;

                insertTime.add(new FillObject(elapsedTime,fillFactor));
                totalInsertTime+=elapsedTime;

                ids.clear();
                statistics.clear();

                insertedSoFar+=sampleSize;

                //randomly look up 1 values
                long totalLookUpTime = 0;
                Random rand = new Random();
                for(int j=0;j<100;j++)
                {
                    int lookUpIndex = rand.nextInt(insertedSoFar-2);
                    CombinedStatistic start = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(lookUpIndex));

                    startTime = System.currentTimeMillis();
                    testTree.getIDs(context, start, 1, AbstractStatistic.Type.COUNT);
                    elapsedTime = System.currentTimeMillis()-startTime;

                    totalLookUpTime+=elapsedTime;

                }

                lookUpTime.add(new FillObject(totalLookUpTime/100,fillFactor));

                if(insertedSoFar >= (int)(0.1*ls.size())) {
                    fillFactor += 5;

                    if(fillFactor>100)
                        fillFactor = 100;

                    testTree.setBlockSize(fillFactor);

                    insertedSoFar = 0;


                }

            }
        }

        if(ids.size() > 0) {
            long startTime = System.currentTimeMillis();
            testTree.insertIDs(context, statistics, ids);
            long elapsedTime = System.currentTimeMillis()-startTime;

            insertTime.add(new FillObject(elapsedTime,fillFactor));
            totalInsertTime+=elapsedTime;
            ids.clear();
            statistics.clear();

        }

        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter("updatable_ff.csv"));

            bw.write("Iteration,Fill Factor, Insert Time (ms), Look Up Time(ms)\n");

            for(int i=0;i<insertTime.size();i++)
            {
                bw.write((i+1)+","+insertTime.get(i).fillFactor+","+insertTime.get(i).elapsedTime+","+lookUpTime.get(i).elapsedTime+"\n");

            }
            bw.close();
        }catch(Exception e)
        {

        }


    }


    private static void writeResults(int sampleSize, int fillFactor, ArrayList<Long> insertTime, String type) {
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(type+"_"+sampleSize+"_"+fillFactor+".csv"));

            bw.write("Iteration, Time (ms)\n");

            for(int i=0;i<insertTime.size();i++)
            {
               bw.write((i+1)+","+insertTime.get(i)+"\n");

            }
            bw.close();

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    private static void reBalancingVConstructionTest() {

        String url = "jdbc:postgresql://127.0.0.1:5432/postgres";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext context = new DBContext(DBHandler.instance.getConnection());

        ArrayList<Integer> ls = new ArrayList<Integer>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("uniform.csv"));

            String line = "";

            while((line = br.readLine())!=null)
            {
                ls.add(Integer.parseInt(line.trim()));
            }

            br.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        int [] sampleSize = {500};//,1000,5000,10000,20000};

        for(int i=0;i<sampleSize.length;i++) {

            rebalanceTest(context,ls,sampleSize[i]);

            reconstructTest(context, ls,sampleSize[i]);
        }

    }

    private static void reconstructTest(DBContext context, ArrayList<Integer> ls, int sampleSize) {

        String tableName = "reconstruct";

        createDropTable(context,tableName);

        //insert values regular


        ArrayList<Long> insertTime = new ArrayList<Long>();
        try{
            StringBuffer sbSS = new StringBuffer();
            PreparedStatement pstSS = null;


            long startTime = System.currentTimeMillis();
            long totalTime = 0;
            for(int i=0;i<ls.size();i++)
            {
                sbSS.append("INSERT into "+tableName+" (row) values(?)");

                pstSS = context.getConnection().prepareStatement(sbSS.toString());

                pstSS.setInt(1,ls.get(i));

                pstSS.executeUpdate();

                sbSS = new StringBuffer();
                if((i+1) % sampleSize ==0) {
                    createIndex(context,tableName);
                    context.getConnection().commit();
                    long elapsedTime = System.currentTimeMillis()-startTime;
                    insertTime.add(elapsedTime);
                }
            }

            if(ls.size()%sampleSize!=0) {
                createIndex(context,tableName);
                context.getConnection().commit();
                long elapsedTime = System.currentTimeMillis()-startTime;
                insertTime.add(elapsedTime);
                totalTime = elapsedTime;
            }

            writeResults(sampleSize,insertTime,"reconstruct");

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println(insertTime);

        //insert values by copy
/*
        createDropTable(context,tableName);

        createIndex(context,tableName);



        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            Connection rawConn = ((DelegatingConnection) connection.getInternalConnection()).getInnermostDelegate();
            CopyManager cm = ((PgConnection) rawConn).getCopyAPI();
            CopyIn cpIN = null;

            StringBuffer sb = new StringBuffer();
            ArrayList<Long> copyTime = new ArrayList<Long>();
            int sampleSize = 10000;
            long startTime = System.currentTimeMillis();
            long totalTime = 0;
            for(int i=0;i<ls.size();i++)
            {
                if (cpIN == null)
                {
                   StringBuffer copyCommand = new StringBuffer("COPY ");
                    copyCommand.append(tableName);
                    copyCommand.append("(row");
                    copyCommand.append(") FROM STDIN ");
                    cpIN = cm.copyIn(copyCommand.toString());

                }

                sb.append(ls.get(i));

                sb.append('\n');

                if ((i+1)%sampleSize==0) {
                    cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
                    sb = new StringBuffer();

                }
            }
            if (sb.length() > 0)
                cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
            cpIN.endCopy();
            rawConn.commit();

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

*/
    }

    private static void rebalanceTest(DBContext context, ArrayList<Integer> ls, int sampleSize) {
        String tableName = "rebalance";


        createDropTable(context,tableName);

        createIndex(context,tableName);

        //insert values regular


        ArrayList<Long> insertTime = new ArrayList<Long>();
        try{
            StringBuffer sbSS = new StringBuffer();
            PreparedStatement pstSS = null;

            long startTime = System.currentTimeMillis();
            long totalTime = 0;
            for(int i=0;i<ls.size();i++)
            {
                sbSS.append("INSERT into "+tableName+" (row) values(?)");

                pstSS = context.getConnection().prepareStatement(sbSS.toString());

                pstSS.setInt(1,ls.get(i));

                pstSS.executeUpdate();

                sbSS = new StringBuffer();
                if((i+1) % sampleSize ==0) {

                    context.getConnection().commit();
                    long elapsedTime = System.currentTimeMillis()-startTime;
                    insertTime.add(elapsedTime);
                }
            }

            if(ls.size()%sampleSize!=0) {
                context.getConnection().commit();
                long elapsedTime = System.currentTimeMillis()-startTime;
                insertTime.add(elapsedTime);
                totalTime = elapsedTime;
            }

            writeResults(sampleSize,insertTime,"rebalance");

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println(insertTime);



        //insert values by copy
/*
        createDropTable(context,tableName);

        createIndex(context,tableName);



        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            Connection rawConn = ((DelegatingConnection) connection.getInternalConnection()).getInnermostDelegate();
            CopyManager cm = ((PgConnection) rawConn).getCopyAPI();
            CopyIn cpIN = null;

            StringBuffer sb = new StringBuffer();
            ArrayList<Long> copyTime = new ArrayList<Long>();
            int sampleSize = 10000;
            long startTime = System.currentTimeMillis();
            long totalTime = 0;
            for(int i=0;i<ls.size();i++)
            {
                if (cpIN == null)
                {
                   StringBuffer copyCommand = new StringBuffer("COPY ");
                    copyCommand.append(tableName);
                    copyCommand.append("(row");
                    copyCommand.append(") FROM STDIN ");
                    cpIN = cm.copyIn(copyCommand.toString());

                }

                sb.append(ls.get(i));

                sb.append('\n');

                if ((i+1)%sampleSize==0) {
                    cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
                    sb = new StringBuffer();

                }
            }
            if (sb.length() > 0)
                cpIN.writeToCopy(sb.toString().getBytes(), 0, sb.length());
            cpIN.endCopy();
            rawConn.commit();

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

*/
    }

    private static void writeResults(int sampleSize, ArrayList<Long> insertTime, String type) {

        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(type+"_"+sampleSize+".csv"));

            bw.write("Iteration, Cumulative Time (s), Time (s)\n");

            for(int i=0;i<insertTime.size();i++)
            {
                if(i==0)
                    bw.write((i+1)+","+insertTime.get(i)+","+insertTime.get(i)+"\n");
                else
                    bw.write((i+1)+","+insertTime.get(i)+","+(insertTime.get(i)-insertTime.get(i-1))+"\n");
            }
            bw.close();

        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void createIndex(DBContext context, String tableName) {
        StringBuffer indexTable = new StringBuffer("DROP INDEX IF EXISTS col_index_insert");

        try ( Statement indexStmt = context.getConnection().createStatement()) {
            indexStmt.executeUpdate(indexTable.toString());
            context.getConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        indexTable = new StringBuffer("CREATE INDEX col_index_insert ON ");
        indexTable.append(tableName+" (row)");
        try ( Statement indexStmt = context.getConnection().createStatement()) {
            indexStmt.executeUpdate(indexTable.toString());
            context.getConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void dropIndex(DBContext context, String tableName) {
        StringBuffer indexTable = new StringBuffer("DROP INDEX IF EXISTS col_index_insert");
        indexTable.append(tableName+" (row)");
        try ( Statement indexStmt = context.getConnection().createStatement()) {
            indexStmt.executeUpdate(indexTable.toString());
            context.getConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createDropTable(DBContext context,String tableName) {
        String dropTable = (new StringBuffer())
                .append("DROP TABLE IF EXISTS ")
                .append(tableName)
                .toString();
        try (Statement stmt = context.getConnection().createStatement()) {
            stmt.execute(dropTable);
            context.getConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String createTable = (new StringBuffer())
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("(row INT)")
                .toString();

        try ( Statement stmt = context.getConnection().createStatement()) {
            stmt.execute(createTable);
            context.getConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateDist(int max) {

        Random r=new Random();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("uniform.csv"));

            BufferedWriter bw1 = new BufferedWriter(new FileWriter("unidec.csv"));

            for(int i=0;i<max;i++) {
                int n = r.nextInt(max/10) + 1;

                if(i==max-1)
                    bw.write(Integer.toString(n));
                else
                    bw.write(n+"\n");

                if(n>max/2)
                {
                    n = getYourPositiveFunctionRandomNumber(max/2,max);
                    if(i==max-1)
                        bw1.write(Integer.toString(n));
                    else
                        bw1.write(n+"\n");
                }
                else
                {
                    if(i==max-1)
                        bw1.write(Integer.toString(n));
                    else
                        bw1.write(n+"\n");
                }

            }
            bw.close();
            bw1.close();
        }
        catch(Exception e)
        {

        }

    }

    public static int getYourPositiveFunctionRandomNumber(int startIndex, int stopIndex) {
        //Generate a random number whose value ranges from 0.0 to the sum of the values of yourFunction for all the possible integer return values from startIndex to stopIndex.
        double randomMultiplier = 0;
        for (int i = startIndex; i <= stopIndex; i++) {
            randomMultiplier += yourFunction(i);//yourFunction(startIndex) + yourFunction(startIndex + 1) + .. yourFunction(stopIndex -1) + yourFunction(stopIndex)
        }
        Random r = new Random();
        double randomDouble = r.nextDouble() * randomMultiplier;

        //For each possible integer return value, subtract yourFunction value for that possible return value till you get below 0.  Once you get below 0, return the current value.
        int yourFunctionRandomNumber = startIndex;
        randomDouble = randomDouble - yourFunction(yourFunctionRandomNumber);
        while (randomDouble >= 0) {
            yourFunctionRandomNumber++;
            randomDouble = randomDouble - yourFunction(yourFunctionRandomNumber);
        }

        return yourFunctionRandomNumber;
    }

    private static double yourFunction(int i) {
        return i;
    }

    public static void deepTest(){
        String url = "jdbc:postgresql://127.0.0.1:5432/postgres";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());

        CombinedDNETest(dbContext);
        dbContext.getConnection().commit();
        dbContext.getConnection().close();
    }
    public static void simpleTest(){
        String url = "jdbc:postgresql://127.0.0.1:5432/postgres";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());
        CountedBTree btree = new CountedBTree(dbContext, "Test1", false);
        btree.setBlockSize(5);
        ArrayList<Integer> arrayList = new ArrayList<>();

        // Random operations
        final int operations=5;
        Random random = new Random(1);
        Random randomOperation = new Random(1);
        int stats_add =0;
        int stats_remove =0;


        System.out.println("Start adding Initial Records");
        // Add initial data points.
        for (int i=0;i<operations;i++)
        {
            stats_add++;
            int randomValue = random.nextInt();
            ArrayList<Integer> insertList = new ArrayList<>();
            insertList.add(randomValue);
            btree.insertIDs(dbContext, i, insertList);
            System.out.println("Records added:" + randomValue);
            dbContext.getConnection().commit();
            arrayList.add(i,randomValue);
        }

        System.out.println("Random operations");
        // Perform RANDOM operations.
        for (int i=0;i<operations;i++)
        {
            int operation = randomOperation.nextInt(2);
            int randomPos = random.nextInt(arrayList.size());
            int randomValue = random.nextInt();

            switch (operation)
            {
                case 0:
                    stats_add++;
                    ArrayList<Integer> randomValueList = new ArrayList<>();
                    randomValueList.add(randomValue);
                    //System.out.println(btree.size(dbContext) + " add " + randomPos + " " + randomValueList);
                    btree.insertIDs(dbContext, randomPos, randomValueList);
                    arrayList.add(randomPos, randomValue);
                    break;
                case 1:
                    stats_remove++;
                    ArrayList<Integer> randomPosList = new ArrayList<>();
                    //System.out.println(btree.size(dbContext) + " remove " + randomPos);
                    btree.deleteIDs(dbContext, randomPos, 1);
                    arrayList.remove(randomPos);
                    break;
            }
            dbContext.getConnection().commit();
        }


        System.out.println("Records added:" + stats_add);
        System.out.println("Records deleated:" + stats_remove);
        System.out.println("BTree size:" + btree.size(dbContext));
        System.out.println("ArrayList size:" + arrayList.size());

        ArrayList<Integer> btree_list = btree.getIDs(dbContext, 0, arrayList.size());
        if (btree.size(dbContext) == arrayList.size()
                && btree_list.equals(arrayList))
            System.out.println("Results Match");
        else
            System.err.println("Results do not match");



        dbContext.getConnection().commit();
        dbContext.getConnection().close();
    }

    public static void sparseTest(DBContext dbContext){
        CountedBTree testTree = new CountedBTree(dbContext, "Test1", false);
        testTree.setBlockSize(5);
        testTree.getIDs(dbContext, 2, 1);
        testTree.getIDs(dbContext,5, 1);
        testTree.getIDs(dbContext,8, 1);
        dbContext.getConnection().commit();
        testTree.deleteIDs(dbContext,6, 1);
        dbContext.getConnection().commit();
        System.out.println(testTree.getIDs(dbContext, 10, 1));
    }

    public static void testRootInsDelByCount(DBContext context) {

        CountedBTree testTree = new CountedBTree(context, "testRootInsDelByCount", false);
        testTree.setBlockSize(5);
        ArrayList<Integer> key = new ArrayList<>();
        key.add(1);

        testTree.insertIDs(context, 0, key);
        //Integer test = testTree.lookup(context, key, AbstractStatistic.Type.COUNT);
        testTree.deleteIDs(context, 0, 1);

        testTree.insertIDs(context, 0, key);
        //test = testTree.getByCount(context, 0);
        ArrayList<Integer> key1 = new ArrayList<>();
        testTree.insertIDs(context, 1, key1);
        //test = testTree.getByCount(context, 2);
        testTree.insertIDs(context, 1, key);
        testTree.insertIDs(context, 1, key);


    }

    public static void testRootSplitByCount(DBContext context) {

        int[] a = {5, 25, 50};
        int[] rootids = {0, 0, 0};

        for(int i = 0; i < 3; i++){
            String testName = "testRootSplit"+i;
            CountedBTree testTree = new CountedBTree(context, testName, false);
            testTree.setBlockSize(5);
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(100);
            ids.add(200);
            ids.add(300);
            ids.add(400);
            ids.add(a[i]*10);
            testTree.insertIDs(context, 0, ids);

        }

    }

    public static void testSplitNodeByCount(DBContext context) {
        CountedBTree testTree = new CountedBTree(context, "testSplitNodeByCount", false);
        testTree.setBlockSize(5);
        ArrayList<Integer> ids = new ArrayList<>();
        int [] numbers = {50, 100, 200, 250, 300, 400, 500, 600, 700, 800};
        for(int i = 0; i < 10; i++){
            ids.add(numbers[i]);

        }
        testTree.insertIDs(context, 0, ids);

        ArrayList<Integer> new_ids = new ArrayList<>();
        new_ids.add(30);
        testTree.insertIDs(context, 1, new_ids);
        new_ids.set(0, 150);
        testTree.insertIDs(context, 3, new_ids);
        new_ids.set(0, 230);
        testTree.insertIDs(context, 5, new_ids);
        new_ids.set(0, 270);
        testTree.insertIDs(context, 7, new_ids);
        new_ids.set(0, 350);
        testTree.insertIDs(context, 9, new_ids);
        new_ids.set(0, 450);
        testTree.insertIDs(context, 11, new_ids);
        new_ids.set(0, 550);
        testTree.insertIDs(context, 13, new_ids);
        new_ids.set(0, 800);
        testTree.insertIDs(context, 16, new_ids);

    }

    public static void testSplitNodeSplitParentByCount(DBContext context) {
        ArrayList<Integer> ids = new ArrayList<>();
        int [] numbers = {50, 100, 200, 250, 300, 400, 500, 600, 700};
        for(int i = 0; i < 9; i++){
            ids.add( numbers[i]);
        }

        int[] a = {1, 8, 16};
        int[] aa = {0, 2, 4};
        for(int i = 0; i < 3; i++){
            CountedBTree testTree = new CountedBTree(context, "testSplitNodeSplitParentByCount", false);
            testTree.setBlockSize(5);
            testTree.insertIDs(context, 0, ids);
            ArrayList<Integer> new_ids = new ArrayList<>();
            new_ids.add(30);
            testTree.insertIDs(context, 0, new_ids);
            new_ids.set(0, 150);
            testTree.insertIDs(context, 3, new_ids);
            new_ids.set(0, 230);
            testTree.insertIDs(context, 5, new_ids);
            new_ids.set(0, 270);
            testTree.insertIDs(context, 7, new_ids);
            new_ids.set(0, 350);
            testTree.insertIDs(context, 9, new_ids);
            new_ids.set(0, 450);
            testTree.insertIDs(context, 11, new_ids);
            new_ids.set(0, 550);
            testTree.insertIDs(context, 13, new_ids);
            new_ids.set(0, a[i]*10);
            testTree.insertIDs(context, aa[i], new_ids);

        }
    }

    public static void testNodeMergeByCount(DBContext context) {
        String testName = "testNodeMergeByCount";
        CountedBTree testTree = new CountedBTree(context, testName, false);
        testTree.setBlockSize(5);
        ArrayList<Integer> ids = new ArrayList<>();
        for(int i = 1; i <= 7; i++){
            ids.add(i*100);
        }
        testTree.insertIDs(context, 0, ids);
        ids = new ArrayList<>();
        ids.add(50);
        testTree.insertIDs(context, 0, ids);
        testTree.deleteIDs(context, 3, 1);

    }

    public static void NodeMergeRootMergeByCount(DBContext context) {
        String testName = "NodeMergeRootMergeByCount";
        CountedBTree testTree = new CountedBTree(context, testName, false);
        testTree.setBlockSize(5);
        ArrayList<Integer> ids = new ArrayList<>();
        int [] numbers = {50, 100, 200, 230, 270, 300, 330, 400, 500, 550, 700, 800, 850};
        for(int i = 0; i < 13; i++){
            ids.add(numbers[i]);

        }
        testTree.insertIDs(context, 0, ids);
        testTree.deleteIDs(context, 0, 1);
        testTree.deleteIDs(context, 0, 1);
        testTree.deleteIDs(context, 0, 1);
        testTree.deleteIDs(context, 0, 1);
        testTree.deleteIDs(context, 0, 1);

    }

    public static void NodeMergeRootMerge1ByCount(DBContext context) {
        String testName = "tNMRM1BC";
        CountedBTree testTree = new CountedBTree(context, testName, false);
        testTree.setBlockSize(5);
        ArrayList<Integer> ids = new ArrayList<>();
        int [] numbers = {50, 100, 200, 230, 270, 300, 330, 400, 500, 550, 700, 800, 850, 900, 950, 1000, 1050, 1100, 1150};
        for(int i = 0; i < 19; i++){
            ids.add(numbers[i]);
        }
        testTree.insertIDs(context, 0, ids);
        testTree.insertIDs(context, 0, ids);
        testTree.deleteIDs(context, 0, 1);
        testTree.deleteIDs(context, 13, 1);
        testTree.deleteIDs(context, 16, 1);


    }
    public static void testRootInsDel(DBContext context){
        String tableName = "testRootInsDel";

        KeyBTree testTree = new KeyBTree(context, tableName,  false);
        testTree.setBlockSize(5);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(300);
        ArrayList<KeyStatistic> statistics = new ArrayList<>();
        statistics.add(new KeyStatistic(30));
        testTree.insertIDs(context, statistics, ids);
        testTree.deleteIDs(context, statistics);
        testTree.insertIDs(context, statistics, ids);
        ids.set(0, 200);
        ids.add(400);
        ArrayList<KeyStatistic> new_statistics = new ArrayList<>();
        statistics.set(0, new KeyStatistic(20));
        statistics.add(new KeyStatistic(40));
        testTree.insertIDs(context, statistics, ids);

    }
    public static void testRootSplit(DBContext context){
        int[] numbers = {10, 20, 30, 40};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<KeyStatistic> statistics = new ArrayList<>();
        for(int j = 0; j < 4; j++){
            ids.add(numbers[j]*10);
            statistics.add(new KeyStatistic(numbers[j]));
        }
        int[] a = {5, 25, 50};
        for(int i = 0; i < 3; i++) {
            String tableName = "testRootSplit"+i;
            KeyBTree testTree = new KeyBTree(context, tableName,  false);
            testTree.setBlockSize(5);
            testTree.insertIDs(context, statistics, ids);
            ArrayList<Integer> new_ids = new ArrayList<>();
            new_ids.add(a[i]*10);
            ArrayList<KeyStatistic> new_statistics = new ArrayList<>();
            new_statistics.add(new KeyStatistic(a[i]));
            testTree.insertIDs(context, new_statistics, new_ids);
        }
    }
    public static void testSplitNode(DBContext context){
        String tableName = "testSplitNode";
        KeyBTree testTree = new KeyBTree(context, tableName,  false);
        testTree.setBlockSize(5);
        int [] a = {5, 10, 20, 25, 30, 40, 50, 60, 70, 3, 15, 23, 27, 35, 45, 55, 80};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<KeyStatistic> statistics = new ArrayList<>();
        for( int i = 0; i < 17; i++){
            ids.add(a[i]*10);
            statistics.add(new KeyStatistic(a[i]));
        }
        testTree.insertIDs(context, statistics, ids);
    }
    public static void testSplitNodeSplitParent(DBContext context){
        int [] a = {5, 10, 20, 25, 30, 40, 50, 60, 70, 3, 15, 23, 27, 35, 45, 55, 80};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<KeyStatistic> statistics = new ArrayList<>();
        for( int i = 0; i < 17; i++){
            ids.add(a[i]*10);
            statistics.add(new KeyStatistic(a[i]));
        }
        int [] aa = {1, 8, 16};
        for(int j = 0; j < 3; j++) {
            String tableName = "testSplitNodeSplitParent"+j;
            KeyBTree testTree = new KeyBTree(context, tableName, false);
            testTree.setBlockSize(5);
            testTree.insertIDs(context, statistics, ids);
            ArrayList<Integer> new_ids = new ArrayList<>();
            new_ids.add(aa[j]*10);
            ArrayList<KeyStatistic> new_statistics = new ArrayList<>();
            new_statistics.add(new KeyStatistic(aa[j]));
            testTree.insertIDs(context, new_statistics, new_ids);
        }
    }
    public static void testNodeMerge(DBContext context){
        String tableName = "testNodeMerge";
        KeyBTree testTree = new KeyBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] a = {10, 20, 30, 40, 50, 60, 70};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<KeyStatistic> statistics = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            ids.add(a[i]*10);
            statistics.add(new KeyStatistic(a[i]));
        }
        testTree.insertIDs(context, statistics, ids);
        ArrayList<KeyStatistic> del_statistics = new ArrayList<>();
        del_statistics.add(new KeyStatistic(20));
        testTree.deleteIDs(context, del_statistics);
    }
    public static void NodeMergeRootMerge(DBContext context){
        String tableName = "NodeMergeRootMerge";
        KeyBTree testTree = new KeyBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] a = {5, 10, 20, 23, 27, 30, 33, 40, 50, 55, 70, 80, 85};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<KeyStatistic> statistics = new ArrayList<>();
        for(int i = 0; i < 13; i++){
            ids.add(a[i]*10);
            statistics.add(new KeyStatistic(a[i]));
        }
        testTree.insertIDs(context, statistics, ids);
        ArrayList<KeyStatistic> del_statistics = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            del_statistics.add(new KeyStatistic(a[i]));
        }
        testTree.deleteIDs(context, del_statistics);
    }
    public static void NodeMergeRootMerge1(DBContext context){
        String tableName = "NodeMergeRootMerge1";
        KeyBTree testTree = new KeyBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] a = {5, 10, 20, 23, 27, 30, 33, 40, 50, 55, 70, 80, 85, 90, 95, 100, 105, 110, 115};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<KeyStatistic> statistics = new ArrayList<>();
        for(int i = 0; i < 19; i++){
            ids.add(a[i]*10);
            statistics.add(new KeyStatistic(a[i]));
        }
        testTree.insertIDs(context, statistics, ids);
        ArrayList<KeyStatistic> del_statistics = new ArrayList<>();
        int [] aa = {5, 95, 115};
        for(int i = 0; i < 3; i++){
            del_statistics.add(new KeyStatistic(aa[i]));
        }
        testTree.deleteIDs(context, del_statistics);
    }
    public static void CombinedOneLevel(DBContext context){
        String tableName = "CombinedOneLevel";
        CombinedBTree testTree = new CombinedBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] num = {10, 20, 30, 40};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            ids.add(num[i]*10);
            statistics.add(new CombinedStatistic(new KeyStatistic(num[i])));
        }
        testTree.insertIDs(context, statistics, ids);
        CombinedStatistic start = new CombinedStatistic(new KeyStatistic(20), new CountStatistic(1));
        ArrayList<Integer> results = testTree.getIDs(context, start, 2, AbstractStatistic.Type.COUNT);
        ArrayList<Integer> outofbounds = testTree.getIDs(context, start, 5 , AbstractStatistic.Type.COUNT);
        System.out.println(results);
        System.out.println(outofbounds);
    }
    public static void CombinedTwoLevels(DBContext context){
        String tableName = "CombinedTwoLevels";
        CombinedBTree testTree = new CombinedBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] num = {10, 20, 30, 40, 50, 60, 70};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();
        for(int i = 0; i < 7; i++) {
            ids.add(num[i]*10);
            statistics.add(new CombinedStatistic(new KeyStatistic(num[i])));
        }
        testTree.insertIDs(context, statistics, ids);
        CombinedStatistic start = new CombinedStatistic(new KeyStatistic(20), new CountStatistic(1));
        ArrayList<Integer> results = testTree.getIDs(context, start, 4, AbstractStatistic.Type.KEY);
        System.out.println(results);
    }
    public static void CombinedDNETest(DBContext context){
        String tableName = "CombinedDNETest";
        String dropTable = (new StringBuffer())
                .append("DROP TABLE IF EXISTS ")
                .append(tableName)
                .toString();
        try (Statement stmt = context.getConnection().createStatement()) {
            stmt.execute(dropTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //load data in to an array list and get unique values
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("uniform_1m.csv"));

            String line = "";

            int element = 0;
            int lineCount=0;
            while((line = br.readLine())!=null)
            {
                element = Integer.parseInt(line.trim());

                ids.add(lineCount++);
                statistics.add(new CombinedStatistic(new KeyStatistic(element)));
            }

            br.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        CombinedBTree testTree = new CombinedBTree(context, tableName,true);
        testTree.setBlockSize(100);


        testTree.insertIDs(context, statistics, ids);


        //randomly look up 1 values

        long totalLookUpTime = 0;
        Random rand = new Random();
        for(int i=0;i<1000;i++)
        {
            int lookUpIndex = rand.nextInt(ids.size()-2);
            CombinedStatistic start = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(lookUpIndex));

            testTree.getIDs(context, start, 1, AbstractStatistic.Type.COUNT);

        }

        /*CombinedBTree testTree = new CombinedBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] num = {30, 50, 10, 10, 50, 80, 100};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();
        for(int i = 0; i < num.length; i++) {
            ids.add(num[i]);
            statistics.add(new CombinedStatistic(new KeyStatistic(num[i])));
        }
        testTree.insertIDs(context, statistics, ids);
        CombinedStatistic start = new CombinedStatistic(new KeyStatistic(30), new CountStatistic(5));
        ArrayList<Integer> results = testTree.getIDs(context, start, 1, AbstractStatistic.Type.COUNT);
        System.out.println(results);*/
    }
    public static void CombinedNodeSplit(DBContext context){
        String tableName = "CombinedNodeSplit";
        CombinedBTree testTree = new CombinedBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] a = {5, 10, 20, 25, 30, 40, 50, 60, 70, 3, 15, 23, 27, 35, 45, 55, 80};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();
        for( int i = 0; i < 17; i++){
            ids.add(a[i]*10);
            statistics.add(new CombinedStatistic(new KeyStatistic(a[i])));
        }
        testTree.insertIDs(context, statistics, ids);

    }
    public static void CombinedNodeMerge(DBContext context){
        String tableName = "NodeMergeRootMerge";
        CombinedBTree testTree = new CombinedBTree(context, tableName, false);
        testTree.setBlockSize(5);
        int [] a = {5, 10, 20, 23, 27, 30, 33, 40, 50, 55, 70, 80, 85};
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<CombinedStatistic> statistics = new ArrayList<>();
        for(int i = 0; i < 13; i++){
            ids.add(a[i]*10);
            statistics.add(new CombinedStatistic(new KeyStatistic(a[i])));
        }
        testTree.insertIDs(context, statistics, ids);
        ArrayList<CombinedStatistic> del_statistics = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            del_statistics.add(new CombinedStatistic(new KeyStatistic(a[i])));
        }
        testTree.deleteIDs(context, del_statistics, AbstractStatistic.Type.KEY);
    }

    static class FillObject {

        public long elapsedTime;
        public int fillFactor;

        FillObject(long elapsedTime,int fillFactor)
        {
            this.elapsedTime = elapsedTime;
            this.fillFactor = fillFactor;

        }
    }
}
