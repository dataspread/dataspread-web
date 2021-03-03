package networkcompression.runners;

import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import networkcompression.compression.Compressable;
import networkcompression.tests.AsyncBaseTest;
import networkcompression.utils.Util;

/**
 *
 * A class that runs test cases asynchronously.
 */
public class AsyncTestRunner extends AsyncBaseTestRunner {

    public AsyncTestRunner (final boolean prioritize, final Compressable compressor) {
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
