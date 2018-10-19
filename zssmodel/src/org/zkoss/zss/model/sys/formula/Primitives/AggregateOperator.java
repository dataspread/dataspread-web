package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.List;

public class AggregateOperator extends PhysicalOperator {
    private BinaryFunction binaryFunction;
    private Double result = null;
    public AggregateOperator(BinaryFunction f){
        super();
        binaryFunction = f;
    }

    @Override
    public void evaluate(FormulaExecutor context) {
        List data = (List)((PhysicalOperator)inOp.get(0)).getOutput(this);
        result = binaryFunction.groupEvaluate(data);
        _evaluated = true;
    }

    @Override
    public void clean() {
        super.clean();
        result = null;
    }

    @Override
    Object getOutput(PhysicalOperator op) {
        super.getOutput(op);
        return result;
    }
}
