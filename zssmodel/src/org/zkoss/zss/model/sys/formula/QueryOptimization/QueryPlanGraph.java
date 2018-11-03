package org.zkoss.zss.model.sys.formula.QueryOptimization;

import org.zkoss.zss.model.sys.formula.Primitives.DataOperator;

import java.util.ArrayList;
import java.util.List;

public class QueryPlanGraph {
    List<DataOperator> dataNodes = new ArrayList<>();
    public void addData(DataOperator op){
        dataNodes.add(op);
    }
}
