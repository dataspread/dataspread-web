package org.zkoss.zss.model.sys.formula.QueryOptimization;

import org.zkoss.zss.model.sys.formula.Primitives.DataReadOperator;

import java.util.ArrayList;
import java.util.List;

public class QueryPlanGraph {
    List<DataReadOperator> dataNodes = new ArrayList<>();
    public void addData(DataReadOperator op){
        dataNodes.add(op);
    }
}
