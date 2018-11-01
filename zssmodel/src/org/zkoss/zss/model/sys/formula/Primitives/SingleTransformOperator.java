package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.RefPtg;
import org.zkoss.poi.ss.formula.ptg.RefVariablePtg;
import org.zkoss.poi.ss.formula.ptg.ScalarConstantPtg;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SingleTransformOperator extends TransformOperator {

    private List<ScalarConstantPtg> literials;

    public SingleTransformOperator(Ptg ptg) throws OptimizationError {
        super();
        if (!(ptg instanceof ScalarConstantPtg)){
            throw OptimizationError.UNSUPPORTED_CASE;
        }
//        literials = ArrayList.
//                new ArrayList<>(new ScalarConstantPtg[]{(ScalarConstantPtg)ptg});
        ptgs = new Ptg[]{ptg};


    }

    public SingleTransformOperator(LogicalOperator[] operators, Ptg ptg) throws OptimizationError {
        super();
        ptgs = new Ptg[getPtgSize(operators) + 1];
        literials = new ArrayList<>();
        Map<LogicalOperator,Integer> operatorId = new TreeMap<>();
        int cursor = 0;
        for (LogicalOperator op:operators){
            if (op instanceof DataOperator || op instanceof AggregateOperator){
                if (!operatorId.containsKey(op)){
                    operatorId.put(op,inEdges.size());
                    connect(op,this);
                }
                ptgs[cursor++] = new RefVariablePtg(operatorId.get(op));
                continue;
            }
            if (op instanceof SingleTransformOperator){
                SingleTransformOperator transform = (SingleTransformOperator)op;
                int[] newRefId = new int[transform.getInEdges().size()];

                continue;
            }
            throw OptimizationError.UNSUPPORTED_CASE;
        }



    }

    @Override
    public void evaluate(FormulaExecutor context) throws OptimizationError {

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
