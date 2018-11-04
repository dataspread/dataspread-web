package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;

public abstract class FunctionDecomposer {
    public abstract LogicalOperator decompose(LogicalOperator[] ops) throws OptimizationError;
}
