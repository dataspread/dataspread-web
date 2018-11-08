package org.zkoss.zss.model.sys.formula.Primitives;

public abstract class FilterOperator extends PhysicalOperator {

    public static FilterOperator buildSingleFilter(LogicalOperator[] ops){
        return new SingleEqualFilterOperator("");
    }

}
