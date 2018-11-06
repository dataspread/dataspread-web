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
        for (int i = 0; i < ops.length; i++){
            if (ops[i] instanceof DataOperator && ((DataOperator) ops[i]).getRegion().getCellCount() > 1){
                LogicalOperator op = new AggregateOperator(function);
                connect(ops[i],op);
                ops[i] = op;
            }

            if (i > 0)
                ops[i] = new SingleTransformOperator(
                        new LogicalOperator[]{ops[i-1],ops[i]}, aggregatePtg);
        }
        return ops[ops.length - 1];
    }
}
