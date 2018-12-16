package org.zkoss.poi.ss.formula;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FormulaComputationStatusManager {
    static FormulaComputationStatusManager _instance = new FormulaComputationStatusManager();
    Map<Long, FormulaComputationStatus> formulaComputationStatusHashMap;
    static int minPriority;

    public void updatePriorities(Map<Object, Map<String, int[]>> uiVisibleMap) {
        try {
            for (FormulaComputationStatus formulaComputationStatus : formulaComputationStatusHashMap.values()) {
                for (Map.Entry<Object, Map<String, int[]>> uiVisibleMapEntry : uiVisibleMap.entrySet()) {
                    if (uiVisibleMapEntry.getKey() == formulaComputationStatus.sheet) {
                        for (int[] visRange : uiVisibleMapEntry.getValue().values()) {
                            if (formulaComputationStatus.row > visRange[0] && formulaComputationStatus.row < visRange[1]) {
                                if (formulaComputationStatus.priority == 10)
                                    System.out.println("Bumping up priority for " + formulaComputationStatus.row + "," + formulaComputationStatus.column);
                                formulaComputationStatus.priority = 5;
                                minPriority = 5;
                                notifyAll();
                            } else {
                                if (formulaComputationStatus.priority == 5)
                                    System.out.println("Bumping down priority for " + formulaComputationStatus.row + "," + formulaComputationStatus.column);

                                formulaComputationStatus.priority = 10; /// TODO: assumes single window, change this.
                                minPriority = formulaComputationStatusHashMap
                                        .values()
                                        .stream()
                                        .mapToInt(e -> e.priority)
                                        .min().orElse(10);
                                notifyAll();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {

        }

    }

    public static class FormulaComputationStatus {
        public Object sheet;
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

    public synchronized void updateFormulaCell(int row, int column, Object cell, Object sheet, int priority) {
        FormulaComputationStatus formulaComputationStatus = new FormulaComputationStatus();
        formulaComputationStatus.row = row;
        formulaComputationStatus.column = column;
        formulaComputationStatus.cell = cell;
        formulaComputationStatus.sheet = sheet;
        formulaComputationStatus.totalCells = 1;
        formulaComputationStatus.currentCells = 0;
        formulaComputationStatus.priority = priority;
        formulaComputationStatusHashMap.put(Thread.currentThread().getId(), formulaComputationStatus);
        if (priority < minPriority)
            minPriority = priority;
    }

    public void startComputation(int totalCells) {
        FormulaComputationStatus formulaComputationStatus =
                formulaComputationStatusHashMap.get(Thread.currentThread().getId());
        if (formulaComputationStatus!=null)
            formulaComputationStatus.totalCells = totalCells;
    }

    public synchronized void updateProgress(int currentCells) {
        FormulaComputationStatus formulaComputationStatus = formulaComputationStatusHashMap
                .get(Thread.currentThread().getId());
        if (formulaComputationStatus!=null)
        {
            formulaComputationStatus.currentCells = currentCells;
            while (formulaComputationStatus.priority > minPriority) {
                try {
                    System.out.println(Thread.currentThread().getId() + " Waiting ");
                    wait();
                    System.out.println(Thread.currentThread().getId() + " Waiting over ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
