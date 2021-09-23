package CompressionSizeTest.detector.PatternDetector;

import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;

public class TypeOneDetector extends PatternDetector {

    public TypeOneDetector(String sheetName, boolean rowWise) {
        super(sheetName, rowWise);
    }

    @Override
    public int getCompressedEdges() {
        if (numCommittedSourceCells == numTotalCommitted) {
            return lastComittedDeps.stream().mapToInt(CellArea::getCellNum).sum()
                    * (numCommittedSourceCells - 1);
        } else {
            return 0;
        }
    }

    boolean isCompressable(CellLoc cellLoc, CellArea newCellArea,
                           CellArea lastCellArea, boolean rowWise) {
        CellLoc newStart = newCellArea.getStart();
        if (newCellArea.getCellNum() == 1 &&
                (cellLoc.isAdjacent(newStart, rowWise) || newStart.isAdjacent(cellLoc, rowWise))) {
            return false;
        } else {
            return lastCellArea.isAdjacent(newCellArea, rowWise);
        }
    }
}
