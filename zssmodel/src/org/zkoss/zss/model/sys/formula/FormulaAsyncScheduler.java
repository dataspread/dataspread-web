package org.zkoss.zss.model.sys.formula;


import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;


public abstract class FormulaAsyncScheduler implements Runnable {
    private static FormulaAsyncScheduler _schedulerInstance;
    private static FormulaAsyncUIController uiController;


    public static void initUiController(FormulaAsyncUIController uiController){
        if (FormulaAsyncScheduler.uiController==null)
            FormulaAsyncScheduler.uiController=uiController;
    }

    public static void initScheduler(FormulaAsyncScheduler scheduler){
        if (_schedulerInstance==null)
            _schedulerInstance=scheduler;
    }

    public static FormulaAsyncUIController getUiController(){
        return uiController;
    }

    public static FormulaAsyncScheduler getScheduler(){
        return _schedulerInstance;
    }

    protected void update(SSheet sheet, CellRegion cellRegion) {
        if (uiController!=null){
            uiController.update(sheet, cellRegion);
        }
    }


    public abstract void waitForCompletion();

    public abstract void shutdown();
}