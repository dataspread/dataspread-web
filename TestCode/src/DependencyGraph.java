import java.util.*;
import java.util.stream.Collectors;

class DependencyGraph {

    //TODO: avoid creating multiple copies of the graph.
    Map<CellRegionRef, Set<CellRegionRef>> forwardMap; //Depends -> DependOn
    Map<CellRegionRef, Set<CellRegionRef>> reverseMap; // DependsOn -> Depends


    // To maintain the formulae corresponding to the each Depends.
    // Ideally we do not require  a list but rather a weight or a count of formulae.
    Map<CellRegionRef, Set<CellRegionRef>> formulaMapping; // Depends -> Set of formulae
    LinkedList<MergeOperation> mergeOperations;

    // To compute areas we need to keep reference to the formulae.
    // We should consider the weight of the formula.
    long dependsOnArea;

    // Remember last merge operation
    //Set<CellRegionRef> mergedRegions;
    Map<CellRegionRef, Set<CellRegionRef>> mergedRegions;
    Map<CellRegionRef, Set<CellRegionRef>> mergedFormulaMapping;
    Side lastMergeSide;

    // Merged region
    CellRegionRef mergedBoundingBox;


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

    public void put(CellRegionRef depends, CellRegionRef dependsOn) {
        put(depends, dependsOn, false);
    }

    private void put(CellRegionRef depends, CellRegionRef dependsOn, boolean isMerge) {
        Set<CellRegionRef> values = forwardMap.get(depends);
        if (values == null) {
            values = new HashSet();
            forwardMap.put(depends, values);
        }
        values.add(dependsOn);

        Set<CellRegionRef> keys = reverseMap.get(dependsOn);
        if (keys == null) {
            keys = new HashSet();
            reverseMap.put(dependsOn, keys);
        }
        keys.add(depends);
        // If this is not a reversibleMerge update formulae map.
        if (!isMerge) {
            Set<CellRegionRef> dependsSet = new HashSet<>();
            dependsSet.add(depends);
            formulaMapping.put(depends, dependsSet);
        }
        // For one to one do we do not need to consider the formula
        if (dependsOn.refType == CellRegionRef.RefType.One2One)
            dependsOnArea += dependsOn.getCellCount();
        else
            dependsOnArea += formulaMapping.get(depends).size() * dependsOn.getCellCount();
    }

    //
    public long area() {
        // Total area for all formulae. Use it to obtain the FP rate.
        return dependsOnArea;
    }

    public Set<CellRegionRef> getFullSet(Side side) {
        return Collections.unmodifiableSet(
                (side == Side.DEPENDS ? forwardMap : reverseMap)
                        .keySet());
    }

    /* Return the bounded box  */
    private CellRegionRef reversibleMerge(Side side, Set<CellRegionRef> dependsSet) {
        // Update graph
        Set<CellRegionRef> dependsOnSet = new HashSet<>();
        mergedBoundingBox = null;
        mergedRegions = new HashMap<>();
        mergedFormulaMapping = new HashMap<>();
        lastMergeSide = side;

        Set<CellRegionRef> formulaSet = new HashSet<>();

        for (CellRegionRef depends : dependsSet) {
            // Delete Individual
            Set<CellRegionRef> removedRegionSet = delete(side, depends);
            mergedRegions.put(depends, removedRegionSet);
            dependsOnSet.addAll(removedRegionSet);

            if (mergedBoundingBox == null)
                mergedBoundingBox = depends;
            else
                mergedBoundingBox = mergedBoundingBox.getBoundingBox(depends);
            if (side == Side.DEPENDS) {
                Set<CellRegionRef> removedFormulaMapping = formulaMapping.remove(depends);
                mergedFormulaMapping.put(depends, removedFormulaMapping);
                formulaSet.addAll(removedFormulaMapping);
            }
        }

        if (side == Side.DEPENDS) {
            // Update formula mapping only if merging Depends.
            formulaMapping.put(mergedBoundingBox, formulaSet);
            dependsOnSet.forEach(e -> put(mergedBoundingBox, e, true));
        } else {
            dependsOnSet.forEach(e -> put(e, mergedBoundingBox, true));
        }
        mergeOperations.add(new MergeOperation(side, dependsSet));
        return mergedBoundingBox;
    }

    public void reverseLastMerge() {
        delete(lastMergeSide, mergedBoundingBox);
        if (lastMergeSide == Side.DEPENDS) {
            //  Revert formula mapping merge - Need to update this first as put will refer to this.
            formulaMapping.remove(mergedBoundingBox);
            mergedFormulaMapping.entrySet().forEach(e -> formulaMapping.put(e.getKey(), e.getValue()));
            mergedRegions.entrySet().forEach(e -> e.getValue().forEach(f -> put(e.getKey(), f, true)));
        } else {
            mergedRegions.entrySet().forEach(e -> e.getValue().forEach(f -> put(f, e.getKey(), true)));
        }
        mergeOperations.removeLast();

    }

    /* Remove value and if the list is empty remove key */
    private void removeValue(Map<CellRegionRef, Set<CellRegionRef>> map, CellRegionRef key, CellRegionRef value) {
        Set<CellRegionRef> valueSet = map.get(key);
        valueSet.remove(value);
        if (valueSet.isEmpty())
            map.remove(key);

    }

    public Set<CellRegionRef> delete(Side side, CellRegionRef CellRegionRef) {
        if (side == Side.DEPENDS) {
            Set<CellRegionRef> dependsOnSet = forwardMap.remove(CellRegionRef);
            dependsOnSet.stream().forEach(e -> {
                removeValue(reverseMap, e, CellRegionRef);
                dependsOnArea -= e.getCellCount() * formulaMapping.get(CellRegionRef).size();
            });
            return dependsOnSet;
        } else {
            Set<CellRegionRef> dependsSet = reverseMap.remove(CellRegionRef);
            dependsSet.stream().forEach(e ->
            {
                removeValue(forwardMap, e, CellRegionRef);
                // Right now we consider each formulae as one.
                // Might be we can add weights later on.
                dependsOnArea -= CellRegionRef.getCellCount() * formulaMapping.get(e).size();
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

    public CellRegionRef reversibleMergeTwo(Side side, CellRegionRef region1, CellRegionRef region2) {
        Set<CellRegionRef> toMerge = new HashSet<>();
        // While merging the DEPENDS, reversibleMerge everything that is within
        // the bounding box of the two regions
        if (side == Side.DEPENDS) {
            CellRegionRef boundingBox = region1.getBoundingBox(region2);
            forwardMap.keySet()
                    .stream()
                    .filter(e -> boundingBox.overlaps(e))
                    .forEach(e -> toMerge.add(e));
        } else {
            toMerge.add(region1);
            toMerge.add(region2);
        }
        return reversibleMerge(side, toMerge);
    }

    public enum Side {DEPENDS, DEPENDSON}

    static class MergeOperation {
        Side side;
        Set<CellRegionRef> mergedRegions;

        MergeOperation(Side side, Set<CellRegionRef> mergedRegions) {
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
