import org.zkoss.zss.model.CellRegion;

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
                                        DependencyGraph.Side.DEPENDSON)
                                        .orElse(null),
                                memoryBudget - e,
                                DependencyGraph.Side.DEPENDS)
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
            List<CellRegion> subSolutionDependsOnList;

            // This is the graph with one node removed
            DependencyGraph partial;

            // Removed nodes.
            CellRegion removedCellRegion = null;    //  DependsOn / Depends
            Set<CellRegion> removedCorrespondingSet = null;   // removedCorrespondingSet  / removedDependsOnSet


            // Next solution to return.
            DependencyGraph nextSolution;

            // Sub solutions.
            Iterator<DependencyGraph> subSet = null;

            {
                //Remove first node
                partial = inputGraph.copy();
                removedCellRegion = partial.getFullSet(side).stream().findAny().orElse(null);

                if (removedCellRegion != null) {
                    removedCorrespondingSet = partial.delete(side, removedCellRegion);
                    subSet = getAllCandidates(partial, side, false);
                    pullNextSubSolution = true;
                    nextSolution = getNextCandidate();
                } else {
                    // Base case, empty graph.
                    nextSolution = inputGraph;
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

                            //TODO: Get a list based on distance from removedCellRegion
                            subSolutionDependsOnList = subSolution.getFullSet(side)
                                    .stream()
                                    //The order here is important as it can reduce the number of candidates.
                                    //TODO: think of a better way  to organizes
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
                    for (CellRegion depends : removedCorrespondingSet)
                        if (side == DependencyGraph.Side.DEPENDSON)
                            solution.put(depends, removedCellRegion);
                        else
                            solution.put(removedCellRegion, depends);
                    if (subIndex >= 0)
                        solution.mergeTwo(side, removedCellRegion, subSolutionDependsOnList.get(subIndex));

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
                if (FPRate(solution) > FPWithinBudget)
                    return false;

                return true;
            }

            @Override
            public boolean hasNext() {
                return nextSolution == null ? false : true;
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
        // Greedily merge two areas the have the least impact on FP rate.
        DependencyGraph current = originalGraph.copy();
        while (current.size() > memoryBudget) {
            DependencyGraph bestMerged = null;
            double bestFPRate = 1.0;

            // Try merging dependsOn
            List<CellRegion> dependsOnList = current.getFullSet(DependencyGraph.Side.DEPENDSON)
                    .stream()
                    .collect(Collectors.toCollection(ArrayList::new));

            for (int i = 0; i < dependsOnList.size() - 1; ++i) {
                for (int j = i + 1; j < dependsOnList.size(); ++j) {
                    DependencyGraph reducedGraph = current.copy();
                    reducedGraph.mergeTwo(DependencyGraph.Side.DEPENDSON,
                            dependsOnList.get(i), dependsOnList.get(j));
                    if (FPRate(reducedGraph) < bestFPRate) {
                        bestMerged = reducedGraph;
                        bestFPRate = FPRate(reducedGraph);
                    }
                }
            }

            // Try merging depends
            List<CellRegion> dependsList = current.getFullSet(DependencyGraph.Side.DEPENDS)
                    .stream()
                    .collect(Collectors.toCollection(ArrayList::new));

            for (int i = 0; i < dependsList.size() - 1; ++i) {
                for (int j = i + 1; j < dependsList.size(); ++j) {
                    DependencyGraph reducedGraph = current.copy();
                    reducedGraph.mergeTwo(DependencyGraph.Side.DEPENDS,
                            dependsList.get(i), dependsList.get(j));
                    if (FPRate(reducedGraph) < bestFPRate) {
                        bestMerged = reducedGraph;
                        bestFPRate = FPRate(reducedGraph);
                    }
                }
            }
            current = bestMerged;
        }
        return current;
    }
}
