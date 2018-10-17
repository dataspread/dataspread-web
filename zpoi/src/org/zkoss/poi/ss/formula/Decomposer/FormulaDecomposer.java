package org.zkoss.poi.ss.formula.Decomposer;

import org.zkoss.poi.hssf.util.CellReference;
import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.Primitives.*;
import org.zkoss.poi.ss.formula.QueryOptimization.QueryPlanGraph;
import org.zkoss.poi.ss.formula.eval.*;
import org.zkoss.poi.ss.formula.functions.Choose;
import org.zkoss.poi.ss.formula.functions.IfFunc;
import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.poi.util.POILogger;
import org.zkoss.zss.model.impl.AbstractCellAdv;

import java.util.Arrays;
import java.util.Stack;

import static org.zkoss.poi.ss.formula.Primitives.LogicalOperator.connect;
import static org.zkoss.poi.ss.formula.WorkbookEvaluator.countTokensToBeSkipped;

public class FormulaDecomposer {

    static FunctionDecomposer[] logicalOpDict = produceLogicalOperatorDictionary();

    public static QueryPlanGraph decomposeFormula(Ptg[] ptgs, AbstractCellAdv target){
        QueryPlanGraph result = new QueryPlanGraph();
        Stack<LogicalOperator> stack = new Stack<>();
        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {
            Ptg ptg = ptgs[i];

            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = (AttrPtg) ptg;
                if (attrPtg.isSum()) {
                    ptg = FuncVarPtg.SUM;
                }
                if (attrPtg.isOptimizedChoose()) {
                    throw new IllegalStateException("Unsupported case:attrPtg.isOptimizedChoose()");
                }
                if (attrPtg.isOptimizedIf()){
                    throw new IllegalStateException("Unsupported case:attrPtg.isOptimizedIf()");
                }
                if (attrPtg.isSkip()) {
                    throw new IllegalStateException("Unsupported case:attrPtg.isSkip()");
                }
            }
            if (ptg instanceof ControlPtg) {
                // skip Parentheses, Attr, etc
                continue;
            }
            if (ptg instanceof MemFuncPtg || ptg instanceof MemAreaPtg) {
                // can ignore, rest of tokens for this expression are in OK RPN order
                continue;
            }
            if (ptg instanceof MemErrPtg) {
                continue;
            }

            if (ptg instanceof FilterHelperPtg) {
                throw new IllegalStateException("Unsupported case:(ptg instanceof FilterHelperPtg");
            }

            LogicalOperator opResult = null;
            if (ptg instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptg;
                if (!(ptg instanceof AbstractFunctionPtg)){
                    throw new IllegalStateException("Unsupported case");
                }
                AbstractFunctionPtg fptg = (AbstractFunctionPtg)ptg;
                if (optg.getInstance() != null)
                    optg = optg.getInstance();
                int numops = optg.getNumberOfOperands();
                LogicalOperator[] ops = new LogicalOperator[numops];

                // storing the ops in reverse order since they are popping
                for (int j = numops - 1; j >= 0; j--) {
                    LogicalOperator p = stack.pop();
                    if (optg instanceof FuncVarPtg && ((FuncVarPtg) optg).isExternal()) {
                    } else {

                    }
                    ops[j] = p;
                }
//				logDebug("invoke " + operation + " (nAgs=" + numops + ")");
                opResult = logicalOpDict[fptg.getFunctionIndex()].decompose(ops);
            } else if (ptg instanceof RelTableAttrPtg) {
                throw new IllegalStateException("Unsupported case:((ptg instanceof RelTableAttrPtg)");
            } else if (ptg instanceof AreaPtg){
                opResult = new DataReadOperator((AreaPtg) ptg);
                result.addData((DataReadOperator) opResult);

            } else {
                throw new IllegalStateException("Unsupported case");
            }
            if (opResult == null) {
                throw new RuntimeException("Evaluation result must not be null");
            }
//			logDebug("push " + opResult);
            stack.push(opResult);
        }

        LogicalOperator value = stack.pop();
        DataWriteOperator targetCell = new DataWriteOperator(target);
        connect(value,targetCell);
        if (!stack.isEmpty()) {
            throw new IllegalStateException("evaluation stack not empty");
        }
        return result;

    }

    private static FunctionDecomposer[] produceLogicalOperatorDictionary() {
        FunctionDecomposer[] retval = new FunctionDecomposer[378];

        retval[4] = new FunctionDecomposer(){

            @Override
            public LogicalOperator decompose(LogicalOperator[] ops) {
                LogicalOperator ret = new AggregateOperator(BinaryFunction.PLUS);
                connect(ops[0],ret);
                return ret;
            }
        };






        return retval;
    }
}
