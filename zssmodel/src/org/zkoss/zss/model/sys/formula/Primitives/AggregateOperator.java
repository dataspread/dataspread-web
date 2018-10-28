package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Primitives.Datastructure.DataWrapper;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public class AggregateOperator extends PhysicalOperator {
    private BinaryFunction binaryFunction;
    public AggregateOperator(BinaryFunction f){
        super();
        binaryFunction = f;
    }

    @Override
    public void evaluate(FormulaExecutor context) {
        assert inEdges.get(0).resultIsReady();
        DataWrapper<Double> data = (DataWrapper<Double>)inEdges.get(0).popResult();
        DataWrapper<Double> result = new DataWrapper<Double>(binaryFunction.groupEvaluate(data));
        for (Edge o:outEdges){
            o.setResult(result);
        }
        _evaluated = true;
    }

}
