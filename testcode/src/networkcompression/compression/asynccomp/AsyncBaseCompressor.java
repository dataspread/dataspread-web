package networkcompression.compression.asynccomp;

import networkcompression.utils.Util;

import networkcompression.compression.BaseCompressor;
import networkcompression.runners.TestMetadata;
import networkcompression.tests.AsyncBaseTest;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;

import java.util.Set;

public abstract class AsyncBaseCompressor extends BaseCompressor {

    protected AsyncBaseCompressor() {
    }

    public AsyncBaseCompressor(TestMetadata metadata, AsyncBaseTest testCase) {
        super(metadata, testCase);
    }

    @Override
    protected void initCellsToUpdate(Set<CellRegion> cellsToUpdateSet) {
        for (CellRegion sheetCell : super.getTest().getCells()) {
            boolean matched = false;
            for (Ref dependency : Util.addAndReturn(super.dependencies, super.getTest().getCellToUpdate())) {
                CellRegion reg = new CellRegion(dependency);
                if (reg.contains(sheetCell)) {
                    super.getMetadata().numberOfCellsToUpdate++;
                    matched = true;
                }
            }
            if (matched) {
                cellsToUpdateSet.add(sheetCell);
            }
        }
    }

}
