import org.zkoss.zss.model.CellRegion;

import java.util.*;

class DependencyGraph {

    //TODO: avoid creating multiple copies of the graph.
    Map<CellRegion, Set<CellRegion>> forwardMap; //Depends -> DependOn
    Map<CellRegion, Set<CellRegion>> reverseMap; // DependsOn -> Depends

    Map<CellRegion, Set<CellRegion>> formulaMapping; // Region -> Set of formulae


    // To compute areas we need to keep reference to the formulae.
    // We should consider the weight of the formula.

    long dependsOnArea;

    public DependencyGraph() {
        forwardMap = new HashMap();
        reverseMap = new HashMap();
        dependsOnArea = 0;
    }

    public void put(CellRegion depends, CellRegion dependsOn) {
        Set<CellRegion> values = forwardMap.get(depends);
        if (values == null) {
            values = new HashSet();
            forwardMap.put(depends, values);
        }
        values.add(dependsOn);

        Set<CellRegion> keys = reverseMap.get(dependsOn);
        if (keys == null) {
            keys = new HashSet();
            reverseMap.put(dependsOn, keys);
        }
        keys.add(depends);
        dependsOnArea += dependsOn.getCellCount();
    }

    //
    public long area() {
        //TODO: if we try to merge the LHS then we need to find a different way to find the area.
        // We need to consider the area per formula.

        return dependsOnArea;
    }

    public Set<CellRegion> getDependsOn(CellRegion depends) {
        // Need to use intersection
        return forwardMap.get(depends);
    }


    public Set<CellRegion> getDepends(CellRegion dependsOn) {
        return reverseMap.get(dependsOn);
    }

    public Set<CellRegion> getDependsSet() {
        return Collections.unmodifiableSet(forwardMap.keySet());
    }


    public Set<CellRegion> getDependsOnSet() {
        return Collections.unmodifiableSet(reverseMap.keySet());

    }

    /* Return the bounded box */
    public CellRegion mergeDependsOn(Set<CellRegion> dependsOnSet) {

        Set<CellRegion> dependsSet = new HashSet<>();
        CellRegion boundingBox = null;

        for (CellRegion dependsOn : dependsOnSet) {
            dependsSet.addAll(deleteDependsOn(dependsOn));
            if (boundingBox == null)
                boundingBox = dependsOn;
            else
                boundingBox = boundingBox.getBoundingBox(dependsOn);
        }
        for (CellRegion depends : dependsSet)
            put(depends, boundingBox);

        return boundingBox;

    }

    public CellRegion mergeTwoDependsOn(CellRegion region1, CellRegion region2) {

        Set<CellRegion> toMerge = new HashSet<>();
        toMerge.add(region1);
        toMerge.add(region2);
        return mergeDependsOn(toMerge);
    }

    /* Remove value and if the list is empty remove key */
    private void removeValue(Map<CellRegion, Set<CellRegion>> map, CellRegion key, CellRegion value) {
        Set<CellRegion> valueSet = map.get(key);
        valueSet.remove(value);
        if (valueSet.isEmpty())
            map.remove(key);

    }

    public Set<CellRegion> deleteDependsOn(CellRegion dependsOn) {
        Set<CellRegion> dependsSet = reverseMap.remove(dependsOn);
        dependsSet.stream().forEach(e -> removeValue(forwardMap, e, dependsOn));
        dependsOnArea -= dependsOn.getCellCount() * dependsSet.size(); // TODO: This will not longer be valid when merging LHS
        return dependsSet;
    }

    public Set<CellRegion> deleteDepends(CellRegion depends) {
        Set<CellRegion> dependsOnSet = forwardMap.remove(depends);
        dependsOnSet.stream().forEach(e -> {
            removeValue(reverseMap, e, depends);
            dependsOnArea -= e.getCellCount();
        });
        return dependsOnSet;
    }


    public DependencyGraph copy() {
        DependencyGraph newGraph = new DependencyGraph();
        for (Map.Entry<CellRegion, Set<CellRegion>> forwardEntry
                : forwardMap.entrySet()) {
            for (CellRegion dependsOn : forwardEntry.getValue()) {
                newGraph.put(forwardEntry.getKey(), dependsOn);
            }
        }
        return newGraph;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CellRegion depends : getDependsSet()) {
            sb.append(depends.getReferenceString())
                    .append("->");
            for (CellRegion dependsOn : getDependsOn(depends))
                sb.append(dependsOn.getReferenceString()).append(' ');
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public int size() {
        // Size of the graph is total number of nodes.
        // Although we need to only account for reverse graph,
        // the nodes form the forward graph need to be stored.
        return reverseMap.size() + forwardMap.size();
    }
}
