package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Primitives.Datastructure.DataWrapper;

public class Edge {
    LogicalOperator in,out;

    Edge(LogicalOperator in, LogicalOperator out){
        this.in = in;
        this.out = out;
    }

    private DataWrapper result = null;

    LogicalOperator getInVertex(){
        return in;
    }

    LogicalOperator getOutVertex(){
        return out;
    }

    void setResult(DataWrapper result){
        this.result = result;
    }

    boolean resultIsReady(){
        return result != null;
    }

    DataWrapper popResult(){
        DataWrapper ret = result;
        result = null;
        return ret;
    }

}
