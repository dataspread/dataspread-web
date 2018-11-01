package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;
import org.zkoss.zss.range.SRange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SingleDataOperator extends DataOperator{
    public SingleDataOperator(SRange range){
        super(range);
    }

    @Override
    public void evaluate(FormulaExecutor context) throws OptimizationError {
        List results;
        if (inEdges.size() == 0){
            results = new ArrayList<>();

            for (SCell cell : _sheet.getCells(_region)){
                if (cell.getType() != SCell.CellType.NUMBER)
                    throw OptimizationError.UNSUPPORTED_TYPE;
                results.add(cell.getValue());
            }
            _evaluated = true;
        }
        else{
            for (Edge e: inEdges)
                if (!e.resultIsReady())
                    return;

            results = inEdges.get(0).popResult();
            Iterator it= results.iterator();
            for (int i = _region.getRow(); i <= _region.getLastRow(); i++)
                for (int j = _region.getColumn(); j <= _region.getLastColumn(); j++) {
                    Object result = it.next();
                    setFormulaValue(i,j,result,context);
                }
        }
        for (Edge o:outEdges){
            o.setResult(results);
        }
        _evaluated = true;
    }

    @Override
    public void merge(DataOperator dataOperator) throws OptimizationError {
        if (!(dataOperator instanceof SingleDataOperator))
            throw OptimizationError.UNSUPPORTED_FUNCTION;
        inEdges.addAll(dataOperator.inEdges);
        outEdges.addAll(dataOperator.outEdges);
    }

    private void setFormulaValue(int row, int column, Object result,FormulaExecutor context) throws OptimizationError {
        ValueEval resultValue;
        if (result instanceof Double){
            resultValue = new NumberEval((Double)result);
        }
        else
            throw OptimizationError.UNSUPPORTED_TYPE;
        AbstractCellAdv sCell = ((AbstractCellAdv)_sheet.getCell(row,column));
        sCell.setFormulaResultValue(resultValue);
        context.update(_sheet,sCell);
    }
}
