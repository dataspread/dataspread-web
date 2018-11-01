package org.zkoss.poi.ss.formula.ptg;

import org.zkoss.poi.util.LittleEndianOutput;

public class VariablePtg extends Ptg {
    
    int index;

    VariablePtg(int index){
        this.index = index;
    }

    public int getIndex(){
       return index;
    }

    public void setIndex(int index){
        this.index = index;
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
