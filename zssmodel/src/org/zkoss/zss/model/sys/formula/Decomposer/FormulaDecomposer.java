package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.*;
import org.zkoss.zss.model.sys.formula.QueryOptimization.QueryPlanGraph;
import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.range.impl.RangeImpl;

import java.util.Stack;

import static org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator.connect;

public class FormulaDecomposer {

    static FunctionDecomposer[] logicalOpDict = produceLogicalOperatorDictionary();

    public static QueryPlanGraph decomposeFormula(Ptg[] ptgs, SCell target) throws OptimizationError {
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
                    throw new OptimizationError("Unsupported case:attrPtg.isOptimizedChoose()");
                }
                if (attrPtg.isOptimizedIf()){
                    throw new OptimizationError("Unsupported case:attrPtg.isOptimizedIf()");
                }
                if (attrPtg.isSkip()) {
                    throw new OptimizationError("Unsupported case:attrPtg.isSkip()");
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
                throw new OptimizationError("Unsupported case:(ptg instanceof FilterHelperPtg");
            }

            LogicalOperator opResult = null;
            if (ptg instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptg;
                if (!(ptg instanceof AbstractFunctionPtg)){
                    throw new OptimizationError("Unsupported case");
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
                throw new OptimizationError("Unsupported case:((ptg instanceof RelTableAttrPtg)");
            } else if (ptg instanceof AreaPtg){
                CellRegion region = new CellRegion(((AreaPtg)ptg).getFirstRow(), ((AreaPtg)ptg).getFirstColumn(),
                        ((AreaPtg)ptg).getLastRow(), ((AreaPtg)ptg).getLastColumn());
                opResult = new SingleDataOperator(new RangeImpl(target.getSheet(),region));
                result.addData((DataOperator) opResult);

            } else {
                throw new OptimizationError("Unsupported case");
            }
            if (opResult == null) {
                throw new RuntimeException("Evaluation result must not be null");
            }
//			logDebug("push " + opResult);
            stack.push(opResult);
        }

        LogicalOperator value = stack.pop();
        DataOperator targetCell = new SingleDataOperator(
                new RangeImpl(target.getSheet(),target.getRowIndex(),target.getColumnIndex()));
        connect(value,targetCell);
        result.addData(targetCell);
        if (!stack.isEmpty()) {
            throw new OptimizationError("evaluation stack not empty");
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
