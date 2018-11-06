package org.zkoss.zss.model.sys.formula.QueryOptimization;

import org.zkoss.zss.model.sys.formula.Primitives.DataOperator;
import org.zkoss.zss.model.sys.formula.Primitives.SingleTransformOperator;

import java.util.ArrayList;
import java.util.List;

public class QueryPlanGraph {
    private List<SingleTransformOperator> constantOperators = new ArrayList<>();
    List<DataOperator> dataNodes = new ArrayList<>();
    public void addData(DataOperator op){
        dataNodes.add(op);
    }
    public List<SingleTransformOperator> getConstants(){
        return constantOperators;
    }

}
