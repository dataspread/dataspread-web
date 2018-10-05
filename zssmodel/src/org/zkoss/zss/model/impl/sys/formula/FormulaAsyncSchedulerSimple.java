package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.poi.ss.formula.FormulaComputationStatusManager;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerSimple extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerSimple.class.getName());
    private boolean keepRunning = true;

    @Override
    public void run() {
        while (keepRunning) {
            Set<DirtyManager.DirtyRecord> dirtyRecordSet = DirtyManager.dirtyManagerInstance.getAllDirtyRegions();
            for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                //logger.info("Processing " + dirtyRecord.region);
                SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);

                Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                for (SCell sCell : cells) {
                    FormulaComputationStatusManager.getInstance().updateFormulaCell(
                            sCell.getRowIndex(),
                            sCell.getColumnIndex(),
                            sCell, sheet, 10);
                    if (sCell.getType() == SCell.CellType.FORMULA) {
                        // A sync call should synchronously compute the cells value.
                        // Push individual cells to the UI
                        DirtyManagerLog.instance.markClean(sCell.getCellRegion());
                        update(sheet.getBook(), sheet, sCell.getCellRegion(),
                                ((CellImpl) sCell).getValue(true, true).toString(),
                                sCell.getFormulaValue());
                    }
                    FormulaComputationStatusManager.getInstance().doneComputation();
                }
                DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                        dirtyRecord.trxId);
            }
            //logger.info("Done computing " + dirtyRecord.region );
        }
    }

    @Override
    public void shutdown() {
        keepRunning = false;
    }


}
