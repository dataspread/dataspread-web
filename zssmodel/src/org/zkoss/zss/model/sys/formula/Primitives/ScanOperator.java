package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.Collections;
import java.util.List;

public class ScanOperator extends PhysicalOperator {
    public ScanOperator(){
        super();
    }

    @Override
    public List getEvaluationResult(FormulaExecutor context) {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }
}
