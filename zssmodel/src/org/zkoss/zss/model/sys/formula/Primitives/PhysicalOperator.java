package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PhysicalOperator extends LogicalOperator {
    PhysicalOperator(){super();}
    abstract public void evaluate(FormulaExecutor context) throws OptimizationError;
    public boolean readyToEvaluate(){
        AtomicBoolean readyToEvaluate = new AtomicBoolean(true);
        forEachInEdge((e)-> readyToEvaluate.set(readyToEvaluate.get() & e.resultIsReady()));
        return readyToEvaluate.get();
    }
}
