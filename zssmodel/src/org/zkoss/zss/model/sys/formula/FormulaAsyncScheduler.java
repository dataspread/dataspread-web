package org.zkoss.zss.model.sys.formula;


import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;


public abstract class FormulaAsyncScheduler implements Runnable {
    private static FormulaAsyncScheduler _schedulerInstance;
    private static FormulaAsyncListener formulaAsyncListener;

    public static void initFormulaAsyncScheduler(FormulaAsyncScheduler formulaAsyncScheduler) {
        _schedulerInstance = formulaAsyncScheduler;
    }

    public static void initFormulaAsyncListener(FormulaAsyncListener formulaAsyncListener) {
        FormulaAsyncScheduler.formulaAsyncListener = formulaAsyncListener;
    }

    public static FormulaAsyncScheduler getScheduler(){
        return _schedulerInstance;
    }


    protected void update(SBook book, SSheet sheet, CellRegion cellRegion, String value, String formula) {
        if (formulaAsyncListener != null) {
            formulaAsyncListener.update(book, sheet, cellRegion, value, formula);
        }
    }

    public abstract void waitForCompletion();

    public abstract void shutdown();
}