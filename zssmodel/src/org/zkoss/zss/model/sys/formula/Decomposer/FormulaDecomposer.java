package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.*;
import org.zkoss.zss.model.sys.formula.QueryOptimization.QueryPlanGraph;
import org.zkoss.zss.range.impl.RangeImpl;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import static org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator.connect;

public class FormulaDecomposer {

    static FunctionDecomposer[] logicalOpDict = produceLogicalOperatorDictionary();

    public static QueryPlanGraph decomposeFormula(Ptg[] ptgs, SCell target) throws OptimizationError {
        QueryPlanGraph result = new QueryPlanGraph();
        Stack<LogicalOperator> stack = new Stack<>();

        Map<String, DataOperator> dataOperatorMap = new TreeMap<>();

        for (Ptg ptg:ptgs){
            if((ptg instanceof RefPtgBase || ptg instanceof AreaPtgBase)
                    && !dataOperatorMap.containsKey(ptg.toString())){
                DataOperator data;
                if (ptg instanceof RefPtgBase){
                    RefPtgBase rptg = (RefPtgBase)ptg;
                    data = new SingleDataOperator(target.getSheet(),
                            new CellRegion(rptg.getRow(),rptg.getColumn()));
                }
                else{
                    AreaPtgBase rptg = (AreaPtgBase)ptg;
                    data = new SingleDataOperator(target.getSheet(),
                            new CellRegion(rptg.getFirstRow(),rptg.getFirstColumn(),
                                    rptg.getLastRow(),rptg.getLastColumn()));
                }
                dataOperatorMap.put(ptg.toString(),data);
                result.addData(data);
            }
        }

        for (int i = 0, iSize = ptgs.length; i < iSize; i++) {
            Ptg ptg = ptgs[i];

            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = (AttrPtg) ptg;
                if (attrPtg.isSum()) {
                    ptg = FuncVarPtg.SUM;
                }
                if (attrPtg.isOptimizedChoose()) {
                    throw OptimizationError.UNSUPPORTED_CASE;
                }
                if (attrPtg.isOptimizedIf()){
                    throw OptimizationError.UNSUPPORTED_CASE;
                }
                if (attrPtg.isSkip()) {
                    throw OptimizationError.UNSUPPORTED_CASE;
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
                throw OptimizationError.UNSUPPORTED_CASE;
            }

            LogicalOperator opResult = null;
            if (ptg instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptg;
                if (!(ptg instanceof AbstractFunctionPtg || ptg instanceof ValueOperatorPtg)){
                    throw OptimizationError.UNSUPPORTED_CASE;
                }
                AbstractFunctionPtg fptg = (AbstractFunctionPtg)ptg;
                if (optg.getInstance() != null)
                    optg = optg.getInstance();
                int numops = optg.getNumberOfOperands();
                LogicalOperator[] ops = new LogicalOperator[numops];

                boolean constantFormula = true;

                // storing the ops in reverse order since they are popping
                for (int j = numops - 1; j >= 0; j--) {
                    LogicalOperator p = stack.pop();
                    ops[j] = p;
                    if (p instanceof DataOperator && ((DataOperator)p).getRegion().getCellCount() > 1)
                        constantFormula = false;
                }
//				logDebug("invoke " + operation + " (nAgs=" + numops + ")");
                if (!constantFormula)
                    opResult = logicalOpDict[fptg.getFunctionIndex()].decompose(ops);
                else
                    opResult = new SingleTransformOperator(ops, ptg);

            } else if (ptg instanceof RelTableAttrPtg) {
                throw OptimizationError.UNSUPPORTED_CASE;
            } else if (ptg instanceof AreaPtgBase || ptg instanceof RefPtgBase){
                opResult = dataOperatorMap.get(ptg.toString());

            } else if (ptg instanceof ScalarConstantPtg){
                opResult = new SingleTransformOperator(ptg);
            }
            else {
                throw OptimizationError.UNSUPPORTED_CASE;
            }
            if (opResult == null) {
                throw new RuntimeException("Evaluation result must not be null");
            }
//			logDebug("push " + opResult);
            stack.push(opResult);
        }

        LogicalOperator value = stack.pop();
        DataOperator targetCell = new SingleDataOperator(target.getSheet(), target.getCellRegion());
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
                assert ops.length == 1;
                LogicalOperator ret = new AggregateOperator(BinaryFunction.PLUS);
                connect(ops[0],ret);
                return ret;
            }
        };






        return retval;
    }
}
