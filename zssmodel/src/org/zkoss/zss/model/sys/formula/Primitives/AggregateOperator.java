package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

public abstract class AggregateOperator extends PhysicalOperator {
    BinaryFunction binaryFunction;
    public AggregateOperator(BinaryFunction f){
        super();
        binaryFunction = f;
    }

    BinaryFunction getBinaryFunction(){
        return binaryFunction;
    }

    public abstract void merge(AggregateOperator aggregate) throws OptimizationError;
}
