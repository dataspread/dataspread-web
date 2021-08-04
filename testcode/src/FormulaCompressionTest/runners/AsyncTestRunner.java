package FormulaCompressionTest.runners;

import FormulaCompressionTest.tests.BaseTest;
import FormulaCompressionTest.utils.Util;

import org.zkoss.zss.model.sys.formula.DirtyManagerLog;

/**
 * A class that runs test cases asynchronously.
 */
public class AsyncTestRunner extends BaseTestRunner {

    public AsyncTestRunner() {
        super();
    }

    @Override
    public void runAfterInit(final BaseTest testCase) {
        testCase.getSheet().setSyncComputation(false);
    }

    @Override
    public void runAfterUpdate(final BaseTest testCase) {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void runAfter(BaseTest testCase) {
        super.testStats.area = DirtyManagerLog.instance.getAreaUnderCurve(
                Util.getSheetCells(testCase.getSheet(), testCase.getRegion())
                , super.testStats.updateCellFinalTime
                , super.testStats.updateCellStartTime
                , super.testStats.curve
        );
    }

}
