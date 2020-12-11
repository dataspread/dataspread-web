package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableImplCostApprox;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.*;


public class FormulaAsyncSchedulerCostApprox extends FormulaAsyncScheduler {
    private boolean keepRunning = true;
    public static boolean started = false;
    public static boolean isDead = false;

    private static PriorityQueue<CellItem> pq = new PriorityQueue<>(new CellItemCompare());
    private static Queue<SCell> queue = new LinkedList<>();
    private static int MAX_QUEUE_SIZE = 2000;
    private static Set<Ref> computedCells = new HashSet<>();
    private static int hops = 1;
    private static String method = "precedents";

    private class CellItem {
        public SCell cell;
        public int weight;

        CellItem(SCell _cell, int _weight) {
            cell = _cell;
            weight = _weight;
        }
    }

    private static class CellItemCompare implements Comparator<CellItem> {
        @Override
        public int compare(CellItem cellItem, CellItem t1) {
            return cellItem.weight-t1.weight;
        }
    }

    public void setHops(int h) {
        if (h >= 1) {
            hops = h;
        }
    }

    public void usePrecedents() {
        method = "precedents";
    }

    public void useDependents() {
        method = "dependents";
    }

    @Override
    public void run() throws ArithmeticException {
        while (keepRunning) {
            if (!started) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) { }
                continue;
            }

            List<DirtyManager.DirtyRecord> dirtyRecordSet = DirtyManager.dirtyManagerInstance.getAllDirtyRegions();
            if (!prioritize) {

                // Workload for this thread
                for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {

                    // Extract cells from region
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));

                    // If prioritization is not enabled, schedule cells in random order
                    List<SCell> cellsList = new ArrayList<>(cells);
                    Random random = new Random(7);
                    Collections.shuffle(cellsList, random);

                    // Compute the scheduled cells
                    for (SCell sCell : cellsList) {
                        letsCompute(computedCells, sheet, sCell);
                    }

                    // After computation, remove the dirty region
                    DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region, dirtyRecord.trxId);

                }
            } else {

                // Insert all cells that this thread should compute into a queue
                for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                    queue.addAll(cells);
                    DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                            dirtyRecord.trxId);
                }

                // while at least one of these queues is not empty
                while (!queue.isEmpty() || !pq.isEmpty()) {

                    // Only compute a subset of cells at a time
                    while (!queue.isEmpty() && pq.size() < MAX_QUEUE_SIZE) {
                        SCell cell = queue.poll();
                        SSheet sheet = cell.getSheet();

                        // DependencyTableAdv table = ((DependencyTableAdv) sheet.getDependencyTable());
                        DependencyTableImplCostApprox table = ((DependencyTableImplCostApprox) sheet.getDependencyTable());

                        // Get the number of precedents k hops away
                        Ref ref = cell.getRef();
                        Set<Ref> sr;
                        if (method.equals("precedents")) {
                            sr = table.getPrecedents(ref, hops);
                        } else {
                            sr = table.getDependents(ref, hops);
                        }
                        int numDirect = (sr == null) ? 0 : sr.stream().mapToInt(Ref::getCellCount).sum();

                        // Add this cell to the priority queue and attach a priority equal to the number of direct precedents / dependents
                        pq.add(new CellItem(cell, numDirect));
                    }

                    // If we have some cells to compute, compute them
                    if (!pq.isEmpty()) {
                        SCell cell = pq.poll().cell;
                        letsCompute(computedCells, cell.getSheet(), cell);
                    }
                }
            }
        }

        isDead = true;
        computedCells.clear();
        pq.clear();
        queue.clear();
    }

    private void letsCompute(Set<Ref> computedCells, SSheet sheet, SCell cell) {
        Ref ref = cell.getRef();
        //if (!computedCells.contains(ref)) {
        if (cell.getType() == SCell.CellType.FORMULA) {
            Object value = ((CellImpl) cell).getValue(true, true);
            update(sheet.getBook(), sheet, cell.getCellRegion(), value, cell.getFormulaValue());
            DirtyManagerLog.instance.markClean(cell.getCellRegion());
        } else {
            Object value = ((CellImpl) cell).getValue(true, true);
            update(sheet.getBook(), sheet, cell.getCellRegion(), value, "");
            DirtyManagerLog.instance.markClean(cell.getCellRegion());
        }
        //    computedCells.add(ref);
        //}
    }

    @Override
    public void shutdown() { keepRunning = false; }
}
