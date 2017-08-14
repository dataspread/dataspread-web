package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
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
    private HashMap<CellImpl,FormulaAsyncTaskInfo> infos;
    private final int maxThread=4;
    private ReentrantLock xlock;
    private int xid;

    public FormulaAsyncSchedulerFIFO(){
        pool=Executors.newFixedThreadPool(maxThread);
        infos =new HashMap<>();
        xlock=new ReentrantLock();
        //reload required.
        xid=0;
    }

    @Override
    public void startTransaction(){
        xlock.lock();
        ++xid;
    }

    public void endTransaction(){
        xlock.unlock();
    }

    @Override
    public boolean addTask(Ref target){
        //new task before last one's end, first cancelIfNotConfirmed as its execution is unordered
        //interrupt the working thread, removed explicit locking inside for preventing deadlock
        //Interrupt MAY CAUSE Problem, but can't be covered here.
        SBook book;
        SSheet sheet;

        if (!xlock.isHeldByCurrentThread())
            return false;

        book=BookBindings.get(target.getBookName());
        if (book==null){
            book=new BookImpl(target.getBookName());
            book.setIdAndLoad(target.getBookName());
        }
        sheet=book.getSheetByName(target.getSheetName());
        if (sheet==null)
            return false;

       if (target.getType()==Ref.RefType.AREA || target.getType()==Ref.RefType.CELL){
            int in=target.getLastRow(),jn=target.getLastColumn();
            for (int i=target.getRow();i<=in;++i)
                for (int j=target.getColumn();j<=jn;++j){
                    //ugly, but works.
                    CellImpl cell=(CellImpl) sheet.getCell(target.getRow(),target.getColumn());
                    if (cell==null) continue;
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
                }
        }
        return true;
    }

    @Override
    public boolean cancelTask(Ref target) {
        SBook book;
        SSheet sheet;
        if (!xlock.isHeldByCurrentThread())
            return false;
        book=BookBindings.get(target.getBookName());
        if (book==null)
            return true;
        sheet=book.getSheetByName(target.getSheetName());
        if (sheet==null)
            return true;
        if (target.getType()==Ref.RefType.AREA || target.getType()==Ref.RefType.CELL){
            int in=target.getLastRow(),jn=target.getLastColumn();
            for (int i=target.getRow();i<=in;++i)
                for (int j=target.getColumn();j<=jn;++j){
                    //ugly, but works.
                    CellImpl cell=(CellImpl) sheet.getCell(target.getRow(),target.getColumn());
                    if (cell==null) continue;
                    FormulaAsyncTaskInfo info= infos.get(cell);
                    //Interrupt should be allowed, will optimize later.
                    if (info != null && info.xid<=xid && info.ctrl.cancel(false))
                        infos.remove(cell);
                }
        }
        return true;
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
    }

    private class FormulaAsyncTaskInfo{
        public int xid;
        public CellImpl target;
        public Future<?> ctrl;
    }
}
