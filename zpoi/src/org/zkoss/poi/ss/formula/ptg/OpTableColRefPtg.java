package org.zkoss.poi.ss.formula.ptg;

import org.zkoss.poi.util.LittleEndianOutput;

/**
 * This class is used by relational algebra operator functions such as select and join.
 * It stores a table and column number to represent part of a conditional argument, e.g. 'Table_1.Col_1'
 * Created by Danny on 10/22/2016.
 */
public class OpTableColRefPtg extends Ptg {

    private int _tableNum = -1;
    private int _columnNum = -1;
    private String _columnName = "";

    public OpTableColRefPtg(String arg1, String arg2) {

        String[] first = arg1.split("_");
        String[] second = arg2.split("_");

        if (first[0].toUpperCase().equals("TABLE")) {
            if (second[0].toUpperCase().equals("COL")) {
                //set tableNum and columnNum to 0 if it's 'Table_1.Col_1'
                _tableNum = Integer.parseInt(first[1]) - 1;
                _columnNum = Integer.parseInt(second[1]) - 1;
            } else if (second[0].toUpperCase().equals("ATTR")) {
                _tableNum = Integer.parseInt(first[1]) - 1;
                _columnName = second[1];
            }
        } else if (first[0].toUpperCase() == "COL" && second[0].toUpperCase() == "TABLE") {
            _tableNum = Integer.parseInt(second[1]);
            _columnNum = Integer.parseInt(first[1]);
        }


    }

    public int getTableNum() {
        return _tableNum;
    }

    public int getColumnNum() {
        return _columnNum;
    }

    public String getColumnName() {
        return _columnName;
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
        if (_columnNum != -1) {
            return "Table_" + Integer.toString(_tableNum + 1) + ".Col_" + Integer.toString(_columnNum + 1);

        }

        return "Table_" + Integer.toString(_tableNum + 1) + ".Attr_" + _columnName;

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
