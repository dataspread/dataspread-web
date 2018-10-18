package org.zkoss.zss.model.sys.formula.QueryOptimization;

public class QueryOptimizer {
    private QueryOptimizer(){}
    static QueryOptimizer queryOptimizer = new QueryOptimizer();
    public static QueryOptimizer getExecutor(){
        return queryOptimizer;
    }
    public QueryPlanGraph optimize(QueryPlanGraph[] graphs){
        return graphs[0];
    }
}
