package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class LogicalOperator implements Comparable<LogicalOperator> {
    private List<Edge> inEdges= new ArrayList<>(), outEdges = new ArrayList<>();

    private boolean removeIn = false,removeOut = false;

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

    private int firstInEdge = 0;

    Edge getFirstInEdge(){
        while (!inEdges.get(firstInEdge).isValid())
            firstInEdge++;

        return inEdges.get(firstInEdge);
    }

    void cleanInEdges(){
        inEdges = cleanEdges(inEdges);
        firstInEdge = 0;
    }

    void cleanOutEdges(){
        outEdges = cleanEdges(outEdges);
    }

    private List<Edge> cleanEdges(List<Edge> edges) {
        ArrayList<Edge> cleanEdges = new ArrayList<>();
        for (int i = 0, isize = edges.size(); i < isize; i++){
            Edge edge = edges.get(i);
            if (edge.isValid())
                cleanEdges.add(edge);
        }
        return cleanEdges;
    }

    private void forEachEdge(Consumer<? super Edge> action, List<Edge> edges){
        for (Edge e:edges)
            action.accept(e);
    }

    void forEachInEdge(Consumer<? super Edge> action){
        synchronized (this){
            if (removeIn) {
                cleanInEdges();
                removeIn = false;
            }
        }
        forEachEdge(action,inEdges);
    }

    void forEachOutEdge(Consumer<? super Edge> action){
        synchronized (this){
            if (removeOut) {
                cleanOutEdges();
                removeOut = false;
            }
        }
        forEachEdge(action,outEdges);
    }

    public void forEachOutVertex(Consumer<LogicalOperator> action){
        forEachOutEdge((e)->action.accept(e.getOutVertex()));
    }

    void removeInEdge(){
        removeIn = true;
    }

    void removeOutEdge(){
        removeOut = true;
    }

    @Override
    public int compareTo(LogicalOperator o) {
        if (hashCode() == o.hashCode()){
            if (this == o)
                return 0;
            else {
                StringBuilder inEdge1 = new StringBuilder();
                forEachInEdge((e)->inEdge1.append(e.hashCode()));
                StringBuilder inEdge2 = new StringBuilder();
                o.forEachInEdge((e)->inEdge2.append(e.hashCode()));
                StringBuilder outEdge1 = new StringBuilder();
                forEachOutEdge((e)->outEdge1.append(e.hashCode()));
                StringBuilder outEdge2 = new StringBuilder();
                o.forEachOutEdge((e)->outEdge2.append(e.hashCode()));
                int cmp = (toString() + inEdge1 + outEdge1)
                        .compareTo(o.toString() + inEdge2 + outEdge2);
                assert cmp != 0;
                return cmp;
            }
        }
        return hashCode() - o.hashCode();
    }
}
