package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.poi.ss.formula.FormulaComputationStatusManager;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerSimple extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerSimple.class.getName());
    private boolean keepRunning = true;

    public static void initScheduler() {
        schedulerInstance = new FormulaAsyncSchedulerSimple();
    }

    @Override
    public void run() {
        while (keepRunning) {
            List<DirtyManager.DirtyRecord> dirtyRecordSet = DirtyManager.dirtyManagerInstance.getAllDirtyRegions();

            List<SCell> computedCells = new ArrayList<>();

            // Compute visible range first
            for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
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
            }

            // Compute the remaining
            for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                //logger.info("Processing " + dirtyRecord.region);
                SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                for (SCell sCell : cells) {
                    if (computedCells.contains(sCell))
                        continue;
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
