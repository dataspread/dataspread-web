import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.poi.ss.formula.functions.Count;
import org.zkoss.zss.model.impl.CountedBTree;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class BTreePerformance {
    private final static int iterations = 100;
    private final static int split_count = 20000;
    private static Logger logger = Logger.getLogger(BTreePerformance.class.getName());
    /*Test data*/
    private static int[] max_rows = {
            //  100000000,
            1000,
            10000,
            100000,
            1000000,
            10000000
    };
    private static String tableName = "pos_";
    public static void main(String[] args) {
        logger.info("Testing started");
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "mangesh";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());
        //max_row = Integer.parseInt(args[0]);
        //Random random = new Random(Integer.parseInt(args[1]));
        Random random = new Random(55);
        int[] pos = new int[iterations];
        for (int max_row : max_rows) {
            // pos[0] = max_row - 10;
            for (int i = 0; i < iterations; ++i) {
                pos[i] = random.nextInt(max_row - 10);
            }
            random.setSeed(55);
            CountedBTree testTree = new CountedBTree(dbContext, "dense_backward_" + max_row, true, true);
            testTree.setBlockSize(100);

            // Data Creation
            preGenerate(dbContext, testTree, max_row);
            // testTree.createIDs(dbContext, 0, max_row);
            dbContext.getConnection().commit();

            logger.info(testTree.getTableName() + " Creation Done");
            // Perform Test
            testUpdateOperations(dbContext, testTree, pos);
        }
        logger.info("All Tests Done");
        dbContext.getConnection().commit();
        dbContext.getConnection().close();
    }
    private static void preGenerate(DBContext dbContext, CountedBTree mapping, int max_row) {
        logger.info(mapping.getTableName() + " Creation Start");
        long start;
        int split = max_row / split_count;
        for (int i = 0; i < split; i++) {
            start = System.currentTimeMillis();
            mapping.createDenseIDs(dbContext, split_count * i, split_count);
            logger.info(mapping.getTableName() + " Creation " + ((i + 1) * 100 / split) + "%" + " in " + (System.currentTimeMillis() - start) + " ms");
        }
        mapping.createDenseIDs(dbContext, split_count * split, max_row - split_count * split);
        logger.info(mapping.getTableName() + " Creation Done");
    }
    private static void addOperation(DBContext context, CountedBTree mapping, int[] pos) {
        long startTime, endTime, cacheTime;
        cacheTime = 0;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            mapping.createDenseIDs(context, pos[i], 10);
            context.getConnection().commit();
            cacheTime -= System.currentTimeMillis();
            mapping.clearCache(context);
            cacheTime += System.currentTimeMillis();
        }
        endTime = System.currentTimeMillis();
        logger.info(mapping.getTableName() + " Insert IDs at random position Time taken(ms) = "
                + (((double) (endTime - startTime - cacheTime)) / iterations));
        mapping.clearCache(context);
    }
    private static void deleteOperation(DBContext context, CountedBTree mapping, int[] pos) {
        long startTime, endTime, cacheTime;
        cacheTime = 0;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            mapping.deleteIDs(context, pos[i], 10);
            context.getConnection().commit();
            cacheTime -= System.currentTimeMillis();
            mapping.clearCache(context);
            cacheTime += System.currentTimeMillis();
        }
        endTime = System.currentTimeMillis();
        logger.info(mapping.getTableName() + " Delete IDs at random position Time taken(ms) = "
                + (((double) (endTime - startTime - cacheTime)) / iterations));
        mapping.clearCache(context);
    }
    private static void getOperation(DBContext context, CountedBTree mapping, int[] pos) {
        long startTime, endTime, cacheTime;
        cacheTime = 0;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            mapping.getIDs(context, pos[i], 10);
            cacheTime -= System.currentTimeMillis();
            mapping.clearCache(context);
            cacheTime += System.currentTimeMillis();
        }
        endTime = System.currentTimeMillis();
        logger.info(mapping.getTableName() + " Get IDs at random position Time taken(ms) = "
                + (((double) (endTime - startTime - cacheTime)) / iterations));
    }
    private static void testUpdateOperations(DBContext context, CountedBTree mapping, int[] pos) {
        logger.info("Table name = " + mapping.getTableName());
        addOperation(context, mapping, pos);
        getOperation(context, mapping, pos);
        deleteOperation(context, mapping, pos);
    }
}