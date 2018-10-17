package org.zkoss.poi.ss.formula.QueryOptimization;

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
