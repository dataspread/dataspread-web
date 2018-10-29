package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.ArrayList;
import java.util.List;

public class AggregateOperator extends PhysicalOperator {
    private BinaryFunction binaryFunction;
    public AggregateOperator(BinaryFunction f){
        super();
        binaryFunction = f;
    }

    @Override
    public void evaluate(FormulaExecutor context) {
        assert inEdges.get(0).resultIsReady();
        List<Double> data = (List<Double>)inEdges.get(0).popResult();
        List<Double> result = new ArrayList<>();
        result.add(binaryFunction.groupEvaluate(data));
        for (Edge o:outEdges){
            o.setResult(result);
        }
        _evaluated = true;
    }

}
