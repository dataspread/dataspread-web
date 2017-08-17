package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.TransactionManager;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zekun.fan@gmail.com on 7/11/17.
 */
public class FormulaAsyncSchedulerFIFO extends FormulaAsyncScheduler {

    private ExecutorService pool;
    private ExecutorService expander;
    private HashMap<CellImpl,FormulaAsyncTaskInfo> infos;
    private final int maxThread=4;


    public FormulaAsyncSchedulerFIFO(){
        pool=Executors.newFixedThreadPool(maxThread);
        expander=Executors.newSingleThreadExecutor();
        infos =new HashMap<>();
    }

    @Override
    public void addTask(Ref target){
        //new task before last one's end, first cancelIfNotConfirmed as its execution is unordered
        //interrupt the working thread, removed explicit locking inside for preventing deadlock
        //Interrupt MAY CAUSE Problem, but can't be covered here.
        if (!TransactionManager.INSTANCE.isInTransaction(null))
            throw new RuntimeException("addTask not within transaction!");

        expander.submit(new Runnable() {
            @Override
            public void run() {
                SBook book;
                SSheet sheet;
                int xid=TransactionManager.INSTANCE.getXid(null);

                book=BookBindings.get(target.getBookName());
                if (book==null){
                    book=new BookImpl(target.getBookName());
                    book.setIdAndLoad(target.getBookName());
                }
                sheet=book.getSheetByName(target.getSheetName());
                if (sheet==null)
                    return;

                Collection<SCell> cells=sheet.getCells(new CellRegion(target.getRow(),target.getColumn(),target.getLastRow(),target.getLastColumn()));
                cells.forEach((sCell)->{
                    //ugly, but works.
                    CellImpl cell=(CellImpl)sCell;
                    FormulaAsyncTaskInfo info= infos.get(cell);
                    if (info==null){
                        info = new FormulaAsyncTaskInfo();
                        info.target=cell;
                    }else if (xid > info.xid)
                        info.ctrl.cancel(false);
                    //This might not work, DK if it'll be loaded
                    FormulaExpression expr=cell.getFormulaExpression();
                    //if the cell isn't a formula, expr==null
                    if (expr!=null) {
                        info.xid=xid;
                        infos.put(cell,info);
                        info.ctrl=pool.submit(new FormulaAsyncTask(cell, expr));
                    }
                });
            }
        });
    }

    @Override
    public void cancelTask(Ref target) {
        if (!TransactionManager.INSTANCE.isInTransaction(null))
            return;
            //throw new RuntimeException("cancelTask not within transaction!");
        expander.submit(new Runnable() {
            @Override
            public void run() {
                SBook book;
                SSheet sheet;
                int xid=TransactionManager.INSTANCE.getXid(null);

                book=BookBindings.get(target.getBookName());
                if (book==null)
                    return;
                sheet=book.getSheetByName(target.getSheetName());
                if (sheet==null || target.getType()!=Ref.RefType.AREA || target.getType()!=Ref.RefType.CELL)
                    return;
                Collection<SCell> cells=sheet.getCells(new CellRegion(target.getRow(),target.getColumn(),target.getLastRow(),target.getLastColumn()));
                cells.forEach((sCell)-> {
                    CellImpl cell=(CellImpl)sCell;
                    FormulaAsyncTaskInfo info= infos.get(cell);
                    if (info != null && info.xid<=xid && info.ctrl.cancel(false))
                        infos.remove(cell);
                });
            }
        });
    }

    @Override
    public void clear() {
        infos.forEach((cell, info) -> info.ctrl.cancel(false));
        infos.clear();
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
            infos.remove(target);
        }
    }

    @Override
    public void shutdown() {
        pool.shutdownNow();
        expander.shutdownNow();
    }

    private class FormulaAsyncTaskInfo{
        public int xid;
        public CellImpl target;
        public Future<?> ctrl;
    }
}
