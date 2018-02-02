package testformula;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.impl.BTree;
import org.zkoss.zss.model.impl.CountedBTree;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;
import org.zkoss.zss.model.impl.statistic.CombinedStatistic;
import org.zkoss.zss.model.impl.statistic.CountStatistic;
import org.zkoss.zss.model.impl.statistic.KeyStatistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class SparseIndexTest {
    public static void main(String[] args) {

    }
    public static void deepTest() {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "mangesh";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());

    }
    public static void simple(DBContext context){
        CountedBTree btree = new CountedBTree(context, "simpleTest", false);
        btree.setBlockSize(5);
        btree.createIDs(context, 0, 10);
        btree.createIDs(context,3, 1);
        System.out.println(btree.getIDs(context, 0, 10));
    }
    public static void testSparseRootInsDelByCount(DBContext context) {
        CountedBTree testTree = new CountedBTree(context, "simpleTest", false);
        testTree.setBlockSize(5);

        testTree.createIDs(context, 0, 1);
        //Integer test = testTree.lookup(context, key, AbstractStatistic.Type.COUNT);
        testTree.deleteIDs(context, 0, 1);

        testTree.createIDs(context, 0, 1);
        testTree.createIDs(context, 1, 1);
        testTree.createIDs(context, 1, 1);
        testTree.createIDs(context, 1, 1);


    }

    public static void testRootSplitByCount(DBContext context) {

        for(int i = 0; i < 3; i++){
            String testName = "testRootSplit"+i;
            CountedBTree testTree = new CountedBTree(context, testName, false);
            testTree.setBlockSize(5);
            testTree.createIDs(context, 0 , 4);
            testTree.createIDs(context, i, 1);
        }

    }

    public static void testSplitNodeByCount(DBContext context) {
        CountedBTree testTree = new CountedBTree(context, "SparseTSNBC", false);
        testTree.setBlockSize(5);
        testTree.createIDs(context, 0, 9);
        testTree.createIDs(context, 0, 1);
        testTree.createIDs(context, 3, 1);
        testTree.createIDs(context, 5, 1);
        testTree.createIDs(context, 7, 1);
        testTree.createIDs(context, 9, 1);
        testTree.createIDs(context, 11, 1);
        testTree.createIDs(context, 13, 1);
        testTree.createIDs(context, 16, 1);

    }

    public static void testSplitNodeSplitParentByCount(DBContext context) {
        int[] a = {1, 8, 16};
        int[] aa = {0, 2, 4};
        for(int i = 0; i < 3; i++){
            CountedBTree testTree = new CountedBTree(context, "tSNSPBC"+i, false);
            testTree.setBlockSize(5);
            testTree.createIDs(context, 0, 9);
            testTree.createIDs(context, 0, 1);
            testTree.createIDs(context, 3, 1);
            testTree.createIDs(context, 5, 1);
            testTree.createIDs(context, 7, 1);
            testTree.createIDs(context, 9, 1);
            testTree.createIDs(context, 11, 1);
            testTree.createIDs(context, 13, 1);
            testTree.createIDs(context, aa[i], 1);

        }
    }

    public static void testNodeMergeByCount(DBContext context) {
        String testName = "SparsetNMBC";
        CountedBTree testTree = new CountedBTree(context, testName, false);
        testTree.setBlockSize(5);
        testTree.createIDs(context, 0, 6);
        testTree.deleteIDs(context, 1, 1);

    }

    public static void NodeMergeRootMergeByCount(DBContext context) {
        String testName = "SparseNMRMBC";
        CountedBTree testTree = new CountedBTree(context, testName, false);
        testTree.setBlockSize(5);
        testTree.createIDs(context, 0, 13);
        testTree.deleteIDs(context, 0, 1);

    }

    public static void NodeMergeRootMerge1ByCount(DBContext context) {
        String testName = "SparseNMRM1BC";
        CountedBTree testTree = new CountedBTree(context, testName, false);
        testTree.setBlockSize(5);
        testTree.createIDs(context, 0, 19);
        testTree.deleteIDs(context, 0, 1);
        testTree.deleteIDs(context, 13, 1);
        testTree.deleteIDs(context, 16, 1);


    }
}
