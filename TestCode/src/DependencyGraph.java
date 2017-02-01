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


    public DependencyGraph() {
        forwardMap = new HashMap();
        reverseMap = new HashMap();
        formulaMapping = new HashMap<>();
        mergeOperations = new LinkedList<>();
    }

    public List<MergeOperation> getMergeOperations() {
        return Collections.unmodifiableList((List) mergeOperations);
    }

    public void put(CellRegionRef depends, CellRegionRef dependsOn) {
        put(depends, dependsOn, false);
    }

    public void put(CellRegionRef depends, CellRegionRef dependsOn, boolean isMerge) {


        forwardMap.computeIfAbsent(depends, e -> new HashSet<>()).add(dependsOn);
        reverseMap.computeIfAbsent(dependsOn, e -> new HashSet<>()).add(depends);

        // If this is not a reversibleMerge update formulae map.
        if (!isMerge) {
            Set<CellRegionRef> dependsSet = new HashSet<>();
            dependsSet.add(depends);
            formulaMapping.put(depends, dependsSet);
        }
    }

    //
    public long area() {
        return
                reverseMap.entrySet()
                        .stream()
                        .mapToLong(
                                e -> e.getValue()
                                        .stream()
                                        .mapToLong(f -> formulaMapping.get(f).size()).sum() *
                                        (e.getKey().refType == CellRegionRef.RefType.One2One ? 1 : e.getKey().getCellCount()))
                        .sum();
    }

    public Set<CellRegionRef> getFullSet(Side side) {
        return Collections.unmodifiableSet(
                (side == Side.DEPENDS ? forwardMap : reverseMap)
                        .keySet());
    }

    /* Return the bounded box  */
    private CellRegionRef reversibleMerge(Side side, Set<CellRegionRef> dependsSet, CellRegionRef.RefType refType) {
        // Update graph
        Set<CellRegionRef> dependsOnSet = new HashSet<>();

        //Set<CellRegionRef> mergedRegions;
        Map<CellRegionRef, Set<CellRegionRef>> mergedRegions = new HashMap<>();
        Map<CellRegionRef, Set<CellRegionRef>> mergedFormulaMapping = new HashMap<>();

        // Merged region
        CellRegionRef boundingBox = dependsSet.stream().reduce((a, b) -> a.getBoundingBox(b)).get();
        boundingBox.refType = refType;
        Set<CellRegionRef> formulaSet = new HashSet<>();

        for (CellRegionRef depends : dependsSet) {
            // Delete Individual
            Set<CellRegionRef> removedRegionSet = delete(side, depends);
            mergedRegions.put(depends, removedRegionSet);
            dependsOnSet.addAll(removedRegionSet);

            if (side == Side.DEPENDS) {
                Set<CellRegionRef> removedFormulaMapping = formulaMapping.remove(depends);
                mergedFormulaMapping.put(depends, removedFormulaMapping);
                formulaSet.addAll(removedFormulaMapping);
            }
        }

        if (side == Side.DEPENDS) {
            // Update formula mapping only if merging Depends.
            formulaMapping.put(boundingBox, formulaSet);
            dependsOnSet.forEach(e -> put(boundingBox, e, true));
        } else {
            dependsOnSet.forEach(e -> put(e, boundingBox, true));
        }
        mergeOperations.add(new MergeOperation(side, mergedRegions, mergedFormulaMapping, boundingBox));

        return boundingBox;
    }

    public void reverseLastMerge() {
        MergeOperation lastMerge = mergeOperations.removeLast();

        delete(lastMerge.side, lastMerge.boundingBox);
        if (lastMerge.side == Side.DEPENDS) {
            // System.out.println("Reversing " + lastMerge.boundingBox + " " + lastMerge.mergedRegions.entrySet() );

            //  Revert formula mapping merge - Need to update this first as put will refer to this.
            formulaMapping.remove(lastMerge.boundingBox);
            lastMerge.mergedFormulaMapping.entrySet()
                    .forEach(e -> formulaMapping.put(e.getKey(), e.getValue()));


            lastMerge.mergedRegions.entrySet()
                    .forEach(e -> e.getValue().forEach(f -> put(e.getKey(), f, true)));
        } else {
            lastMerge.mergedRegions.entrySet()
                    .forEach(e -> e.getValue().forEach(f -> put(f, e.getKey(), true)));
        }

    }

    /* Remove value and if the list is empty remove key */
    private void removeValue(Map<CellRegionRef, Set<CellRegionRef>> map, CellRegionRef key, CellRegionRef value) {
        Set<CellRegionRef> valueSet = map.get(key);
        valueSet.remove(value);
        if (valueSet.isEmpty())
            map.remove(key);

    }

    public Set<CellRegionRef> delete(Side side, CellRegionRef cellRegionRef) {
        if (side == Side.DEPENDS) {
            Set<CellRegionRef> dependsOnSet = forwardMap.remove(cellRegionRef);
            dependsOnSet.forEach(e -> removeValue(reverseMap, e, cellRegionRef));
            return dependsOnSet;
        } else {
            Set<CellRegionRef> dependsSet = reverseMap.remove(cellRegionRef);
            dependsSet.forEach(e -> removeValue(forwardMap, e, cellRegionRef));
            return dependsSet;
        }
    }

    /* Make a deep copy */
    public DependencyGraph copy() {
        DependencyGraph newGraph = new DependencyGraph();
        forwardMap.entrySet()
                .forEach(e -> newGraph.forwardMap
                        .put(e.getKey(), e.getValue()
                                .stream().collect(Collectors.toSet())));

        reverseMap.entrySet()
                .forEach(e -> newGraph.reverseMap
                        .put(e.getKey(), e.getValue()
                                .stream().collect(Collectors.toSet())));


        formulaMapping.entrySet()
                .forEach(e -> newGraph.formulaMapping
                        .put(e.getKey(), e.getValue()
                                .stream().collect(Collectors.toSet())));

        newGraph.mergeOperations.addAll(mergeOperations);

        return newGraph;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        getFullSet(Side.DEPENDS).stream()
                .sorted(Comparator.comparingInt(e -> e.getRow()))
                .forEach(depends ->
                        sb.append(depends.getReferenceString())
                                .append(depends.refType == CellRegionRef.RefType.One2One ? "*" : "")
                                .append("->")
                                .append(forwardMap.get(depends)
                                        .stream()
                                        .map(e -> e.getReferenceString()
                                                + (e.refType == CellRegionRef.RefType.One2One ? "*" : ""))
                                        .collect(Collectors.joining(",")))
                                .append(System.lineSeparator()));

        sb.append("Formulae Mapping\n");
        formulaMapping.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getRow()))
                .forEach(e -> sb.append(e.getKey().getReferenceString())
                        .append("->")
                        .append(e.getValue()
                                .stream()
                                .map(f -> f.getReferenceString())
                                .collect(Collectors.joining(",")))
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

    // Returns true - if merge is successful, else false.
    private boolean mergeOne2One(CellRegionRef region1, CellRegionRef region2) {
        MergeType mergeType = MergeType.Invalid;
        int r1Rows = -1, r2Rows = -1;
        int r1Columns = -1, r2Columns = -1;
        int distance = 0;

        // If columns match, their lengths should be same
        if (region1.getColumn() == region2.getColumn() &&
                region1.getLastColumn() == region2.getLastColumn()) {
            mergeType = MergeType.Vertical;
            r1Rows = region1.getRowCount();
            r2Rows = region2.getRowCount();
            distance = region1.getRow() - region2.getRow();
        }

        if (region1.getRow() == region2.getRow() &&
                region1.getLastRow() == region2.getLastRow()) {
            mergeType = MergeType.Horizontal;
            r1Columns = region1.getColumnCount();
            r2Columns = region2.getColumnCount();
            distance = region1.getColumn() - region2.getColumn();
        }

        if (mergeType == MergeType.Invalid)
            return false;

        // Check for the dependsOn to merge corresponding areas.
        LinkedList<CellRegionRef> r1DependsOn = forwardMap.get(region1)
                .stream()
                .filter(e -> e.refType == CellRegionRef.RefType.One2One)
                .collect(Collectors.toCollection(LinkedList::new));

        LinkedList<CellRegionRef> r2DependsOn = forwardMap.get(region2)
                .stream()
                .filter(e -> e.refType == CellRegionRef.RefType.One2One)
                .collect(Collectors.toCollection(LinkedList::new));

        //System.out.println("Merge One 2 One " + region1.getReferenceString() + " " + region2.getReferenceString());
        for (CellRegionRef r1 : r1DependsOn) {
            for (CellRegionRef r2 : r2DependsOn) {
                if (((mergeType == MergeType.Vertical) &&
                        (r1.getColumn() == r2.getColumn()) &&
                        (r1.getLastColumn() == r2.getLastColumn()) &&
                        (r1.getRow() != r2.getRow()) &&
                        (r1.getRowCount() == r1Rows) &&
                        (r2.getRowCount() == r2Rows) &&
                        (distance == (r1.getRow() - r2.getRow())))
                        ||
                        (((mergeType == MergeType.Horizontal) &&
                                (r1.getRow() == r2.getRow()) &&
                                (r1.getLastRow() == r2.getLastRow())) &&
                                (r1.getColumn() != r2.getColumn()) &&
                                (r1.getColumnCount() == r1Columns) &&
                                (r2.getColumnCount() == r2Columns) &&
                                (distance == (r1.getColumn() - r2.getColumn())))) {
                    reversibleMergeTwo(Side.DEPENDSON, r1, r2, CellRegionRef.RefType.One2One)
                            .refType = CellRegionRef.RefType.One2One;
                    //System.out.println("   Sub merge  " + r1.getReferenceString() + " " + r2.getReferenceString());

                }
            }
        }
        return true;
    }

    public CellRegionRef reversibleMergeTwo(Side side, CellRegionRef region1, CellRegionRef region2) {
        return reversibleMergeTwo(side, region1, region2, CellRegionRef.RefType.One2Many);
    }

    private CellRegionRef reversibleMergeTwo(Side side, CellRegionRef region1, CellRegionRef region2,
                                             CellRegionRef.RefType refType) {

        Set<CellRegionRef> toMerge = new HashSet<>();
        // While merging the DEPENDS, reversibleMerge everything that is within
        // the bounding box of the two regions
        if (side == Side.DEPENDS) {
            CellRegionRef boundingBox = region1.getBoundingBox(region2);
            forwardMap.keySet()
                    .stream()
                    .filter(e -> boundingBox.overlaps(e))
                    .forEach(e -> toMerge.add(e));

            //////////// One to One merge  ////////
            // Handle simple case of merging two nearby regions.
            // Might be this merge is not reversible.
            if (toMerge.size() == 2)
                if (mergeOne2One(region1, region2))
                    refType = CellRegionRef.RefType.One2One;
            ///////////////////////////////////////

        } else {
            toMerge.add(region1);
            toMerge.add(region2);
        }
        return reversibleMerge(side, toMerge, refType);
    }

    enum MergeType {Invalid, Vertical, Horizontal}

    public enum Side {DEPENDS, DEPENDSON}

    static class MergeOperation {
        Side side;
        Map<CellRegionRef, Set<CellRegionRef>> mergedRegions;
        Map<CellRegionRef, Set<CellRegionRef>> mergedFormulaMapping;
        CellRegionRef boundingBox;


        MergeOperation(Side side, Map<CellRegionRef, Set<CellRegionRef>> mergedRegions,
                       Map<CellRegionRef, Set<CellRegionRef>> mergedFormulaMapping,
                       CellRegionRef boundingBox) {
            this.side = side;
            this.mergedRegions = mergedRegions;
            this.mergedFormulaMapping = mergedFormulaMapping;
            this.boundingBox = boundingBox;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(side.name()).append(' ');
            sb.append(mergedRegions.entrySet()
                    .stream()
                    .map(e -> e.toString())
                    .collect(Collectors.joining(",")));
            return sb.toString();
        }
    }
}
