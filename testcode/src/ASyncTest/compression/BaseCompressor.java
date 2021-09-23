package ASyncTest.compression;

import ASyncTest.runners.TestMetadata;
import ASyncTest.tests.AsyncBaseTest;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;

import java.util.List;
import java.util.Set;

public abstract class BaseCompressor {

    protected List<Ref> dependencies = null;
    private AsyncBaseTest testCase = null;
    private TestMetadata metadata = null;

    public BaseCompressor() {
    }

    protected BaseCompressor(TestMetadata metadata, AsyncBaseTest testCase) {
        this.dependencies = testCase.getDependenciesOfUpdatedCell();
        this.metadata = metadata;
        this.testCase = testCase;
    }

    public AsyncBaseTest getTest() {
        return this.testCase;
    }

    public TestMetadata getMetadata() {
        return this.metadata;
    }

    /**
     * @param metadata
     * @param testCase
     * @return A new BaseCompressor instance with the `metadata` and
     * `testCase` members initialized.
     */
    protected abstract BaseCompressor newCompressor(TestMetadata metadata, AsyncBaseTest testCase);

    /**
     * This function should update the following fields:
     * - `metadata.compStartTime`
     * - `metadata.compFinalTime`
     * - `metadata.startNumberOfDependents`
     * - `metadata.finalNumberOfDependents`
     * <p>
     * It should also perform dependency compression on `dependencies`
     * in place.
     */
    protected abstract void compress();

    /**
     * This function will be called right after `compress`. It should
     * fill `cellsToUpdateSet` with the cells that the async scheduler
     * should update and initialize `metadata.numberOfCellsToUpdate`
     * to be, well, the number of cells to update.
     *
     * @param cellsToUpdateSet
     */
    protected abstract void initCellsToUpdate(Set<CellRegion> cellsToUpdateSet);

    /**
     * Runs all abstract methods.
     *
     * @param metadata
     * @param testCase
     * @param cellsToUpdateSet
     */
    public void runAll(TestMetadata metadata, AsyncBaseTest testCase, Set<CellRegion> cellsToUpdateSet) {
        BaseCompressor compressor = this.newCompressor(metadata, testCase);
        compressor.compress();
        compressor.initCellsToUpdate(cellsToUpdateSet);
    }

}
