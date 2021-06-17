package ASyncTest;

import ASyncTest.compression.asynccomp.AsyncPgCompressor;
import ASyncTest.compression.asynccomp.AsyncCompressor;
import ASyncTest.compression.DefaultCompressor;
import ASyncTest.runners.AsyncBaseTestRunner;
import ASyncTest.runners.AsyncTestRunner;
import ASyncTest.runners.SyncTestRunner;
import ASyncTest.utils.Util;
import ASyncTest.tests.*;

import org.zkoss.zss.model.impl.sys.DependencyTableImplV4;
import org.zkoss.zss.model.impl.sys.DependencyTablePGImpl;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.impl.SheetImpl;

import java.time.LocalDateTime;
import java.time.Duration;

import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * Test Runners:
 *
 *      A test runner is an object that executes test cases. There are currently two types of test runners:
 *
 *          1. SyncTestRunner   : Runs test cases WITHOUT  asynchronous computation
 *          2. AsyncTestRunner  : Runs test cases WITH     asynchronous computation
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
 *      Each test runner has a `run` method which can execute any test case that extends AsyncBaseTest.
 *
 * Test cases:
 *
 *      To create a new test case, the following steps should be performed:
 *
 *          1. Create your new test in the tests directory
 *          2. Have the test extend AsyncBaseTest
 *          3. Implement the required methods
 *          4. Create your test in the TESTS variable of AsyncPerformanceMain.java
 *
 *      It is important that test cases define two constructors. One constructor should be public and initialize all
 *      test parameters except the test book. The other constructor should be private and initialize all members with
 *      the test book. If this convention is upheld (and all the previous steps have been completed), you shouldn't
 *      need to make any changes to main().
 *
 * Test execution:
 *
 *      The TESTS variable and SCHEDULE variable control how test cases are executed. The TESTS variable is an array
 *      that stores test cases. Each test will be run in the order you specify and should be initialized with all
 *      necessary parameters except the test book (book creation is handled later in main() by the newTest() method).
 *      The SCHEDULE variable maps strings to test runners. All runners in SCHEDULE will run the current test before
 *      moving on to the next test. Each runner is executed in the order you specify. In pseudocode, this is basically
 *      equivalent to:
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
 *      OUT_PATH    :   The name of the directory to write test reports to. This directory is automatically created
 *                      for you in the project's root folder.
 *
 *      The rest of the variables are reserved for database configurations and setting the dependency table
 *      implementation.
 *
 */
public class AsyncPerformanceMain {

    /******************************************************************************************************************
     * EDIT ZONE ******************************************************************************************************
     ******************************************************************************************************************/

    // The dependency table implementation below will be used for all compressors except AsyncPgCompressor. If a runner
    // uses AsyncPgCompressor, the dependency table implementation will automatically be set to DependencyTablePGImpl.
    public static final Class<?>    DEPENDENCY_TABLE_IMPLEMENTATION = DependencyTableImplV4.class;

    public static final String      URL         = "jdbc:postgresql://127.0.0.1:5433/dataspread";
    public static final String      DBDRIVER    = "org.postgresql.Driver";
    public static final String      USERNAME    = "dataspreaduser";
    public static final String      PASSWORD    = "password";
    public static final Path        OUT_PATH    = Paths.get("..", "REPORTS");
    public static final int         ROUNDS      = 10;
    public static final int         SLEEP       = 5000;

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

    // List the factories of the tests needed
    public static final AsyncTestFactory[] TESTS = {
            //TestRealWorldSheet.getFactory(Paths.get("..", "EXCEL", "sample.xlsx")),
            TestRunningTotalSlow.getFactory(3000),
    };

    // Keys should also be valid directory names
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
        SheetImpl.simpleModel = true;
        Util.createDirectory(OUT_PATH);
    }

    private static void setDependencyTableImpl (AsyncBaseTestRunner runner) {
        if (runner.COMPRESSOR instanceof AsyncPgCompressor) {
            EngineFactory.dependencyTableClazz = DependencyTablePGImpl.class;
        } else {
            EngineFactory.dependencyTableClazz = DEPENDENCY_TABLE_IMPLEMENTATION;
        }
    }

    public static void main (String[] args) {
        LocalDateTime start = LocalDateTime.now();
        AsyncPerformanceMain.basicSetup();
        for (int r = 0; r < ROUNDS; r++) {
            for (AsyncTestFactory testFactory : TESTS) {
                for (Map.Entry<String, AsyncBaseTestRunner> entry : SCHEDULE.entrySet()) {

                    // Setup
                    String runName = String.join("-", new String[]{ testFactory.toString(), "round" + r });
                    AsyncBaseTestRunner runner = entry.getValue();
                    AsyncPerformanceMain.setDependencyTableImpl(runner);
                    System.out.println("\n" + Util.getCurrentTime() + ": " + entry.getKey() + "-" + runName);

                    // Run the test
                    System.out.println(Util.getCurrentTime() + ": Running test...");
                    runner.run(testFactory.createTest());
                    System.out.println(Util.getCurrentTime() + ": Done!");

                    // Output results
                    Path subDirectory = Paths.get(OUT_PATH.toString(), entry.getKey());
                    runner.dumpMetadata(subDirectory, Paths.get(runName + ".txt"));
                    System.out.println(Util.getCurrentTime() + ": Results logged to " + subDirectory);

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
