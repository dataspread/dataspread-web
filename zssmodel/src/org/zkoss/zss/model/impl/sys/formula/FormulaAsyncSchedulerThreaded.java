package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.poi.ss.formula.FormulaComputationStatusManager;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerThreaded extends FormulaAsyncScheduler {
    private boolean keepRunning = true;
    private boolean emptyQueue = false;
    ThreadPoolExecutor executorPool;
    MyMonitorThread monitor;
    final int MaximumWorkers = 4;


    public class WorkerThread implements Runnable {
        private SCell sCell;

        public WorkerThread(SCell s) {
            this.sCell = s;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " Start. Cell = " + sCell);
            FormulaComputationStatusManager.getInstance().updateFormulaCell(
                    sCell.getRowIndex(),
                    sCell.getColumnIndex(),
                    sCell);
            if (sCell.getType() == SCell.CellType.FORMULA) {
                // A sync call should synchronously compute the cells value.
                // Push individual cells to the UI
                update(sCell.getSheet().getBook(), sCell.getSheet(), sCell.getCellRegion(),
                        ((CellImpl) sCell).getValue(true, true).toString(),
                        sCell.getFormulaValue());
                DirtyManagerLog.instance.markClean(sCell.getCellRegion());
            }
            FormulaComputationStatusManager.getInstance().doneComputation();


            System.out.println(Thread.currentThread().getName() + " End.");
        }
    }

    public class MyMonitorThread implements Runnable {
        private ThreadPoolExecutor executor;
        private int seconds;
        private boolean run = true;

        public MyMonitorThread(ThreadPoolExecutor executor, int delay) {
            this.executor = executor;
            this.seconds = delay;
        }

        public void shutdown() {
            this.run = false;
        }

        @Override
        public void run() {
            while (run) {
                System.out.println(
                        String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                                this.executor.getPoolSize(),
                                this.executor.getCorePoolSize(),
                                this.executor.getActiveCount(),
                                this.executor.getCompletedTaskCount(),
                                this.executor.getTaskCount(),
                                this.executor.isShutdown(),
                                this.executor.isTerminated()));
                try {
                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void run() {
        System.out.println("Starting FormulaAsyncSchedulerThreaded");
        executorPool = new ThreadPoolExecutor(4, MaximumWorkers, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(MaximumWorkers));
        monitor = new MyMonitorThread(executorPool, 3);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();


        while (keepRunning) {
            DirtyManager.DirtyRecord dirtyRecord = DirtyManager.dirtyManagerInstance.getDirtyRegionFromQueue();
            if (DirtyManager.dirtyManagerInstance.isEmpty()) {
                synchronized (this) {
                    emptyQueue = true;
                    notifyAll();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else {
                emptyQueue = false;
            }
            if (dirtyRecord == null)
                continue;

            // logger.info("Processing " + dirtyRecord.region );
            SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);

            //TODO - Change to streaming.
            // Or break a big region into smaller parts.
            Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
            for (SCell sCell : cells) {
                executorPool.execute(new WorkerThread(sCell));
            }
            DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                    dirtyRecord.trxId);
            //This is to update the entire region
            //update(sheet, new CellRegion(dirtyRecord.region));
            //logger.info("Done computing " + dirtyRecord.region );
        }

        executorPool.shutdown();
        monitor.shutdown();
    }

    @Override
    public synchronized void waitForCompletion() {
        while (!emptyQueue) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void shutdown() {
        keepRunning = false;
    }


}
