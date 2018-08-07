package org.zkoss.poi.ss.formula;

public class FormulaConfiguration {
    private static FormulaConfiguration ourInstance = new FormulaConfiguration();

    public static FormulaConfiguration getInstance() {
        return ourInstance;
    }

    private FormulaConfiguration() {
    }

    public boolean isCutEvalAtIfCond() {
        return cutEvalAtIfCond;
    }

    public void setCutEvalAtIfCond(boolean cutEvalAtIfCond) {
        this.cutEvalAtIfCond = cutEvalAtIfCond;
    }

    private boolean cutEvalAtIfCond = false;
}
