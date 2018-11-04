package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class LogicalOperator {
    private List<Edge> inEdges= new ArrayList<>(), outEdges = new ArrayList<>();

    LogicalOperator(){}

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

    void transferInEdge(Edge e){
        inEdges.add(e);
        e.setOutVertex(this);
    }

    void transferOutEdge(Edge e){
        outEdges.add(e);
        e.setInVertex(this);
    }

    int inDegree(){
        return inEdges.size();
    }

    int outDegree(){
        return outEdges.size();
    }

    Edge getInEdge(int i){ // todo: change to get first
        return inEdges.get(i);
    }

    void cleanInEdge(){
        ArrayList cleanEdges = new ArrayList();
        for (Edge e:inEdges)
            if (e.isValid())
                cleanEdges.add(e);
        inEdges = cleanEdges;
    }

    void cleanOutEdge(){
        ArrayList cleanEdges = new ArrayList();
        for (Edge e:outEdges)
            if (e.isValid())
                cleanEdges.add(e);
        outEdges = cleanEdges;
    }

    void forEachInEdge(Consumer<? super Edge> action){
        int validSize = 0;
        for (Edge e:inEdges){
            if (e.isValid()){
                action.accept(e);
                validSize++;
            }

        }
        if (validSize < inEdges.size())
            synchronized (this) {
                if (validSize < inEdges.size())
                    cleanInEdge();
            }
    }

    void forEachOutEdge(Consumer<? super Edge> action){
        int validSize = 0;
        for (Edge e:outEdges){
            if (e.isValid()){
                action.accept(e);
                validSize++;
            }

        }
        if (validSize < outEdges.size())
            synchronized (this) {
                if (validSize < outEdges.size())
                    cleanOutEdge();
            }
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
