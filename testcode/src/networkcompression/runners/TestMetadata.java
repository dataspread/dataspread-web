package networkcompression.runners;

import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import java.util.stream.Collectors;
import java.util.*;


/**
 *
 * A class that keeps track of test statistics.
 */
public class TestMetadata {

    public double           area = 0.0;
    public long             testStartTime;
    public long             testFinalTime;
    public long             asyncStartTime;
    public long             asyncFinalTime;
    public long             numberOfCellsToUpdate;
    public long             totlTimeToUpdateCells;
    public long             startNumberOfDependents;
    public long             finalNumberOfDependents;
    public List<Long>       timeList = new ArrayList<>();
    public List<Integer>    cellList = new ArrayList<>();

    private <T> String formatList (List<T> list) {
        return list.stream().map(T::toString).collect(Collectors.joining(" "));
    }

    public void writeStatsToFile (String dir, String filename) {
        try (PrintWriter prw = new PrintWriter(new FileWriter(Paths.get(dir, filename).toString()))) {
            prw.println("Report:\n\n"
                    + "Test start time: "                 + testStartTime             + "\n"
                    + "Test end time: "                   + testFinalTime             + "\n"
                    + "Async start time: "                + asyncStartTime            + "\n"
                    + "Async end time: "                  + asyncFinalTime            + "\n"
                    + "Number of cells updated: "         + numberOfCellsToUpdate     + "\n"
                    + "Total time to update cells: "      + totlTimeToUpdateCells     + "\n"
                    + "Initial number of dependents: "    + startNumberOfDependents   + "\n"
                    + "Final number of dependents: "      + finalNumberOfDependents   + "\n"
                    + "Area under curve: "                + area                      + "\n"
                    + "\nTimes: "                         + formatList(timeList)      + "\n"
                    + "\nCells: "                         + formatList(cellList)      + "\n"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
