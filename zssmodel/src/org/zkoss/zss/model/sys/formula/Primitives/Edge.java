package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.List;

class Edge {
    private LogicalOperator in,out;

    private boolean valid = true;

    Edge(LogicalOperator in, LogicalOperator out){
        this.in = in;
        this.out = out;
    }

    void remove(){
        valid = false;
        in.removeOutEdge();
        out.removeInEdge();
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
        ((PhysicalOperator)out).incInputCount();
    }

    List popResult(){
        List ret = result;
        result = null;
        ((PhysicalOperator)out).decInputCount();
        return ret;
    }

}
