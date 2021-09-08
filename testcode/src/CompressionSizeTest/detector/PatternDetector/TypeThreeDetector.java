package CompressionSizeTest.detector.PatternDetector;

import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;

public class TypeThreeDetector extends PatternDetector {

    public TypeThreeDetector(String sheetName,
                             boolean rowWise) { super(sheetName, rowWise); }

    boolean isCompressable(CellLoc source,
                           CellArea newCellArea, CellArea lastCellArea, boolean rowWise) {
        return lastCellArea.isStartExended(newCellArea, rowWise);
    }
}
