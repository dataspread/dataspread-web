package FormulaCompressionTest.runners;

import FormulaCompressionTest.tests.BaseTest;
import FormulaCompressionTest.utils.Util;

import org.zkoss.util.Pair;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;

/**
 * A class that runs test cases synchronously.
 */
public class SyncTestRunner extends BaseTestRunner {

    public SyncTestRunner() {
        super();
    }

    @Override
    protected void runBefore(final BaseTest testCase) {
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(testCase.getBook().getBookSeries()));
    }

    @Override
    protected void runAfter(BaseTest testCase) {
        long elapsedTime = super.testStats.touchedTime - super.testStats.updateCellStartTime;
        int sheetSize = Util.getSheetCells(testCase.getSheet(), testCase.getRegion()).size();
        super.testStats.curve.add(new Pair<>(0L, sheetSize));
        super.testStats.curve.add(new Pair<>(elapsedTime, sheetSize));
        super.testStats.curve.add(new Pair<>(elapsedTime, 0));
        super.testStats.area = ((double) elapsedTime) * sheetSize;
    }
}
