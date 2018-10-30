package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;
import org.zkoss.zss.range.SRange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class DataOperator extends PhysicalOperator{
    SRange _range;
    public DataOperator(SRange range){
        super();
        _range = range;
    }

    public DataOperator(){
        super();
    }

    public SRange getRange(){
        return _range;
    }


    @Override
    public abstract void evaluate(FormulaExecutor context) throws OptimizationError ;
}
