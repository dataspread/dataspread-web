import org.zkoss.zss.model.CellRegion;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * Created by Mangesh on 11/26/2016.
 */


class DependencyGraph
{

    //TODO: avoid creating multiple copies of the graph.
    Map<CellRegion, Set<CellRegion>> forwardMap; //Depends -> DependOn
    Map<CellRegion, Set<CellRegion>> reverseMap; // DependsOn -> Depends
    static int count;

    public DependencyGraph()
    {
        forwardMap = new HashMap();
        reverseMap = new HashMap();
    }

    public void put(CellRegion depends, CellRegion dependsOn)
    {
        Set<CellRegion> values = forwardMap.get(depends);
        if (values==null)
        {
            values = new HashSet();
            forwardMap.put(depends, values);
        }
        values.add(dependsOn);

        Set<CellRegion> keys = reverseMap.get(dependsOn);
        if (keys==null)
        {
            keys = new HashSet();
            reverseMap.put(dependsOn, keys);
        }
        keys.add(depends);

    }

    public Set<CellRegion> getDependsOn(CellRegion depends)
    {
        // Need to use intersection
        return forwardMap.get(depends);
    }


    public Set<CellRegion> getDepends(CellRegion dependsOn)
    {
        return reverseMap.get(dependsOn);
    }

    public Set<CellRegion> getDependsSet()
    {
        return Collections.unmodifiableSet(forwardMap.keySet());
    }


    public Set<CellRegion> getDependsOnSet()
    {
        return Collections.unmodifiableSet(reverseMap.keySet());

    }

    /* Return the bounded box */
    public CellRegion mergeDependsOn(Set<CellRegion> dependsOnSet) {

        Set<CellRegion> dependsSet = new HashSet<>();
        CellRegion boundingBox=null;

        for (CellRegion dependsOn:dependsOnSet) {
            dependsSet.addAll(deleteDependsOn(dependsOn));
            if (boundingBox==null)
                boundingBox = dependsOn;
            else
                boundingBox = boundingBox.getBoundingBox(dependsOn);
        }
        for (CellRegion depends:dependsSet)
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

    public Set<CellRegion> deleteDependsOn(CellRegion dependsOn)
    {
        Set<CellRegion> dependsSet = reverseMap.remove(dependsOn);
        dependsSet.stream().forEach(e -> removeValue(forwardMap, e, dependsOn));
        return dependsSet;
    }

    public Set<CellRegion> deleteDepends(CellRegion depends)
    {
        Set<CellRegion> dependsOnSet = forwardMap.remove(depends);
        dependsOnSet.stream().forEach(e -> removeValue(reverseMap, e, depends));
        return dependsOnSet;
    }


    public DependencyGraph copy()
    {
        DependencyGraph newGraph = new DependencyGraph();
        for(Map.Entry<CellRegion, Set<CellRegion>> forwardEntry
                :forwardMap.entrySet())
        {
            for(CellRegion dependsOn:forwardEntry.getValue())
            {
                newGraph.put(forwardEntry.getKey(), dependsOn);
            }
        }
        return newGraph;
    }


    public int dependsOnSize() {
        return reverseMap.size();
    }
}

public class DTOpt {
    // Dependency graph
    static DependencyGraph originalGraph;

    public static void main(String[] args) throws IOException {
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
        getSpace(originalGraph).forEach(e -> System.out.println("Size " + e.dependsOnSize() + " FP " + FPRate(originalGraph, e)));

        int memoryBudget = 3;
        DependencyGraph sol = getSpace(originalGraph)
                .filter(e -> e.dependsOnSize() <= memoryBudget)
                .min(Comparator.comparingDouble(e -> FPRate(originalGraph, e))).get();
        System.out.println("Solution");
        System.out.println("Size " + sol.dependsOnSize() + " FP Rate " + FPRate(originalGraph, sol));
        for (CellRegion region : sol.getDependsSet())
            System.out.println(region.toString() + " " + sol.getDependsOn(region));

    }

    static Stream<DependencyGraph> getSpace(DependencyGraph inputGraph) {
        Iterator<DependencyGraph> dependencyGraphIterator = new Iterator<DependencyGraph>() {

            boolean first;
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
                    subSet = getSpace(partial).iterator();
                }
                //System.out.println("Removed subset " + subSet);

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
                first = false;
                if (subSet == null)
                    return inputGraph;

                if (pullNextSubSolution) {
                    subSolution = subSet.next();
                    subSolutionDependsOnList = subSolution.getDependsOnSet()
                            .stream().collect(Collectors.toCollection(ArrayList::new));
                    subIndex = -1;
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




    static void traverse(DependencyGraph newGraph, DependencyGraph originalGraph) {

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

    static double FPRate(DependencyGraph originalGraph,
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
