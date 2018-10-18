package org.zkoss.zss.model.sys.formula.QueryOptimization;

import java.util.List;

public class QueryOptimizer {
    private QueryOptimizer(){}
    static QueryOptimizer queryOptimizer = new QueryOptimizer();
    public static QueryOptimizer getOptimizer(){
        return queryOptimizer;
    }
    public QueryPlanGraph optimize(List<QueryPlanGraph> graphs){
        QueryPlanGraph ret = new QueryPlanGraph();
        for (QueryPlanGraph graph:graphs){
            ret.dataNodes.addAll(graph.dataNodes);
        }
        return ret;
    }
}
