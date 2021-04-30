package org.zkoss.zss.model.impl.sys.utils;

public class Offset {

    public static Offset noOffset = new Offset(0, 0);

    private int rowOffset;
    private int colOffset;

    public Offset(int rowOffset,
                  int colOffset) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
    }

    public int getColOffset() {
        return colOffset;
    }

    public int getRowOffset() {
        return rowOffset;
    }
}
