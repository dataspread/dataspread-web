package ASyncTest.runners;

import ASyncTest.compression.BaseCompressor;
import ASyncTest.tests.AsyncBaseTest;
import ASyncTest.utils.Util;

import org.zkoss.zss.model.sys.formula.DirtyManagerLog;

/**
 * A class that runs test cases asynchronously.
 */
public class AsyncTestRunner extends AsyncBaseTestRunner {

    public AsyncTestRunner(final boolean prioritize, final BaseCompressor compressor) {
        super(prioritize, compressor);
    }

    @Override
    public void runAfterInit(final AsyncBaseTest testCase) {
        testCase.getSheet().setSyncComputation(false);
    }

    @Override
    public void runAfterUpdate(final AsyncBaseTest testCase) {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void runAfter(AsyncBaseTest testCase) {
        super.metadata.area = DirtyManagerLog.instance.getAreaUnderCurve(
                Util.getSheetCells(testCase.getSheet(), testCase.getRegion())
                , super.metadata.updateCellFinalTime
                , super.metadata.updateCellStartTime
                , super.metadata.curve
        );
    }

}
