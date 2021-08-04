package FormulaCompressionTest.runners;

import FormulaCompressionTest.tests.BaseTest;

import FormulaCompressionTest.utils.Util;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerTesting;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The parent class for all Async test runners.
 */
public abstract class BaseTestRunner implements FormulaAsyncListener {

    public TestStats testStats;
    private Set<CellRegion> cellsToUpdateSet;
    private boolean testStarted;
    private String statsOutFolder;

    private final boolean PRIORITIZE = true;

    public BaseTestRunner() {}

    public void setStatsOutFolder(String outputFolder) {
        this.statsOutFolder = outputFolder;
    }

    /**
     * Any setup steps that should be performed for all subclasses
     * should be placed here.
     *
     * @param testCase
     */
    protected void runSetup(final BaseTest testCase) {
        CellImpl.disableDBUpdates = false;
        this.runBefore(testCase);
        FormulaAsyncScheduler.getScheduler().updateVisibleMap(new HashMap<>());
        testCase.getSheet().setSyncComputation(true);
        testCase.init();
        testStats.addBatchTime = testCase.getLastAddBatchTime();
        this.runAfterInit(testCase);
        testCase.genCellsToUpdate(this.cellsToUpdateSet, testStats);
        DirtyManagerLog.instance.init();
        CellImpl.disableDBUpdates = true;
    }

    /**
     * Any testing steps that should be performed for all subclasses
     * should be placed here.
     *
     * @param testCase
     */
    protected void runTest(final BaseTest testCase) {
        this.testStarted = true;
        this.testStats.updateCellStartTime = System.currentTimeMillis();
        testCase.updateCell();
        this.testStats.updateCellFinalTime = System.currentTimeMillis();
        this.testStats.getDependentsTime = testCase.getLastLookupTime();
        FormulaAsyncScheduler.getScheduler().start();
        this.runAfterUpdate(testCase);
        testCase.execAfterUpdate();
        this.testStats.refreshCacheTime = testCase.getLastRefreshCacheTime();
        testCase.touchAll();
        this.testStats.touchedTime = System.currentTimeMillis();
        this.testStats.isCorrect = testCase.verify();
        this.runAfter(testCase);
    }

    /**
     * This method will be run before the default setup steps have
     * been performed.
     *
     * @param testCase
     */
    protected void runBefore(final BaseTest testCase) {
    }

    /**
     * This method will be run directly after sheet initialization
     * occurs.
     *
     * @param testCase
     */
    protected void runAfterInit(final BaseTest testCase) {
    }

    /**
     * This method will be run after the cell has been updated.
     *
     * @param testCase
     */
    protected void runAfterUpdate(final BaseTest testCase) {
    }

    /**
     * This method will be run after the default testing steps have
     * been performed.
     *
     * @param testCase
     */
    protected void runAfter(final BaseTest testCase) {
    }

    /**
     * Runs the given test case.
     *
     * @param testCase
     */
    public void run(final BaseTest testCase) {
        try {

            // Setup async scheduler
            this.reset();
            testStats.testCase = testCase.toString();

            DirtyManager.dirtyManagerInstance.reset();
            FormulaAsyncSchedulerTesting.initScheduler();
            Thread asyncThread = new Thread(FormulaAsyncScheduler.getScheduler());
            asyncThread.start();
            FormulaAsyncScheduler.getScheduler().setFormulaAsyncListener(this);
            FormulaAsyncScheduler.getScheduler().setPrioritize(this.PRIORITIZE);

            // Perform the test
            this.testStats.testStartTime = System.currentTimeMillis();
            this.runSetup(testCase);
            this.runTest(testCase);
            testCase.cleanup();
            this.testStats.testFinalTime = System.currentTimeMillis();

            // Wait for the test to finish
            FormulaAsyncScheduler.getScheduler().shutdown();
            while (!FormulaAsyncScheduler.getScheduler().isShutdownCompleted()) {
                Thread.sleep(10);
            }
            FormulaAsyncScheduler.getScheduler().reset();
            asyncThread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void dumpStatdata() {
        this.testStats.writeStatsToFile(statsOutFolder);
    }

    public void collectConfigInfo(boolean isSyncRunner,
                                  String   dependencyTableClass,
                                  String[] testArgs,
                                  int cacheSize) {
        this.testStats.isSyncRunner = isSyncRunner;
        this.testStats.dependencyTableClass = dependencyTableClass;
        this.testStats.testArgs = testArgs;
        this.testStats.cacheSize = cacheSize;
    }

    private void reset() {
        this.testStats = new TestStats();
        cellsToUpdateSet = new HashSet<>();
        testStarted = false;
    }

    @Override
    public void update(SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula) {
        if (this.testStarted) {
            this.testStats.updatedCells++;
            this.cellsToUpdateSet.remove(cellRegion);
            if (this.cellsToUpdateSet.size() == 0) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }

}
