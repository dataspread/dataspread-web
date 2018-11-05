package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.*;
import java.util.function.Consumer;

public class SingleTransformOperator extends TransformOperator {

    public SingleTransformOperator(Ptg ptg) throws OptimizationError {
        super();
        if (!(ptg instanceof ScalarConstantPtg)){
            throw OptimizationError.UNSUPPORTED_CASE;
        }
        ptgs = new Ptg[] {ptg};


    }

    public SingleTransformOperator(LogicalOperator[] operators, Ptg ptg) throws OptimizationError {
        super();
        ptgs = new Ptg[getPtgSize(operators) + 1];
        final Map<LogicalOperator,Integer> operatorId = new TreeMap<>((o1, o2) -> {
            if (o1.hashCode() == o2.hashCode()){
                if (o1 == o2)
                    return 0;
                else {
                    StringBuilder inEdge1 = new StringBuilder();
                    o1.forEachInEdge((e)->inEdge1.append(e.hashCode()));
                    StringBuilder inEdge2 = new StringBuilder();
                    o2.forEachInEdge((e)->inEdge2.append(e.hashCode()));
                    StringBuilder outEdge1 = new StringBuilder();
                    o1.forEachOutEdge((e)->outEdge1.append(e.hashCode()));
                    StringBuilder outEdge2 = new StringBuilder();
                    o2.forEachOutEdge((e)->outEdge2.append(e.hashCode()));
                    int cmp = (o1.toString() + inEdge1 + outEdge1)
                            .compareTo(o2.toString() + inEdge2 + outEdge2);
                    assert cmp != 0;
                    return cmp;
                }
            }
            return o1.hashCode() - o2.hashCode();
        });
        int cursor = 0;
        for (LogicalOperator op:operators){
            if (op instanceof DataOperator || op instanceof AggregateOperator){
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
    public void evaluate(FormulaExecutor context) throws OptimizationError {

        Ptg[] data = new Ptg[inDegree()];

        try {
            forEachInEdge(new Consumer<Edge>() {
                int i = 0;
                @Override
                public void accept(Edge edge) {
                    Object result = edge.popResult().get(0);
                    if (result instanceof Double)
                        data[i++] = new NumberPtg((Double)result);
                    else if (result instanceof String)
                        data[i++] = new StringPtg((String) result);
                    else
                        throw new RuntimeException(OptimizationError.UNSUPPORTED_TYPE);
                }
            });
        }
        catch (RuntimeException e){
            throw (OptimizationError)e.getCause();
        }


        Ptg[] ptgs = Arrays.copyOf(this.ptgs, this.ptgs.length);
        for (int i = 0, isize = ptgs.length; i < isize;i++)
            if (ptgs[i] instanceof  RefVariablePtg){
                ptgs[i] = data[((VariablePtg)ptgs[i]).getIndex()];
            }

        List result = Collections.singletonList(evaluate(ptgs));

        forEachOutEdge((e)->e.setResult(result));
    }

    @Override
    public void merge(DataOperator TransformOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    public int returnSize() {
        return 1;
    }

    private int getPtgSize(LogicalOperator[] operators) throws OptimizationError {
        int size = 0;
        for (LogicalOperator op:operators){
            if (op instanceof DataOperator){
                if (((DataOperator) op).getRegion().getCellCount() > 1)
                    throw new OptimizationError("Multiple Values in Single Operator");
                size++;
                continue;
            }
            if (op instanceof AggregateOperator){
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
