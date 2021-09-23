package CompressionSizeTest.detector.PatternDetector;

import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;

public class TypeZeroDetector extends PatternDetector {

    public TypeZeroDetector(String sheetName, boolean rowWise) {
        super(sheetName, rowWise);
    }

    boolean isCompressable(CellLoc source, CellArea newCellArea,
                           CellArea lastCellArea, boolean rowWise) {
        CellLoc newStart = newCellArea.getStart();
        return (newCellArea.getCellNum() == 1 &&
                (source.isAdjacent(newStart, rowWise) || newStart.isAdjacent(source, rowWise)));
    }
}
