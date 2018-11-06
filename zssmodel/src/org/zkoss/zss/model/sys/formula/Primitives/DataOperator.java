package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;
import org.zkoss.zss.range.SRange;

public abstract class DataOperator extends PhysicalOperator{
    SSheet _sheet = null;
    CellRegion _region = null;
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

    public static DataOperator getFatherOfConstant(){
        return fatherOfConstant;
    }

    @Override
    public abstract void evaluate(FormulaExecutor context) throws OptimizationError ;

    public abstract void merge(DataOperator dataOperator) throws OptimizationError;

    private final static DataOperator fatherOfConstant = new DataOperator() {
        @Override
        public void evaluate(FormulaExecutor context) {
            forEachOutEdge((e)->{
                e.setResult(null);
                e.remove();
            });
        }

        @Override
        public void merge(DataOperator dataOperator) throws OptimizationError {
            throw OptimizationError.UNSUPPORTED_CASE;
        }
    };
}
