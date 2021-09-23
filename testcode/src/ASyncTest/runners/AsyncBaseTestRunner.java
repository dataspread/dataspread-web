package ASyncTest.runners;

import ASyncTest.compression.BaseCompressor;
import ASyncTest.tests.AsyncBaseTest;
import ASyncTest.utils.Util;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The parent class for all Async test runners.
 */
public abstract class AsyncBaseTestRunner implements FormulaAsyncListener {

    public final BaseCompressor COMPRESSOR;
    public final boolean PRIORITIZE;

    public TestMetadata metadata;
    private Set<CellRegion> cellsToUpdateSet;
    private boolean testStarted;

    public AsyncBaseTestRunner(final boolean prioritize, final BaseCompressor compressor) {
        this.PRIORITIZE = prioritize;
        this.COMPRESSOR = compressor;
    }

    /**
     * Any setup steps that should be performed for all subclasses
     * should be placed here.
     *
     * @param testCase
     */
    protected void runSetup(final AsyncBaseTest testCase) {
        CellImpl.disableDBUpdates = false;
        this.runBefore(testCase);
        FormulaAsyncScheduler.getScheduler().updateVisibleMap(new HashMap<>());
        testCase.getSheet().setSyncComputation(true);
        testCase.init();
        this.runAfterInit(testCase);
        COMPRESSOR.runAll(this.metadata, testCase, this.cellsToUpdateSet);
        DirtyManagerLog.instance.init();
        CellImpl.disableDBUpdates = true;
    }

    /**
     * Any testing steps that should be performed for all subclasses
     * should be placed here.
     *
     * @param testCase
     */
    protected void runTest(final AsyncBaseTest testCase) {
        this.testStarted = true;
        this.metadata.updateCellStartTime = System.currentTimeMillis();
        testCase.updateCell();
        this.metadata.updateCellFinalTime = System.currentTimeMillis();
        FormulaAsyncScheduler.getScheduler().start();
        this.runAfterUpdate(testCase);
        testCase.touchAll();
        this.metadata.touchedTime = System.currentTimeMillis();
        this.metadata.isCorrect = testCase.verify();
        this.runAfter(testCase);
    }

    /**
     * This method will be run before the default setup steps have
     * been performed.
     *
     * @param testCase
     */
    protected void runBefore(final AsyncBaseTest testCase) {
    }

    /**
     * This method will be run directly after sheet initialization
     * occurs.
     *
     * @param testCase
     */
    protected void runAfterInit(final AsyncBaseTest testCase) {
    }

    /**
     * This method will be run after the cell has been updated.
     *
     * @param testCase
     */
    protected void runAfterUpdate(final AsyncBaseTest testCase) {
    }

    /**
     * This method will be run after the default testing steps have
     * been performed.
     *
     * @param testCase
     */
    protected void runAfter(final AsyncBaseTest testCase) {
    }

    /**
     * Runs the given test case.
     *
     * @param testCase
     */
    public void run(final AsyncBaseTest testCase) {
        try {

            // Setup async scheduler
            this.reset();
            DirtyManager.dirtyManagerInstance.reset();
            FormulaAsyncSchedulerTesting.initScheduler();
            Thread asyncThread = new Thread(FormulaAsyncScheduler.getScheduler());
            asyncThread.start();
            FormulaAsyncScheduler.getScheduler().setFormulaAsyncListener(this);
            FormulaAsyncScheduler.getScheduler().setPrioritize(this.PRIORITIZE);

            // Perform the test
            this.metadata.testStartTime = System.currentTimeMillis();
            this.runSetup(testCase);
            this.runTest(testCase);
            this.metadata.testFinalTime = System.currentTimeMillis();

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

    public void dumpMetadata(Path dirName, Path fileName) {
        Util.createDirectory(dirName);
        this.metadata.writeStatsToFile(Util.joinPaths(dirName, fileName));
    }

    private void reset() {
        this.metadata = new TestMetadata();
        cellsToUpdateSet = new HashSet<>();
        testStarted = false;
    }

    @Override
    public void update(SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula) {
        if (this.testStarted) {
            this.metadata.updatedCells++;
            this.cellsToUpdateSet.remove(cellRegion);
            if (this.cellsToUpdateSet.size() == 0) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }

}
