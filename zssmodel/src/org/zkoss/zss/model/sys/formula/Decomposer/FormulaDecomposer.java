package org.zkoss.zss.model.sys.formula.Decomposer;

import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.Primitives.*;
import org.zkoss.zss.model.sys.formula.QueryOptimization.QueryPlanGraph;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import static org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator.connect;

public class FormulaDecomposer {

    private FunctionDecomposer[] logicalOpDict = produceLogicalOperatorDictionary();

    private static FormulaDecomposer instance = new FormulaDecomposer();

    public static FormulaDecomposer getInstance(){
        return instance;
    }

    public QueryPlanGraph decomposeFormula(Ptg[] ptgs, SCell target) throws OptimizationError {
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

        for (Ptg ptg1 : ptgs) {
            Ptg ptg = ptg1;

            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = (AttrPtg) ptg;
                if (attrPtg.isSum()) {
                    ptg = FuncVarPtg.SUM;
                }
                if (attrPtg.isOptimizedChoose()) {
                    throw OptimizationError.UNSUPPORTED_CASE;
                }
                if (attrPtg.isOptimizedIf()) {
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

            LogicalOperator opResult;
            if (ptg instanceof OperationPtg) {
                OperationPtg optg = (OperationPtg) ptg;
                if (!(ptg instanceof AbstractFunctionPtg || ptg instanceof ValueOperatorPtg)) {
                    throw OptimizationError.UNSUPPORTED_CASE;
                }
                if (optg.getInstance() != null)
                    optg = optg.getInstance();
                int numops = optg.getNumberOfOperands();
                LogicalOperator[] ops = new LogicalOperator[numops];

                for (int j = numops - 1; j >= 0; j--)
                    ops[j] = stack.pop();

                opResult = getOperationOperator(ops,ptg);
            } else if (ptg instanceof RelTableAttrPtg) {
                throw OptimizationError.UNSUPPORTED_CASE;
            } else if (ptg instanceof AreaPtgBase || ptg instanceof RefPtgBase) {
                opResult = dataOperatorMap.get(ptg.toString());

            } else if (ptg instanceof ScalarConstantPtg) {
                opResult = new SingleTransformOperator(ptg);
            } else {
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

    private  FunctionDecomposer[] produceLogicalOperatorDictionary() {
        FunctionDecomposer[] retval = new FunctionDecomposer[378];

        retval[4] = new FunctionDecomposer(){

            @Override
            public LogicalOperator decompose(LogicalOperator[] ops) throws OptimizationError {
                for (int i = 0; i < ops.length; i++){
                    if (ops[i] instanceof DataOperator && ((DataOperator) ops[i]).getRegion().getCellCount() > 1){
                        LogicalOperator op = new AggregateOperator(BinaryFunction.PLUS);
                        connect(ops[i],op);
                        ops[i] = op;
                    }

                    if (i > 0)
                        ops[i] = new SingleTransformOperator(
                                new LogicalOperator[]{ops[i-1],ops[i]}, AddPtg.instance);

                }
                return ops[ops.length - 1];
            }
        };
        return retval;
    }

    private LogicalOperator getOperationOperator(LogicalOperator[] operators, Ptg ptg) throws OptimizationError {
        boolean constantFormula = true;
        for (LogicalOperator op:operators) {
            if (op instanceof DataOperator && ((DataOperator) op).getRegion().getCellCount() > 1)
                constantFormula = false;
        }
        if (!constantFormula) {
            AbstractFunctionPtg fptg = (AbstractFunctionPtg) ptg;
            return logicalOpDict[fptg.getFunctionIndex()].decompose(operators);
        } else
            return new SingleTransformOperator(operators, ptg);
    }
}
