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
    static int minPriority;

    public static class FormulaComputationStatus {
        public Object cell;
        public int row;
        public int column;
        public int totalCells;
        public int currentCells;
        public int priority;
    }


    private FormulaComputationStatusManager() {
        formulaComputationStatusHashMap = new HashMap<>();
        minPriority = 10;
    }

    public static void refreshPriorites() {
        // Visible cell
    }

    public static FormulaComputationStatusManager getInstance() {
        return _instance;
    }

    public synchronized void updateFormulaCell(int row, int column, Object cell, int priority) {
        FormulaComputationStatus formulaComputationStatus = new FormulaComputationStatus();
        formulaComputationStatus.row = row;
        formulaComputationStatus.column = column;
        formulaComputationStatus.cell = cell;
        formulaComputationStatus.totalCells = 1;
        formulaComputationStatus.currentCells = 0;
        formulaComputationStatus.priority = priority;
        formulaComputationStatusHashMap.put(Thread.currentThread().getId(), formulaComputationStatus);
        if (priority < minPriority)
            minPriority = priority;
    }

    public void startComputation(int totalCells) {
        formulaComputationStatusHashMap.get(Thread.currentThread().getId()).totalCells = totalCells;
    }

    public void updateProgress(int currentCells) {
        FormulaComputationStatus formulaComputationStatus = formulaComputationStatusHashMap
                .get(Thread.currentThread().getId());
        formulaComputationStatus.currentCells = currentCells;
        while (formulaComputationStatus.priority >= minPriority) {
            try {
                System.out.println(Thread.currentThread().getId() + " Waiting ");
                wait();
                System.out.println(Thread.currentThread().getId() + " Waiting over ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    public synchronized Collection<FormulaComputationStatus> getCurrentStatus() {
        return new HashSet<>(formulaComputationStatusHashMap.values());
    }

    public synchronized void doneComputation() {
        formulaComputationStatusHashMap.remove(Thread.currentThread().getId());
        minPriority = formulaComputationStatusHashMap
                .values()
                .stream()
                .mapToInt(e -> e.priority)
                .min().orElse(10);
        notifyAll();
    }
}
