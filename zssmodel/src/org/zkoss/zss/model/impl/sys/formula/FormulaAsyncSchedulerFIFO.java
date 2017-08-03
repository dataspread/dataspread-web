package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.FormulaResultCellValue;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.*;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zekun.fan@gmail.com on 7/11/17.
 */
public class FormulaAsyncSchedulerFIFO extends FormulaAsyncScheduler {

    private ExecutorService pool;
    private HashMap<CellImpl,Future<?>> futures;
    private final int maxThread=4;

    public FormulaAsyncSchedulerFIFO(){
        pool=Executors.newFixedThreadPool(maxThread);
        futures=new HashMap<>();
    }

    @Override
    public synchronized void addTask(CellImpl target, FormulaExpression expr) {
        //new task before last one's end, first cancelIfNotConfirmed as its execution is unordered
        //interrupt the working thread, removed explicit locking inside for preventing deadlock
        //Interrupt MAY CAUSE Problem, but can't be covered here.
        Future<?> last=futures.get(target);
        if (last!=null)
            last.cancel(false);
        if (FormulaAsyncScheduler.uiController!=null)
            FormulaAsyncScheduler.uiController.confirm(target);
        futures.put(target,pool.submit(new FormulaAsyncTask(target,expr)));
    }

    @Override
    public synchronized boolean cancelTask(CellImpl target) {
        Future<?> future = futures.get(target);
        //Interrupt not allowed
        if (future != null && future.cancel(false)){
            futures.remove(target);
            return true;
        }
        else return false;
    }

    @Override
    public synchronized void clear() {
        futures.forEach((formulaResultCellValue, future) -> future.cancel(false));
        futures.clear();
    }

    private class FormulaAsyncTask implements Runnable{
        private CellImpl target;
        private FormulaExpression expr;
        private FormulaEvaluationContext evalContext;

        FormulaAsyncTask(CellImpl target, FormulaExpression expr) {
            this.target = target;
            this.expr = expr;
            this.evalContext = new FormulaEvaluationContext(target,new RefImpl(target));
        }

        @Override
        public void run() {
            //try {Thread.sleep(5000);}catch (InterruptedException ignored){}
            FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
            EvaluationResult result = fe.evaluate(expr,evalContext);
            target.updateFormulaResultValue(result);
            if (FormulaAsyncScheduler.uiController!=null){
                FormulaAsyncScheduler.uiController.updateAndRelease(target);
            }
            futures.remove(target);
        }
    }
}
