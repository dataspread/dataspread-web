package org.zkoss.zss.model.sys.formula.QueryOptimization;

public class MultiFormulaExecutor {
    private MultiFormulaExecutor(){}
    static MultiFormulaExecutor uniqueExecutor = new MultiFormulaExecutor();
    public static MultiFormulaExecutor getExecutor(){
        return uniqueExecutor;
    }
    public void execute(QueryPlanGraph graph){

    }
}
