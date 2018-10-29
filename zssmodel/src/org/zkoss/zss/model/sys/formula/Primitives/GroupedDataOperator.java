package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.LinkedList;
import java.util.List;

public class GroupedDataOperator extends PhysicalOperator{

    GroupedDataOperator(List<DataOperator> dataOperators){
        super();
//        dataOperators.remove()
    }

    @Override
    public void evaluate(FormulaExecutor context) throws OptimizationError {

    }
}
