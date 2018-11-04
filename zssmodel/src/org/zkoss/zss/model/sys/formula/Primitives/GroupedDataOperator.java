package org.zkoss.zss.model.sys.formula.Primitives;

import javafx.util.Pair;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.*;
import java.util.function.Consumer;

public class GroupedDataOperator extends DataOperator{

    private List<Pair<Integer, Integer>> inEdgesRange = new ArrayList<>(), outEdgesRange = new ArrayList<>();

    private Pair<Integer, Integer> getIndexRange(CellRegion region){
        int left = (region.getRow() - _region.getRow()) * _region.getColumnCount();
        int right = (region.getLastRow() - _region.getRow() + 1) * _region.getColumnCount();
        return new Pair<>(left,right);
    }

    private int getIndex(SCell cell){
        return  (cell.getRowIndex() - _region.getRow()) * _region.getColumnCount() +
                cell.getColumnIndex() - _region.getColumn();
    }

    public GroupedDataOperator(List<DataOperator> dataOperators){
        super();
        int row = Integer.MAX_VALUE,column = Integer.MAX_VALUE,maxRow = 0,maxColumn = 0;
        for (DataOperator data : dataOperators) {
            row = Math.min(data.getRegion().getRow(), row);
            column = Math.min(data.getRegion().getColumn(), column);
            maxRow = Math.max(data.getRegion().getLastRow(), maxRow);
            maxColumn = Math.max(data.getRegion().getLastColumn(), maxColumn);
        }
        _region = new CellRegion(row,column,maxRow,maxColumn);
        _sheet = dataOperators.get(0).getSheet();
        for (DataOperator data : dataOperators) {
            data.forEachInEdge(this::transferInEdge);
            data.forEachOutEdge(this::transferOutEdge);
        }
    }

    @Override
    public void evaluate(FormulaExecutor context) throws OptimizationError {
        Object[] data = new Object[_region.getCellCount()];
        Collection<SCell> cells = _sheet.getCells(_region);

        int inEdgeCursor = 0;

        Pair<Integer, Integer> currentRange = null;
        Iterator currentResultIterator = null;
        if (inEdgeCursor < inEdgesRange.size()){
            currentRange = inEdgesRange.get(inEdgeCursor);
            currentResultIterator = getInEdge(inEdgeCursor).popResult().iterator();
        }

        for (SCell cell : cells){
            if (cell.getType() != SCell.CellType.NUMBER)
                throw OptimizationError.UNSUPPORTED_TYPE;
            int i = getIndex(cell);
            if (inEdgeCursor < inEdgesRange.size() && i >= currentRange.getValue()) {
                inEdgeCursor++;
                if (inEdgeCursor < inEdgesRange.size()){
                    currentRange = inEdgesRange.get(inEdgeCursor);
                    currentResultIterator = getInEdge(inEdgeCursor).popResult().iterator();
                }
            }
            if (inEdgeCursor < inEdgesRange.size() && i >= currentRange.getKey()){
                Object value = currentResultIterator.next();
                ValueEval resultValue;
                if (value instanceof Double){
                    resultValue = new NumberEval((Double)value);
                }
                else
                    throw OptimizationError.UNSUPPORTED_TYPE;
                ((AbstractCellAdv)cell).setFormulaResultValue(resultValue);
                context.update(_sheet, (AbstractCellAdv) cell);
                data[i] = value;
            }
            else
                data[i] = cell.getValue();
        }
        List results = Arrays.asList(data);
        forEachOutEdge(new Consumer<Edge>() {
            int i = 0;
            @Override
            public void accept(Edge edge) {
                edge.setResult(results.subList(outEdgesRange.get(i).getKey(),outEdgesRange.get(i).getValue()));
                i++;
            }
        });
    }

    @Override
    public void merge(DataOperator dataOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    void cleanInEdges(Consumer<Integer> action){
        List<Pair<Integer,Integer>> cleanRange = new ArrayList<>();
        super.cleanInEdges((i)->cleanRange.add(inEdgesRange.get(i)));
        inEdgesRange = cleanRange;
    }

    @Override
    void cleanOutEdges(Consumer<Integer> action){
        List<Pair<Integer,Integer>> cleanRange = new ArrayList<>();
        super.cleanOutEdges((i)->cleanRange.add(outEdgesRange.get(i)));
        outEdgesRange = cleanRange;
    }

    @Override
    void transferInEdge(Edge e){
        inEdgesRange.add(getIndexRange(((DataOperator)e.getOutVertex()).getRegion()));
        super.transferInEdge(e);
    }

    @Override
    void transferOutEdge(Edge e){
        outEdgesRange.add(getIndexRange(((DataOperator)e.getInVertex()).getRegion()));
        super.transferOutEdge(e);
    }
}
