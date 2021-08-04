package FormulaCompressionTest;

import FormulaCompressionTest.runners.BaseTestRunner;
import FormulaCompressionTest.runners.AsyncTestRunner;
import FormulaCompressionTest.runners.SyncTestRunner;
import FormulaCompressionTest.tests.testmaintenance.TestDelete;
import FormulaCompressionTest.tests.testmaintenance.TestRefreshCache;
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
    public static String configPath = "../config.properties";

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
    public static String[]    testArgs = null;

    /**
     * CONSTANTS, Not configured by users
     * */
    public static final int         ASYNC_COMPRESS_CONSTANT = 1;

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
            case "comp":
                EngineFactory.dependencyTableClazz = DependencyTableComp.class;
                break;
            case "async":
                EngineFactory.dependencyTableClazz = DependencyTableASync.class;
                break;
            default:
                System.out.println("Dependency table class " + depTableString
                        + " is not supported yet");
                System.exit(-1);
        }
    }

    // TODO: add more test cases and make all test cases configurable
    private static void genTestCase(String sheetString,
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
            case "delete":
                oneTest = new TestDelete(Integer.parseInt(testArgs[0]));
                break;
            case "refreshcache":
                oneTest = new TestRefreshCache(Integer.parseInt(testArgs[0]), Integer.parseInt(testArgs[1]));
                break;
            default:
                System.out.println("Spreadsheet " + sheetString
                        + " is not supported yet");
                System.exit(-1);
        }

        oneTest.configDepTable(depTblCacheSize, compConstant);
    }

    private static void readConfig() {
        try (InputStream input = new FileInputStream(configPath)) {
            Properties defultProperties = new Properties();
            defultProperties.load(input);

            Properties systemProperties = System.getProperties();

            url = systemProperties.getProperty("url", defultProperties.getProperty("url"));
            dbDriver = systemProperties.getProperty("dbDriver", defultProperties.getProperty("dbDriver"));
            userName = systemProperties.getProperty("username", defultProperties.getProperty("username"));
            password = systemProperties.getProperty("password", defultProperties.getProperty("password"));

            outFolder = systemProperties.getProperty("outFolder", defultProperties.getProperty("outFolder"));

            useSyncRunner = systemProperties.getProperty("useSyncRunner", defultProperties.getProperty("useSyncRunner"))
                    .toLowerCase().equals("true");
            depTableClassString = systemProperties.getProperty("depTableClassString",
                    defultProperties.getProperty("depTableClassString"));
            depTableCacheSize = Integer.parseInt(systemProperties.getProperty("depTableCacheSize",
                    defultProperties.getProperty("depTableCacheSize")));
            spreadsheetString = systemProperties.getProperty("spreadsheetString",
                    defultProperties.getProperty("spreadsheetString"));
            testArgs = new String[Integer.parseInt(systemProperties.getProperty("numTestArgs",
                    defultProperties.getProperty("numTestArgs")))];
            for (int i = 0; i < testArgs.length; i++) {
                String key = "testArg." + i;
                testArgs[i] = systemProperties.getProperty(key, defultProperties.getProperty(key));
            }

        } catch (Exception e) {
            System.out.println("Error trying to open config file at " +
                    new File(configPath).getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static void main (String[] args) {

        if (args.length != 1) {
            System.out.println("Wrong argument number; We need a configuration file");
            System.exit(-1);
        }
        configPath = args[0];
        readConfig();

        CompressionTestMain.basicSetup(outFolder);
        CompressionTestMain.configTestRunner(useSyncRunner, outFolder);
        CompressionTestMain.configDependencyTable(depTableClassString);
        CompressionTestMain.genTestCase(spreadsheetString,
                depTableCacheSize, ASYNC_COMPRESS_CONSTANT);

        // Run the test
        System.out.println(Util.getCurrentTime() + ": Running test...");
        testRunner.run(oneTest);
        System.out.println(Util.getCurrentTime() + ": Done!");

        testRunner.collectConfigInfo(useSyncRunner, depTableClassString,
                testArgs, depTableCacheSize);

        testRunner.dumpStatdata();

    }

}
