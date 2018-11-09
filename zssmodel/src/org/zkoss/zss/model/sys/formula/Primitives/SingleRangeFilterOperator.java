package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.List;

public class SingleRangeFilterOperator extends FilterOperator {
    private String literal;

    SingleRangeFilterOperator(String value){
        super();
        literal = value;
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    SingleRangeFilterOperator(LogicalOperator criteria){
        super();
        connect(criteria,this);
        literal = null;
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    List getEvaluationResult(FormulaExecutor context) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }
}
