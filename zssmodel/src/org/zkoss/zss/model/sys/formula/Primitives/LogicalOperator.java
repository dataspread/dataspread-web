package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.Vector;

public class LogicalOperator {
    Vector<LogicalOperator> inOp,outOp;

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
}
