package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.CellRegion;
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
    SSheet _sheet = null;
    CellRegion _region = null;
    public DataOperator(SRange range){
        super();
        _sheet = range.getSheet();
        _region = range.getRegion().getOverlap(
                new CellRegion(0,0,_sheet.getEndRowIndex(),_sheet.getEndColumnIndex()));
    }

    public DataOperator(){
        super();
    }

    public CellRegion getRegion(){
        return _region;
    }

    public SSheet getSheet() {
        return _sheet;
    }


    @Override
    public abstract void evaluate(FormulaExecutor context) throws OptimizationError ;

    public abstract void merge(DataOperator dataOperator) throws OptimizationError;
}
