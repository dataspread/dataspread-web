package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.Collections;
import java.util.List;

public class AggregateOperator extends PhysicalOperator {
    private BinaryFunction binaryFunction;
    public AggregateOperator(BinaryFunction f){
        super();
        binaryFunction = f;
    }

    @Override
    public List getEvaluationResult(FormulaExecutor context) {
        List<Double> data = (List<Double>) getFirstInEdge().popResult();
        List<Double> result = Collections.singletonList(binaryFunction.groupEvaluate(data));

        return result;
    }

}
