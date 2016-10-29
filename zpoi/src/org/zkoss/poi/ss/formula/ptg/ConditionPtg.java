package org.zkoss.poi.ss.formula.ptg;

import org.zkoss.poi.util.LittleEndianOutput;

/**
 * Created by Danny on 10/22/2016.
 */
public class ConditionPtg extends Ptg {

    private int _tableNum;
    private int _columnNum;

    public ConditionPtg(String arg1, String arg2) {

        String[] first = arg1.split("_");
        String[] second = arg2.split("_");

        if (first[0].toUpperCase().equals("TABLE") && second[0].toUpperCase().equals("COL")) {
            _tableNum = Integer.parseInt(first[1]);
            _columnNum = Integer.parseInt(second[1]);
        } else if (first[0].toUpperCase() == "COL" && second[0].toUpperCase() == "TABLE") {
            _tableNum = Integer.parseInt(second[1]);
            _columnNum = Integer.parseInt(first[1]);
        }


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
