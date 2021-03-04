package networkcompression.runners;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A class for tracking test statistics.
 */
public class TestMetadata {

    public List<String> curve = new ArrayList<>();

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

    public void writeStatsToFile (Path path) {
        try (PrintWriter prw = new PrintWriter(new FileWriter(path.toFile()))) {
            prw.println("Report:\n\n"
                    + "Correct: "                           + isCorrect                                     + "\n"
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
                    + "Area under curve: "                  + area                                          + "\n"
                    + "Curve: "                             + String.join("", curve)                + "\n"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset () {
        curve = new ArrayList<>();
        area = 0.0;
        isCorrect = false;
        touchedTime = 0;
        updatedCells = 0;
        compStartTime = 0;
        compFinalTime = 0;
        testStartTime = 0;
        testFinalTime = 0;
        updateCellStartTime = 0;
        updateCellFinalTime = 0;
        numberOfCellsToUpdate = 0;
        startNumberOfDependents = 0;
        finalNumberOfDependents = 0;
    }

}
