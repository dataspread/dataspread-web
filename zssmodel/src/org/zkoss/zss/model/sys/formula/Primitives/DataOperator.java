package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.range.SRange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler.getScheduler;

public class DataOperator extends PhysicalOperator{
    private SRange _range;
    private List<Object> data = null;
    public DataOperator(SRange range){
        super();
        _range = range;
    }



    @Override
    public void evaluate() throws OptimizationError {
        SSheet sheet = _range.getSheet();
        if (inOp.size() == 0){
            data = new ArrayList<>();
            for (int i = _range.getRow(); i <= _range.getLastRow(); i++)
                for (int j = _range.getColumn(); j <= _range.getLastColumn(); j++) {
                    SCell cell = sheet.getCell(i, j);
                    if (cell.getType() != SCell.CellType.NUMBER)
                        throw new OptimizationError("Unexpected cell type");
                    data.add(cell.getValue());
                }
            _evaluated = true;
        }
        else{
            for (LogicalOperator op:inOp)
                if (!(op instanceof PhysicalOperator) || !((PhysicalOperator)op).isEvaluated())
                    return;

            if (_range.getRow() == _range.getLastRow() && _range.getColumn() == _range.getLastColumn()){
                Object result = ((PhysicalOperator)inOp.get(0)).getOutput(this);
                data = new ArrayList<>();
                data.add(result);
                setFormulaValue(_range.getRow(),_range.getColumn(),result,sheet);
            }else {
                List results = (List) ((PhysicalOperator)inOp.get(0)).getOutput(this);
                data = results;
                Iterator it= results.iterator();
                for (int i = _range.getRow(); i <= _range.getLastRow(); i++)
                    for (int j = _range.getColumn(); j <= _range.getLastColumn(); j++) {
                        Object result = it.next();
                        setFormulaValue(i,j,result,sheet);
                    }
            }
            _evaluated = true;
        }
    }

    private void setFormulaValue(int row, int column, Object result, SSheet sheet) throws OptimizationError {
        ValueEval resultValue;
        if (result instanceof Double){
            resultValue = new NumberEval((Double)result);
        }
        else
            throw new OptimizationError("Unsupported result type");
        AbstractCellAdv sCell = ((AbstractCellAdv)sheet.getCell(row,column));
        sCell.setFormulaResultValue(resultValue);
        getScheduler().update(sheet.getBook(), sheet, sCell.getCellRegion(),
                sCell.getValue(true, true).toString(),
                sCell.getFormulaValue());
    }



    @Override
    public void clean() {
        super.clean();
        data = null;
    }

    @Override
    Object getOutput(PhysicalOperator op){
        super.getOutput(op);
        return data;
    }
}
