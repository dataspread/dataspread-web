package org.zkoss.zss.model.sys.formula.DataStructure;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

public class Range {
    public int left,right;
    public Range(int inclusiveLeft,int exclusiveRight){
        left = inclusiveLeft;
        right = exclusiveRight;
        if (left >= right)
            throw OptimizationError.ERROR;
    }

    public Range(Range another){
        left = another.left;
        right = another.right;
        if (left >= right)
            throw OptimizationError.ERROR;
    }

    public int size(){
        return right - left + 1;
    }
}
