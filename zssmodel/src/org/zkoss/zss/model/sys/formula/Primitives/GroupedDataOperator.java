package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.DataStructure.Range;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupedDataOperator extends DataOperator {

    private Range getIndexRange(CellRegion region){
        int left = (region.getRow() - _region.getRow()) * _region.getColumnCount();
        int right = (region.getLastRow() - _region.getRow() + 1) * _region.getColumnCount();
        return new Range(left,right);
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
    public List getEvaluationResult(FormulaExecutor context) throws OptimizationError {
        Object[] data = new Object[_region.getCellCount()];
        AbstractCellAdv[] cells = getCells();
        try {
            forEachInEdge(edge -> {
                List result = edge.popResult();
                int offset = edge.outRange.left;
                for (int j = offset, jsize = edge.outRange.right;j < jsize; j++)
                    try {
                        setFormulaValue(cells[j],result.get(j - offset),context);
                    } catch (OptimizationError optimizationError) {
                        throw new RuntimeException(optimizationError);
                    }
            });
        } catch (RuntimeException e){
            throw (OptimizationError)e.getCause();
        }

        for (int i = 0; i < data.length; i++)
            data[i] = cells[i] == null? null : cells[i].getValue();

        List<Object> results = Arrays.asList(data);
        return results;
    }

    @Override
    public void merge(DataOperator dataOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    public void mergeChildren() {
        ArrayList<Edge>[] aggregateEdges = new ArrayList[BinaryFunction.getMaxId()];
        for (int i = 0; i < aggregateEdges.length; i++)
            aggregateEdges[i] = new ArrayList<>();
        forEachOutEdge(edge -> {
            LogicalOperator out = edge.getOutVertex();
            if (out instanceof SingleAggregateOperator)
                aggregateEdges[((AggregateOperator)out).getBinaryFunction().getId()].add(edge);
        });

        for (ArrayList<Edge> edges:aggregateEdges)
            if (edges.size() > 0){
                if (OptimizationError.detectBucketSort && edges.size() * Math.log(edges.size()) > _region.getRowCount())
                    throw OptimizationError.BUCKETSORT;
                edges.sort((e1, e2) -> e1.inRange.left == e2.inRange.left ?
                        e1.inRange.right - e2.inRange.right
                        :e1.inRange.left - e2.inRange.left);
                List<SingleAggregateOperator> temp = new ArrayList<>();
                temp.add((SingleAggregateOperator) edges.get(0).getOutVertex());
                int currentMax = edges.get(0).inRange.right;
                Edge edge;
                for (int i = 1, isize = edges.size(); i < isize; i++){
                    edge = edges.get(i);
                    int left = edge.inRange.left, right = edge.inRange.right;
                    if (left > currentMax){
                        if (temp.size() > 1){
                            new GroupedAggregateOperator(temp,
                                    new Range(temp.get(0).getFirstInEdge().inRange.left,currentMax));
                        }
                        temp = new ArrayList<>();
                        temp.add((SingleAggregateOperator)edge.getOutVertex());
                    }
                    else {
                        int size = temp.size();
                        if (size > 0 && left == temp.get(size - 1).getFirstInEdge().inRange.left
                                && right == temp.get(size - 1).getFirstInEdge().inRange.right)
                            temp.get(size - 1).merge((AggregateOperator) edge.getOutVertex());
                        else
                            temp.add((SingleAggregateOperator)edge.getOutVertex());
                    }
                    currentMax = Math.max(currentMax, right);

                }

                if (temp.size() > 1){
                    new GroupedAggregateOperator(temp,
                            new Range(temp.get(0).getFirstInEdge().inRange.left,currentMax));
                }
            }
    }

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

    @Override
    public void mergeIndex(){
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    public void splitFilters() {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

}
