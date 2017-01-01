import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.zss.model.CellRegion;

import java.util.*;
import java.util.stream.Collectors;

class DependencyGraph {

    //TODO: avoid creating multiple copies of the graph.
    Map<CellRegion, Set<CellRegion>> forwardMap; //Depends -> DependOn
    Map<CellRegion, Set<CellRegion>> reverseMap; // DependsOn -> Depends


    // To maintain the formulae corresponding to the each Depends.
    // Ideally we do not require  a list but rather a weight or a count of formulae.
    Map<CellRegion, Set<CellRegion>> formulaMapping; // Depends -> Set of formulae

    public List<MergeOperation> getMergeOperations() {
        return Collections.unmodifiableList(mergeOperations);
    }

    // To compute areas we need to keep reference to the formulae.
    // We should consider the weight of the formula.

    public enum Side {DEPENDS, DEPENDSON}

    class MergeOperation {
        Side side;
        Set<CellRegion> mergedRegions;

        MergeOperation(Side side, Set<CellRegion> mergedRegions) {
            this.side = side;
            this.mergedRegions = mergedRegions;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(side.name()).append(' ');
            sb.append(mergedRegions
                    .stream()
                    .map(e -> e.getReferenceString())
                    .collect(Collectors.joining(",")));
            return sb.toString();
        }
    }

    List<MergeOperation> mergeOperations;

    long dependsOnArea;

    public DependencyGraph() {
        forwardMap = new HashMap();
        reverseMap = new HashMap();
        formulaMapping = new HashMap<>();
        dependsOnArea = 0;
        mergeOperations = new LinkedList<>();
    }

    public void put(CellRegion depends, CellRegion dependsOn) {
        put(depends, dependsOn, false);
    }

    private void put(CellRegion depends, CellRegion dependsOn, boolean isMerge) {
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
        // If this is not a merge update formulae map.
        if (!isMerge) {
            Set<CellRegion> dependsSet = new HashSet<>();
            dependsSet.add(depends);
            formulaMapping.put(depends, dependsSet);
        }
        dependsOnArea += formulaMapping.get(depends).size() * dependsOn.getCellCount();
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


    public CellRegion mergeDepends(Set<CellRegion> dependsSet) {
        // Update graph
        Set<CellRegion> dependsOnSet = new HashSet<>();
        CellRegion boundingBox = null;

        Set<CellRegion> formulaSet = new HashSet<>();

        for (CellRegion depends : dependsSet) {
            dependsOnSet.addAll(deleteDepends(depends));
            if (boundingBox == null)
                boundingBox = depends;
            else
                boundingBox = boundingBox.getBoundingBox(depends);

            formulaSet.addAll(formulaMapping.remove(depends));
        }

        // Update formula mapping
        formulaMapping.put(boundingBox, formulaSet);

        for (CellRegion dependsOn : dependsOnSet)
            put(boundingBox, dependsOn, true);


        mergeOperations.add(new MergeOperation(Side.DEPENDS, dependsSet));
        return boundingBox;

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
            put(depends, boundingBox, true);

        mergeOperations.add(new MergeOperation(Side.DEPENDSON, dependsOnSet));
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
        dependsSet.stream().forEach(e ->
        {
            removeValue(forwardMap, e, dependsOn);
            // Right now we consider each formulae as one.
            // Might be we can add weights later on.
            dependsOnArea -= dependsOn.getCellCount() * formulaMapping.get(e).size();

        });
        return dependsSet;
    }


    public Set<CellRegion> deleteDepends(CellRegion depends) {
        Set<CellRegion> dependsOnSet = forwardMap.remove(depends);
        dependsOnSet.stream().forEach(e -> {
            removeValue(reverseMap, e, depends);
            dependsOnArea -= e.getCellCount() * formulaMapping.get(depends).size();
        });
        return dependsOnSet;
    }


    public DependencyGraph copy() {

        /* Make a deep copy */
        DependencyGraph newGraph = new DependencyGraph();
        forwardMap.entrySet()
                .stream()
                .forEach(e -> newGraph.forwardMap
                        .put(e.getKey(), e.getValue()
                                .stream().collect(Collectors.toSet())));

        reverseMap.entrySet()
                .stream()
                .forEach(e -> newGraph.reverseMap
                        .put(e.getKey(), e.getValue()
                                .stream().collect(Collectors.toSet())));


        formulaMapping.entrySet()
                .stream()
                .forEach(e -> newGraph.formulaMapping
                        .put(e.getKey(), e.getValue()
                                .stream().collect(Collectors.toSet())));

        newGraph.mergeOperations.addAll(mergeOperations);

        newGraph.dependsOnArea = dependsOnArea;

        return newGraph;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        getDependsSet().stream()
                .sorted(Comparator.comparingInt(e -> e.getRow()))
                .forEach(depends ->
                        sb.append(depends.getReferenceString())
                                .append("->")
                                .append(getDependsOn(depends)
                                        .stream()
                                        .map(e -> e.getReferenceString())
                                        .collect(Collectors.joining(",")))
                                .append(System.lineSeparator()));

        sb.append("Formulae Mapping\n");
        formulaMapping.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getRow()))
                .forEach(e -> sb.append(e.getKey().getReferenceString())
                        .append("->")
                        .append(e.getValue()
                                .stream()
                                .map(f -> f.getReferenceString()).collect(Collectors.joining(",")))
                        .append(System.lineSeparator()));
        sb.append("Area:").append(area()).append(System.lineSeparator());
        sb.append("Size:").append(size()).append(System.lineSeparator());
        return sb.toString();
    }

    public int size() {
        // Corresponds to the memory requirement.
        // Size of the graph is total number of nodes.
        // Although we need to only account for reverse graph,
        // the nodes form the forward graph need to be stored.
        return reverseMap.size() + forwardMap.size();
    }

    public CellRegion mergeTwoDepends(CellRegion region1, CellRegion region2) {
        Set<CellRegion> toMerge = new HashSet<>();
        toMerge.add(region1);
        toMerge.add(region2);
        return mergeDepends(toMerge);
    }
}
