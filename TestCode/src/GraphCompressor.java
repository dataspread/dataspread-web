import org.zkoss.zss.model.CellRegion;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.Queue;


public class GraphCompressor extends Frame {

    private Collection<CellRegion> cellRegionCollection;

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
                if (getCoverageArea(reverseMap.get(dependsOn)) > 20) {
                    System.out.print(dependsOn.getReferenceString());
                    System.out.print("=>");
                    for (CellRegion depends : reverseMap.get(dependsOn))
                        System.out.print(depends.getReferenceString() + " ");
                    System.out.print(getCoverageArea(reverseMap.get(dependsOn)));
                    System.out.println();
                }
            }
        }

        public void printMaxTree() {
            int maxArea = 0;
            for (CellRegion dependsOn : reverseMap.keySet()) {
                System.out.println(dependsOn);
                Set<CellRegion> expandedRegions = expandNode(dependsOn);
                int area = expandedRegions.stream().mapToInt(e -> e.getHeight() * e.getLength()).sum();
                if (area > maxArea) {
                    maxArea = area;
                    System.out.print(dependsOn.getReferenceString());
                    System.out.print("=>");
                    for (CellRegion depends : expandedRegions)
                        System.out.print(depends.getReferenceString() + " ");
                    System.out.print(area);
                    System.out.println();
                }
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
                greedyReduceOne(depends);
            }
        }

        public void greedyReduceOne(Set<CellRegion> depends) {
            List<CellRegion> cellRegionList = new ArrayList<>(depends);
            int best_i = 0, best_j = 1;
            int best_area = getCoverageArea(depends);
            for (int i = 0; i < cellRegionList.size() - 1; i++) {
                for (int j = i + 1; j < cellRegionList.size(); j++) {
                    CellRegion region1 = cellRegionList.get(i);
                    CellRegion region2 = cellRegionList.get(j);
                    depends.remove(region1);
                    depends.remove(region2);
                    CellRegion mergedBox = region1.getBoundingBox(region2);
                    depends.add(mergedBox);
                    int newArea = getCoverageArea(depends);
                    if (newArea < best_area) {
                        best_i = i;
                        best_j = j;
                        best_area = newArea;
                    }
                    depends.remove(mergedBox);
                    depends.add(region1);
                    depends.add(region2);
                }
            }
            // Perform the best merge.
            CellRegion region1 = cellRegionList.get(best_i);
            CellRegion region2 = cellRegionList.get(best_j);
            depends.remove(region1);
            depends.remove(region2);
            CellRegion mergedBox = region1.getBoundingBox(region2);
            depends.add(mergedBox);
        }

        public Set<CellRegion> getDirectDepends(CellRegion dependsOn)
        {
            Set<CellRegion> returnSet = new HashSet<>();
            for (Map.Entry<CellRegion, Set<CellRegion>> reverseEntry:reverseMap.entrySet())
            {
                if (reverseEntry.getKey().overlaps(dependsOn))
                    returnSet.addAll(reverseEntry.getValue());
            }
            return returnSet;
        }

        public Set<CellRegion> expandNode(CellRegion dependsOn)
        {
            Set<CellRegion> expandedRegions = new HashSet<>();
            Queue<CellRegion> queue = new LinkedList<>();
            queue.add(dependsOn);
            while(!queue.isEmpty()) {
                CellRegion p = queue.remove();
                Set<CellRegion> directDepends = getDirectDepends(p);
                for(CellRegion depends:directDepends)
                {
                    if (!expandedRegions.contains(depends))
                    {
                        queue.add(depends);
                        expandedRegions.add(depends);
                    }
                }
            }
            return expandedRegions;
        }

    }

    GraphCompressor() {
        super("Graph Compressor demo");
        prepareGUI();
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

    private static DependencyGraph getGraphDB() throws SQLException, ClassNotFoundException {
        String url = "jdbc:postgresql://localhost/dataset";
        String user = "mangesh";
        String password = "";
        String driver = "org.postgresql.Driver";
        Class.forName(driver);
        Connection con = DriverManager.getConnection(
                url, user, password);

        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM survey_sheetdata " +
                " WHERE filename = '3.17.17_1985-2015_industrials_pull.v7.xlsx'" +
                " AND sheetname = 'Cash Flow Statement'");

        DependencyGraph dependencyGraph;
        dependencyGraph = new DependencyGraph();

        while (rs.next()) {
            String formula = rs.getString("formula");
            if (formula != null) {
                String tokens[] = formula.split("[ \t*+-/()<>!,]");
                for (String token : tokens)
                    if (token.matches("[A-Z]+[0-9]+:[A-Z]+[0-9]+") || token.matches("[A-Z]+[0-9]+"))
                        dependencyGraph.putEdge(new CellRegion(rs.getInt("row")
                                        , rs.getInt("col")),
                                new CellRegion(token));
            }
        }
        return dependencyGraph;
    }

    private void prepareGUI() {
        setSize(400, 400);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        setSize(480, 200);
        int o = 100;
        int m = 10;
        // g2.drawRect(50,50,100,100);
        for (CellRegion cellRegion : cellRegionCollection) {
            g2.drawRect(cellRegion.getRow() * m + o, cellRegion.getColumn() * m + o, cellRegion.getHeight() * m, cellRegion.getLength() * m);
        }

    }

    public void setGraphToPlot(Collection<CellRegion> cellRegionCollection) {
        this.cellRegionCollection = cellRegionCollection;
    }

    public static void main(String args[]) throws Exception {
        //GraphCompressor graphCompressor1 = new GraphCompressor();


        //DependencyGraph dependencyGraph = getGraphFile();
        DependencyGraph dependencyGraph = getGraphDB();
        dependencyGraph.printMaxTree();
        System.out.println("Done");

        //dependencyGraph.printReverseGraph();
        //dependencyGraph.expandNode(new CellRegion("A1"));
        //dependencyGraph.printReverseGraph();


        //graphCompressor1.setGraphToPlot(dependencyGraph.reverseMap.get(new CellRegion("A1")));

        //dependencyGraph.greedyCompressNode(new CellRegion("A1"), 3);
        //System.out.println("Compressed Graph");
        //dependencyGraph.printReverseGraph();


        //graphCompressor1.setVisible(true);

    }
}
