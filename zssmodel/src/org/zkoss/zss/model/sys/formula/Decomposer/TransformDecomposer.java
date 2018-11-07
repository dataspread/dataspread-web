package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.poi.ss.formula.ptg.DividePtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.SingleTransformOperator;

import java.util.Arrays;

public class TransformDecomposer extends FunctionDecomposer {

    FunctionDecomposer[] functions;
    Ptg ptg;

    static TransformDecomposer divide(FunctionDecomposer function1, FunctionDecomposer function2){
        return new TransformDecomposer(new FunctionDecomposer[]{function1,
                function2}, DividePtg.instance);
    }

    private TransformDecomposer(FunctionDecomposer[] functions, Ptg transformPtg){
        ptg = transformPtg;
        this.functions = functions;

    }

    @Override
    public LogicalOperator decompose(LogicalOperator[] ops) throws OptimizationError {
        return new SingleTransformOperator(
                Arrays.stream(functions)
                .map(f -> f.decompose(ops))
                .toArray(LogicalOperator[]::new), ptg);
    }
}
