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

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

        //No longer throws exception as long as it's registered in transaction
        //if (!TransactionManager.INSTANCE.isInTransaction(null))
        //    throw new RuntimeException("addTask not within transaction!");
        if (target.getType()!= Ref.RefType.CELL && target.getType()!=Ref.RefType.AREA)
            return;
        int xid=TransactionManager.INSTANCE.getXid(BookBindings.get(target.getBookName()));

        expander.submit(new Runnable() {
            @Override
            public void run() {
                SBook book;
                SSheet sheet;
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
                    FormulaAsyncTaskInfo info=infos.get(cell);
                    if (info==null)
                        info = new FormulaAsyncTaskInfo();
                    else if (xid > info.xid)
                        info.ctrl.cancel(false);
                    info.target=cell;
                    //This might not work, DK if it'll be loaded
                    FormulaExpression expr=cell.getFormulaExpression();
                    //if the cell isn't a formula, expr==null
                    if (expr!=null) {
                        info.xid=xid;
                        info.expr=expr;
                        infos.put(cell,info);
                        info.ctrl=pool.submit(new FormulaAsyncTask(info));
                    }
                });
            }
        });
    }

    @Override
    public void cancelTask(Ref target) {
        //No longer throws exception as long as it's registered in transaction
        //if (!TransactionManager.INSTANCE.isInTransaction(null))
        //throw new RuntimeException("cancelTask not within transaction!");
        if (target.getType()!= Ref.RefType.CELL && target.getType()!=Ref.RefType.AREA)
            return;
        int xid=TransactionManager.INSTANCE.getXid(BookBindings.get(target.getBookName()));
        expander.submit(new Runnable() {
            @Override
            public void run() {
                SBook book;
                SSheet sheet;
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
                    if (info != null && info.xid<xid && info.ctrl.cancel(false))
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
        private FormulaAsyncTaskInfo info;

        FormulaAsyncTask(FormulaAsyncTaskInfo info) {
            this.info=info;
        }

        @Override
        public void run() {
            //try {Thread.sleep(5000);}catch (InterruptedException ignored){}
            TransactionManager.INSTANCE.registerWorker(info.xid);
            Ref refTarget=new RefImpl(this.info.target);
            FormulaEvaluationContext evalContext=new FormulaEvaluationContext(this.info.target,refTarget);
            FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
            EvaluationResult result = fe.evaluate(info.expr,evalContext);
            info.target.updateFormulaResultValue(result,info.xid);
            if (FormulaAsyncScheduler.uiController!=null){
                FormulaAsyncScheduler.uiController.update(info.target.getSheet(),new CellRegion(info.target.getRowIndex(),info.target.getColumnIndex()));
            }
            infos.remove(info.target);
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
        public FormulaExpression expr;
    }
}
