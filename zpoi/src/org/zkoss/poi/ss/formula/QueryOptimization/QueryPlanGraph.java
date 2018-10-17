package org.zkoss.poi.ss.formula.QueryOptimization;

import org.zkoss.poi.ss.formula.Primitives.DataReadOperator;

import java.util.ArrayList;
import java.util.List;

public class QueryPlanGraph {
    List<DataReadOperator> dataNodes = new ArrayList<>();
    public void addData(DataReadOperator op){
        dataNodes.add(op);
    }
}
