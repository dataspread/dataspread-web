package org.zkoss.zss.model.sys.formula.QueryOptimization;

import org.zkoss.zss.model.sys.formula.Primitives.DataOperator;
import org.zkoss.zss.model.sys.formula.Primitives.GroupedDataOperator;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.SingleDataOperator;

import java.util.*;

public class QueryOptimizer {
    private QueryOptimizer(){}
    private static QueryOptimizer queryOptimizer = new QueryOptimizer();
    public static QueryOptimizer getOptimizer(){
        return queryOptimizer;
    }
    public QueryPlanGraph optimize(List<QueryPlanGraph> graphs){
        QueryPlanGraph ret = new QueryPlanGraph();
        Map<String, List<DataOperator>> dataOperators = new HashMap<>();
        for (QueryPlanGraph graph:graphs){
            for (DataOperator data:graph.dataNodes){
                assert data instanceof SingleDataOperator;
                String key = data.getRange().getSheet().getId() +
                        ":" +
                        data.getRange().getColumn() +
                        "-" +
                        data.getRange().getLastColumn();
                dataOperators.computeIfAbsent(key, k -> new ArrayList<>()).add(data);
            }
        }

        List<DataOperator> groupedDataNodes = new ArrayList<>();

        for (List<DataOperator> value:dataOperators.values()){
            int maxRow =  value.get(0).getRange().getSheet().getEndRowIndex();
            assert value.size() * Math.log(value.size()) < maxRow;
            value.sort((o1, o2) -> o2.getRange().getRow() - o1.getRange().getRow());
            List<DataOperator> temp = new ArrayList<>();
            temp.add(value.get(0));
            int currentMaxRow = value.get(0).getRange().getLastRow();
            DataOperator data;
            for (int i = 1, isize = value.size(); i < isize; i++){
                 data = value.get(i);
                 if (data.getRange().getRow() > currentMaxRow){
                     if (temp.size() > 1){
                         groupedDataNodes.add(new GroupedDataOperator(temp));
                     }
                     else {
                         groupedDataNodes.add(temp.get(0));
                     }
                     temp = new ArrayList<>();
                 }
                 else {
                     temp.add(data);
                 }
                currentMaxRow = Math.max(currentMaxRow, data.getRange().getLastRow());

            }
            if (temp.size() > 1){
                groupedDataNodes.add(new GroupedDataOperator(temp));
            }
            else {
                groupedDataNodes.add(temp.get(0));
            }
        }

        ret.dataNodes = groupedDataNodes;

        return ret;
    }
}
