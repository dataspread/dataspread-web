import org.zkoss.zss.model.CellRegion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class GraphCompressor {

    private static class DependencyGraph {

        private Map<CellRegion, Set<CellRegion>> forwardMap; //Depends -> DependOn
        private Map<CellRegion, Set<CellRegion>> reverseMap; // DependsOn -> Depends

        public DependencyGraph makeCopy() {
            DependencyGraph newDependencyGraph = new DependencyGraph();
            for (CellRegion depends : forwardMap.keySet())
                for (CellRegion dependsOn : forwardMap.get(depends))
                    newDependencyGraph.putEdge(depends, dependsOn);
            return newDependencyGraph;
        }

        DependencyGraph() {
            forwardMap = new HashMap<>();
            reverseMap = new HashMap<>();
        }

        public void putEdge(CellRegion depends, CellRegion dependsOn) {
            forwardMap.computeIfAbsent(depends, e -> new HashSet<>()).add(dependsOn);
            //reverseMap.computeIfAbsent(dependsOn, e->new HashSet<>()).add(depends);
            // Expand the reverse map.
            for (int row = dependsOn.getRow(); row <= dependsOn.getLastRow(); row++)
                for (int column = dependsOn.getColumn(); column <= dependsOn.getLastColumn(); column++)
                    reverseMap.computeIfAbsent(new CellRegion(row, column), e -> new HashSet<>())
                            .add(depends);

        }


        public void printReverseGraph() {
            for (CellRegion dependsOn : reverseMap.keySet()) {
                System.out.print(dependsOn.getReferenceString());
                System.out.print("=>");
                for (CellRegion depends : reverseMap.get(dependsOn))
                    System.out.print(depends.getReferenceString() + " ");
                System.out.print(getCoverageArea(reverseMap.get(dependsOn)));
                System.out.println();
            }
        }

        public int getCoverageArea(Set<CellRegion> cellRegionSet) {
            Set<CellRegion> individualCells = new HashSet<>();
            for (CellRegion cellRegion : cellRegionSet)
                for (int row = cellRegion.getRow(); row <= cellRegion.getLastRow(); row++)
                    for (int column = cellRegion.getColumn(); column <= cellRegion.getLastColumn(); column++)
                        individualCells.add(new CellRegion(row, column));
            return individualCells.size();
        }


        public void greedyCompressNode(CellRegion dependsOn, int nodeLimit) {
            Set<CellRegion> depends = reverseMap.get(dependsOn);
            // Each step reduce one node by merging two nodes.
            // Simple merge, allow overlap.
            int minArea = getCoverageArea(depends);
            System.out.print("Start Area " + minArea);

            while (depends.size() > nodeLimit) {


            }
        }

        public void greedyReduceOne(Set<CellRegion> depends) {
            List<CellRegion> cellRegionList = new ArrayList<>(depends);
            int best_i = 0, best_j = 1;
            for (int i = 0; i < cellRegionList.size() - 1; i++)
                for (int j = i + 1; i < cellRegionList.size(); j++) {

                }


        }

        public void greedyCompressor() {

        }


    }

    private static DependencyGraph getGraphFile() throws IOException {
        DependencyGraph dependencyGraph;
        dependencyGraph = new DependencyGraph();

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
                    dependencyGraph.putEdge(new CellRegion(formula[0]),
                            new CellRegion(token));

        }
        return dependencyGraph;
    }


    public static void main(String args[]) throws IOException {
        DependencyGraph dependencyGraph = getGraphFile();
        dependencyGraph.printReverseGraph();
        dependencyGraph.greedyCompressNode(new CellRegion("A1"), 2);
    }
}
