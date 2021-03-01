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

import java.time.LocalDateTime;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

/**
 *
 * Test Runners:
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
 *      Each test runner has a `run` method that executes any test case that extends AsyncBaseTest.
 *
 * Test cases:
 *
 *      To create a new test case, the following steps should be performed:
 *
 *          1. Create your new test in the tests directory
 *          2. Have the test extend AsyncBaseTest.java
 *          3. Implement the required methods
 *          4. Create your test in the TESTS variable of AsyncPerformanceMain.java
 *
 * Test execution:
 *
 *      The TESTS variable and SCHEDULE variable control how test cases are executed. The TESTS variable is an array
 *      that stores test cases. Each test will be run in the order you specify and should be initialized with their
 *      `isTemplate` parameters set to true so that the other constructor parameters you want to use are stored for
 *      later. The SCHEDULE variable maps strings to test runners. All runners in SCHEDULE will perform the current
 *      test before moving on to the next test. Each runner is executed in the order you specify. In pseudocode, this
 *      is basically equivalent to:
 *
 *          for test in TESTS:
 *              for name, runnner in SCHEDULE:
 *                  runner.run(test)
 *
 *      You can also control the number of times to repeat the loops above with the ROUNDS parameter:
 *
 *          for i = 1 to ROUNDS:
 *              for test in TESTS:
 *                  for name, runnner in SCHEDULE:
 *                      runner.run(test)
 *
 * Explanation of other variables:
 *
 *      RUNNERS     :   A map from string to test runner. This variable is used to define your test runners and give
 *                      them names so that they are easier to identify when initializing the SCHEDULE variable.
 *
 *      SLEEP       :   Controls the number of milliseconds to sleep after running a single test runner in SCHEDULE.
 *
 *      PATH        :   The name of the directory to write test reports to. This directory is automatically created
 *                      for you in the project's root folder.
 *
 *      graphInDB   :   If true, AsyncCompressor will assume that the spreadsheet formula network exists in a
 *                      PostgreSQL database and will issue SQL queries to perform dependency compression.
 *
 *      The rest of the variables are reserved for database configurations.
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

    // Number of testing rounds to perform
    public static final int     ROUNDS      = 1;

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

    // The order that runners should be executed. Keys should serve as valid directory names.
    public static final LinkedHashMap<String, AsyncBaseTestRunner> SCHEDULE = Util.pairsToMap(
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

        LocalDateTime start = LocalDateTime.now();

        AsyncPerformanceMain.basicSetup();
        for (int r = 0; r < ROUNDS; r++) {
            for (AsyncBaseTest test : TESTS) {
                for (Map.Entry<String, AsyncBaseTestRunner> entry : SCHEDULE.entrySet()) {

                    // Setup
                    String runName = String.join("-", new String[]{ test.toString(), "round" + r });
                    AsyncBaseTestRunner runner = entry.getValue();
                    Path subDirectory = Paths.get(PATH.toString(), entry.getKey());
                    System.out.println("\n" + Util.getCurrentTime() + ": " + entry.getKey() + "-" + runName + "\n");

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

        long seconds = Duration.between(start, LocalDateTime.now()).getSeconds();
        System.out.println("\nTime taken (HH:MM:SS): " +
                String.format("%02d:%02d:%02d"
                        , (seconds / 3600)
                        , (seconds % 3600) / 60
                        , (seconds % 60)
                )
        );

    }

}
