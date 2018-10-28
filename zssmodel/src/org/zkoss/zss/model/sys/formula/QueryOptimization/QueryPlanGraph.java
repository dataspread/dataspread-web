package org.zkoss.zss.model.sys.formula.QueryOptimization;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.DataOperator;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.PhysicalOperator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueryPlanGraph {
    List<DataOperator> dataNodes = new ArrayList<>();
    public void addData(DataOperator op){
        dataNodes.add(op);
    }

    public void clean() throws OptimizationError {
        for (LogicalOperator op:dataNodes)
            recursiveClean(op);
    }

    private void recursiveClean(LogicalOperator op) throws OptimizationError {
        if (!(op instanceof PhysicalOperator))
            throw new OptimizationError("Logical Operator not converted");
        PhysicalOperator p = (PhysicalOperator)op;
        if (!p.isEvaluated())
            return;
        p.clean();
        for (Iterator<LogicalOperator> it = p.getOutputNodes(); it.hasNext();)
            recursiveClean(it.next());
    }
}
