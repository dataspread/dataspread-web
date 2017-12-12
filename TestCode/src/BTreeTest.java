import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.impl.BTree;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;
import org.zkoss.zss.model.impl.statistic.CombinedStatistic;
import org.zkoss.zss.model.impl.statistic.CountStatistic;
import org.zkoss.zss.model.impl.statistic.KeyStatistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class BTreeTest {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "mangesh";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());
        KeyStatistic<Integer> key = new KeyStatistic<>(0);
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key);
        BTree btree = new BTree<CombinedStatistic<Integer>, Integer>(dbContext, "Test1", emptyStatistic);
        btree.useKryo(false);
        ArrayList<Integer> arrayList = new ArrayList<>();

        // Random operations
        final int operations=5;
        Random random = new Random(1);
        Random randomOperation = new Random(1);
        int stats_add =0;
        int stats_remove =0;

        // Add initial data points.
        for (int i=0;i<operations;i++)
        {
            stats_add++;
            int randomValue = random.nextInt();
            ArrayList<Integer> insertList = new ArrayList<>();
            insertList.add(randomValue);
            btree.insertIDs(dbContext, i, insertList);
            dbContext.getConnection().commit();
            arrayList.add(i,randomValue);
        }

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

        ArrayList<CombinedStatistic<Integer>> lookup_statistics = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            CountStatistic count = new CountStatistic(i);
            CombinedStatistic<Integer> statistic = new CombinedStatistic<>(key, count);
            lookup_statistics.add(statistic);
        }

        if (btree.size(dbContext) == arrayList.size()
                && Arrays.asList((btree.getIDsCombined(dbContext, lookup_statistics, AbstractStatistic.Type.COUNT)))
                .equals(arrayList))
            System.out.println("Results Match");
        else
            System.err.println("Results do not match");

        dbContext.getConnection().commit();
        dbContext.getConnection().close();
    }
    public static void testRootInsDelByCount(DBContext context)throws Exception{

        String tableName = "testRootInsDelByCount";
        KeyStatistic<Integer> key = new KeyStatistic<>(0);
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key);
        BTree testTree = new BTree<CombinedStatistic<Integer>, Integer>(context, tableName, emptyStatistic);


        testTree.add(context, key, 10, false, AbstractStatistic.Type.COUNT);
        //Integer test = testTree.lookup(context, key, AbstractStatistic.Type.COUNT);
        testTree.remove(context, key, false, AbstractStatistic.Type.COUNT);

        testTree.add(context, key, 20, false, AbstractStatistic.Type.COUNT);
        //test = testTree.getByCount(context, 0);
        KeyStatistic<Integer> key1 = new KeyStatistic<>(1);
        testTree.add(context, key1, 10, false, AbstractStatistic.Type.COUNT);
        //test = testTree.getByCount(context, 2);
        testTree.remove(context, key, false, AbstractStatistic.Type.COUNT);
        testTree.remove(context, key, false, AbstractStatistic.Type.COUNT);


    }

    public static void testRootSplitByCount(DBContext context)throws Exception{

        int[] a = {5, 25, 50};
        int[] rootids = {0, 0, 0};
        boolean valid;
        ArrayList<KeyStatistic<Integer>> key = new ArrayList<>();
        for(int x = 0; x < 6; x++) {
            key.add(new KeyStatistic<>(x));
        }
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key.get(0));
        for(int i = 0; i < 3; i++){
            String testName = "testRootSplit"+i;
            BTree testTree = new BTree<CombinedStatistic<Integer>, Integer>(context, testName, emptyStatistic);
            testTree.add(context, key.get(0), 100, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(1), 200, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(2), 300, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(3), 400, false, AbstractStatistic.Type.COUNT);

            testTree.add(context, key.get(i*2), a[i]*10, false,AbstractStatistic.Type.COUNT);
        }

    }

    public static void testSplitNodeByCount(DBContext context)throws Exception{
        ArrayList<KeyStatistic<Integer>> key = new ArrayList<>();
        for(int x = 0; x < 16; x++) {
            key.add(new KeyStatistic<>(x));
        }
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key.get(0));
        BTree testTree = new BTree<CombinedStatistic<Integer>, Integer>(context, "tSNBC", emptyStatistic);

        testTree.add(context, key.get(0), 50, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(1), 100, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(2), 200, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(3), 250, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(4), 300, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(5), 400, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(6), 500, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(7), 600, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(8), 700, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(9), 800, false, AbstractStatistic.Type.COUNT);

        testTree.add(context, key.get(0), 30, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(3), 150, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(5), 230, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(7), 270, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(9), 350, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(11), 450, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(13), 550, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(16), 800, false, AbstractStatistic.Type.COUNT);

    }

    public static void testSplitNodeSplitParentByCount(DBContext context)throws Exception{
        ArrayList<KeyStatistic<Integer>> key = new ArrayList<>();
        for(int x = 0; x < 16; x++) {
            key.add(new KeyStatistic<>(x));
        }
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key.get(0));

        int[] a = {1, 8, 16};
        int[] aa = {3, 8, 17};
        boolean valid;
        for(int i = 0; i < 3; i++){
            BTree testTree = new BTree<CombinedStatistic<Integer>, Integer>(context, "tSNSPBC"+i, emptyStatistic);
            testTree.add(context, key.get(0), 50, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(1), 100, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(2), 200, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(3), 250, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(4), 300, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(5), 400, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(6), 500, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(7), 600, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(8), 700, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(9), 800, false, AbstractStatistic.Type.COUNT);

            testTree.add(context, key.get(0), 30, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(3), 150, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(5), 230, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(7), 270, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(9), 350, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(11), 450, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(13), 550, false, AbstractStatistic.Type.COUNT);
            testTree.add(context, key.get(16), 800, false, AbstractStatistic.Type.COUNT);

            testTree.add(context, key.get(aa[i]), a[i]*10, false, AbstractStatistic.Type.COUNT);

        }
    }

    public static void testNodeMergeByCount(DBContext context) throws Exception {
        ArrayList<KeyStatistic<Integer>> key = new ArrayList<>();
        for(int x = 0; x < 6; x++) {
            key.add(new KeyStatistic<>(x));
        }
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key.get(0));



        BTree testTree = new BTree<CombinedStatistic<Integer>, Integer>(context, "tNMBC", emptyStatistic);
        testTree.add(context, key.get(0), 100, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(1), 200, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(2), 300, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(3), 400, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(4), 500, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(5), 600, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(6), 700, false, AbstractStatistic.Type.COUNT);
        testTree.remove(context, key.get(2), false, AbstractStatistic.Type.COUNT);

    }

    public static void NodeMergeRootMergeByCount(DBContext context) throws Exception {
        ArrayList<KeyStatistic<Integer>> key = new ArrayList<>();
        for(int x = 0; x < 12; x++) {
            key.add(new KeyStatistic<>(x));
        }
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key.get(0));



        BTree testTree = new BTree<CombinedStatistic<Integer>, Integer>(context, "NMRNBC", emptyStatistic);
        testTree.add(context, key.get(0), 50, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(1), 100, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(2), 200, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(3), 230, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(4), 270, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(5), 300, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(6), 330, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(7), 400, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(8), 500, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(9), 550, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(10), 700, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(11), 800, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(12), 850, false, AbstractStatistic.Type.COUNT);
        testTree.remove(context, key.get(0), false, AbstractStatistic.Type.COUNT);

    }

    public static void NodeMergeRootMerge1ByCount(DBContext context) throws Exception {
        ArrayList<KeyStatistic<Integer>> key = new ArrayList<>();
        for(int x = 0; x < 18; x++) {
            key.add(new KeyStatistic<>(x));
        }
        CombinedStatistic<Integer> emptyStatistic = new CombinedStatistic<>(key.get(0));

        // left rotate
        BTree testTree = new BTree<CombinedStatistic<Integer>, Integer>(context, "NMRMBC", emptyStatistic);
        testTree.add(context, key.get(0), 50, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(1), 100, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(2), 200, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(3), 230, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(4), 270, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(5), 300, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(6), 330, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(7), 400, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(8), 500, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(9), 550, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(10), 700, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(11), 800, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(12), 850, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(13), 900, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(14), 950, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(15), 1000, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(16), 1050, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(17), 1100, false, AbstractStatistic.Type.COUNT);
        testTree.add(context, key.get(18), 1150, false, AbstractStatistic.Type.COUNT);
        testTree.remove(context, key.get(0), false, AbstractStatistic.Type.COUNT);
        testTree.remove(context, key.get(13), false, AbstractStatistic.Type.COUNT);
        testTree.remove(context, key.get(16), false, AbstractStatistic.Type.COUNT);


    }


}
