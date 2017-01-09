import org.zkoss.zss.model.CellRegion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by mangesh on 1/5/17.
 */
public class DepOptTest {

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
                if (token.matches("[A-Z]+[0-9]+:[A-Z]+[0-9]+") || token.matches("[A-Z]+[0-9]+"))
                    originalGraph.put(new CellRegion(formula[0]),
                            new CellRegion(token));

        }
        System.out.println("Original Graph ");
        System.out.print(originalGraph);
        System.out.println();


        int memoryBudget = 8;
        DepGraphOpt depGraphOpt = new DepGraphOpt();
        DependencyGraph sol = depGraphOpt.getOptimalGraph(originalGraph, memoryBudget);

        if (sol != null) {
            System.out.println("DP Solution");
            System.out.println("Candidates " + depGraphOpt.getCandidatesGenerated());
            System.out.println("Graphs Explored  " + depGraphOpt.getGraphsExplored());


            System.out.println("FP Rate " + depGraphOpt.FPRate(sol));
            System.out.println(sol);
        }

        DependencyGraph greedySol = depGraphOpt.greedyMerge(originalGraph, memoryBudget);
        System.out.println("Greedy Solution");
        System.out.println("FP Rate " + depGraphOpt.FPRate(greedySol));
        System.out.println(greedySol);

        System.out.println(greedySol.getMergeOperations());

    }

}
