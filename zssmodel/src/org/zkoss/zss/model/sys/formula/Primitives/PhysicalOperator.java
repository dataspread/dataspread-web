package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public abstract class PhysicalOperator extends LogicalOperator {
    PhysicalOperator(){super();}
    boolean _evaluated = false;
    abstract public void evaluate(FormulaExecutor context) throws OptimizationError;
    public void clean(){
        _evaluated = false;
    }
    public boolean isEvaluated(){
        return _evaluated;
    }
}
