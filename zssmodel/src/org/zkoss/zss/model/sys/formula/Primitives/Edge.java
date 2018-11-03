package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.List;

class Edge {
    private LogicalOperator in,out;

    boolean valid = true;

    Edge(LogicalOperator in, LogicalOperator out){
        this.in = in;
        this.out = out;
    }

    void remove(){
        valid = false;
    }

    boolean isValid(){
        return valid;
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
