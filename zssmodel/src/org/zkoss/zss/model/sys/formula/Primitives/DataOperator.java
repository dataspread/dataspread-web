package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

public abstract class DataOperator extends PhysicalOperator implements MultiOutputOperator{
    SSheet _sheet = null;
    CellRegion _region = null;
//    List<SCell> updateCells = new ArrayList<>(); todo:add it when bulk writing fixed

    public DataOperator(SSheet sheet, CellRegion region){
        super();
        _sheet = sheet;
        _region = region; // todo: add it somewhere else
//                region.getOverlap(
//                new CellRegion(0,0,_sheet.getEndRowIndex(),_sheet.getEndColumnIndex()));

    }

    public DataOperator(){
        super();
    }

    public CellRegion getRegion(){
        return _region;
    }

    public SSheet getSheet() {
        return _sheet;
    }

    int getIndex(SCell cell){
        return  (cell.getRowIndex() - _region.getRow()) * _region.getColumnCount() +
                cell.getColumnIndex() - _region.getColumn();
    }

    @Override
    public abstract void evaluate(FormulaExecutor context) throws OptimizationError ;

    public abstract void merge(DataOperator dataOperator) throws OptimizationError;

    AbstractCellAdv[] getCells(){
        AbstractCellAdv[] cells = new AbstractCellAdv[_region.getCellCount()];
        for (SCell cell:_sheet.getCells(_region))
            cells[getIndex(cell)] = (AbstractCellAdv)cell;
        return cells;
    }

    void setFormulaValue(AbstractCellAdv cell, Object result,FormulaExecutor context) throws OptimizationError {
        ValueEval resultValue;
        if (result instanceof Double){
            resultValue = new NumberEval((Double)result);
        }
        else {
            System.out.println(result.toString() + result.hashCode());
            throw new RuntimeException(OptimizationError.UNSUPPORTED_TYPE);
        }
        cell.setFormulaResultValue(resultValue);
        context.addToUpdateQueue(_sheet, cell);
    }

    @Override
    public int outputSize(){
        return _region.getCellCount();
    }
}
