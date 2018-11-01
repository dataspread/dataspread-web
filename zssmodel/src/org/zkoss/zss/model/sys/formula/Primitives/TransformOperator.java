package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public abstract class TransformOperator extends PhysicalOperator{
    Ptg[] ptgs;

    TransformOperator(Ptg[] ptgs){
        super();
        this.ptgs = ptgs;
    }

    @Override
    public abstract void evaluate(FormulaExecutor context) throws OptimizationError ;

    public abstract void merge(DataOperator TransformOperator) throws OptimizationError;
}
