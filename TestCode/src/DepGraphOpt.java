import org.zkoss.zss.model.CellRegion;

import java.io.*;
import java.util.*;
import java.util.stream.*;


public class DepGraphOpt {
    // Dependency graph
    DependencyGraph originalGraph;
    private int candidatesGenerated;

    DepGraphOpt(DependencyGraph originalGraph) {
        this.originalGraph = originalGraph;

    }


    public int getCandidatesGenerated() {
        return candidatesGenerated;
    }

    public Iterator<DependencyGraph> getAllCandidates() {
        candidatesGenerated = 0;
        return getAllCandidates(originalGraph);
    }


    public Iterator<DependencyGraph> getAllCandidates(DependencyGraph inputGraph) {
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
                    subSet = getAllCandidates(partial);
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
                //  Number of candidate
                candidatesGenerated++;
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

        return dependencyGraphIterator;
    }


    public double FPRate(DependencyGraph newGraph)
    {
        return (double) (newGraph.area() - originalGraph.area()) / newGraph.area();
    }

    public static void main(String[] args) throws IOException {
        DependencyGraph originalGraph;
        originalGraph = new DependencyGraph();

        FileInputStream inputStream = new FileInputStream("TestCode/SampleGraph.txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String s;
        while ((s = br.readLine()) != null) {
            if (s.startsWith("#"))
                continue;
            String formula[] = s.split("=");
            String tokens[] = formula[1].split("[ \t*+-/()<>!,]");
            for (String token : tokens)
                originalGraph.put(new CellRegion(formula[0]),
                        new CellRegion(token));

        }
        System.out.println("Original Graph ");
        System.out.println(originalGraph);


        //traverse(dt, originalGraph);
        DepGraphOpt depGraphOpt = new DepGraphOpt(originalGraph);

        Stream<DependencyGraph> dependencyGraphStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        depGraphOpt.getAllCandidates(), Spliterator.DISTINCT), false);

        int memoryBudget = 10;
        DependencyGraph sol = dependencyGraphStream
                .filter(e -> e.size() <= memoryBudget)
                .min(Comparator.comparingDouble(e -> depGraphOpt.FPRate(e))).get();

        System.out.println("Solution");
        System.out.println("Candidates " + depGraphOpt.getCandidatesGenerated());
        System.out.println("Size " + sol.size() + " FP Rate " + depGraphOpt.FPRate(sol));
        System.out.println(sol);
    }



}
