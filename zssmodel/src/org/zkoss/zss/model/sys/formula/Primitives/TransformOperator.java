package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.EvaluationException;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.impl.sys.formula.FormulaEngineImpl;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public abstract class TransformOperator extends PhysicalOperator{
    Ptg[] ptgs = null;

    private static WorkbookEvaluator evaluator = new WorkbookEvaluator(null,null,null);
    private static OperationEvaluationContext context = new OperationEvaluationContext(null,null,
            0,0,0,null,null,null);

    TransformOperator(){
        super();
    }

    @Override
    public abstract void evaluate(FormulaExecutor context) throws OptimizationError ;

    public abstract void merge(DataOperator TransformOperator) throws OptimizationError;

    public abstract int returnSize();

    static Object evaluate(Ptg[] ptgs){
        try {
            ValueEval result = evaluator.evaluateFormula(context,ptgs,true,true,null);
            return FormulaEngineImpl.convertToEvaluationResult(result).getValue();
        }
        catch (NullPointerException | EvaluationException e){
            e.printStackTrace();
        }


        return null;
    }
}
