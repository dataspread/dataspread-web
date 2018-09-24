package org.zkoss.poi.ss.formula;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FormulaComputationStatusManager {
    // Currently only supports single thread.
    // Handle a single formula at a time.
    static FormulaComputationStatusManager _instance = new FormulaComputationStatusManager();

    Map<Long, FormulaComputationStatus> formulaComputationStatusHashMap;



    public static class FormulaComputationStatus {
        public Object cell;
        public int row;
        public int column;
        public int totalCells;
        public int currentCells;
    }


    private FormulaComputationStatusManager() {
        formulaComputationStatusHashMap = new HashMap<>();
    }

    public static FormulaComputationStatusManager getInstance() {
        return _instance;
    }

    public synchronized void updateFormulaCell(int row, int column, Object cell) {
        FormulaComputationStatus formulaComputationStatus = new FormulaComputationStatus();
        formulaComputationStatus.row = row;
        formulaComputationStatus.column = column;
        formulaComputationStatus.cell = cell;
        formulaComputationStatus.totalCells = 1;
        formulaComputationStatus.currentCells = 0;
        formulaComputationStatusHashMap.put(Thread.currentThread().getId(), formulaComputationStatus);
    }

    public synchronized void startComputation(int totalCells) {
        formulaComputationStatusHashMap.get(Thread.currentThread().getId()).totalCells = totalCells;
    }

    public synchronized void updateProgress(int currentCells) {
        formulaComputationStatusHashMap.get(Thread.currentThread().getId()).currentCells = currentCells;
    }

    public synchronized Collection<FormulaComputationStatus> getCurrentStatus() {
        return new HashSet<>(formulaComputationStatusHashMap.values());
    }

    public synchronized void doneComputation() {
        formulaComputationStatusHashMap.remove(Thread.currentThread().getId());
    }
}
