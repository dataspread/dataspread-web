package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public class SingleTransformOperator extends TransformOperator {
    SingleTransformOperator(Ptg[] ptgs) {
        super(ptgs);
    }

    @Override
    public void evaluate(FormulaExecutor context) throws OptimizationError {

    }

    @Override
    public void merge(DataOperator TransformOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }
}
