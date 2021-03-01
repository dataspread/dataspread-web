package networkcompression.runners;

import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import networkcompression.compression.Compressable;
import networkcompression.tests.AsyncBaseTest;

/**
 *
 * A class that runs test cases synchronously.
 */
public class SyncTestRunner extends AsyncBaseTestRunner {

    public SyncTestRunner (final boolean prioritize, final Compressable compressor) {
        super(prioritize, compressor);
    }

    @Override
    protected void runBefore (final AsyncBaseTest testCase) {
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(testCase.getBook().getBookSeries()));
    }

}
