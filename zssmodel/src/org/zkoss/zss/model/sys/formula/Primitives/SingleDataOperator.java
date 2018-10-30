package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
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
        SSheet sheet = _range.getSheet();
        List results;
        if (inEdges.size() == 0){
            results = new ArrayList<>();

            for (int i = _range.getRow(); i <= _range.getLastRow(); i++)
                for (int j = _range.getColumn(); j <= _range.getLastColumn(); j++) {
                    SCell cell = sheet.getCell(i, j);
                    if (cell.getType() != SCell.CellType.NUMBER)
                        throw new OptimizationError("Unexpected cell type");
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
            for (int i = _range.getRow(); i <= _range.getLastRow(); i++)
                for (int j = _range.getColumn(); j <= _range.getLastColumn(); j++) {
                    Object result = it.next();
                    setFormulaValue(i,j,result,sheet,context);
                }
        }
        for (Edge o:outEdges){
            o.setResult(results);
        }
        _evaluated = true;
    }

    private void setFormulaValue(int row, int column, Object result, SSheet sheet,FormulaExecutor context) throws OptimizationError {
        ValueEval resultValue;
        if (result instanceof Double){
            resultValue = new NumberEval((Double)result);
        }
        else
            throw new OptimizationError("Unsupported result type");
        AbstractCellAdv sCell = ((AbstractCellAdv)sheet.getCell(row,column));
        sCell.setFormulaResultValue(resultValue);
        context.update(sheet,sCell);
    }
}
