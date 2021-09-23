package CompressionSizeTest.detector.Utils;

public class CellArea {
    private final CellLoc start;
    private final CellLoc end;

    public CellArea(int startRow, int startCol,
                    int endRow, int endCol) {
        this.start = new CellLoc(startRow, startCol);
        this.end = new CellLoc(endRow, endCol);
    }

    public int getCellNum() {
        return (end.getRow() - start.getRow() + 1) * (end.getColumn() - start.getColumn() + 1);
    }

    public CellLoc getStart() {
        return start;
    }

    public CellLoc getEnd() {
        return end;
    }

    public boolean isAdjacent(CellArea other, boolean rowWise) {
        if (this.start.isAdjacent(other.start, rowWise)
                && this.end.isAdjacent(other.end, rowWise)) return true;
        else return false;
    }

    public boolean isStartExended(CellArea other, boolean rowWise) {
        if (this.start.isAdjacent(other.start, rowWise)
                && this.end.equals(other.end)) return true;
        else return false;
    }

    public boolean isEndExtended(CellArea other, boolean rowWise) {
        if (this.start.equals(other.start)
                && this.end.isAdjacent(other.end, rowWise)) return true;
        else return false;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true;  }
        if (!(obj instanceof CellArea)) {
            return false;
        } else {
            CellArea other = (CellArea) obj;
            return  this.start.equals(other.start) &&
                    this.end.equals(other.end);
        }
    }

    @Override
    public String toString () {
        return "(" + this.start + ":" + this.end + ")";
    }
}
