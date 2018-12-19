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
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Execute formulae in a single thread.
 */
public class FormulaAsyncSchedulerThreaded extends FormulaAsyncScheduler {
    private boolean keepRunning = true;
    private boolean emptyQueue = false;
    private ThreadPoolExecutor executorPool;
    private MyMonitorThread monitor;
    private final boolean runMonitor = true;
    private final int MaximumWorkers = 4;

    public class DynamicPriorityAdjuster implements Runnable {
        boolean run = true;

        @Override
        public void run() {
            while (run) {
                FormulaComputationStatusManager.getInstance().updatePriorities(uiVisibleMap);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void shutdown() {
            this.run = false;
        }

    }

    public class WorkerThread implements Runnable {
        private SCell sCell;

        public WorkerThread(SCell s) {
            this.sCell = s;
        }

        @Override
        public void run() {
            //System.out.println(Thread.currentThread().getName() + " Start. Cell = " + sCell);
            int priority = 10;
            // if visible increase priority.


            FormulaComputationStatusManager.getInstance().updateFormulaCell(
                    sCell.getRowIndex(),
                    sCell.getColumnIndex(),
                    sCell,
                    sCell.getSheet(),
                    priority);
            if (sCell.getType() == SCell.CellType.FORMULA) {
                // A sync call should synchronously compute the cells value.
                // Push individual cells to the UI
                update(sCell.getSheet().getBook(), sCell.getSheet(), sCell.getCellRegion(),
                        ((CellImpl) sCell).getValue(true, true).toString(),
                        sCell.getFormulaValue());
                DirtyManagerLog.instance.markClean(sCell.getCellRegion());
            }
            FormulaComputationStatusManager.getInstance().doneComputation();

            // System.out.println(Thread.currentThread().getName() + " End.");
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
        executorPool = new ThreadPoolExecutor(MaximumWorkers, MaximumWorkers, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        Thread monitorThread;
        if (runMonitor) {
            monitor = new MyMonitorThread(executorPool, 10);
            monitorThread = new Thread(monitor);
            monitorThread.start();
        }

        DynamicPriorityAdjuster dynamicPriorityAdjuster = new DynamicPriorityAdjuster();
        Thread dynamicPriorityAdjusterThread = new Thread(dynamicPriorityAdjuster);
        dynamicPriorityAdjusterThread.start();

        while (keepRunning) {
            List<DirtyManager.DirtyRecord> dirtyRecordSet = DirtyManager.dirtyManagerInstance.getAllDirtyRegions();
            for (DirtyManager.DirtyRecord dirtyRecord : dirtyRecordSet) {
                // logger.info("Processing " + dirtyRecord.region );
                SSheet sheet = BookBindings.getSheetByRef(dirtyRecord.region);

                //TODO - Change to streaming.
                // Or break a big region into smaller parts.
                Collection<SCell> cells = sheet.getCells(new CellRegion(dirtyRecord.region));
                for (SCell sCell : cells) {
                    executorPool.execute(new WorkerThread(sCell));
                }
                //DirtyManager.dirtyManagerInstance.removeDirtyRegion(dirtyRecord.region,
                //        dirtyRecord.trxId);
                //logger.info("Done computing " + dirtyRecord.region );
            }
        }
        System.out.println("Shutdown  FormulaAsyncSchedulerThreaded");
        executorPool.shutdown();
        if (runMonitor)
            monitor.shutdown();
        dynamicPriorityAdjuster.shutdown();
        try {
            if (runMonitor)
                monitorThread.join();
            dynamicPriorityAdjusterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown Called FormulaAsyncSchedulerThreaded");
        keepRunning = false;
    }
}
