package networkcompression;

import networkcompression.compression.DefaultCompressor;
import networkcompression.compression.AsyncCompressor;
import networkcompression.runners.AsyncBaseTestRunner;
import networkcompression.tests.TestRunningTotalDumb;
import networkcompression.runners.AsyncTestRunner;
import networkcompression.runners.SyncTestRunner;
import networkcompression.tests.AsyncBaseTest;
import networkcompression.utils.Util;

import org.zkoss.zss.model.impl.sys.DependencyTableImplV4;
import org.zkoss.zss.model.impl.sys.DependencyTablePGImpl;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.impl.SheetImpl;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

/**
 *
 * How does testing work?
 *
 *      Each test is run by a particular "test runner". There are currently two types of test runners:
 *
 *          1. SyncTestRunner   : Runs a test case WITHOUT  asynchronous computation
 *          2. AsyncTestRunner  : Runs a test case WITH     asynchronous computation
 *
 *      A test runner can be configured with particular settings such as the type of compression and the type of
 *      scheduler. For example, the following call creates a synchronous test runner that does not prioritize cells
 *      or apply compression:
 *
 *          AsyncBaseTestRunner runner = new SyncTestRunner (false, new DefaultCompressor())
 *
 *      The following call creates an asynchronous test runner that prioritizes cells and applies the compression
 *      described in the Anti Freeze paper using a compression constant of 5:
 *
 *          AsyncBaseTestRunner runner = new AsyncTestRunner (true, new AsyncCompressor(5))
 *
 *      Each test runner can run any test case that extends AsyncBaseTest by calling the method `run(testCase)`.
 *
 * How can I run my own tests?
 *
 *      1. Be sure your database configurations are correct. If they aren't an error will be raised.
 *      2. Define the name of the directory to output testing data
 *      3. Define the number of runs to perform
 *      4. 
 *
 */
public class AsyncPerformanceMain {

    /******************************************************************************************************************
     * EDIT ZONE ******************************************************************************************************
     ******************************************************************************************************************/

    // For AsyncCompressor
    public static final boolean graphInDB   = false;

    // Database configurations
    public static final String  URL         = "jdbc:postgresql://127.0.0.1:5433/dataspread";
    public static final String  DBDRIVER    = "org.postgresql.Driver";
    public static final String  USERNAME    = "dataspreaduser";
    public static final String  PASSWORD    = "password";

    // The directory to write test reports to
    public static final Path    PATH        = Paths.get("REPORTS");

    // Number of runs to perform
    public static final int     RUNS        = 1;

    // Number of milliseconds to sleep after every runner
    public static final int     SLEEP       = 5000;

    // The test parameters to use
    public static final LinkedHashMap<String, AsyncBaseTestRunner> RUNNERS = Util.pairsToMap(
        Arrays.asList(
            Util.pair("runner0", new SyncTestRunner (false, new DefaultCompressor())),
            Util.pair("runner1", new AsyncTestRunner(false, new DefaultCompressor())),
            Util.pair("runner2", new AsyncTestRunner(false, new AsyncCompressor(2))),
            Util.pair("runner3", new AsyncTestRunner(false, new AsyncCompressor(20))),
            Util.pair("runner4", new AsyncTestRunner(true , new DefaultCompressor())),
            Util.pair("runner5", new AsyncTestRunner(true , new AsyncCompressor(2))),
            Util.pair("runner6", new AsyncTestRunner(true , new AsyncCompressor(20)))
        )
    );

    // The tests to perform
    public static final AsyncBaseTest[] TESTS = {
            new TestRunningTotalDumb(true, 10),
            new TestRunningTotalDumb(true, 10000)
    };

    // Defines the order of a single test run
    public static final LinkedHashMap<String, AsyncBaseTestRunner> RUN_ORDER = Util.pairsToMap(
        Arrays.asList(
            Util.pair("brn1"   , RUNNERS.get("runner0")),
            Util.pair("brn2"   , RUNNERS.get("runner1")),
            Util.pair("s"      , RUNNERS.get("runner0")),
            Util.pair("brn3"   , RUNNERS.get("runner1")),
            Util.pair("brn4"   , RUNNERS.get("runner1")),
            Util.pair("a"      , RUNNERS.get("runner1")),
            Util.pair("ac2"    , RUNNERS.get("runner2")),
            Util.pair("ac20"   , RUNNERS.get("runner3")),
            Util.pair("ap"     , RUNNERS.get("runner4")),
            Util.pair("ac2p"   , RUNNERS.get("runner5")),
            Util.pair("ac20p"  , RUNNERS.get("runner6"))
        )
    );

    /******************************************************************************************************************/

    private static void basicSetup () {
        EngineFactory.dependencyTableClazz = (graphInDB ? DependencyTablePGImpl.class : DependencyTableImplV4.class);
        SheetImpl.simpleModel = true;
        Util.createDirectory(PATH);
    }

    public static void main (String[] args) {
        AsyncPerformanceMain.basicSetup();
        for (int run = 0; run < RUNS; run++) {
            for (AsyncBaseTest test : TESTS) {
                for (Map.Entry<String, AsyncBaseTestRunner> entry : RUN_ORDER.entrySet()) {

                    // Setup
                    String runName = String.join("-", new String[]{ test.toString(), "run" + run });
                    AsyncBaseTestRunner runner = entry.getValue();
                    Path subDirectory = Paths.get(PATH.toString(), entry.getKey());
                    System.out.println(Util.getCurrentTime() + ": " + entry.getKey() + "-" + runName);

                    // Run the test
                    runner.run(test.duplicate(false));

                    // Output results
                    Util.createDirectory(subDirectory);
                    runner.metadata.writeStatsToFile(subDirectory.toString(), runName + ".txt");

                    // Clean up
                    System.gc();
                    Util.sleep(SLEEP);

                }
            }
        }
    }

}
