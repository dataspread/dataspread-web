/*
 * Developed by Shichu Zhu on 8/7/18 8:52 AM.
 * Last modified 8/7/18 8:46 AM.
 */

package org.zkoss.poi.ss.formula;

/**
 * A global configuration and flag for extra features built upon original ZK formula engine.
 *
 * Including:
 * 1. Retrieve If condition from COUNTIF and SUMIF for the navigation aggregation charts.
 */
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
