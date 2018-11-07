package org.zkoss.zss.model.sys.formula.Primitives;

import javafx.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GroupedDataOperator extends DataOperator{

    private Pair<Integer, Integer> getIndexRange(CellRegion region){
        int left = (region.getRow() - _region.getRow()) * _region.getColumnCount();
        int right = (region.getLastRow() - _region.getRow() + 1) * _region.getColumnCount();
        return new Pair<>(left,right);
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
        AbstractCellAdv[] cells = getCells();
        try {
            forEachInEdge(new Consumer<Edge>() {
                int i = 0;
                @Override
                public void accept(Edge edge) {
                    List result = edge.popResult();
                    int offset = edge.outRange.getKey();
                    for (int j = offset, jsize = edge.outRange.getValue();j < jsize; j++)
                        try {
                            setFormulaValue(cells[j],result.get(j - offset),context);
                        } catch (OptimizationError optimizationError) {
                            throw new RuntimeException(optimizationError);
                        }
                    i++;
                }
            });
        } catch (RuntimeException e){
            throw (OptimizationError)e.getCause();
        }

        for (int i = 0; i < data.length; i++)
            data[i] = cells[i] == null? null : cells[i].getValue();

        List results = Arrays.asList(data);
        forEachOutEdge(edge -> edge.setResult(results));
    }

    @Override
    public void merge(DataOperator dataOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

//    @Override
//    void cleanInEdges(Consumer<Integer> action){
//        List<Pair<Integer,Integer>> cleanRange = new ArrayList<>();
//        super.cleanInEdges((i)->cleanRange.add(inEdgesRange.get(i)));
//        inEdgesRange = cleanRange;
//    }
//
//    @Override
//    void cleanOutEdges(Consumer<Integer> action){
//        List<Pair<Integer,Integer>> cleanRange = new ArrayList<>();
//        super.cleanOutEdges((i)->cleanRange.add(outEdgesRange.get(i)));
//        outEdgesRange = cleanRange;
//    }

    @Override
    void transferInEdge(Edge e){
        e.outRange = getIndexRange(((SingleDataOperator)e.getOutVertex()).getRegion());
        super.transferInEdge(e);
    }

    @Override
    void transferOutEdge(Edge e){
        e.inRange = getIndexRange(((DataOperator)e.getInVertex()).getRegion());
        super.transferOutEdge(e);
    }
}
