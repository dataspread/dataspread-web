import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class DepGraphOpt {
    final boolean pruning = true;
    // Dependency graph
    DependencyGraph originalGraph;
    private int candidatesGenerated;
    private int graphsExplored; // This includes the sub graphs explored
    private double FPWithinBudget;
    private int memoryBudget;


    public int getGraphsExplored() {
        return graphsExplored;
    }

    DependencyGraph getOptimalGraph(DependencyGraph originalGraph, int memoryBudget) {
        candidatesGenerated = 0;
        graphsExplored = 0;
        this.originalGraph = originalGraph;


        return IntStream.range(1, memoryBudget)
                .mapToObj(e ->
                        getOptimalGraph(
                                getOptimalGraph(this.originalGraph, e,
                                        DependencyGraph.Side.DEPENDS)
                                        .orElse(null),
                                memoryBudget - e,
                                DependencyGraph.Side.DEPENDSON)
                                .orElse(null))
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(e -> FPRate(e)))
                .orElse(null);
    }

    private Optional<DependencyGraph> getOptimalGraph(DependencyGraph startingGraph,
                                                      int memoryBudget,
                                                      DependencyGraph.Side side) {
        FPWithinBudget = 1.0;   // Used to eliminate sub-candidates that have a bad FP compared to the best seen.
        this.memoryBudget = memoryBudget;
        Stream<DependencyGraph> dependencyGraphStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        getAllCandidates(startingGraph, side), Spliterator.DISTINCT), false);

        return dependencyGraphStream
                .filter(e -> e.size(side) <= memoryBudget)
                .filter(e -> {
                    FPWithinBudget = Math.min(FPRate(e), FPWithinBudget);
                    return FPRate(e) <= FPWithinBudget;
                })
                .min(Comparator.comparingDouble(e -> FPRate(e)));

    }

    public int getCandidatesGenerated() {
        return candidatesGenerated;
    }

    private Iterator<DependencyGraph> getAllCandidates(DependencyGraph startingGraph, DependencyGraph.Side side) {
        return getAllCandidates(startingGraph, side, true);
    }


    private Iterator<DependencyGraph> getAllCandidates(DependencyGraph inputGraph,
                                                       DependencyGraph.Side side,
                                                       boolean outerCall) {
        Iterator<DependencyGraph> dependencyGraphIterator = new Iterator<DependencyGraph>() {
            // Pull the next sub solution
            boolean pullNextSubSolution;
            // Index within the sub solution
            int subIndex;

            DependencyGraph subSolution;
            List<CellRegionRef> subSolutionDependsOnList;

            // This is the graph with one node removed
            DependencyGraph partial;

            // Removed nodes.
            CellRegionRef removedCellRegionRef = null;    //  DependsOn / Depends
            Set<CellRegionRef> removedCorrespondingSet = null;   // removedCorrespondingSet  / removedDependsOnSet


            // Next solution to return.
            DependencyGraph nextSolution;

            // Sub solutions.
            Iterator<DependencyGraph> subSet = null;

            {
                if (inputGraph != null) {
                    //Remove first node
                    partial = inputGraph.copy();
                    removedCellRegionRef = partial.getFullSet(side).stream().findAny().orElse(null);

                    if (removedCellRegionRef != null) {
                        removedCorrespondingSet = partial.delete(side, removedCellRegionRef);
                        subSet = getAllCandidates(partial, side, false);
                        pullNextSubSolution = true;
                        nextSolution = getNextCandidate();
                    } else {
                        // Base case, empty graph.
                        nextSolution = inputGraph;
                    }
                } else {
                    nextSolution = null;
                }
            }


            private DependencyGraph getNextCandidate() {
                // Base case
                if (subSet == null)
                    return null;


                DependencyGraph nextCandidate = null;
                while (nextCandidate == null) {
                    if (pullNextSubSolution) {
                        if (subSet.hasNext()) {
                            subSolution = subSet.next();

                            //TODO: Get a list based on distance from removedCellRegionRef
                            subSolutionDependsOnList = subSolution.getFullSet(side)
                                    .stream()
                                    //The order here is important as it can reduce the number of candidates.
                                    //TODO: think of a better way  to organize
                                    .sorted(Comparator.comparing(e -> e.getRow()))
                                    .collect(Collectors.toCollection(ArrayList::new));

                            subIndex = -1; // First candidate, where regions are not combined.
                            pullNextSubSolution = false;
                        } else {
                            // All done
                            break;
                        }
                    }

                    DependencyGraph solution = subSolution.copy();
                    for (CellRegionRef depends : removedCorrespondingSet)
                        if (side == DependencyGraph.Side.DEPENDSON)
                            solution.put(depends, removedCellRegionRef, true);
                        else
                            solution.put(removedCellRegionRef, depends, true);
                    if (subIndex >= 0)
                        solution.reversibleMergeTwo(side, removedCellRegionRef, subSolutionDependsOnList.get(subIndex));

                    subIndex++;

                    if (subIndex == subSolutionDependsOnList.size())
                        pullNextSubSolution = true;


                    if (isCandidateGood(solution))
                        nextCandidate = solution;
                }
                return nextCandidate;
            }

            private boolean isCandidateGood(DependencyGraph solution) {
                if (!pruning)
                    return true;
                // Do not consider sub-solutions that violate memory budget
                if (solution.size(side) > memoryBudget)
                    return false;

                // Use the anti-monotonicity property.
                // Need to check if the pruning is correct.
                // This FP rate is for the sub solution,
                //      while comparing it with the original graph.
                // FPWithinBudget - The best complete solution seen till now.
                return !(FPRate(solution) > FPWithinBudget);
            }

            @Override
            public boolean hasNext() {
                return nextSolution != null;
            }

            @Override
            public DependencyGraph next() {
                //  Number of candidate fetched in the final call
                if (outerCall)
                    ++candidatesGenerated;
                ++graphsExplored;

                // Get the next solution
                DependencyGraph solutionToReturn = nextSolution;
                nextSolution = getNextCandidate();
                return solutionToReturn;

            }
        };

        return dependencyGraphIterator;
    }


    public double FPRate(DependencyGraph newGraph) {
        return (double) (newGraph.area() - originalGraph.area()) / newGraph.area();
    }

    public DependencyGraph greedyMerge(DependencyGraph originalGraph, int memoryBudget) {
        this.originalGraph = originalGraph;
        // Greedily reversibleMerge two areas the have the least impact on FP rate.
        DependencyGraph current = originalGraph.copy();
        int startingSize = originalGraph.size();

        while (current.size() > memoryBudget) {
            System.out.print("\rProgress " +
                    Math.round(
                            (100.0 - (100.0 * (current.size() - memoryBudget) / (startingSize - memoryBudget))))
                    + " %");

            // Record best merge step.
            CellRegionRef bestMergeRegion[] = new CellRegionRef[2];
            DependencyGraph.Side bestMergeSide = null;
            double bestFPRate = 1.0;
            // Current FP rate
            double currentFPRate = FPRate(current);

            DependencyGraph.Side sides[] = {DependencyGraph.Side.DEPENDS, DependencyGraph.Side.DEPENDSON};

            for (DependencyGraph.Side side : sides) {
                // Try merging dependsOn
                List<CellRegionRef> dependsOnList = current.getFullSet(side)
                        .stream()
                        .collect(Collectors.toCollection(ArrayList::new));

                DependsOn:
                for (int i = 0; i < dependsOnList.size() - 1; ++i) {
                    for (int j = i + 1; j < dependsOnList.size(); ++j) {

                        int mergeLength = current.getMergeOperations().size();
                        current.reversibleMergeTwo(side,
                                dependsOnList.get(i), dependsOnList.get(j));
                        double reducedGraphFPRate = FPRate(current);
                        while (current.getMergeOperations().size() > mergeLength)
                            current.reverseLastMerge();

                        if (reducedGraphFPRate < bestFPRate) {
                            bestMergeRegion[0] = dependsOnList.get(i);
                            bestMergeRegion[1] = dependsOnList.get(j);
                            bestFPRate = reducedGraphFPRate;
                            bestMergeSide = side;
                        }
                        if (reducedGraphFPRate == currentFPRate)
                            break DependsOn;
                    }
                }
            }

            // Make changes
            if (bestMergeSide != null)
                current.reversibleMergeTwo(bestMergeSide, bestMergeRegion[0], bestMergeRegion[1]);
        }
        System.out.println();
        return current;
    }
}
