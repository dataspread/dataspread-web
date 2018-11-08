package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GroupedTransformOperator extends TransformOperator implements MultiOutputOperator {

    private int size;

    private List<Integer>[] variablePositionMap;

    public GroupedTransformOperator(LogicalOperator[] operators, Ptg ptgs[]) throws OptimizationError {
        assert operators.length > 0;
        this.ptgs = ptgs;
        size = -1;
        for (LogicalOperator op:operators){
            if (!(op instanceof DataOperator))
                throw OptimizationError.UNSUPPORTED_CASE;
            if (size == -1)
                size = ((DataOperator)op).outputSize();
            else
                assert ((DataOperator)op).outputSize() == size;
        }
        int[] ids = addInputOperators(operators);
        variablePositionMap = Stream.generate((Supplier<ArrayList>) ArrayList<Integer>::new)
                .limit(inDegree())
                .toArray((IntFunction<List<Integer>[]>) List[]::new);
        for (int i = 0, isize = ptgs.length; i < isize;i++)
            if (ptgs[i] instanceof RefVariablePtg){
                VariablePtg ptg = (VariablePtg)ptgs[i];
                int index = ids[ptg.getIndex()];
                ptg.setIndex(index);
                variablePositionMap[index].add(i);
            }

    }

    private int[] addInputOperators(LogicalOperator[] operators){
        final Map<LogicalOperator,Integer> operatorId = new TreeMap<>();
        int[] ret = new int[operators.length];
        for (int i = 0; i < operators.length;i++){
            LogicalOperator op = operators[i];
            if (!operatorId.containsKey(op)){
                operatorId.put(op,inDegree());
                ret[i] = inDegree();
                connect(op,this);
            }
            else
                ret[i] = operatorId.get(op);
        }
        return ret;
    }

    @Override
    public List getEvaluationResult(FormulaExecutor context) throws OptimizationError {
        List<Queue<Ptg>> data = new ArrayList<>(inDegree());

        try {
            forEachInEdge(edge -> {
                ArrayDeque<Ptg> ptgs = new ArrayDeque<>();
                data.add(ptgs);
                for (Object result:edge.popResult()) {
                    try {
                        ptgs.add(valueToPtg(result));
                    } catch (OptimizationError optimizationError) {
                        throw new RuntimeException(optimizationError);
                    }
                }
            });
        }
        catch (RuntimeException e){
            throw (OptimizationError)e.getCause();
        }

        List<Object> result = new ArrayList<>();

        Ptg[] ptgs = Arrays.copyOf(this.ptgs, this.ptgs.length);

        while (!data.get(0).isEmpty()) {
            for (int i = 0, isize = variablePositionMap.length; i < isize; i++) {
                for (int j : variablePositionMap[i])
                    ptgs[j] = data.get(i).peek();
                data.get(i).poll();
            }
            result.add(evaluate(ptgs));
        }

        return result;
    }

    @Override
    public void merge(DataOperator TransformOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    public int outputSize() {
        return size;
    }
}
