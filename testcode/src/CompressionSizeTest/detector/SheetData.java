package CompressionSizeTest.detector;

import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;
import CompressionSizeTest.detector.Utils.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

public class SheetData implements Iterable<Pair<HashSet<CellArea>, CellLoc>> {

    private String sheetName;

    private final ArrayList<Pair<HashSet<CellArea>, CellLoc>> sheetDeps = new ArrayList<>();

    public SheetData(String sheetName) {
        this.sheetName = sheetName;
    }

    public static Comparator<Pair<HashSet<CellArea>, CellLoc>> rowWiseComp =
            (pairA, pairB) -> {
                CellLoc locA = pairA.second;
                CellLoc locB = pairB.second;

                int rowResult = Integer.compare(locA.getRow(), locB.getRow());
                if (rowResult == 0) return Integer.compare(locA.getColumn(), locB.getColumn());
                else return rowResult;
            };

    public static Comparator<Pair<HashSet<CellArea>, CellLoc>> colWiseComp =
            (pairA, pairB) -> {
                CellLoc locA = pairA.second;
                CellLoc locB = pairB.second;

                int colResult = Integer.compare(locA.getColumn(), locB.getColumn());
                if (colResult == 0) return Integer.compare(locA.getRow(), locB.getRow());
                else return colResult;
            };

    public void addOneDep(Pair<HashSet<CellArea>, CellLoc> dep) {
        sheetDeps.add(dep);
    }

    public void sortByLoc(boolean rowWise) {
        if (rowWise) sheetDeps.sort(rowWiseComp);
        else sheetDeps.sort(colWiseComp);
    }

    public Iterator<Pair<HashSet<CellArea>, CellLoc>> iterator() {
        return sheetDeps.iterator();
    }

    public String getSheetName() {
        return sheetName;
    }
}
