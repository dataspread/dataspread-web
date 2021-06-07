package ASyncTest.compression;

import ASyncTest.utils.Util;

import ASyncTest.runners.TestMetadata;
import ASyncTest.tests.AsyncBaseTest;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class for performing no dependency compression.
 */
public class DefaultCompressor extends BaseCompressor {

    public DefaultCompressor() {
    }

    public DefaultCompressor(TestMetadata metadata, AsyncBaseTest testCase) {
        super(metadata, testCase);
    }

    @Override
    protected BaseCompressor newCompressor(TestMetadata metadata, AsyncBaseTest testCase) {
        return new DefaultCompressor(metadata, testCase);
    }

    @Override
    protected void compress() {
        // Perform no compression
        super.getMetadata().compStartTime = System.currentTimeMillis();
        super.getMetadata().compFinalTime = System.currentTimeMillis();
        super.getMetadata().startNumberOfDependents = super.dependencies.size();
        super.getMetadata().finalNumberOfDependents = super.dependencies.size();
    }

    @Override
    protected void initCellsToUpdate(Set<CellRegion> cellsToUpdateSet) {
        List<Ref>       dependenciesMultpl = new ArrayList<>();
        Set<CellRegion> dependenciesSingle = new HashSet<>();
        for (Ref dependency : Util.addAndReturn(super.dependencies, super.getTest().getCellToUpdate())) {
            if (dependency.getCellCount() == 1) {
                dependenciesSingle.add(new CellRegion(dependency));
            } else {
                dependenciesMultpl.add(dependency);
            }
        }
        for (CellRegion sheetCell : super.getTest().getCells()) {
            boolean matched = false;
            if (dependenciesSingle.contains(sheetCell)) {
                super.getMetadata().numberOfCellsToUpdate++;
                matched = true;
            }
            for (Ref dependency : dependenciesMultpl) {
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
