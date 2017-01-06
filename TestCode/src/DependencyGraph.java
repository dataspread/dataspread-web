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
    List<MergeOperation> mergeOperations;

    // To compute areas we need to keep reference to the formulae.
    // We should consider the weight of the formula.
    long dependsOnArea;

    public DependencyGraph() {
        forwardMap = new HashMap();
        reverseMap = new HashMap();
        formulaMapping = new HashMap<>();
        dependsOnArea = 0;
        mergeOperations = new LinkedList<>();
    }

    public List<MergeOperation> getMergeOperations() {
        return Collections.unmodifiableList(mergeOperations);
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
        // Total area for all formulae. Use it to obtain the FP rate.
        return dependsOnArea;
    }

    public Set<CellRegion> getFullSet(Side side) {
        return Collections.unmodifiableSet(
                (side == Side.DEPENDS ? forwardMap : reverseMap)
                        .keySet());
    }

    /* Return the bounded box  */
    public CellRegion merge(Side side, Set<CellRegion> dependsSet) {
        // Update graph
        Set<CellRegion> dependsOnSet = new HashSet<>();
        CellRegion boundingBox = null;

        Set<CellRegion> formulaSet = new HashSet<>();

        for (CellRegion depends : dependsSet) {
            dependsOnSet.addAll(delete(side, depends));
            if (boundingBox == null)
                boundingBox = depends;
            else
                boundingBox = boundingBox.getBoundingBox(depends);
            if (side == Side.DEPENDS)
                formulaSet.addAll(formulaMapping.remove(depends));
        }

        // Update formula mapping only if merging Depends.
        if (side == Side.DEPENDS)
            formulaMapping.put(boundingBox, formulaSet);

        for (CellRegion dependsOn : dependsOnSet)
            if (side == Side.DEPENDS)
                put(boundingBox, dependsOn, true);
            else
                put(dependsOn, boundingBox, true);


        mergeOperations.add(new MergeOperation(side, dependsSet));
        return boundingBox;

    }

    /* Remove value and if the list is empty remove key */
    private void removeValue(Map<CellRegion, Set<CellRegion>> map, CellRegion key, CellRegion value) {
        Set<CellRegion> valueSet = map.get(key);
        valueSet.remove(value);
        if (valueSet.isEmpty())
            map.remove(key);

    }

    public Set<CellRegion> delete(Side side, CellRegion cellRegion) {
        if (side == Side.DEPENDS) {
            Set<CellRegion> dependsOnSet = forwardMap.remove(cellRegion);
            dependsOnSet.stream().forEach(e -> {
                removeValue(reverseMap, e, cellRegion);
                dependsOnArea -= e.getCellCount() * formulaMapping.get(cellRegion).size();
            });
            return dependsOnSet;
        } else {
            Set<CellRegion> dependsSet = reverseMap.remove(cellRegion);
            dependsSet.stream().forEach(e ->
            {
                removeValue(forwardMap, e, cellRegion);
                // Right now we consider each formulae as one.
                // Might be we can add weights later on.
                dependsOnArea -= cellRegion.getCellCount() * formulaMapping.get(e).size();

            });
            return dependsSet;
        }
    }

    /* Make a deep copy */
    public DependencyGraph copy() {

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
        getFullSet(Side.DEPENDS).stream()
                .sorted(Comparator.comparingInt(e -> e.getRow()))
                .forEach(depends ->
                        sb.append(depends.getReferenceString())
                                .append("->")
                                .append(forwardMap.get(depends)
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
        sb.append("Size: Depends: ")
                .append(size(Side.DEPENDS))
                .append(", DependsON: ")
                .append(size(Side.DEPENDSON))
                .append(", Total: ")
                .append(size())
                .append(System.lineSeparator());
        return sb.toString();
    }

    public int size() {
        // Corresponds to the memory requirement.
        // Size of the graph is total number of nodes.
        // Although we need to only account for reverse graph,
        // the nodes form the forward graph need to be stored.

        return size(Side.DEPENDSON) + size(Side.DEPENDS);
        //return reverseMap.size() + forwardMap.size();

    }

    public int size(Side side) {
        return side == Side.DEPENDSON ? reverseMap.size() :
                reverseMap.values().stream().mapToInt(e -> e.size()).sum();
    }

    public CellRegion mergeTwo(Side side, CellRegion region1, CellRegion region2) {
        Set<CellRegion> toMerge = new HashSet<>();
        toMerge.add(region1);
        toMerge.add(region2);
        return merge(side, toMerge);
    }

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
}
