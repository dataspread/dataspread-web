package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.List;

public class SingleRangeIndexOperator extends SingleIndexOperator {
    @Override
    List getEvaluationResult(FormulaExecutor context) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }
}
