package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.Collections;
import java.util.List;

public class SingleAggregateOperator extends AggregateOperator {
    public SingleAggregateOperator(BinaryFunction f){
        super(f);
    }

    @Override
    public List getEvaluationResult(FormulaExecutor context) {
        List<Double> data = (List<Double>) getFirstInEdge().popResult();
        List<Double> result = Collections.singletonList(binaryFunction.groupEvaluate(data));
        return result;
    }

    @Override
    public void merge(AggregateOperator aggregate) throws OptimizationError {
        if (!(aggregate instanceof SingleAggregateOperator))
            throw OptimizationError.UNSUPPORTED_FUNCTION;
        aggregate.forEachInEdge(Edge::remove);
        aggregate.forEachOutEdge(this::transferOutEdge);
    }

}
