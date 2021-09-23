package org.zkoss.zss.model.impl.sys.utils;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Offset)) return false;
        Offset offset = (Offset) o;
        return rowOffset == offset.rowOffset &&
                colOffset == offset.colOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowOffset, colOffset);
    }
}
