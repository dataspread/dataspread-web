package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.List;

public class AggregateOperator extends PhysicalOperator {
    private BinaryFunction binaryFunction;
    private Double result = null;
    public AggregateOperator(BinaryFunction f){
        super();
        binaryFunction = f;
    }

    @Override
    public void evaluate() {
        List data = (List)((PhysicalOperator)inOp.get(0)).getOutput(this);
        result = binaryFunction.groupEvaluate((Double[])data.toArray());
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
