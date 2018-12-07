package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.functions.Countif.CmpOp;
import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.List;
import java.util.function.Consumer;

public abstract class FilterOperator extends PhysicalOperator implements MultiOutputOperator {

    private static final int CRITERIA = 0;
    private static final int FILTERRANGE = 1;
    private static final int RETURNRANGE = 2;

    private static FilterOperator newSingleFilter(CmpOp op, LogicalOperator criteria){
        if (op == CmpOp.OP_EQ || op == CmpOp.OP_NONE)
            return new SingleEqualFilterOperator(criteria);
        else
            return new SingleRangeFilterOperator(criteria);
    }

    private static FilterOperator newSingleFilter(CmpOp op, String literal){
        if (op == CmpOp.OP_EQ || op == CmpOp.OP_NONE)
            return new SingleEqualFilterOperator(literal);
        else
            return new SingleRangeFilterOperator(literal);
    }

    private static FilterOperator getCriteria(LogicalOperator criteria ){
        if (!(criteria instanceof SingleTransformOperator)){
            if (criteria instanceof MultiOutputOperator
                    && ((MultiOutputOperator)criteria).outputSize() > 1)
                throw new OptimizationError("Wrong Parameters");
            return new SingleEqualFilterOperator(criteria);
        }

        SingleTransformOperator combinedCriteria = (SingleTransformOperator) criteria;

        Ptg[] ptgs = combinedCriteria.ptgs;

        if ((!(ptgs[0] instanceof StringPtg) && ptgs.length > 1) || ptgs.length > 3
                || !(ptgs[0] instanceof ScalarConstantPtg)){
            return new SingleEqualFilterOperator(criteria);
        }

        if (!(ptgs[0] instanceof StringPtg)) // todo: do not treat everything as string.
            return new SingleEqualFilterOperator(ptgs[0].toFormulaString());



        String predicate = ((StringPtg)ptgs[0]).getValue();

        CmpOp op = CmpOp.getOperator(predicate);

        if (ptgs.length > 1 && (predicate.length() > op.getLength() ||
                !(ptgs.length == 3 && ptgs[2] instanceof ConcatPtg))) // Judge if it is operator + & + Reference
            return newSingleFilter(op,criteria);


        if (ptgs.length == 1)
            return newSingleFilter(op,predicate.substring(op.getLength()));

        if (criteria.inDegree() == 1){
            FilterOperator ret;
            if (op == CmpOp.OP_EQ || op == CmpOp.OP_NONE)
                ret = new SingleEqualFilterOperator((String)null);
            else
                ret = new SingleRangeFilterOperator((String)null);
            ret.transferInEdge(criteria.getFirstInEdge());
            return ret;
        }

        if (ptgs[1] instanceof ScalarConstantPtg)
            return newSingleFilter(op,ptgs[1].toFormulaString());
        else
            return newSingleFilter(op,criteria);
    }

    public static FilterOperator buildSingleFilter(LogicalOperator[] ops){

        //todo:supprot ifs

        if (ops.length != 2)
            throw OptimizationError.UNSUPPORTED_CASE;

        FilterOperator filter = getCriteria(ops[CRITERIA]);

        connect(ops[FILTERRANGE],filter);

        return filter;

    }

    public void split(){
        if (this instanceof EqualFilterOperator){
            SingleIndexOperator index = new SingleEqualIndexOperator();
            ScanOperator scan = new ScanOperator();
            connect(index,scan);
            forEachInEdge(new Consumer<Edge>() {
                int i = 0;
                @Override
                public void accept(Edge edge) {
                    if (i == inDegree() - 1){ // todo: check if there is remove
                        return;
                    }

                    if (i % 2 != CRITERIA){
                        index.transferInEdge(edge);
                    }
                    else{
                        scan.transferInEdge(edge);
                    }

                    i++;
                }
            });
            forEachOutEdge(scan::transferOutEdge);
        }
        else{
            throw OptimizationError.UNSUPPORTED_FUNCTION;
        }

    }

    @Override
    public int outputSize(){
        return Integer.MAX_VALUE;
    }
}
