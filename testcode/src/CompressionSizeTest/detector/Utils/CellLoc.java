package CompressionSizeTest.detector.Utils;

public class CellLoc {
    private final int row;
    private final int column;

    public CellLoc(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean isAdjacent(CellLoc other, boolean rowWise) {
        if (this.row < 0 || this.column < 0) {
            return true;
        } else if (rowWise && this.row == other.row && this.column + 1 == other.column) {
            return true;
        } else if (!rowWise && this.column == other.column && this.row + 1 == other.row) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true;  }
        if (!(obj instanceof CellLoc)) {
            return false;
        } else {
            CellLoc other = (CellLoc) obj;
            return  this.row == other.row && this.column == other.column;
        }
    }


    private String fromNumToAlphabet(int i) {
        return i >= 0 && i < 26 ? String.valueOf((char)(i + 65)) : Integer.toString(i);
    }

    private String genColumnLabel(int column) {
        int remaining = column;
        int digit = 1;
        StringBuilder colLabel = new StringBuilder();
        while (Math.pow(26, digit) < remaining + 1) {
            remaining = remaining - digit * 26;
            digit++;
        }
        for (int i = 0; i < digit; i++) {
            int cur = remaining % 26;
            remaining = remaining / 26;
            colLabel.append(fromNumToAlphabet(cur));
        }
        return colLabel.reverse().toString();
    }

    @Override
    public String toString () {
        return this.genColumnLabel(column) + (this.row + 1);
    }

}
