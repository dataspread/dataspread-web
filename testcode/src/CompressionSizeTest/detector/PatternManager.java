package CompressionSizeTest.detector;

import CompressionSizeTest.detector.PatternDetector.*;
import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;

import java.util.Arrays;
import java.util.HashSet;

public class PatternManager {

    private final PatternDetector[] detectors;
    private final int[] compressedEdges;
    private final String[] lastCommitted;
    private int committedCells = 0;

    private int totalEdges;
    private String sheetName;
    private String fileName;
    private CellLoc lastCommittedCell;
    private CellLoc uncommittedCell;
    private HashSet<CellArea> lastCommittedDeps;
    private HashSet<CellArea> uncommittedDeps;

    public enum PatternType {
        TYPE_ZERO,
        TYPE_ONE,
        TYPE_TWO,
        TYPE_THREE,
        TYPE_FOUR
    }

    public PatternManager(String fileName,
                          String sheetName,
                          boolean rowWise) {

        this.totalEdges = 0;
        this.fileName = fileName;
        this.sheetName = sheetName;
        int patternLength = PatternType.values().length;
        compressedEdges = new int[patternLength + 1];
        lastCommitted = new String[patternLength + 1];
        detectors = new PatternDetector[patternLength];
        for(int i = 0; i < patternLength; i++) {
            switch (PatternType.values()[i]) {
                case TYPE_ZERO:
                    detectors[i] = new TypeZeroDetector(sheetName, rowWise);
                    break;
                case TYPE_ONE:
                    detectors[i] = new TypeOneDetector(sheetName, rowWise);
                    break;
                case TYPE_TWO:
                    detectors[i] = new TypeTwoDetector(sheetName, rowWise);
                    break;
                case TYPE_THREE:
                    detectors[i] = new TypeThreeDetector(sheetName, rowWise);
                    break;
                case TYPE_FOUR:
                    detectors[i] = new TypeFourDetector(sheetName, rowWise);
                    break;
            }
        }
    }

    /* true: valid
     * false: invalid
     */
    public boolean addDeps(HashSet<CellArea> deps, CellLoc source) {
        uncommittedDeps = deps;
        uncommittedCell = source;
        HashSet<CellArea> validSet = new HashSet<>(deps);
        for (PatternDetector detector: detectors) {
             detector.addDeps(deps, source).forEach(validSet::remove);
        }
        return validSet.isEmpty();
    }

    public void reset() {
        uncommittedCell = null;
        lastCommittedCell = null;
        uncommittedDeps = null;
        lastCommittedDeps = null;
        committedCells = 0;
        totalEdges = 0;
        for (PatternDetector detector: detectors) {
            detector.reset();
        }
    }

    public void commitDeps() {
        if (uncommittedCell == null) {
            System.err.println("Committed null cell");
            System.exit(-1);
        }
        lastCommittedCell = uncommittedCell;
        uncommittedCell = null;
        lastCommittedDeps = uncommittedDeps;
        uncommittedDeps = null;

        committedCells += 1;
        totalEdges += lastCommittedDeps.stream().mapToInt(CellArea::getCellNum).sum();

        for (PatternDetector detector: detectors) {
            detector.commitDeps();
        }
    }

    public int[] getCompressedEdges() {
        if (committedCells > 2) {
            compressedEdges[0] = Arrays.stream(detectors)
                    .mapToInt((detector) -> detector.getCompressedEdges()).sum();
            for (int i = 0; i < detectors.length; i++) {
                compressedEdges[i + 1] = detectors[i].getCompressedEdges();
            }

            if (totalEdges <= compressedEdges[0]) {
                System.out.println(fileName + "." + sheetName + "Wrong!");
            }
        } else {
            Arrays.fill(compressedEdges, 0);
        }
        return compressedEdges;
    }

    public String[] getLastComitted() {
        if (committedCells > 2) {
            lastCommitted[0] = sheetName + "." + lastCommittedCell.toString();
            for (int i = 0; i < detectors.length; i++) {
                lastCommitted[i + 1] = detectors[i].getLastComitted();
            }
        } else {
            Arrays.fill(lastCommitted, "");
        }

        return lastCommitted;
    }

    public int getCommittedCells() {
        return committedCells;
    }
}
