package org.zkoss.zss.model.sys.formula;


import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;

import java.util.Map;


public abstract class FormulaAsyncScheduler implements Runnable {
    private static FormulaAsyncScheduler _schedulerInstance;
    private static FormulaAsyncListener formulaAsyncListener;
    // sheet->session-> start,end row
    protected static Map<Object, Map<String, int[]>> uiVisibleMap;

    public static void initFormulaAsyncScheduler(FormulaAsyncScheduler formulaAsyncScheduler) {
        _schedulerInstance = formulaAsyncScheduler;
    }

    public static void initFormulaAsyncListener(FormulaAsyncListener formulaAsyncListener) {
        FormulaAsyncScheduler.formulaAsyncListener = formulaAsyncListener;
    }

    public static FormulaAsyncScheduler getScheduler(){
        return _schedulerInstance;
    }

    public static void updateVisibleMap(Map<Object, Map<String, int[]>> uiVisibleMap) {
        FormulaAsyncScheduler.uiVisibleMap = uiVisibleMap;
    }


    protected void update(SBook book, SSheet sheet, CellRegion cellRegion, String value, String formula) {
        if (formulaAsyncListener != null) {
            formulaAsyncListener.update(book, sheet, cellRegion, value, formula);
        }
    }

    public abstract void shutdown();
}