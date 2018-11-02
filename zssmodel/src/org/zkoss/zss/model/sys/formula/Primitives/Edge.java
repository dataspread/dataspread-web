package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.List;

class Edge {
    private LogicalOperator in,out;

    Edge(LogicalOperator in, LogicalOperator out){
        this.in = in;
        this.out = out;
    }

    private List result = null;

    LogicalOperator getInVertex(){
        return in;
    }

    LogicalOperator getOutVertex(){
        return out;
    }

    void setInVertex(LogicalOperator op){
        in = op;
    }

    void setOutVertex(LogicalOperator op){
        out = op;
    }

    void setResult(List result){
        this.result = result;
    }

    boolean resultIsReady(){
        return result != null;
    }

    List popResult(){
        List ret = result;
        result = null;
        return ret;
    }

}
