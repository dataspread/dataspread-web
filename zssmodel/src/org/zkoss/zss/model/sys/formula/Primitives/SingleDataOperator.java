package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.Arrays;
import java.util.List;

public class SingleDataOperator extends DataOperator{
    public SingleDataOperator(SSheet sheet, CellRegion region){
        super(sheet, region);
    }

    @Override
    public List getEvaluationResult(FormulaExecutor context) throws OptimizationError {
        List results;
        AbstractCellAdv[] cells = getCells();
        if (inDegree() == 0){
            Object[] resultObject = new Object[_region.getCellCount()];
            for (int i = 0; i < cells.length;i++){
                AbstractCellAdv cell = cells[i];
                if (cell.getType() != SCell.CellType.NUMBER && cell.getType() != SCell.CellType.FORMULA)
                    throw OptimizationError.UNSUPPORTED_TYPE;
                resultObject[i] = cell.getValue();
            }
            results = Arrays.asList(resultObject);
        }
        else{
            results = getInEdge(0).popResult();
            for (int i = 0; i < cells.length;i++)
                setFormulaValue(cells[i],results.get(i),context);
        }

        return results;
    }

    @Override
    public void merge(DataOperator dataOperator) throws OptimizationError {
        if (!(dataOperator instanceof SingleDataOperator))
            throw OptimizationError.UNSUPPORTED_FUNCTION;
        dataOperator.forEachInEdge(this::transferInEdge);
        dataOperator.forEachOutEdge(this::transferOutEdge);
    }
}
