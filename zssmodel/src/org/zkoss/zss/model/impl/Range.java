package org.zkoss.zss.model.impl;

public class Range {

    /* Zero based and inclusive */
    private int minRow = Integer.MAX_VALUE;
    private int minCol = Integer.MAX_VALUE;
    private int maxRow = -1;
    private int maxCol = -1;


    private Range() {
    }

    // From Excel String (A1:B100) (A1)
    public Range(String s) {
        int rc[] = {0, 0, 0, 0};
        int i = 0;
        for (char c : s.toUpperCase().toCharArray())
            if (c == ':')
                i = 2;
            else if (c < 65)
                //Row
                rc[i + 1] = rc[i + 1] * 10 + c - '0';
            else
                // Column
                rc[i] = rc[i] * 26 + c - '@';
        if (rc[2] == 0)
            rc[2] = rc[0];
        if (rc[3] == 0)
            rc[3] = rc[1];

        minCol = Math.min(rc[0], rc[2]) - 1;
        maxCol = Math.max(rc[0], rc[2]) - 1;
        minRow = Math.min(rc[1], rc[3]) - 1;
        maxRow = Math.max(rc[1], rc[3]) - 1;
    }

    //Set row=-1 to select full row
    //Set col=-1 to select full column
    // Full column/row range
    public Range(int row, int col) {
        if (row == -1) {
            minRow = 0;
            maxRow = Integer.MAX_VALUE;
        } else {
            minRow = maxRow = row;
        }

        if (col == -1) {
            minCol = 0;
            maxCol = Integer.MAX_VALUE;
        } else {
            minCol = maxCol = col;
        }
    }

    public Range(int minRow, int minCol, int maxRow, int maxCol) {
        this.minRow = minRow;
        this.minCol = minCol;
        this.maxRow = maxRow;
        this.maxCol = maxCol;
    }


    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;
        Range range = (Range) obj;
        return minRow == range.minRow && minCol == range.minCol
                && maxRow == range.maxRow && maxCol == range.maxCol;
    }

    public int hashCode() {
        return minRow + minCol + maxRow + maxCol;
    }

    int getMinRow() {
        return minRow;
    }

    int getMinCol() {
        return minCol;
    }

    int getMaxRow() {
        return maxRow;
    }

    int getMaxCol() {
        return maxCol;
    }

    @Override
    public String toString() {
        return "(" + minRow + "," + minCol + "," + maxRow + "," + maxCol + ")";
    }

    public int size() {
        return (maxRow - minRow + 1) * (maxCol - minCol + 1);
    }

    public boolean cellInRange(int row, int col) {
        return row >= minRow && row <= maxRow
                && col >= minCol && col <= maxCol;
    }

    public boolean encompasses(Range range2) {
        return this.minRow <= range2.minRow &&
                this.minCol <= range2.minCol &&
                this.maxRow >= range2.maxCol &&
                this.maxCol >= range2.maxCol;
    }


    public boolean intersects(Range range2) {
        return cellInRange(range2.minRow, range2.minCol) ||
                cellInRange(range2.minRow, range2.maxCol) ||
                cellInRange(range2.maxRow, range2.maxCol) ||
                cellInRange(range2.maxRow, range2.maxCol) ||
                range2.cellInRange(minRow, minCol) ||
                range2.cellInRange(minRow, maxCol) ||
                range2.cellInRange(maxRow, maxCol) ||
                range2.cellInRange(maxRow, maxCol);
    }

    public Range intersection(Range range2) {
        if (!intersects(range2))
            return null;
        else
            return new Range(Math.max(minRow, range2.minRow),
                    Math.max(minCol, range2.minCol),
                    Math.min(maxRow, range2.maxRow),
                    Math.min(maxCol, range2.maxCol));
    }

    public Range extendRange(int rows, int cols) {
        return new Range(this.minRow,
                this.minCol,
                this.maxRow + rows,
                this.maxCol + cols);
    }


    public Range shiftedRange(int row_shift, int col_shift) {
        return new Range(this.minRow + row_shift,
                this.minCol + col_shift,
                this.maxRow + row_shift,
                this.maxCol + col_shift);
    }

    public int getHeight() {
        return maxRow - minRow + 1;
    }

    public int getLength() {
        return maxCol - minCol + 1;
    }
}