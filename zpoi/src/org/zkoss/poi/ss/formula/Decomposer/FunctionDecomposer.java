package org.zkoss.poi.ss.formula.Decomposer;

import org.zkoss.poi.ss.formula.Primitives.LogicalOperator;

public abstract class FunctionDecomposer {
    public abstract LogicalOperator decompose(LogicalOperator[] ops);
}
