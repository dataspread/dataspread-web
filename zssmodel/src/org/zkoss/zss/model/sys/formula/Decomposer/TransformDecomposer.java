package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.poi.ss.formula.ptg.*;
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

    static TransformDecomposer subtract(FunctionDecomposer function1, FunctionDecomposer function2){
        return new TransformDecomposer(new FunctionDecomposer[]{function1,
                function2}, SubtractPtg.instance);
    }

    static TransformDecomposer multiply(FunctionDecomposer function1, FunctionDecomposer function2){
        return new TransformDecomposer(new FunctionDecomposer[]{function1,
                function2}, MultiplyPtg.nonOperatorInstance);
    }

    static TransformDecomposer sqrt(FunctionDecomposer function1){
        return new TransformDecomposer(new FunctionDecomposer[]{function1}, FuncPtg.create(FunctionDecomposer.SQRT));
    }

    static TransformDecomposer ONE = new TransformDecomposer(new FunctionDecomposer[]{}, new NumberPtg(1));

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
