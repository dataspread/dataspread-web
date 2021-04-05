package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;

import java.util.Map;

public abstract class FormulaAsyncScheduler implements Runnable {

    protected static FormulaAsyncScheduler schedulerInstance = null;

    public static FormulaAsyncScheduler getScheduler() {
        return schedulerInstance;
    }

    private FormulaAsyncListener formulaAsyncListener;
    // sheet->session-> start,end row
    protected Map<Object, Map<String, int[]>> uiVisibleMap;
    protected boolean prioritize = true;

    public void setFormulaAsyncListener(FormulaAsyncListener listener) {
        formulaAsyncListener = listener;
    }

    public void setPrioritize(boolean p) {
        prioritize = p;
    }

    public void updateVisibleMap(Map<Object, Map<String, int[]>> visibleMap) {
        uiVisibleMap = visibleMap;
    }

    protected void update(SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula) {
        if (formulaAsyncListener != null) {
            formulaAsyncListener.update(book, sheet, cellRegion, value, formula);
        }
    }

    public void start() {

    }

    public boolean isShutdownCompleted() {
        return false;
    }

    public void reset() {

    }

    public abstract void shutdown();

}