package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerPriority extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerPriority.class.getName());
    private boolean keepRunning = true;
    ArrayList<SCell> cellsToCompute;
    final private static int QUEUE_SIZE = 20;

    public FormulaAsyncSchedulerPriority() {
        cellsToCompute = new ArrayList<>(QUEUE_SIZE);
    }

    @Override
    public void run() {
        ArrayList<SCell> cellsToCompute = new ArrayList<>(QUEUE_SIZE);
        DirtyManager.DirtyRecord dirtyRecord = null;
        SSheet sheet = null;
        int currentRow = 0;
        int currentColumn = 0;
        while (keepRunning) {
            if (dirtyRecord == null) {
                if (cellsToCompute.isEmpty()) {
                    synchronized (this) {
                        this.notify();
                    }
                    dirtyRecord = DirtyManager.dirtyManagerInstance.getDirtyRegionFromQueue(20);
                } else
                    dirtyRecord = DirtyManager.dirtyManagerInstance.getDirtyRegionFromQueue();

                if (dirtyRecord != null) {
                    sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    currentRow = dirtyRecord.region.getRow();
                    currentColumn = dirtyRecord.region.getColumn();
                }
            }


            if (cellsToCompute.size() == QUEUE_SIZE || (!cellsToCompute.isEmpty() && dirtyRecord == null)) {
                // Compute Cells
                costBasedSort(cellsToCompute);
                // Execute only half
                int i;
                for (i = 0; i < Math.min(cellsToCompute.size(), QUEUE_SIZE / 2); i++) {
                    SCell sCell = cellsToCompute.get(i);
                    ((CellImpl) sCell).getValue(true, true);
                    //System.out.println("Computing " + sCell);
                    // Push individual cells to the UI
                    update(sCell.getSheet(), sCell.getCellRegion());
                    DirtyManagerLog.instance.markClean(sCell.getCellRegion());
                }
                cellsToCompute.subList(0, i).clear();
            }

            while (cellsToCompute.size() < QUEUE_SIZE && dirtyRecord != null) {
                SCell sCell = sheet.getCell(currentRow, currentColumn);
                if (sCell.getType() == SCell.CellType.FORMULA) {
                    sCell.clearFormulaResultCache();
                    ((CellImpl) sCell).setTrxId(dirtyRecord.trxId);
                    cellsToCompute.add(sCell);
                }

                // Next cell
                currentColumn++;
                if (currentColumn > dirtyRecord.region.getLastColumn()) {
                    currentRow++;
                    if (currentRow > dirtyRecord.region.getLastRow()) {
                        DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                                dirtyRecord.trxId);
                        dirtyRecord = null;
                        break;
                    }
                    currentColumn = dirtyRecord.region.getColumn();
                }
            }
        }
    }

    private void costBasedSort(ArrayList<SCell> cellsToCompute) {
        cellsToCompute.sort(Comparator.comparingInt(e->e.getComputeCost()));
    }

    @Override
    public synchronized void waitForCompletion() {
        while (!cellsToCompute.isEmpty() || !DirtyManager.dirtyManagerInstance.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void shutdown() {
        keepRunning = false;
    }
}
