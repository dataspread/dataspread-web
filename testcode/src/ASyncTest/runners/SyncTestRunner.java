package ASyncTest.runners;

import ASyncTest.compression.BaseCompressor;
import ASyncTest.tests.AsyncBaseTest;
import ASyncTest.utils.Util;

import org.zkoss.util.Pair;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;

/**
 * A class that runs test cases synchronously.
 */
public class SyncTestRunner extends AsyncBaseTestRunner {

    public SyncTestRunner(final boolean prioritize, final BaseCompressor compressor) {
        super(prioritize, compressor);
    }

    @Override
    protected void runBefore(final AsyncBaseTest testCase) {
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(testCase.getBook().getBookSeries()));
    }

    @Override
    protected void runAfter(AsyncBaseTest testCase) {
        long elapsedTime = super.metadata.touchedTime - super.metadata.updateCellStartTime;
        int sheetSize = Util.getSheetCells(testCase.getSheet(), testCase.getRegion()).size();
        super.metadata.curve.add(new Pair<>(0L, sheetSize));
        super.metadata.curve.add(new Pair<>(elapsedTime, sheetSize));
        super.metadata.curve.add(new Pair<>(elapsedTime, 0));
        super.metadata.area = ((double) elapsedTime) * sheetSize;
    }
}
