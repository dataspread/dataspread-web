package FormulaCompressionTest;

import FormulaCompressionTest.runners.BaseTestRunner;
import FormulaCompressionTest.runners.AsyncTestRunner;
import FormulaCompressionTest.runners.SyncTestRunner;
import FormulaCompressionTest.utils.Util;
import FormulaCompressionTest.tests.*;

import org.zkoss.zss.model.impl.sys.*;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.impl.SheetImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

public class CompressionTestMain {

    /******************************************************************************************************************
     * CONFIG ZONE ******************************************************************************************************
     ******************************************************************************************************************/

    /**
     * Config File Path
     */
    public static final String configPath = "../config.properties";

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
    public static String[]    testArgs = null;

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
        switch(depTableString.toLowerCase()) {
            case "pgimpl":
                EngineFactory.dependencyTableClazz = DependencyTablePGImpl.class;
                break;
            case "implv":
                EngineFactory.dependencyTableClazz = DependencyTableImpl.class;
                break;
            case "implv2":
                EngineFactory.dependencyTableClazz = DependencyTableImplV2.class;
                break;
            case "implv4":
                EngineFactory.dependencyTableClazz = DependencyTableImplV4.class;
                break;
            case "comp":
                EngineFactory.dependencyTableClazz = DependencyTableComp.class;
                break;
            default:
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
        switch (sheetString.toLowerCase()) {
            case "runningtotalslow":
                oneTest = new TestRunningTotalSlow(Integer.parseInt(testArgs[0]));
                break;
            case "runningtotalfast":
                oneTest = new TestRunningTotalFast(Integer.parseInt(testArgs[0]));
                break;
            case "rate":
                oneTest = new TestRate(Integer.parseInt(testArgs[0]));
                break;
            case "random":
                oneTest = new TestRandom(Integer.parseInt(testArgs[0]));
                break;
            case "customsheet":
                oneTest = new TestCustomSheet(Paths.get(testArgs[0]));
                break;
            case "expschedule":
                oneTest = new TestExpSchedule(Integer.parseInt(testArgs[0]), Integer.parseInt(testArgs[1]));
                break;
            default:
                System.out.println("Spreadsheet " + sheetString
                        + " is not supported yet");
                System.exit(-1);
        }

        oneTest.configDepTable(depTblCacheSize, compConstant);
        oneTest.setTestOperation(sheetOperation);
    }

    private static void readConfig() {
        try (InputStream input = new FileInputStream(configPath)) {
            Properties config = new Properties();
            config.load(input);

            url = config.getProperty("url");
            dbDriver = config.getProperty("dbDriver");
            userName = config.getProperty("username");
            password = config.getProperty("password");

            outFolder = config.getProperty("outFolder");

            useSyncRunner = config.getProperty("useSyncRunner").toLowerCase().equals("true");
            depTableClassString = config.getProperty("depTableClassString");
            depTableCacheSize = Integer.parseInt(config.getProperty("depTableCacheSize"));

            spreadsheetString = config.getProperty("spreadsheetString");
            spreadsheetOperation = config.getProperty("spreadsheetOperation");
            testArgs = new String[] {config.getProperty("testarg1"), config.getProperty("testarg2")};
        } catch (Exception e) {
            System.out.println("Error trying to open config file at " +
                    new File(configPath).getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static void main (String[] args) {

        // Setup
        readConfig();
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
