package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.Vector;

public class LogicalOperator {
    Vector<LogicalOperator> inOp,outOp;

    LogicalOperator(){
        inOp = new Vector<>();
        outOp = new Vector<>();
    }

    public static void connect(LogicalOperator in, LogicalOperator out){
        in.addOutput(out);
        out.addInput(in);
    }

    public void addInput(LogicalOperator op){
        inOp.add(op);
    }

    public void addOutput(LogicalOperator op){
        outOp.add(op);
    }

    public Vector<LogicalOperator> getOutputNodes(){
        return outOp;
    }
}
