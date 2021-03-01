package networkcompression.runners;

import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import java.util.stream.Collectors;
import java.util.*;


/**
 *
 * A class for tracking test statistics.
 */
public class TestMetadata {

    public List<String> curve = new ArrayList<>();

    public double   area = 0.0;
    public boolean  isCorrect;
    public long     touchedTime;
    public long     testStartTime;
    public long     testFinalTime;
    public long     asyncStartTime;
    public long     asyncFinalTime;
    public long     numberOfCellsToUpdate;
    public long     totlTimeToUpdateCells;
    public long     startNumberOfDependents;
    public long     finalNumberOfDependents;

    public void writeStatsToFile (String dir, String filename) {
        try (PrintWriter prw = new PrintWriter(new FileWriter(Paths.get(dir, filename).toString()))) {
            prw.println("Report:\n\n"
                    + "Correct: "                           + isCorrect                         + "\n"
                    + "Test start time: "                   + testStartTime                     + "\n"
                    + "Test end time: "                     + testFinalTime                     + "\n"
                    + "Async start time: "                  + asyncStartTime                    + "\n"
                    + "Async end time: "                    + asyncFinalTime                    + "\n"
                    + "Touched time: "                      + touchedTime                       + "\n"
                    + "Number of cells updated: "           + numberOfCellsToUpdate             + "\n"
                    + "Total time to update cells: "        + totlTimeToUpdateCells             + "\n"
                    + "Initial number of dependents: "      + startNumberOfDependents           + "\n"
                    + "Final number of dependents: "        + finalNumberOfDependents           + "\n"
                    + "Area under curve: "                  + area                              + "\n"
                    + "Curve: "                             + String.join("", curve)    + "\n"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
