package org.zkoss.poi.ss.formula;

public class FormulaComputationStatusManager {
    // Currently only supports single thread.
    // Need to update for multiple threads.
    // Handle a single formula at a time.
    static FormulaComputationStatusManager _instance = new FormulaComputationStatusManager();
    static FormulaComputationStatus formulaComputationStatus = new FormulaComputationStatus();

    public static class FormulaComputationStatus {
        public Object cell;
        public int row;
        public int column;
        public int totalCells;
        public int currentCells;
    }


    private FormulaComputationStatusManager() {

    }

    public static FormulaComputationStatusManager getInstance() {
        return _instance;
    }

    public synchronized void updateFormulaCell(int row, int column, Object cell) {
        formulaComputationStatus.row = row;
        formulaComputationStatus.column = column;
        formulaComputationStatus.cell = cell;
        formulaComputationStatus.totalCells = 1;
        formulaComputationStatus.currentCells = 0;
    }

    public synchronized void startComputation(int totalCells) {
        formulaComputationStatus.totalCells = totalCells;
    }

    public synchronized void updateProgress(int currentCells) {
        formulaComputationStatus.currentCells = currentCells;
    }

    public synchronized FormulaComputationStatus getCurrentStatus() {
        FormulaComputationStatus ret = new FormulaComputationStatus();
        ret.cell = formulaComputationStatus.cell;
        ret.row = formulaComputationStatus.row;
        ret.column = formulaComputationStatus.column;
        ret.totalCells = formulaComputationStatus.totalCells;
        ret.currentCells = formulaComputationStatus.currentCells;
        return ret;
    }

    public synchronized void doneComputation() {
        formulaComputationStatus.cell = null;
    }
}
