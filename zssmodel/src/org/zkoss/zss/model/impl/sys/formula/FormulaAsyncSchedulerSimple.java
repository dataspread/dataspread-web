package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerSimple extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerSimple.class.getName());
    private boolean keepRunning = true;
    private boolean emptyQueue = false;

    @Override
    public void run() {
        while (keepRunning) {
            DirtyManager.DirtyRecord dirtyRecord=DirtyManager.dirtyManagerInstance.getDirtyRegionFromQueue();
            if (dirtyRecord==null) {
                emptyQueue = true;
                synchronized (this) {
                      notifyAll();
                }
                continue;
            }
            emptyQueue = false;
            //logger.info("Processing " + dirtyRecord.region );
            SSheet sheet=BookBindings.getSheetByRef(dirtyRecord.region);

            //TODO - Change to streaming.
            // Or break a big region into smaller parts.
            Collection<SCell> cells=sheet.getCells(new CellRegion(dirtyRecord.region));
            for (SCell sCell:cells)
            {
                // Delay to demonstrate.
                //try {
                //    Thread.sleep(1000);
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                //}
                if (sCell.getType()== SCell.CellType.FORMULA) {
                    // A sync call should synchronously compute the cells value.
                    ((CellImpl) sCell).getValue(true,true);
                }
            }
            DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                    dirtyRecord.trxId);
            update(sheet,new CellRegion(dirtyRecord.region));
            //logger.info("Done computing " + dirtyRecord.region );
        }
    }

    @Override
    public synchronized void waitForCompletion() {
        while(!emptyQueue) {
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
