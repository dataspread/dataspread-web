package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.ModelEvents;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.TransactionManager;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.*;

import javax.servlet.ServletContextEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
            DirtyManager.DirtyRecord dirtyRecord=DirtyManager.dirtyManagerInstance.getDirtyRegionFromQueue();
            if (dirtyRecord==null)
                continue;
            logger.info("Processing " + dirtyRecord.region );
            SSheet sheet=BookBindings.getSheetByRef(dirtyRecord.region,true);

            //TODO - Change to streaming.
            Collection<SCell> cells=sheet.getCells(new CellRegion(dirtyRecord.region));
            for (SCell sCell:cells)
            {
                CellImpl cell = (CellImpl) sCell;
                if (cell.getType()== SCell.CellType.FORMULA) {
                    FormulaEvaluationContext evalContext =
                            new FormulaEvaluationContext(sCell, ((CellImpl) sCell).getRef());
                    FormulaExpression expr = cell.getFormulaExpression();
                    FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
                    fe.clearCache(new FormulaClearContext(sheet));
                    EvaluationResult result = fe.evaluate(expr, evalContext);
                    cell.updateFormulaResultValue(result, dirtyRecord.trxId);
                }
            }
            update(sheet,new CellRegion(dirtyRecord.region));
        }
    }

    @Override
    public void shutdown() {
        keepRunning = false;
    }


}
