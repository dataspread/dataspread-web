package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public abstract class PhysicalOperator extends LogicalOperator {
    PhysicalOperator(){super();}
    private int inputCount = 0;
    void incInputCount(){
        inputCount++;
    }
    void decInputCount(){
        inputCount--;
    }
    abstract public void evaluate(FormulaExecutor context) throws OptimizationError;
    public boolean readyToEvaluate(){ // todo: remove it and add topsort
        return inputCount == inDegree();
    }
}
