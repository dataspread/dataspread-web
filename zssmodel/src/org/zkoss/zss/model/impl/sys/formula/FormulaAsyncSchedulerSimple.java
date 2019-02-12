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
public class FormulaAsyncSchedulerSimple extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerSimple.class.getName());
    private boolean keepRunning = true;
    public static boolean started = false;
    public static boolean isDead = false;
    private static PriorityQueue<CellItem> pq = new PriorityQueue<>(new CellItemCompare());
    private static Queue<SCell> queue = new LinkedList<>();
    private static int MAX_QUEUE_SIZE = 2000;
    private static Set<Ref> computedCells = new HashSet<>();

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

            //List<SCell> computedCells = new ArrayList<>();


            // Compute visible range first
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
                                    computedCells.add(sCell);
                                }
                                FormulaComputationStatusManager.getInstance().doneComputation();
                            }
                        }
                    }
                }
            }*/


            // Compute the remaining
            if (!prioritize) {
                for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                    //logger.info("Processing " + dirtyRecord.region);
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                    // Order of computation
                    List<SCell> cellsList = new ArrayList<>(cells);
                    //if (prioritize) {
                    //    cellsList.sort(Comparator.comparingInt(SCell::getComputeCost));
                    //} else {
                    Random random = new Random(7);
                    Collections.shuffle(cellsList, random);
                    //}


                    /// Order of computation
                    for (SCell sCell : cellsList) {
                        letsCompute(computedCells, sheet, sCell);
                    }

                    DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                            dirtyRecord.trxId);
                }
            } else {

                //System.err.println("QSIZE NOW: "+queue.size()+" "+pq.size());
                for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                    queue.addAll(cells);
                    DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                            dirtyRecord.trxId);
                }
                while (!queue.isEmpty() || !pq.isEmpty()) {
                    //System.err.println("QSIZE NOW: "+queue.size()+" "+pq.size());
                    while (!queue.isEmpty() && pq.size() < MAX_QUEUE_SIZE) {
                        SCell cell = queue.poll();
                        SSheet sheet = cell.getSheet();
                        DependencyTableAdv table = ((DependencyTableAdv) sheet.getDependencyTable());
                        Ref ref = cell.getRef();
                        Set<Ref> sr = table.getDirectPrecedents(ref);
                        int numPrec = (sr == null) ? 0 : sr.stream().mapToInt(Ref::getCellCount).sum();
                        int z = numPrec;//((cell.getRowIndex()+21)*29+11)%17;//(cell.getRowIndex()*7)%13;
                        pq.add(new CellItem(cell, z));
                    }
                    if (!pq.isEmpty()) {
                        SCell cell = pq.poll().cell;
                        //System.err.println("I CHOOSE YOU: "+cell.getCellRegion());
                        letsCompute(computedCells, cell.getSheet(), cell);
                    }
                }
            }
            //logger.info("Done computing " + dirtyRecord.region );
        }
        isDead = true;
        computedCells.clear();
        pq.clear();
        queue.clear();
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

    private void letsCompute(Set<Ref> computedCells,SSheet sheet, SCell cell) {
        //System.out.println("letsCompute " + cell);
        Ref ref = cell.getRef();
        //if (!computedCells.contains(ref)) {
            if (cell.getType() == SCell.CellType.FORMULA) {
                //System.err.println("computed: "+cell.getCellRegion());
                Object value = ((CellImpl) cell).getValue(true, true);
                update(sheet.getBook(), sheet, cell.getCellRegion(), value, cell.getFormulaValue());
                DirtyManagerLog.instance.markClean(cell.getCellRegion());
            } else {
                //System.err.println("computed: "+cell.getCellRegion());
                Object value = ((CellImpl) cell).getValue(true, true);
                update(sheet.getBook(), sheet, cell.getCellRegion(), value, "");
                DirtyManagerLog.instance.markClean(cell.getCellRegion());
            }
        //    computedCells.add(ref);
        //}
    }

    private void letsCompute_old(Set<Ref> computedCells, SSheet sheet, SCell cell) {
        //System.out.println("letsCompute " + cell);
        Ref ref = cell.getRef();
        if (!computedCells.contains(ref)) {
            if (cell.getType() == SCell.CellType.FORMULA) {
                Set<Ref> precedents = ((DependencyTableAdv) sheet.getDependencyTable()).getDirectPrecedents(ref);
                for (Ref precedentBlock : precedents) {
                    for (Ref precedent : getRefsInBlock(precedentBlock)) {
                        if (!computedCells.contains(precedent)) {
                            letsCompute(computedCells, sheet, sheet.getCell(new CellRegion(precedent)));
                        }
                    }
                }
                //System.err.println("computed: "+cell.getCellRegion());
                Object value = ((CellImpl) cell).getValue(true, true);
                update(sheet.getBook(), sheet, cell.getCellRegion(), value, cell.getFormulaValue());
                DirtyManagerLog.instance.markClean(cell.getCellRegion());
            } else {
                //System.err.println("computed: "+cell.getCellRegion());
                Object value = ((CellImpl) cell).getValue(true, true);
                update(sheet.getBook(), sheet, cell.getCellRegion(), value, "");
                DirtyManagerLog.instance.markClean(cell.getCellRegion());
            }
            computedCells.add(ref);
        }
    }


    @Override
    public void shutdown() {
        keepRunning = false;
    }


}
