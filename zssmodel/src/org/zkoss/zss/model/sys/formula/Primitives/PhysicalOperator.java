package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.List;

public abstract class PhysicalOperator extends LogicalOperator {

    private int inputCount = 0;

    abstract List getEvaluationResult(FormulaExecutor context) throws OptimizationError;

    PhysicalOperator(){super();}

    void incInputCount(){
        inputCount++;
    }
    void decInputCount(){
        inputCount--;
    }

    public void evaluate(FormulaExecutor context) throws OptimizationError{
        List result = getEvaluationResult(context);
        pushEvaluationResult(result);
    }
    public boolean readyToEvaluate(){ // todo: remove it and add topsort
        return inputCount == inDegree();
    }

    private void pushEvaluationResult(List result){
        forEachOutEdge((e)->e.setResult(result));
    }

}
