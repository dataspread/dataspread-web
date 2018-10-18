package org.zkoss.zss.model.sys.formula.QueryOptimization;

import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.PhysicalOperator;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

import java.util.Vector;

public class MultiFormulaExecutor {
    private MultiFormulaExecutor(){}
    static MultiFormulaExecutor uniqueExecutor = new MultiFormulaExecutor();
    public static MultiFormulaExecutor getExecutor(){
        return uniqueExecutor;
    }
    public void execute(QueryPlanGraph graph) throws OptimizationError {
        for (LogicalOperator op:graph.dataNodes)
            recursiveEvaluate(op);
    }

    private void recursiveEvaluate(LogicalOperator op) throws OptimizationError {
        if (!(op instanceof PhysicalOperator))
            throw new OptimizationError("Logical Operator not converted");
        PhysicalOperator p = (PhysicalOperator)op;
        if (p.isEvaluated())
            return;
        p.evaluate();
        if (p.isEvaluated()){
            for (LogicalOperator l :p.getOutputNodes())
                recursiveEvaluate(l);
        }
    }
}
