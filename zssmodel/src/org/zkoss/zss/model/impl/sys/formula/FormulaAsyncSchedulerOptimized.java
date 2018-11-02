package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;
import org.zkoss.zss.model.sys.formula.QueryOptimization.QueryOptimizer;
import org.zkoss.zss.model.sys.formula.QueryOptimization.QueryPlanGraph;
import org.zkoss.zss.model.sys.formula.Test.Timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.zkoss.zss.model.sys.formula.Decomposer.FormulaDecomposer.decomposeFormula;
import static org.zkoss.zss.model.sys.formula.Test.Timer.time;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerOptimized extends FormulaAsyncScheduler {
    private static final Logger logger = Logger.getLogger(FormulaAsyncSchedulerOptimized.class.getName());
    private boolean keepRunning = true;

    @Override
    public void run() {
        while (keepRunning) {
            formulaUpdateLock.lock();
            formulaUpdateLock.unlock();

            List<DirtyManager.DirtyRecord> dirtyRecordSet = DirtyManager.dirtyManagerInstance.getAllDirtyRegions();

            if (dirtyRecordSet.size() == 0)
                continue;

            final FormulaAsyncScheduler scheduler = this;

            AtomicReference<Boolean> noException = new AtomicReference<>(true);

            time("Whole running cycle", ()->{
                final ArrayList<QueryPlanGraph> graphs = new ArrayList<>();
                for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                    SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);
                    Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                    for (SCell sCell : cells) {
                        if (sCell.getType() == SCell.CellType.FORMULA) {
                            DirtyManagerLog.instance.markClean(sCell.getCellRegion());
                            time("Decomposition",()->{
                                try {
                                    graphs.add(decomposeFormula(((FormulaExpression) ((AbstractCellAdv) sCell)
                                            .getValue(false)).getPtgs(),sCell));
                                } catch (OptimizationError optimizationError) {
                                    optimizationError.printStackTrace();
                                    noException.set(false);
                                }
                            });


                        }
                    }
                    DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                            dirtyRecord.trxId);
                }

                if (graphs.size() == 0)
                    return;
                AtomicReference<QueryPlanGraph> optimizedGraph = new AtomicReference<>(null);
                time("Optimization",()->{
                    try {
                        optimizedGraph.set(QueryOptimizer.getOptimizer().optimize(graphs));
                    } catch (OptimizationError optimizationError) {
                        optimizationError.printStackTrace();
                        noException.set(false);
                    }
                });
                if (noException.get())
                    try {
                        FormulaExecutor.getExecutor().execute(optimizedGraph.get(),scheduler);
                    } catch (Exception e) {
                        e.printStackTrace();
                        noException.set(false);
                    }
            });
            if (noException.get())
                Timer.outputTime(Collections.singleton("Whole running cycle"));
            else
                Timer.clear();

        }
    }

    @Override
    public void shutdown() {
        keepRunning = false;
    }


}
