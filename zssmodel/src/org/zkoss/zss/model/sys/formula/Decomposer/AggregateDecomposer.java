package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.poi.ss.formula.ptg.AddPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.*;

import static org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator.connect;

public class AggregateDecomposer extends FunctionDecomposer {

    BinaryFunction function;
    Ptg aggregatePtg;

    AggregateDecomposer(BinaryFunction binaryFunction, Ptg ptg){
        super();
        function = binaryFunction;
        aggregatePtg = ptg;
    }

    @Override
    public LogicalOperator decompose(LogicalOperator[] ops) throws OptimizationError {
        LogicalOperator lastOp = null;
        LogicalOperator op = null;
        for (int i = 0; i < ops.length; i++){
            if (ops[i] instanceof MultiOutputOperator && ((MultiOutputOperator) ops[i]).outputSize() > 1){
                op = new AggregateOperator(function);
                connect(ops[i],op);
            }
            else
                op = ops[i];

            if (lastOp != null)
                op = new SingleTransformOperator(
                        new LogicalOperator[]{lastOp,op}, aggregatePtg);

            lastOp = op;
        }
        return op;
    }
}
