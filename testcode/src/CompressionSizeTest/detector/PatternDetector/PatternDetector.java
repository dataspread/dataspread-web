package CompressionSizeTest.detector.PatternDetector;

import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;

import java.util.HashSet;

public abstract class PatternDetector {

    private boolean rowWise;
    private HashSet<CellArea> uncomittedDeps;
    private CellLoc uncomittedSource;

    protected HashSet<CellArea> lastComittedDeps;
    private CellLoc lastComittedSource;

    protected int numTotalCommitted = 0;
    protected int numCommittedSourceCells = 0;
    private int numCommittedCells = 0;
    private int numCommittedEdges = 0;
    private int numInitCells = 0;
    private int numInitEdges = 0;
    private String sheetName;

    public PatternDetector(String sheetName,
                           boolean rowWise) {
        this.sheetName = sheetName;
        this.rowWise = rowWise;
    }

    public HashSet<CellArea> addDeps(HashSet<CellArea> deps, CellLoc source) {
        assert (uncomittedDeps == null && uncomittedSource == null);
        uncomittedSource = source;
        uncomittedDeps = deps;

        HashSet<CellArea> invalidDeps = findInvalidDep();
        HashSet<CellArea> validUncommitted = new HashSet<>();
        uncomittedDeps.forEach(cellArea -> {
            if (!invalidDeps.contains(cellArea)) validUncommitted.add(cellArea);
        });
        if (isPatternStop(lastComittedDeps, validUncommitted)) validUncommitted.clear();
        uncomittedDeps = validUncommitted;
        return validUncommitted;
    }

    private boolean isPatternStop(HashSet<CellArea> lastCommittedDeps,
                                      HashSet<CellArea> valiUncommitted) {
        return numTotalCommitted >= 2 &&
                lastCommittedDeps.size() != valiUncommitted.size();
    }

    public void commitDeps() {
        lastComittedDeps = uncomittedDeps;
        lastComittedSource = uncomittedSource;
        uncomittedDeps = null;
        uncomittedSource = null;

        int numCells = lastComittedDeps.size();
        int numEdges = lastComittedDeps.stream().mapToInt(CellArea::getCellNum).sum();

        numTotalCommitted += 1;
        if (lastComittedDeps.size() != 0) numCommittedSourceCells += 1;
        numCommittedCells += numCells;
        numCommittedEdges += numEdges;

        if (numCells > numInitCells) numInitCells = numCells;
        if (numEdges > numInitEdges) numInitEdges = numEdges;
    }

    private HashSet<CellArea> findInvalidDep() {
        if (lastComittedDeps == null && lastComittedSource == null) return new HashSet<>();
        assert(lastComittedDeps != null && lastComittedSource != null);

        HashSet<CellArea> newUnmatch = new HashSet<>();
        HashSet<CellArea> lastMatch = new HashSet<>();
        for (CellArea newCellArea: uncomittedDeps) {
            boolean isMatch = false;
            for (CellArea lastCellArea: lastComittedDeps) {
                if (!lastMatch.contains(lastCellArea) &&
                        isCompressable(uncomittedSource, newCellArea,
                                lastCellArea, rowWise)) {
                    isMatch = true;
                    lastMatch.add(lastCellArea);
                    break;
                }
            }
            if (!isMatch) newUnmatch.add(newCellArea);
        }

        return newUnmatch;
    }

    public void reset() {
        uncomittedSource = null;
        uncomittedDeps = null;

        lastComittedSource = null;
        lastComittedDeps = null;

        numTotalCommitted = 0;
        numCommittedSourceCells = 0;
        numCommittedEdges = 0;
        numCommittedCells = 0;
        numInitEdges = 0;
        numInitCells = 0;
    }

    public int getCompressedEdges() {

        if (numCommittedSourceCells == numTotalCommitted) {
            return numCommittedEdges - numInitEdges;
        } else {
            return 0;
        }
    }

    public String getLastComitted() {
        StringBuilder stringBuilder = new StringBuilder(sheetName + "." +
                lastComittedSource.toString() + ":");
        lastComittedDeps.forEach(cellArea -> {
            stringBuilder.append(cellArea.toString()).append(" ");
        });
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    abstract boolean isCompressable(CellLoc source, CellArea newCellArea,
                                    CellArea lastCellArea, boolean rowWise);
}
