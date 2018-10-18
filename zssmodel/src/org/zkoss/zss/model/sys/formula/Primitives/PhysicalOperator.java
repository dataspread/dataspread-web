package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

public abstract class PhysicalOperator extends LogicalOperator {
    protected boolean _evaluated = false;
    abstract public void evaluate() throws OptimizationError;
    Object getOutput(PhysicalOperator op){ return null;}
    public void clean(){
        _evaluated = false;
    }
    public boolean isEvaluated(){
        return _evaluated;
    }
}
