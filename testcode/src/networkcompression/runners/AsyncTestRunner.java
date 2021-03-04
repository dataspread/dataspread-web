package networkcompression.runners;

import networkcompression.compression.BaseCompressor;
import networkcompression.tests.AsyncBaseTest;
import networkcompression.utils.Util;

import org.zkoss.zss.model.sys.formula.DirtyManagerLog;

/**
 *
 * A class that runs test cases asynchronously.
 */
public class AsyncTestRunner extends AsyncBaseTestRunner {

    public AsyncTestRunner (final boolean prioritize, final BaseCompressor compressor) {
        super(prioritize, compressor);
    }

    @Override
    public void extraSetup (final AsyncBaseTest testCase) {
        testCase.getSheet().setSyncComputation(false);
    }

    @Override
    public void runAfter (final AsyncBaseTest testCase) {
        synchronized (this) { try { wait(); } catch (InterruptedException e) { e.printStackTrace(); } }
        super.metadata.area = DirtyManagerLog.instance.getAreaUnderCurve(
            Util.getSheetCells(testCase.getSheet(), testCase.getRegion())
            , super.metadata.updateCellFinalTime
            , super.metadata.updateCellStartTime
            , super.metadata.curve
        );
    }

}
