package networkcompression.runners;

import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import networkcompression.compression.Compressable;
import networkcompression.tests.AsyncBaseTest;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * The parent class for all Async test runners.
 */
public abstract class AsyncBaseTestRunner implements FormulaAsyncListener {

    public final boolean        PRIORITIZE;
    public final Compressable   COMPRESSOR;

    public TestMetadata         metadata            = new TestMetadata();
    public Set<CellRegion>      cellsToUpdateSet    = new HashSet<>();
    public boolean              testStarted         = false;
    public long                 updatedCells        = 0;

    public AsyncBaseTestRunner (final boolean prioritize, final Compressable compressor) {
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
        this.metadata.depStartTime = System.currentTimeMillis();
        this.metadata.numberOfCellsToUpdate = this.COMPRESSOR.getCellsToUpdate(this, testCase);
        this.metadata.depFinalTime = System.currentTimeMillis();
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
        this.metadata.totlTimeToUpdateCells = this.metadata.updateCellFinalTime - this.metadata.updateCellStartTime;
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

    @Override
    public void update(SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula) {
        if (this.testStarted) {
            this.updatedCells++;
            this.cellsToUpdateSet.remove(cellRegion);
            if (this.cellsToUpdateSet.size() == 0) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }

}
