package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public class GroupedTransformOperator extends TransformOperator {

    GroupedTransformOperator(LogicalOperator[] operators, Ptg ptgs[]){
        this.ptgs = ptgs;
        for (LogicalOperator op:operators){
            connect(op, this);
        }
    }

    @Override
    public void evaluate(FormulaExecutor context) throws OptimizationError {

    }

    @Override
    public void merge(DataOperator TransformOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    public int returnSize() {
        return 0;
    }
}
