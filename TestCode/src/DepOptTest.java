import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

/**
 * Created by mangesh on 1/5/17.
 */
public class DepOptTest {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        long startTime, endTime;
        DependencyGraph originalGraph;
        originalGraph = getGraphFile();
        //originalGraph = getGraphDB();


        System.out.println("Original Graph ");
        System.out.print(originalGraph);
        System.out.println();

        int memoryBudget = 5;
        DepGraphOpt depGraphOpt = new DepGraphOpt();
        startTime = System.currentTimeMillis();
        DependencyGraph sol = depGraphOpt.getOptimalGraph(originalGraph, memoryBudget);
        endTime = System.currentTimeMillis();
        System.out.println("TIme taken " + (endTime - startTime));

        if (sol != null) {
            System.out.println("DP Solution");
            System.out.println("Candidates " + depGraphOpt.getCandidatesGenerated());
            System.out.println("Graphs Explored  " + depGraphOpt.getGraphsExplored());


            System.out.println("FP Rate " + depGraphOpt.FPRate(sol));
            System.out.println(sol);
        }

        startTime = System.currentTimeMillis();
        DependencyGraph greedySol = depGraphOpt.greedyMerge(originalGraph, memoryBudget);
        endTime = System.currentTimeMillis();
        System.out.println("Greedy Solution");
        System.out.println("FP Rate " + depGraphOpt.FPRate(greedySol));
        System.out.println(greedySol);
        System.out.println("Time taken " + (endTime - startTime));

        //System.out.println(greedySol.getMergeOperations());

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
                    dependencyGraph.put(new CellRegionRef(formula[0]),
                            new CellRegionRef(token));

        }
        return dependencyGraph;
    }

    private static DependencyGraph getGraphDB() throws SQLException, ClassNotFoundException {
        String url = "jdbc:postgresql://localhost/XLAnalysis";
        String user = "mangesh";
        String password = "";
        String driver = "org.postgresql.Driver";
        Class.forName(driver);
        Connection con = DriverManager.getConnection(
                url, user, password);

        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM acad_sheetdata " +
                " WHERE filename = 'CHCP Result Processing.xls'" +
                " AND sheetname = 'All'");

        DependencyGraph dependencyGraph;
        dependencyGraph = new DependencyGraph();

        while (rs.next()) {
            String formula = rs.getString("testformula");
            if (formula != null) {
                String tokens[] = formula.split("[ \t*+-/()<>!,]");
                for (String token : tokens)
                    if (token.matches("[A-Z]+[0-9]+:[A-Z]+[0-9]+") || token.matches("[A-Z]+[0-9]+"))
                        dependencyGraph.put(new CellRegionRef(rs.getInt("row")
                                        , rs.getInt("col")),
                                new CellRegionRef(token));
            }
        }
        return dependencyGraph;
    }

}
