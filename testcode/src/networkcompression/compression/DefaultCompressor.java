package networkcompression.compression;

import networkcompression.runners.AsyncBaseTestRunner;
import networkcompression.tests.AsyncBaseTest;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import networkcompression.utils.Util;
import org.zkoss.zss.model.SSheet;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * A class for performing no dependency compression.
 */
public class DefaultCompressor implements Compressable {

    @Override
    public int getCellsToUpdate (AsyncBaseTestRunner testRunner, AsyncBaseTest test) {
        SSheet sheet = test.getSheet();
        Collection<CellRegion> sheetCells   = Util.getSheetCells(sheet, test.getRegion());
        ArrayList<Ref>  dependencies        = new ArrayList<>(sheet.getDependencyTable().getDependents(test.getCellToUpdate()));
        ArrayList<Ref>  dependenciesMultpl  = new ArrayList<>();
        Set<CellRegion> dependenciesSingle  = new HashSet<>();

        testRunner.metadata.startNumberOfDependents = dependencies.size();
        testRunner.metadata.finalNumberOfDependents = dependencies.size();

        dependencies.add(test.getCellToUpdate());
        for (Ref dependency : dependencies) {
            if (dependency.getCellCount() == 1) {
                dependenciesSingle.add(new CellRegion(dependency));
            } else {
                dependenciesMultpl.add(dependency);
            }
        }

        int cellsToUpdate = 0;
        for (CellRegion sheetCell : sheetCells) {
            boolean matched = false;
            if (dependenciesSingle.contains(sheetCell)) {
                cellsToUpdate++;
                matched = true;
            }
            for (Ref dependency : dependenciesMultpl) {
                CellRegion reg = new CellRegion(dependency);
                if (reg.contains(sheetCell)) {
                    cellsToUpdate++;
                    matched = true;
                }
            }
            if (matched) {
                testRunner.cellsToUpdateSet.add(sheetCell);
            }
        }

        return cellsToUpdate;
    }

}
