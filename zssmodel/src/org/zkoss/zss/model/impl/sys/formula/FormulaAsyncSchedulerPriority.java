package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerPriority extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerPriority.class.getName());
    private boolean keepRunning = true;
    private boolean emptyQueue = false;
    private List<DirtyManager.DirtyRecord> dirtyQueue;
    private static int queueSize = 500;
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
        ArrayList<SCell> cellsToCompute = new ArrayList<>(queueSize);
        Collections.shuffle(cellsToCompute);
        while (keepRunning) {
            if (regionToCells.cellQueue.isEmpty() && DirtyManager.dirtyManagerInstance.isEmpty()) {
                synchronized (this) {
                    emptyQueue = true;
                    notifyAll();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else {
                emptyQueue = false;
            }
            cellsToCompute.clear();
            cellsToCompute.addAll(regionToCells.cellQueue);
            for (SCell sCell : cellsToCompute) {
                ((CellImpl) sCell).getValue(true, true);
                // Push individual cells to the UI
                update(sCell.getSheet(), sCell.getCellRegion());
                DirtyManagerLog.instance.markClean(sCell.getCellRegion());

                regionToCells.cellQueue.remove(sCell);
                //logger.info("Done computing " + sCell.getCellRegion());
            }
        }
    }




/*
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
*/
            //logger.info("Done computing " + dirtyRecord.region );

    @Override
    public synchronized void waitForCompletion() {
        while (!emptyQueue || !regionToCells.cellQueue.isEmpty() || !DirtyManager.dirtyManagerInstance.isEmpty()) {
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
            cellQueue = new ArrayBlockingQueue<>(queueSize);
        }


        @Override
        public void run() {
            while (keepRunning) {
                DirtyManager.DirtyRecord dirtyRecord = DirtyManager.dirtyManagerInstance.getDirtyRegionFromQueue();
                if (dirtyRecord == null) {
                    continue;
                }

                SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                for (int row = dirtyRecord.region.getRow();
                     row <= dirtyRecord.region.getLastRow(); row++) {
                    for (int col = dirtyRecord.region.getColumn();
                         col <= dirtyRecord.region.getLastColumn(); col++) {
                        SCell sCell = sheet.getCell(row, col);
                        if (sCell.getType() == SCell.CellType.FORMULA)
                            cellQueue.add(sCell);
                        //else
                        //logger.info("sCell.getType()  " + sCell.getType() + " " +  sCell.getValue()  );
                    }
                }
                DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                        dirtyRecord.trxId);
            }

        }

        public void shutdown() {
            keepRunning = false;
        }
    }

}
