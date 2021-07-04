package DependencyTablePgImplCaching.Tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
    RunningSumTest.class,
    LongDependentChainTest.class
})
public class TestRunner {
}
