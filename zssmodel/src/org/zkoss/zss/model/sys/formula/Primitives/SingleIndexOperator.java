package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.Collections;
import java.util.List;

public abstract class SingleIndexOperator extends PhysicalOperator {
    public SingleIndexOperator(){
        super();
    }
}
