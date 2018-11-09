package org.zkoss.zss.model.sys.formula.Primitives;

import javafx.util.Pair;
import org.zkoss.zss.model.sys.formula.DataStructure.Range;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class GroupedAggregateOperator extends AggregateOperator {

    private ArrayList<Integer> uniqueEndPoints;
    private Range[] operatorRanges;

    public GroupedAggregateOperator(List<SingleAggregateOperator> operators, Range inputRange) {
        super(operators.get(0).getBinaryFunction());
        int oSize = operators.size();
        uniqueEndPoints = new ArrayList<>(oSize*2);
        operatorRanges = new Range[oSize];
        if (oSize * Math.log(oSize*2) > inputRange.size())
            throw OptimizationError.BUCKETSORT;
        Pair<Integer,Range>[] endPoints = new Pair[oSize * 2];
        for (int i = 0;i < oSize;i++){
            operatorRanges[i] = new Range(operators.get(i).getFirstInEdge().inRange);
            endPoints[i * 2] = new Pair<>(operatorRanges[i].left,operatorRanges[i]);
            endPoints[i * 2 + 1] = new Pair<>(operatorRanges[i].right,operatorRanges[i]);
        }
        Arrays.sort(endPoints,Comparator.comparingInt(Pair::getKey));

        uniqueEndPoints.add(endPoints[0].getKey());
        for (Pair<Integer,Range> p : endPoints){
            int point = p.getKey();
            Range range = p.getValue();
            if (point > uniqueEndPoints.get(uniqueEndPoints.size()-1))
                uniqueEndPoints.add(point);
            if (point == range.left)
                range.left = uniqueEndPoints.size() - 1;
            else
                range.right = uniqueEndPoints.size() - 1;
        }

        Edge preservedEdge = operators.get(0).getFirstInEdge();
        preservedEdge.inRange =inputRange;
        transferInEdge(preservedEdge);


        for (int i = 0;i < oSize;i++){
            SingleAggregateOperator op= operators.get(i);
            if (i > 0) {
                op.getFirstInEdge().remove();
            }
            int finalI = i;
            op.forEachOutEdge(edge -> {
                transferOutEdge(edge);
                edge.inRange = new Range(finalI, finalI +1);
            });
        }
    }

    @Override
    List getEvaluationResult(FormulaExecutor context) throws OptimizationError {
        if (!binaryFunction.isInvertable()){
            throw OptimizationError.UNSUPPORTED_FUNCTION;
        }

        List<Double> data = (List<Double>)getFirstInEdge().popResult();

        double partialAggregate[] = new double[uniqueEndPoints.size()];
        double results[] = new double[operatorRanges.length];

        partialAggregate[0] = 0;

        int right = uniqueEndPoints.get(uniqueEndPoints.size() - 1);

        for (int i = uniqueEndPoints.size() - 2; i >= 0; i--){
            int left = uniqueEndPoints.get(i);
            partialAggregate[i] = binaryFunction.groupEvaluate(data.subList(left,right)) + partialAggregate[i + 1];
            right = left;
        }

        for (int i = 0; i < operatorRanges.length;i++){
            results[i] = partialAggregate[operatorRanges[i].left] - partialAggregate[operatorRanges[i].right];
        }

        return Arrays.asList(results);
    }
}
