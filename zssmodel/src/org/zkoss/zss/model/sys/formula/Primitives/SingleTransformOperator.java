package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.*;
import java.util.function.Consumer;

public class SingleTransformOperator extends TransformOperator {

    public SingleTransformOperator(Ptg ptg) {
        super();
        ptgs = new Ptg[] {ptg};
    }

    public SingleTransformOperator(LogicalOperator[] operators, Ptg ptg) throws OptimizationError {
        super();
        ptgs = new Ptg[getPtgSize(operators) + 1];
        final Map<LogicalOperator,Integer> operatorId = new TreeMap<>();
        int cursor = 0;
        for (LogicalOperator op:operators){
            if (op instanceof DataOperator || op instanceof SingleAggregateOperator){
                if (!operatorId.containsKey(op)){
                    operatorId.put(op,inDegree());
                    connect(op,this);
                }
                ptgs[cursor++] = new RefVariablePtg(operatorId.get(op));
                continue;
            }
            if (op instanceof SingleTransformOperator){
                SingleTransformOperator transform = (SingleTransformOperator)op;
                int[] newRefId = new int[transform.inDegree()];

                transform.forEachInEdge(new Consumer<Edge>() {
                    int i = 0;
                    @Override
                    public void accept(Edge edge) {
                        LogicalOperator o = edge.getInVertex();
                        if (operatorId.containsKey(o)){
                            newRefId[i] = operatorId.get(o);
                            edge.remove();
                        }
                        else {
                            operatorId.put(o,inDegree());
                            newRefId[i] = inDegree();
                            transferInEdge(edge);
                        }
                        i++;
                    }
                });

                for (int i = 0; i < transform.ptgs.length; i++){
                    Ptg p =transform.ptgs[i];
                    ptgs[cursor++] = p;
                    if (!(p instanceof VariablePtg))
                        continue;
                    VariablePtg var = (VariablePtg)p;
                    var.setIndex(newRefId[var.getIndex()]);
                }

                continue;
            }
            throw OptimizationError.UNSUPPORTED_CASE;
        }
        assert ptgs.length == cursor + 1;
        ptgs[cursor] = ptg;
    }

    @Override
    public List getEvaluationResult(FormulaExecutor context) throws OptimizationError {

        List<Ptg> data = new ArrayList<>(inDegree());

        try {
            forEachInEdge(edge -> {
                try {
                    data.add(valueToPtg(edge.popResult().get(0)));
                } catch (OptimizationError optimizationError) {
                    throw new RuntimeException(optimizationError);
                }
            });
        }
        catch (RuntimeException e){
            throw (OptimizationError)e.getCause();
        }


        Ptg[] ptgs = Arrays.copyOf(this.ptgs, this.ptgs.length);
        for (int i = 0, isize = ptgs.length; i < isize;i++)
            if (ptgs[i] instanceof  RefVariablePtg){
                ptgs[i] = data.get(((VariablePtg)ptgs[i]).getIndex());
            }

        List result = Collections.singletonList(evaluate(ptgs));

        return result;
    }

    @Override
    public void merge(DataOperator TransformOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    private int getPtgSize(LogicalOperator[] operators) throws OptimizationError {
        int size = 0;
        for (LogicalOperator op:operators){
            if (op instanceof MultiOutputOperator){
                if (((MultiOutputOperator) op).outputSize() > 1)
                    throw new OptimizationError("Multiple Values in Single Operator");
                size++;
                continue;
            }
            if (op instanceof SingleAggregateOperator){
                size++;
                continue;
            }
            if (op instanceof SingleTransformOperator){
                size += ((SingleTransformOperator) op).ptgs.length;
                continue;
            }
            throw OptimizationError.UNSUPPORTED_CASE;
        }
        return size;
    }

}
