package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.poi.ss.formula.functions.Function;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.SingleTransformOperator;

public class TransformDecomposer extends FunctionDecomposer {

    FunctionDecomposer f1,f2;
    Ptg ptg;

    TransformDecomposer(FunctionDecomposer function1, FunctionDecomposer function2, Ptg transformPtg){
        ptg = transformPtg;
        f1 = function1;
        f2 = function2;

    }

    @Override
    public LogicalOperator decompose(LogicalOperator[] ops) throws OptimizationError {
        return new SingleTransformOperator(new LogicalOperator[]{f1.decompose(ops),f2.decompose(ops)},ptg);
    }
}
