package FormulaCompressionTest;

import FormulaCompressionTest.runners.BaseTestRunner;
import FormulaCompressionTest.runners.AsyncTestRunner;
import FormulaCompressionTest.runners.SyncTestRunner;
import FormulaCompressionTest.utils.Util;
import FormulaCompressionTest.tests.*;

import org.zkoss.zss.model.impl.sys.DependencyTablePGImpl;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.impl.SheetImpl;

import java.nio.file.Paths;
import java.nio.file.Path;

public class CompressionTestMain {

    /******************************************************************************************************************
     * CONFIG ZONE ******************************************************************************************************
     ******************************************************************************************************************/

    /**
     * DB Connection Configuration
     * */
    public static String      url         = "jdbc:postgresql://127.0.0.1:5433/dbtest";
    public static String      dbDriver    = "org.postgresql.Driver";
    public static String      userName    = "totemtang";
    public static String      password    = "1234";

    /**
     * Stats Output Folder
     * */
    public static String      outFolder   = "/home/totemtang/dataspread/REPORTS";

    /**
     * Test method configuration
     * */
    public static boolean     useSyncRunner  = false;
    public static String      depTableClassString = "PGImpl";
    public static int         depTableCacheSize = 5000;

    /**
     * Workload configuration
     * */
    public static String      spreadsheetString = "RunningTotalSlow";
    public static String      spreadsheetOperation = "LongestChainUpdate";

    /**
     * CONSTANTS, Not configured by users
     * */
    public static final int         ASYNC_COMPRESS_CONSTANT = 20;

    /**
     * Variables set based on the configuration
     * */
    public static BaseTestRunner testRunner = null;
    public static BaseTest oneTest = null;

    private static void basicSetup (String outFolder) {
        SheetImpl.simpleModel = true;
        Path outputPath = Paths.get(outFolder);
        Util.createDirectory(outputPath);
    }

    private static void configTestRunner (boolean useSyncRunner, String outFolder) {
        if (useSyncRunner) testRunner = new SyncTestRunner();
        else testRunner = new AsyncTestRunner();

        testRunner.setStatsOutFolder(outFolder);
    }

    // TODO: add more dependencyTable options
    private static void configDependencyTable (String depTableString) {
        if (depTableString.compareToIgnoreCase("PGImpl") == 0) {
            EngineFactory.dependencyTableClazz = DependencyTablePGImpl.class;
        } else {
            System.out.println("Dependency table class " + depTableString
                    + " is not supported yet");
            System.exit(-1);
        }
    }

    // TODO: add more test cases and make all test cases configurable
    private static void genTestCase(String sheetString,
                                    String sheetOperation,
                                    int depTblCacheSize,
                                    int compConstant) {
        if (sheetString.compareToIgnoreCase("RunningTotalSlow") == 0) {
            oneTest = new TestRunningTotalSlow(3000);
        } else {
            System.out.println("Spreadsheet " + sheetString
                    + " is not supported yet");
            System.exit(-1);
        }

        oneTest.configDepTable(depTblCacheSize, compConstant);
        oneTest.setTestOperation(sheetOperation);
    }

    public static void main (String[] args) {

        // Setup
        CompressionTestMain.basicSetup(outFolder);
        CompressionTestMain.configTestRunner(useSyncRunner, outFolder);
        CompressionTestMain.configDependencyTable(depTableClassString);
        CompressionTestMain.genTestCase(spreadsheetString, spreadsheetOperation,
                depTableCacheSize, ASYNC_COMPRESS_CONSTANT);

        // Run the test
        System.out.println(Util.getCurrentTime() + ": Running test...");
        testRunner.run(oneTest);
        System.out.println(Util.getCurrentTime() + ": Done!");

        testRunner.dumpStatdata();

    }

}
