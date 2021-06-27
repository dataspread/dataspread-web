package DependencyTablePgImplCaching;

import DependencyTablePgImplCaching.Tests.TestRunner;
import DependencyTablePgImplCaching.Tests.BaseTest;

import org.zkoss.zss.model.impl.sys.DependencyTablePGImplCache;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.impl.SheetImpl;
import org.model.DBHandler;

import org.junit.runner.notification.Failure;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class Main {

    public static final String PSQL_URL = "jdbc:postgresql://127.0.0.1:5433/dataspread";
    public static final String DBDRIVER = "org.postgresql.Driver";
    public static final String USERNAME = "dataspreaduser";
    public static final String PASSWORD = "password";

    static {
        DBHandler.connectToDB(Main.PSQL_URL, Main.DBDRIVER, Main.USERNAME, Main.PASSWORD);
        EngineFactory.dependencyTableClazz = DependencyTablePGImplCache.class;
        BaseTest.makeCacheFieldsPublic();
        SheetImpl.simpleModel = true;
    }

    public static void main(String[] args) {

        // Run all tests
        Result result = JUnitCore.runClasses(TestRunner.class);

        // Print all test failures
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

        // Print a brief status report
        int numTestsFailed = result.getFailureCount();
        System.out.println(numTestsFailed == 0 ? "All tests passed!" : numTestsFailed + " test(s) failed.");

    }

}
