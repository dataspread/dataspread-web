package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;

public class FilterDecomposer extends FunctionDecomposer {
    int[] parameterMap;

    FilterDecomposer(int[] parameterMap){

    }

    @Override
    public LogicalOperator decompose(LogicalOperator[] ops) throws OptimizationError {
        return null;
    }
}
