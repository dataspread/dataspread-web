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

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zekun.fan@gmail.com on 7/11/17.
 * Todo maybe: load relief. In addTask, don't expand the area immediately.
 */
public class FormulaAsyncSchedulerFIFO extends FormulaAsyncScheduler {

    private ExecutorService pool;
    private HashMap<CellImpl,FormulaAsyncTaskInfo> infos;
    private HashMap<Object,Integer> xids;
    private final int maxThread=4;

    public FormulaAsyncSchedulerFIFO(){
        pool=Executors.newFixedThreadPool(maxThread);
        infos =new HashMap<>();
        xids =new HashMap<>();
    }

    @Override
    public synchronized void startTransaction(Object xsrc){
        Integer x= xids.get(xsrc);
        if (x!=null)
            ++x;
        else
            //May require resume from DB
            xids.put(xsrc,0);
    }

    @Override
    public synchronized void addTask(Ref target,Object xsrc) {
        //new task before last one's end, first cancelIfNotConfirmed as its execution is unordered
        //interrupt the working thread, removed explicit locking inside for preventing deadlock
        //Interrupt MAY CAUSE Problem, but can't be covered here.
        SBook book;
        SSheet sheet;

        book=BookBindings.get(target.getBookName());
        if (book==null){
            book=new BookImpl(target.getBookName());
            book.setIdAndLoad(target.getBookName());
        }
        sheet=book.getSheetByName(target.getSheetName());

       if (target.getType()==Ref.RefType.AREA || target.getType()==Ref.RefType.CELL){
            int in=target.getLastRow(),jn=target.getLastColumn();
            for (int i=target.getRow();i<=in;++i)
                for (int j=target.getColumn();j<=jn;++j){
                    //ugly, but works.
                    CellImpl cell=(CellImpl) sheet.getCell(target.getRow(),target.getColumn());
                    FormulaAsyncTaskInfo info= infos.get(cell);
                    if (info==null){
                        info = new FormulaAsyncTaskInfo();
                        info.target=cell;
                    }else if (xids.get(xsrc) > info.xid)
                        info.ctrl.cancel(false);
                    //This might not work, DK if it'll be loaded
                    FormulaExpression expr=cell.getFormulaExpression();
                    if (expr!=null) {
                        info.xid=xids.getOrDefault(xsrc,-1);
                        info.ctrl=pool.submit(new FormulaAsyncTask(cell, expr));
                    }
                }
        }
    }

    @Override
    public synchronized void cancelTask(Ref target,Object xsrc) {
        SBook book;
        SSheet sheet;
        int nowId=xids.getOrDefault(xsrc,-1);
        book=BookBindings.get(target.getBookName());
        if (book==null)
            return;
        sheet=book.getSheetByName(target.getSheetName());
        if (target.getType()==Ref.RefType.AREA || target.getType()==Ref.RefType.CELL){
            int in=target.getLastRow(),jn=target.getLastColumn();
            for (int i=target.getRow();i<=in;++i)
                for (int j=target.getColumn();j<=jn;++j){
                    //ugly, but works.
                    CellImpl cell=(CellImpl) sheet.getCell(target.getRow(),target.getColumn());
                    FormulaAsyncTaskInfo info= infos.get(cell);
                    //Interrupt should be allowed, will optimize later.
                    if (info != null && info.xid<=nowId && info.ctrl.cancel(false))
                        infos.remove(cell);
                }
        }
    }

    @Override
    public synchronized void clear() {
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

    private class FormulaAsyncTaskInfo{
        public int xid;
        public CellImpl target;
        public Future<?> ctrl;
    }
}
