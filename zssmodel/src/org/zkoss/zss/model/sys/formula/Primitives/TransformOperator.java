package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.EvaluationException;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.ptg.NumberPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.StringPtg;
import org.zkoss.zss.model.impl.sys.formula.FormulaEngineImpl;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.List;

public abstract class TransformOperator extends PhysicalOperator{
    Ptg[] ptgs = null;

    private static WorkbookEvaluator evaluator = new WorkbookEvaluator(null,null,null);
    private static OperationEvaluationContext context = new OperationEvaluationContext(null,null,
            0,0,0,null,null,null);

    TransformOperator(){
        super();
    }

    @Override
    public abstract List getEvaluationResult(FormulaExecutor context) throws OptimizationError ;

    public abstract void merge(DataOperator TransformOperator) throws OptimizationError;

    static Object evaluate(final Ptg[] ptgs){
        try {
            ValueEval result = evaluator.evaluateFormula(context,ptgs,true,true,null);
            return FormulaEngineImpl.convertToEvaluationResult(result).getValue();
        }
        catch (NullPointerException | EvaluationException e){
            e.printStackTrace();
        }


        return null;
    }

    Ptg valueToPtg(Object value) throws OptimizationError {
        if (value instanceof Double)
            return  new NumberPtg((Double)value);
        else if (value instanceof String)
            return new StringPtg((String) value);
        else
            throw OptimizationError.UNSUPPORTED_TYPE;
    }

    @Override
    void cleanInEdges(){// todo:remove edges in Transform Operator
        throw OptimizationError.UNSUPPORTED_CASE;
    }

    @Override
    void cleanOutEdges(){
        throw OptimizationError.UNSUPPORTED_CASE;
    }
}
