import org.zkoss.zss.model.CellRegion;

import java.io.*;
import java.util.*;
import java.util.stream.*;


public class DepGraphOpt {
    // Dependency graph


    public static void main(String[] args) throws IOException {
        DependencyGraph originalGraph;
        originalGraph = new DependencyGraph();

        FileInputStream inputStream = new FileInputStream("TestCode/SampleGraph.txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String s;
        while ((s = br.readLine()) != null) {
            if (s.startsWith("#"))
                continue;
            System.out.println(s);
            String formula[]=s.split("=");
            String tokens[] = formula[1].split("[ \t*+-/()<>!,]");
            for (String token:tokens)
                originalGraph.put(new CellRegion(formula[0]),
                        new CellRegion(token));

        }
        System.out.println(originalGraph);


        //traverse(dt, originalGraph);


        getAllCandidates(originalGraph)
                .forEach(e -> System.out.println("Size " + e.dependsOnSize() + " FP " + FPRate(originalGraph, e)));

        int memoryBudget = 3;
        DependencyGraph sol = getAllCandidates(originalGraph)
                .filter(e -> e.dependsOnSize() <= memoryBudget)
                .min(Comparator.comparingDouble(e -> FPRate(originalGraph, e))).get();

        System.out.println("Solution");
        System.out.println("Size " + sol.dependsOnSize() + " FP Rate " + FPRate(originalGraph, sol));

        for (CellRegion region : sol.getDependsSet())
            System.out.println(region.toString() + " " + sol.getDependsOn(region));

    }

    public static Stream<DependencyGraph> getAllCandidates(DependencyGraph inputGraph) {
        Iterator<DependencyGraph> dependencyGraphIterator = new Iterator<DependencyGraph>() {

            // Are we getting the fist solution?
            boolean first;
            // Pull the next sub solution
            boolean pullNextSubSolution;
            // Index within the sub solution
            int subIndex;

            DependencyGraph subSolution;
            List<CellRegion> subSolutionDependsOnList;

            // This is the graph with one node removed
            DependencyGraph partial;

            // Removed region
            CellRegion removedDependsOn = null;
            Set<CellRegion> removedDependsSet = null;

            Iterator<DependencyGraph> subSet = null;

            {
                first = true;
                pullNextSubSolution = true;


                //Remove first node
                //TODO: Avoid making a copy.
                partial = inputGraph.copy();
                //TODO: Check if we can do it in a streaming way.
                removedDependsOn = partial.getDependsOnSet().stream().findAny().orElse(null);

                if (removedDependsOn != null) {
                    removedDependsSet = partial.deleteDependsOn(removedDependsOn);
                    subSet = getAllCandidates(partial).iterator();
                }

            }


            @Override
            public boolean hasNext() {
                if (first)
                    return true;
                if (subSet == null)
                    return false;
                if (pullNextSubSolution)
                    return subSet.hasNext();
                else
                    return true;
            }

            @Override
            public DependencyGraph next() {
                // not longer is the first
                first = false;

                // Base case
                if (subSet == null)
                    return inputGraph;

                if (pullNextSubSolution) {
                    subSolution = subSet.next();
                    subSolutionDependsOnList = subSolution.getDependsOnSet()
                            .stream().collect(Collectors.toCollection(ArrayList::new));
                    subIndex = -1; // First candidate, where regions are not combined.
                    pullNextSubSolution = false;
                }

                DependencyGraph solution = subSolution.copy();
                for (CellRegion depends : removedDependsSet)
                    solution.put(depends, removedDependsOn);
                if (subIndex >= 0)
                    solution.mergeTwoDependsOn(removedDependsOn, subSolutionDependsOnList.get(subIndex));
                subIndex++;

                if (subIndex == subSolutionDependsOnList.size())
                    pullNextSubSolution = true;
                return solution;
            }
        };

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        dependencyGraphIterator, Spliterator.DISTINCT), false);
    }


    /* Simple traverse, results in duplicates */
    void traverse(DependencyGraph newGraph, DependencyGraph originalGraph) {

        int count = 0;
        List<CellRegion> dependsOnList = new ArrayList<>(newGraph.getDependsOnSet());

        System.out.println(++count + " FP rate " + FPRate(originalGraph, newGraph) + " " + dependsOnList.size());


        for (int i = 0; i < dependsOnList.size() - 1; i++) {
            Set<CellRegion> region1Depends = newGraph.getDepends(dependsOnList.get(i));
            for (int j = i + 1; j < dependsOnList.size(); j++) {

                Set<CellRegion> region2Depends = newGraph.getDepends(dependsOnList.get(j));

                CellRegion mergedRegion = newGraph.mergeTwoDependsOn(dependsOnList.get(i), dependsOnList.get(j));

                traverse(newGraph, originalGraph);

                //Revert the merge
                newGraph.deleteDependsOn(mergedRegion);
                for (CellRegion depends : region1Depends)
                    newGraph.put(depends, dependsOnList.get(i));
                for (CellRegion depends : region2Depends)
                    newGraph.put(depends, dependsOnList.get(j));


            }
        }
    }

    public static double FPRate(DependencyGraph originalGraph,
                                DependencyGraph newGraph)
    {
        // Sum up the FP rates for each of the formula
        int original_cells = 0;
        int new_cells = 0;
        for (CellRegion depends : originalGraph.getDependsSet())
        {
            original_cells += originalGraph.getDependsOn(depends)
                    .stream().mapToInt(e->e.getCellCount()).sum();
            new_cells += newGraph.getDependsOn(depends)
                    .stream().mapToInt(e->e.getCellCount()).sum();
        }
        return (double) (new_cells - original_cells) / new_cells;
    }


}
