package networkcompression.runners;

import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A class for tracking test statistics.
 */
public class TestMetadata {

    public List<String> curve = new ArrayList<>();

    public double   area = 0.0;
    public boolean  isCorrect;
    public long     touchedTime;
    public long     compStartTime;
    public long     compFinalTime;
    public long     testStartTime;
    public long     testFinalTime;
    public long     updateCellStartTime;
    public long     updateCellFinalTime;
    public long     numberOfCellsToUpdate;
    public long     startNumberOfDependents;
    public long     finalNumberOfDependents;

    public void writeStatsToFile (String dir, String filename) {
        try (PrintWriter prw = new PrintWriter(new FileWriter(Paths.get(dir, filename).toString()))) {
            prw.println("Report:\n\n"
                    + "Correct: "                           + isCorrect                                     + "\n"
                    + "Test start time: "                   + testStartTime                                 + "\n"
                    + "Test end time: "                     + testFinalTime                                 + "\n"
                    + "Total test time (ms): "              + (testFinalTime - testStartTime)               + "\n"
                    + "Initial number of dependents: "      + startNumberOfDependents                       + "\n"
                    + "Final number of dependents: "        + finalNumberOfDependents                       + "\n"
                    + "Compression start time: "            + compStartTime                                 + "\n"
                    + "Compression end time: "              + compFinalTime                                 + "\n"
                    + "Total compression time (ms): "       + (compFinalTime - compStartTime)               + "\n"
                    + "Update cell start time: "            + updateCellStartTime                           + "\n"
                    + "Update cell end time: "              + updateCellFinalTime                           + "\n"
                    + "Total time to update cells (ms): "   + (updateCellFinalTime - updateCellStartTime)   + "\n"
                    + "Touched time: "                      + touchedTime                                   + "\n"
                    + "Number of cells updated: "           + numberOfCellsToUpdate                         + "\n"
                    + "Area under curve: "                  + area                                          + "\n"
                    + "Curve: "                             + String.join("", curve)                + "\n"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
