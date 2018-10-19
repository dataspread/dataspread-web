package org.zkoss.zss.model.sys.formula.QueryOptimization;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.PhysicalOperator;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

import static org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler.getScheduler;

public class FormulaExecutor {
    static FormulaExecutor uniqueExecutor = new FormulaExecutor();
    FormulaAsyncScheduler scheduler = null;
    private FormulaExecutor(){}
    public static FormulaExecutor getExecutor(){
        return uniqueExecutor;
    }
    public void execute(QueryPlanGraph graph, FormulaAsyncScheduler scheduler) throws OptimizationError {
        for (LogicalOperator op:graph.dataNodes)
            recursiveEvaluate(op);
        this.scheduler = scheduler;
    }

    public void update(SSheet sheet, AbstractCellAdv sCell){
        scheduler.update(sheet.getBook(), sheet, sCell.getCellRegion(),
                sCell.getValue(true, false).toString(),
                sCell.getFormulaValue());
    }

    private void recursiveEvaluate(LogicalOperator op) throws OptimizationError {
        if (!(op instanceof PhysicalOperator))
            throw new OptimizationError("Logical Operator not converted");
        PhysicalOperator p = (PhysicalOperator)op;
        if (p.isEvaluated())
            return;
        p.evaluate(this);
        if (p.isEvaluated()){
            for (LogicalOperator l :p.getOutputNodes())
                recursiveEvaluate(l);
        }
    }
}
