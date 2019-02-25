package org.zkoss.zss.model.sys.formula.Primitives;

import javafx.util.Pair;
import org.zkoss.zss.model.sys.formula.DataStructure.Range;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GroupedEqualFilterOperator extends FilterOperator implements EqualFilterOperator{

    public GroupedEqualFilterOperator(List<SingleEqualFilterOperator> operators, Range inputRange) {
        super();

        for (SingleEqualFilterOperator op:operators){
            op.forEachInEdge(edge ->{
                if (edge.getTag() == FilterOperator.CRITERIA)
                    transferInEdge(edge);
            });
        }

        operators.get(0).forEachInEdge(edge ->{
            if (edge.getTag() != FilterOperator.CRITERIA) { // todo: multiple data edges
                edge.inRange =inputRange;
                transferInEdge(edge);
            }
        });


        for (int i = 0;i < operators.size();i++){
            SingleEqualFilterOperator op= operators.get(i);
            if (i > 0) {
                op.getFirstInEdge().remove();
            }
            int finalI = i;
            op.forEachOutEdge(edge -> {
                transferOutEdge(edge);
//                edge.inRange = new Range(finalI, finalI +1);
            });
        }
    }

    @Override
    List getEvaluationResult(FormulaExecutor context) throws OptimizationError {
        List<String> criteria = new ArrayList<>(), data = new ArrayList<>();

        List<String> result = new ArrayList<>();


        return result;
    }
}
