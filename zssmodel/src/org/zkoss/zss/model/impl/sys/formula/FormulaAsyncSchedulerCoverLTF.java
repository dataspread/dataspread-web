package org.zkoss.zss.model.impl.sys.formula;

import org.model.LruCache;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.TransactionManager;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.*;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

//Least Time First
public class FormulaAsyncSchedulerCoverLTF extends FormulaAsyncScheduler {

    private ExecutorService pool;
    private ExecutorService expander;
    private HashMap<CellImpl,FormulaAsyncTaskInfo> infos;

    private Map<Ref,Long> metric;

    private final int maxThread=8;
    private PrintWriter writer;

    public FormulaAsyncSchedulerCoverLTF(){
        pool= new ThreadPoolExecutor(maxThread/2,maxThread,5,TimeUnit.MINUTES,
                new PriorityBlockingQueue<>(1024, new PriorityFutureComparator())){
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                return new PriorityFuture<T>(runnable,value,((FormulaAsyncTask)runnable).info.metric);
            }
        };

        expander=Executors.newSingleThreadExecutor();
        infos =new HashMap<>();
        metric= Collections.synchronizedMap(new LinkedHashMap<>());
        if (logWriter!=null)
            writer=new PrintWriter(logWriter);
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
        if (!TransactionManager.INSTANCE.isInTransaction(BookBindings.get(target.getBookName())))
            return;
        int xid=TransactionManager.INSTANCE.getXid(BookBindings.get(target.getBookName()));

        expander.submit(new Runnable() {
            @Override
            public void run() {
                SSheet sheet=BookBindings.getSheetByRef(target,true);

                Collection<SCell> cells=sheet.getCells(new CellRegion(target.getRow(),target.getColumn(),target.getLastRow(),target.getLastColumn()));
                cells.forEach((sCell)->{
                    //ugly, but works.
                    CellImpl cell=(CellImpl)sCell;
                    FormulaAsyncTaskInfo info=infos.get(cell);
                    if (info==null)
                        info = new FormulaAsyncTaskInfo();
                    else if (xid > info.xid)
                        info.ctrl.cancel(true);
                    info.target=cell;
                    //This might not work, DK if it'll be loaded
                    FormulaExpression expr=cell.getFormulaExpression();
                    //if the cell isn't a formula, expr==null
                    if (expr!=null) {
                        info.xid=xid;
                        info.expr=expr;
                        info.metric=metric.getOrDefault(target,0L);
                        infos.put(cell,info);
                        info.ctime= Instant.now().toEpochMilli();
                        try {
                            info.ctrl = pool.submit(new FormulaAsyncTask(info));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        FormulaCacheMasker.INSTANCE.unmask(new RefImpl(cell),xid);
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
        if (!TransactionManager.INSTANCE.isInTransaction(BookBindings.get(target.getBookName())))
            return;
        int xid=TransactionManager.INSTANCE.getXid(BookBindings.get(target.getBookName()));
        expander.submit(new Runnable() {
            @Override
            public void run() {
                SSheet sheet=BookBindings.getSheetByRef(target,true);

                Collection<SCell> cells=sheet.getCells(new CellRegion(target.getRow(),target.getColumn(),target.getLastRow(),target.getLastColumn()));
                cells.forEach((sCell)-> {
                    CellImpl cell=(CellImpl)sCell;
                    FormulaAsyncTaskInfo info= infos.get(cell);
                    if (info != null && info.xid<xid && info.ctrl.cancel(true))
                        infos.remove(cell);
                });
            }
        });
    }

    @Override
    public void clear() {
        infos.forEach((cell, info) -> info.ctrl.cancel(true));
        infos.clear();
    }

    private class FormulaAsyncTask implements Runnable{
        private FormulaAsyncTaskInfo info;

        FormulaAsyncTask(FormulaAsyncTaskInfo info) {
            this.info=info;
        }

        @Override
        public void run() {
            //try {Thread.sleep(5000);}catch (InterruptedException ignored){return;}
            long stime=Instant.now().toEpochMilli();
            TransactionManager.INSTANCE.registerWorker(info.xid);
            Ref refTarget=new RefImpl(this.info.target);
            FormulaEvaluationContext evalContext=new FormulaEvaluationContext(this.info.target,refTarget);
            FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
            EvaluationResult result = fe.evaluate(info.expr,evalContext);
            info.target.updateFormulaResultValue(result,info.xid);
            if (FormulaAsyncScheduler.uiController!=null){
                FormulaAsyncScheduler.uiController.update(info.target.getSheet(),new CellRegion(info.target.getRowIndex(),info.target.getColumnIndex()));
            }
            FormulaCacheMasker.INSTANCE.unmask(refTarget,info.xid);
            infos.remove(info.target);
            long etime=Instant.now().toEpochMilli();
            metric.put(refTarget,stime-etime);
            if (writer!=null)
                writer.printf("%s,%d,%d\n", info.expr.getFormulaString(), etime-stime, etime-info.ctime);
        }
    }

    @Override
    public void shutdown() {
        pool.shutdownNow();
        expander.shutdownNow();
    }

    public void shutdownGracefully() throws InterruptedException{
        while (!pool.awaitTermination(1, TimeUnit.MINUTES));
        shutdown();
    }

    private class FormulaAsyncTaskInfo{
        public int xid;
        public CellImpl target;
        public Future<?> ctrl;
        public FormulaExpression expr;
        public long ctime;
        public long metric;
    }

    class PriorityFuture<T> extends FutureTask<T>{
        private long priority;

        public PriorityFuture(Callable callable,long priority) {
            super(callable);
            this.priority=priority;
        }

        public PriorityFuture(Runnable runnable, T result, long priority) {
            super(runnable, result);
            this.priority = priority;
        }

        public long getPriority() {
            return priority;
        }
    }

    class PriorityFutureComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable o1, Runnable o2) {
            if (o1 == null && o2 == null)
                return 0;
            else if (o1 == null)
                return -1;
            else if (o2 == null)
                return 1;
            else
                try {
                    return ((PriorityFuture<?>) o1).getPriority() > ((PriorityFuture<?>) o2).getPriority()
                            ? 1 : -1;
                }catch (Exception e){
                    e.printStackTrace();
                }
            return 0;
        }
    }
}