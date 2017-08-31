package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.FormulaResultCellValue;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerCoverFIFO;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerFIFO;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.io.Writer;

/**
 * Created by zekun.fan@gmail.com on 7/11/17.
 */

public abstract class FormulaAsyncScheduler {
    private static Class _instanceType=FormulaAsyncSchedulerFIFO.class;
    private static FormulaAsyncScheduler _schedulerInstance;
    protected static FormulaAsyncUIController uiController;
    protected static Writer logWriter;

    public static void initUiController(FormulaAsyncUIController uiController){
        if (FormulaAsyncScheduler.uiController==null)
            FormulaAsyncScheduler.uiController=uiController;
    }

    public static void initLogWriter(Writer logWriter){
        if (FormulaAsyncScheduler.logWriter==null)
            FormulaAsyncScheduler.logWriter=logWriter;
    }

    public static void initScheduler(FormulaAsyncScheduler scheduler){
        if (_schedulerInstance==null)
            _schedulerInstance=scheduler;
    }

    public static FormulaAsyncUIController getUiController(){
        return uiController;
    }

    public static FormulaAsyncScheduler getScheduler(){
        if (_schedulerInstance !=null)
            return _schedulerInstance;
        if (_instanceType==FormulaAsyncSchedulerFIFO.class)
            _schedulerInstance =new FormulaAsyncSchedulerFIFO();
        else if (_instanceType== FormulaAsyncSchedulerCoverFIFO.class)
            _schedulerInstance=new FormulaAsyncSchedulerCoverFIFO();
        return _schedulerInstance;
    }

    /*
    * startTransaction: Increment transaction number on a given object
    */

    /* addTask: add an async task.
    * 1. If previous task on same target is not yet scheduled, it will be canceled
    * 2. If it's in progress, will block until it's finished and execute
    * 3. If no previous task, will add.
    * To prevent the presence of stale value. Ensure serial execution.
    * This also enforces one target one task.
    */
    public abstract void addTask(Ref target);

    /* cancelTask:
    * 1. If the task is not yet scheduled, it will be canceled, return true
    * 2. If it's in progress, will block until it's finished, return false.
    */
    public abstract void cancelTask(Ref target);
    /* clear:
     * Clear all unscheduled task. Tasks in progress will finish
     */
    public abstract void clear();

    public abstract void shutdown();
}