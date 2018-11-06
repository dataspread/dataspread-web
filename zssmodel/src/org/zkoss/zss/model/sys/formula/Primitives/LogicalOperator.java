package org.zkoss.zss.model.sys.formula.Primitives;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    void cleanInEdges(Consumer<Integer> action){
        inEdges = cleanEdges(action, inEdges);;
    }

    void cleanOutEdges(Consumer<Integer> action){
        outEdges = cleanEdges(action, outEdges);
    }

    private List<Edge> cleanEdges(Consumer<Integer> action, List<Edge> edges) {
        ArrayList<Edge> cleanEdges = new ArrayList<>();
        for (int i = 0, isize = edges.size(); i < isize; i++){
            Edge edge = edges.get(i);
            if (edge.isValid()) {
                cleanEdges.add(edge);
                if (action != null)
                    action.accept(i);
            }
        }
        return cleanEdges;
    }

    private int forEachEdge(Consumer<? super Edge> action, List<Edge> edges){
        int validSize = 0;
        for (Edge e:edges){
            if (e.isValid()){
                action.accept(e);
                validSize++;
            }

        }
        return validSize;
    }

    void forEachInEdge(Consumer<? super Edge> action){
        int validSize = forEachEdge(action,inEdges);
        if (validSize < inEdges.size())
            synchronized (this) {
                if (validSize < inEdges.size())
                    cleanInEdges(null);
            }
    }

    void forEachOutEdge(Consumer<? super Edge> action){
        int validSize = forEachEdge(action,outEdges);
        if (validSize < outEdges.size())
            synchronized (this) {
                if (validSize < outEdges.size())
                    cleanOutEdges(null);
            }
    }

    public void forEachOutVertex(Consumer<LogicalOperator> action){
        forEachOutEdge((e)->action.accept(e.getOutVertex()));
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
