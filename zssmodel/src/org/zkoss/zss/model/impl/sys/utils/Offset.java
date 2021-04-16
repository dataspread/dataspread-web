package org.zkoss.zss.model.impl.sys.utils;

public class Offset {

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
