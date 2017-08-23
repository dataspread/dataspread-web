package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerFIFO;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * Created by zekun.fan@gmail.com on 7/11/17.
 */

public abstract class FormulaAsyncScheduler {
    protected static FormulaAsyncUIController uiController;
    private static Class _instanceType = FormulaAsyncSchedulerFIFO.class;
    private static FormulaAsyncScheduler _schedulerInstance;

    public static void initUiController(FormulaAsyncUIController uiController) {
        if (FormulaAsyncScheduler.uiController == null)
            FormulaAsyncScheduler.uiController = uiController;
    }

    public static FormulaAsyncUIController getUiController() {
        return uiController;
    }

    public static FormulaAsyncScheduler getScheduler() {
        if (_schedulerInstance != null)
            return _schedulerInstance;
        if (_instanceType == FormulaAsyncSchedulerFIFO.class)
            _schedulerInstance = new FormulaAsyncSchedulerFIFO();
        return _schedulerInstance;
    }

    /*
    * startTransaction: Increment transaction number on a given object
    */

    public abstract void startTransaction();

    public abstract void endTransaction();

    /* addTask: add an async task.
    * 1. If previous task on same target is not yet scheduled, it will be canceled
    * 2. If it's in progress, will block until it's finished and execute
    * 3. If no previous task, will add.
    * To prevent the presence of stale value. Ensure serial execution.
    * This also enforces one target one task.
    */
    public abstract boolean addTask(Ref target);

    /* cancelTask:
    * 1. If the task is not yet scheduled, it will be canceled, return true
    * 2. If it's in progress, will block until it's finished, return false.
    */
    public abstract boolean cancelTask(Ref target);

    /* clear:
     * Clear all unscheduled task. Tasks in progress will finish
     */
    public abstract void clear();

    public abstract void shutdown();
}