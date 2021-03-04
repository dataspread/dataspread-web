package networkcompression.runners;

import networkcompression.compression.BaseCompressor;

import networkcompression.utils.Util;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import networkcompression.tests.AsyncBaseTest;
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
 *
 * The parent class for all Async test runners.
 */
public abstract class AsyncBaseTestRunner implements FormulaAsyncListener {

    public final BaseCompressor COMPRESSOR;
    public final boolean        PRIORITIZE;

    public TestMetadata         metadata            = new TestMetadata();
    private Set<CellRegion>     cellsToUpdateSet    = new HashSet<>();
    private boolean             testStarted         = false;

    public AsyncBaseTestRunner (final boolean prioritize, final BaseCompressor compressor) {
        this.PRIORITIZE = prioritize;
        this.COMPRESSOR = compressor;
    }

    /**
     * Any setup steps that should be performed for all subclasses
     * should be placed here.
     *
     * @param testCase
     */
    protected void runSetup (final AsyncBaseTest testCase) {
        CellImpl.disableDBUpdates = false;
        this.runBefore(testCase);
        FormulaAsyncScheduler.updateVisibleMap(new HashMap<>());
        testCase.getSheet().setSyncComputation(true);
        testCase.initSheet();
        this.extraSetup(testCase);
        COMPRESSOR.runAll(this.metadata, testCase, this.cellsToUpdateSet);
        DirtyManagerLog.instance.init();
        CellImpl.disableDBUpdates =  true;
    }

    /**
     * Any testing steps that should be performed for all subclasses
     * should be placed here.
     *
     * @param testCase
     */
    protected void runTest (final AsyncBaseTest testCase) {
        this.testStarted = true;
        this.metadata.updateCellStartTime = System.currentTimeMillis();
        testCase.updateCell();
        this.metadata.updateCellFinalTime = System.currentTimeMillis();
        FormulaAsyncSchedulerSimple.started = true;
        this.runAfter(testCase);
        testCase.touchAll();
        this.metadata.touchedTime = System.currentTimeMillis();
        this.metadata.isCorrect = testCase.verify();
    }

    /**
     * This method will be run before the default setup steps have
     * been performed.
     *
     * @param testCase
     */
    protected void runBefore (final AsyncBaseTest testCase) { }

    /**
     * This method will be run directly after sheet initialization
     * occurs.
     *
     * @param testCase
     */
    protected void extraSetup (final AsyncBaseTest testCase) { }

    /**
     * This method will be run after the default testing steps have
     * been performed.
     *
     * @param testCase
     */
    protected void runAfter (final AsyncBaseTest testCase) {}

    /**
     * Runs the given test case.
     *
     * @param testCase
     */
    public void run (final AsyncBaseTest testCase) {
        try {

            // Setup async scheduler
            this.reset();
            DirtyManager.dirtyManagerInstance.reset();
            FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
            Thread asyncThread = new Thread(formulaAsyncScheduler);
            asyncThread.start();
            FormulaAsyncScheduler.initFormulaAsyncListener(this);
            FormulaAsyncScheduler.setPrioritize(this.PRIORITIZE);

            // Perform the test
            this.metadata.testStartTime = System.currentTimeMillis();
            this.runSetup(testCase);
            this.runTest (testCase);
            this.metadata.testFinalTime = System.currentTimeMillis();

            // Wait for the test to finish
            formulaAsyncScheduler.shutdown();
            while (!FormulaAsyncSchedulerSimple.isDead) { Thread.sleep(10); }
            FormulaAsyncSchedulerSimple.started = false;
            FormulaAsyncSchedulerSimple.isDead  = false;
            asyncThread.join();

        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    public void dumpMetadata (Path dirName, Path fileName) {
        Util.createDirectory(dirName);
        this.metadata.writeStatsToFile(Util.joinPaths(dirName, fileName));
    }

    private void reset () {
        this.metadata.reset();
        cellsToUpdateSet = new HashSet<>();
        testStarted = false;
    }

    @Override
    public void update (SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula) {
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
