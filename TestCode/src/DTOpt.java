import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.zss.model.CellRegion;

import java.io.*;
import java.util.*;

/**
 * Created by Mangesh on 11/26/2016.
 */


class DependencyGraph
{
    Map<CellRegion, Set<CellRegion>> forwardMap; //Depends -> DependOn
    Map<CellRegion, Set<CellRegion>> reverseMap; // DependsOn -> Depends

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

    public Set<CellRegion> dependsSet()
    {
        return forwardMap.keySet();
    }


    public Set<CellRegion> dependsOnSet()
    {
        return reverseMap.keySet();

    }


    public void mergeDependsOn(Set<CellRegion> dependsOnSet)
    {

        Set<CellRegion> dependsSet = new HashSet<>();
        CellRegion boundingBox=null;

        for (CellRegion dependsOn:dependsOnSet)
        {
            dependsSet.addAll(deleteDependsOn(dependsOn));
            if (boundingBox==null)
                boundingBox = dependsOn;
            else
                boundingBox = boundingBox.getBoundingBox(dependsOn);
        }
        for (CellRegion depends:dependsSet)
            put(depends, boundingBox);

    }

    public Set<CellRegion> deleteDependsOn(CellRegion dependsOn)
    {
        Set<CellRegion> depends = reverseMap.remove(dependsOn);
        depends.stream().forEach(e->forwardMap.get(e).remove(dependsOn));
        return depends;
    }

    public Set<CellRegion> deleteDepends(CellRegion depends)
    {
        Set<CellRegion> dependsOn = forwardMap.remove(depends);


        dependsOn.stream().forEach(e->reverseMap.get(e).remove(depends));
        return dependsOn;
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


}

public class DTOpt {
    // Dependency graph
    static DependencyGraph originalGraph;
    static DependencyGraph dt;

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

        dt = originalGraph.copy();

        for(CellRegion region:dt.dependsSet())
            System.out.println(region.toString() + " " +  dt.getDependsOn(region));

        // Merge two values.
        Set<CellRegion> toMerge = new HashSet<>();
        toMerge.add(new CellRegion("A1:A100"));
        toMerge.add(new CellRegion("A90:A110"));
        dt.mergeDependsOn(toMerge);
        System.out.println("After Merge");
        for(CellRegion region:dt.dependsSet())
            System.out.println(region.toString() + " " +  dt.getDependsOn(region));


        // First focus on merging the RHS only.
        System.out.println("FPRate " + FPRate(originalGraph, dt));
        System.out.println("depends " + dt.getDependsOn(new CellRegion("C3")));

    }

    static double FPRate(DependencyGraph originalGraph,
                  DependencyGraph newGraph)
    {
        // Sum up the FP rates for each of the formula
        double fprate=0.0;
        int original_cells = 0;
        int new_cells = 0;
        for(CellRegion depends:originalGraph.dependsSet())
        {
            original_cells += originalGraph.getDependsOn(depends)
                    .stream().mapToInt(e->e.getCellCount()).sum();
            new_cells += newGraph.getDependsOn(depends)
                    .stream().mapToInt(e->e.getCellCount()).sum();
        }
        System.out.println("original_cells " + original_cells);
        System.out.println("new_cells " + new_cells);
        return (double) (new_cells - original_cells) / new_cells;
    }


}
