package networkcompression.compression;

import networkcompression.runners.AsyncBaseTestRunner;
import networkcompression.tests.AsyncBaseTest;

public interface Compressable {

    /**
     *
     * This function should initialize the following fields:
     *  - `testRunner.metadata.startNumberOfDependents`
     *  - `testRunner.metadata.finalNumberOfDependents`
     *  - `testRunner.cellsToUpdateSet`
     *
     * @param testRunner
     * @param test
     * @return The number of cells to update.
     */
    int getCellsToUpdate (AsyncBaseTestRunner testRunner, AsyncBaseTest test);

}