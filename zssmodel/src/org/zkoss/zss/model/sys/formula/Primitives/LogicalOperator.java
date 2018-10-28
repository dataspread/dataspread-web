package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.Iterator;
import java.util.Vector;

public class LogicalOperator {
    Vector<Edge> inEdges, outEdges;

    LogicalOperator(){
        inEdges = new Vector<>();
        outEdges = new Vector<>();
    }

    public static void connect(LogicalOperator in, LogicalOperator out){
        Edge edge = new Edge(in,out);
        in.addOutput(edge);
        out.addInput(edge);
    }

    private void addInput(Edge op){
        inEdges.add(op);
    }

    private void addOutput(Edge op){
        outEdges.add(op);
    }

    public Iterator<LogicalOperator> getOutputNodes(){
        return new Iterator<LogicalOperator>() {
            int i = -1;
            @Override
            public boolean hasNext() {
                return i < outEdges.size() - 1;
            }

            @Override
            public LogicalOperator next() {
                i += 1;
                return outEdges.get(i).getOutVertex();
            }
        };
    }
}
