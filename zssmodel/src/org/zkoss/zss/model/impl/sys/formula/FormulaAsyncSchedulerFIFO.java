package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.impl.FormulaResultCellValue;
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
    private HashMap<FormulaResultCellValue,Future<?>> futures;
    private final int maxThread=4;

    public FormulaAsyncSchedulerFIFO(){
        pool=Executors.newFixedThreadPool(maxThread);
        futures=new HashMap<>();
    }

    @Override
    public synchronized void addTask(FormulaResultCellValue target, FormulaExpression expr, FormulaEvaluationContext evalContext) {
        //new task before last one's end, first cancelIfNotConfirmed as its execution is unordered
        Future<?> last=futures.get(target);
        if (last!=null)
            if (!last.cancel(false))
                try {
                    last.get();
                }catch (Exception ignored){
                    // Exception can be seen as done.
                }
        if (FormulaAsyncScheduler.uiController!=null)
            FormulaAsyncScheduler.uiController.confirm(evalContext.getCell());
        futures.put(target,pool.submit(new FormulaAsyncTask(target,expr,evalContext)));
    }

    @Override
    public synchronized boolean cancelTask(FormulaResultCellValue target) {
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
        private FormulaResultCellValue target;
        private FormulaExpression expr;
        private FormulaEvaluationContext evalContext;

        FormulaAsyncTask(FormulaResultCellValue target, FormulaExpression expr, FormulaEvaluationContext evalContext) {
            this.target = target;
            this.expr = expr;
            this.evalContext = evalContext;
        }

        @Override
        public void run() {
            FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
            EvaluationResult result = fe.evaluate(expr,evalContext);
            target.updateByEvaluationResult(result);
            if (FormulaAsyncScheduler.uiController!=null){
                FormulaAsyncScheduler.uiController.updateAndRelease(evalContext.getCell());
            }
            futures.remove(target);
        }
    }
}
