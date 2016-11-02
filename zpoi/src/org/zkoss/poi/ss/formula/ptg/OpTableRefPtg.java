package org.zkoss.poi.ss.formula.ptg;

import org.zkoss.poi.util.LittleEndianOutput;

/**
 * This class is used for relational algebra operator functions such as select and join.
 * It stores a table number, so we can relate conditions such as 'Table_1.col_1 > 0' to an area.
 * Created by Danny on 11/1/2016.
 */
public class OpTableRefPtg extends Ptg {

    private int _tableNum = -1;

    public OpTableRefPtg(int tableNum) {
        _tableNum = tableNum;
    }

    public int getTableNum() {
        return _tableNum;
    }


    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void write(LittleEndianOutput out) {

    }

    @Override
    public String toFormulaString() {
        return null;
    }

    @Override
    public byte getDefaultOperandClass() {
        return 0;
    }

    @Override
    public boolean isBaseToken() {
        return false;
    }
}
