package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerPriority extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerSimple.class.getName());
    private boolean keepRunning = true;
    private boolean emptyQueue = false;
    private List<DirtyManager.DirtyRecord> dirtyQueue;
    private int maxQueueSize = 1000;
    Thread thread;
    RegionToCells regionToCells;

    public FormulaAsyncSchedulerPriority() {

        dirtyQueue = new ArrayList<>();
        regionToCells = new RegionToCells();
        thread = new Thread(regionToCells);
        thread.start();

    }

    @Override
    public void run() {
        while (keepRunning) {





            while (true) {
                DirtyManager.DirtyRecord dirtyRecord = DirtyManager
                        .dirtyManagerInstance.getDirtyRegionFromQueue(10);
                if (dirtyRecord == null)
                    break;
                dirtyQueue.add(dirtyRecord);
                if (dirtyQueue.size() == maxQueueSize)
                    break;
            }

            if (DirtyManager.dirtyManagerInstance.isEmpty()) {
                synchronized (this) {
                    emptyQueue = true;
                    notifyAll();
                }
                continue;
            } else {
                emptyQueue = false;
            }

            if (emptyQueue && dirtyQueue.size() == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // Or break a big region into smaller parts.
            Collection<SCell> cells =null;// sheet.getCells(new CellRegion(dirtyRecord.region));
            for (SCell sCell : cells) {
                // Delay to demonstrate.
                //try {
                //    Thread.sleep(1000);
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                //}
                if (sCell.getType() == SCell.CellType.FORMULA) {
                    // A sync call should synchronously compute the cells value.
                    ((CellImpl) sCell).getValue(true, true);
                    // Push individual cells to the UI
                    // update(sheet, sCell.getCellRegion());
                    DirtyManagerLog.instance.markClean(sCell.getCellRegion());
                }
            }
            //   DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
            //          dirtyRecord.trxId);
            //This is to update the entire region
            //update(sheet, new CellRegion(dirtyRecord.region));
            //logger.info("Done computing " + dirtyRecord.region );
        }
    }

    @Override
    public synchronized void waitForCompletion() {
        while (!emptyQueue) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void shutdown() {
        keepRunning = false;
        regionToCells.shutdown();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    static class RegionToCells implements Runnable
    {
        private boolean keepRunning;
        public ArrayBlockingQueue<SCell> cellQueue;

        RegionToCells()
        {
            keepRunning = true;
            cellQueue = new ArrayBlockingQueue<>(10);
        }

        @Override
        public void run() {
            while (keepRunning) {
                DirtyManager.DirtyRecord dirtyRecord = DirtyManager.dirtyManagerInstance.getDirtyRegionFromQueue();
                if (DirtyManager.dirtyManagerInstance.isEmpty()) {
                    continue;
                }
                // logger.info("Processing " + dirtyRecord.region );
                SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                for (int row = dirtyRecord.region.getRow();
                     row <= dirtyRecord.region.getLastRow(); row++)
                    for (int col = dirtyRecord.region.getColumn();
                         col <= dirtyRecord.region.getLastColumn(); col++) {
                        SCell sCell = sheet.getCell(row, col);
                        if (sCell.getType() == SCell.CellType.FORMULA)
                            cellQueue.add(sCell);

                    }
            }

        }

        public void shutdown() {
            keepRunning = false;
        }
    }

}
