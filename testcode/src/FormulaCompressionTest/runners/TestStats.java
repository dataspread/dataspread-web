package FormulaCompressionTest.runners;

import org.zkoss.util.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A class for tracking test statistics.
 */
public class TestStats {

    public List<Pair<Long, Integer>> curve = new ArrayList<>();

    private final String debugStatsOutFileName = "debug.stat";
    private final String coreStatsOutFileName = "core.stat";

    public double   area = 0.0;
    public boolean  isCorrect = false;
    public long     touchedTime = 0;
    public long     updatedCells = 0;
    public long     compStartTime = 0;
    public long     compFinalTime = 0;
    public long     testStartTime = 0;
    public long     testFinalTime = 0;
    public long     updateCellStartTime = 0;
    public long     updateCellFinalTime = 0;
    public long     numberOfCellsToUpdate = 0;
    public long     startNumberOfDependents = 0;
    public long     finalNumberOfDependents = 0;
    public long     getDependentsTime = 0;
    public long     addBatchTime = 0;
    public long     refreshCacheTime = 0;
    public String   testCase = "";
    public boolean  isSyncRunner = false;
    public String   dependencyTableClass = "";
    public String[] testArgs = new String[0];
    public int      cacheSize = 0;

    public void writeStatsToFile (String outFolder) {
        Path debugPath = Paths.get(outFolder + "/" + debugStatsOutFileName);
        Path corePath = Paths.get(outFolder + "/" + coreStatsOutFileName);

        try (PrintWriter prw = new PrintWriter(new FileWriter(debugPath.toFile(), true))) {
            prw.println("Report:\n\n"
                    + "TestCase: "                          + testCase                                      + "\n"
                    + "Correct: "                           + isCorrect                                     + "\n"
                    + "isSyncRunner: "                      + isSyncRunner                                  + "\n"
                    + "dependencyTableClass: "              + dependencyTableClass                          + "\n"
                    + "cacheSize: "                         + cacheSize                                     + "\n"
                    + genTestArgsString(testArgs)
                    + "Touched time: "                      + touchedTime                                   + "\n"
                    + "Test start time: "                   + testStartTime                                 + "\n"
                    + "Test end time: "                     + testFinalTime                                 + "\n"
                    + "Total test time (ms): "              + (testFinalTime - testStartTime)               + "\n"
                    + "Initial number of dependents: "      + startNumberOfDependents                       + "\n"
                    + "Final number of dependents: "        + finalNumberOfDependents                       + "\n"
                    + "Compression start time: "            + compStartTime                                 + "\n"
                    + "Compression end time: "              + compFinalTime                                 + "\n"
                    + "Total compression time (ms): "       + (compFinalTime - compStartTime)               + "\n"
                    + "Number of cells to update: "         + numberOfCellsToUpdate                         + "\n"
                    + "Number of cells updated: "           + updatedCells                                  + "\n"
                    + "Update cell start time: "            + updateCellStartTime                           + "\n"
                    + "Update cell end time: "              + updateCellFinalTime                           + "\n"
                    + "Total time to update cells (ms): "   + (updateCellFinalTime - updateCellStartTime)   + "\n"
                    + "Total time after the update (ms): "  + (touchedTime - updateCellFinalTime)           + "\n"
                    + "Total time of getting dependents (ms): "  + getDependentsTime                        + "\n"
                    + "Area under curve: "                  + area                                          + "\n"
                    + "Curve: "
            );
            for (Pair<Long, Integer> p : curve) {
                prw.println(p.getX() + ", " + p.getY());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try (PrintWriter prw = new PrintWriter(new FileWriter(corePath.toFile(), true))) {
            prw.println(
                      "Total test time (ms): "              + (testFinalTime - testStartTime)               + "\n"
                    + "Number of cells to update: "         + numberOfCellsToUpdate                         + "\n"
                    + "Number of cells updated: "           + updatedCells                                  + "\n"
                    + "Total time to update cells (ms): "   + (updateCellFinalTime - updateCellStartTime)   + "\n"
                    + "Total time after the update (ms): "  + (touchedTime - updateCellFinalTime)           + "\n"
                    + "Total time of getting dependents (ms): "  + getDependentsTime                        + "\n"
                    + "Total time of adding the batch (ms): "    + addBatchTime                             + "\n"
                    + "Total time of refreshing the cache (ms): " + refreshCacheTime                        + "\n"
                    + "Area under curve: "                  + area                                          + "\n"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String genTestArgsString(String[] testArgs) {
        StringBuilder testArgsString = new StringBuilder();
        for (int i = 0; i < testArgs.length; i++) {
            testArgsString.append("testArg").append(i + 1).append(": ").append(testArgs[i]).append("\n");
        }
        return testArgsString.toString();
    }

}
