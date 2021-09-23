package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableAdv;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.*;
import java.util.logging.Logger;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerTesting extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerTesting.class.getName());

    private boolean keepRunning = true;
    private boolean started = false;
    private boolean shutdownCompleted = false;

    // private final PriorityQueue<CellItem> pq = new PriorityQueue<>(new CellItemCompare());
    // private final Queue<SCell> queue = new LinkedList<>();
    private final List<SCell> dirtyList = new LinkedList<>();
    private final Set<Ref> computedCells = new HashSet<>();

    private static final int MAX_QUEUE_SIZE = 2000;
    private static final boolean SHUFFLE_UNPRIORITIZED = true;
    private static final boolean COMPUTE_WINDOW_FIRST = false;
    private static final boolean EXPLICITLY_COMPUTE_PRECEDENTS_FIRST = false;
    private static final boolean TRACK_COMPUTED_CELLS = false;

    public static void initScheduler() {
        schedulerInstance = new FormulaAsyncSchedulerTesting();
    }

    private static class CellItem {
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

    @Override
    public void run() {
        while (keepRunning) {
            if (!started) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {

                }
                continue;
            }
            List<DirtyManager.DirtyRecord> dirtyRecordSet = DirtyManager.dirtyManagerInstance.getAllDirtyRegions();

            if (COMPUTE_WINDOW_FIRST) {
                // TODO: Replace with prioritization by weight
                /*for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Map<String, int[]> visibleRange = uiVisibleMap.get(sheet);
                    if (visibleRange != null && !visibleRange.isEmpty()) {
                        for (int[] rows : visibleRange.values()) {
                            Ref overlap = dirtyRecord.region
                                    .getOverlap(new RefImpl(null, null,
                                            rows[0], 0, rows[1], Integer.MAX_VALUE));
                            if (overlap != null) {
                                Collection<SCell> cells = sheet.getCells(new CellRegion(overlap));
                                for (SCell sCell : cells) {
                                    if (sCell.getType() == SCell.CellType.FORMULA) {
                                        FormulaComputationStatusManager.getInstance().updateFormulaCell(
                                                sCell.getRowIndex(),
                                                sCell.getColumnIndex(),
                                                sCell, sheet, 10);

                                        // A sync call should synchronously compute the cells value.
                                        // Push individual cells to the UI
                                        DirtyManagerLog.instance.markClean(sCell.getCellRegion());
                                        update(sheet.getBook(), sheet, sCell.getCellRegion(),
                                                ((CellImpl) sCell).getValue(true, true),
                                                sCell.getFormulaValue());
                                        computedCells.add(sCell.getRef());
                                    }
                                    FormulaComputationStatusManager.getInstance().doneComputation();
                                }
                            }
                        }
                    }
                }*/
            }


            // Compute the remaining
            if (!prioritize) {
                for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                    // Decide on the order of computation
                    List<SCell> cellsList = new ArrayList<>(cells);
                    if (SHUFFLE_UNPRIORITIZED) {
                        Random random = new Random(7);
                        Collections.shuffle(cellsList, random);
                    }
                    for (SCell sCell : cellsList) {
                        computeCell(computedCells, sheet, sCell);
                    }
                    DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                            dirtyRecord.trxId);
                }
            } else {
                for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                    // queue.addAll(cells);
                    dirtyList.addAll(cells);
                    DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                            dirtyRecord.trxId);
                }

                Collections.sort(dirtyList, new SortbyPos());
                while (!dirtyList.isEmpty()) {
                    SCell cell = dirtyList.remove(0);
                    computeCell(computedCells, cell.getSheet(), cell);
                }

                // while (!queue.isEmpty() || !pq.isEmpty()) {
                //     // Replenish the priority queue if possible
                //     while (!queue.isEmpty() && pq.size() < MAX_QUEUE_SIZE) {
                //         SCell cell = queue.poll();
                //         int priority = getPriorityPrecedentsCount(cell);
                //         pq.add(new CellItem(cell, priority));
                //     }
                //     // See if there is a cell to be computed
                //     if (!pq.isEmpty()) {
                //         SCell cell = pq.poll().cell;
                //         computeCell(computedCells, cell.getSheet(), cell);
                //     }
                // }
            }
        }
        shutdownCompleted = true;
        //pq.clear();
        //queue.clear();
        if (TRACK_COMPUTED_CELLS) {
            computedCells.clear();
        }
    }

    private static class SortbyPos implements Comparator<SCell>
    {
        public int compare(SCell a, SCell b)
        {
            return a.getRowIndex() - b.getRowIndex();
        }
    }


    private int getPriorityRandomCellHash(SCell cell) {
        return ((cell.getRowIndex()+21)*29+11)%17;
    }

    private int getPriorityPrecedentsCount(SCell cell) {
        SSheet sheet = cell.getSheet();
        DependencyTableAdv table = ((DependencyTableAdv) sheet.getDependencyTable());
        Set<Ref> sr = table.getDirectPrecedents(cell.getRef());
        return (sr == null) ? 0 : sr.stream().mapToInt(Ref::getCellCount).sum();
    }

    private List<Ref> getRefsInBlock(Ref ref) {
        List<Ref> ans = new ArrayList<>();
        if (ref.getType() == Ref.RefType.CELL) {
            ans.add(ref);
        } else if (ref.getType() == Ref.RefType.AREA) {
            String bookName = ref.getBookName();
            String sheetName = ref.getSheetName();
            String lastSheetName = ref.getLastSheetName();
            for (int i = ref.getRow(); i <= ref.getLastRow(); i++) {
                for (int j = ref.getColumn(); j <= ref.getLastColumn(); j++) {
                    ans.add(new RefImpl(bookName, sheetName, lastSheetName, i, j));
                }
            }
        }
        return ans;
    }

    private void computeCell(Set<Ref> computedCells, SSheet sheet, SCell cell) {
        Ref ref = cell.getRef();
        if (!TRACK_COMPUTED_CELLS || !computedCells.contains(ref)) {
            String formula = "";
            if (cell.getType() == SCell.CellType.FORMULA) {
                formula = cell.getFormulaValue();
                if (EXPLICITLY_COMPUTE_PRECEDENTS_FIRST) {
                    Set<Ref> precedents = ((DependencyTableAdv) sheet.getDependencyTable()).getDirectPrecedents(ref);
                    for (Ref precedentBlock : precedents) {
                        for (Ref precedent : getRefsInBlock(precedentBlock)) {
                            if (!TRACK_COMPUTED_CELLS || !computedCells.contains(precedent)) {
                                computeCell(computedCells, sheet, sheet.getCell(new CellRegion(precedent)));
                            }
                        }
                    }
                }
            }
            Object value = ((CellImpl) cell).getValue(true, true);
            update(sheet.getBook(), sheet, cell.getCellRegion(), value, formula);
            DirtyManagerLog.instance.markClean(cell.getCellRegion());
            if (TRACK_COMPUTED_CELLS) {
                computedCells.add(ref);
            }
        }
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public boolean isShutdownCompleted() {
        return shutdownCompleted;
    }

    @Override
    public void reset() {
        started = false;
        shutdownCompleted = false;
    }

    @Override
    public void shutdown() {
        keepRunning = false;
    }

}
