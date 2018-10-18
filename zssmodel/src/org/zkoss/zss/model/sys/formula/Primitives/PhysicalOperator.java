package org.zkoss.zss.model.sys.formula.Primitives;

public class PhysicalOperator {
    LogicalOperator logicalOperator;
    PhysicalOperator(LogicalOperator op){
        logicalOperator = op;
    }

    public LogicalOperator getlogicalOperator() {
        return logicalOperator;
    }
}
