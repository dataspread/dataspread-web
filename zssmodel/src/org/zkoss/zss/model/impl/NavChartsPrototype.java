package org.zkoss.zss.model.impl;

import org.zkoss.poi.ss.formula.FormulaConfiguration;
import org.zkoss.poi.ss.formula.functions.Countif;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;

import java.util.*;

public class NavChartsPrototype {
    private static Map<String, Integer> chartType;
    private static final String[] type0Stat = new String[]{
            "MIN", "MAX", "MEDIAN", "AVERAGE"
    };
    private static final String[] type2Stat = new String[]{
            "AVERAGE", "STDEV"
    };
    private static NavChartsPrototype uniqueInstance = null;
    private static Model model = null;
    private static NavigationStructure navS = null;

    /**
     * Get the globally unique instance of this class.
     *
     * @return The unique instance.
     */
    public static NavChartsPrototype getPrototype() {
        if (uniqueInstance == null) {
            uniqueInstance = new NavChartsPrototype();
        }
        return uniqueInstance;
    }

    void generateChartObject(Model model, NavigationStructure navS, Map<String, Object> obj, int attr, Bucket<String> subGroup, String formula, List<String> paraList) {
        NavChartsPrototype.model = model;
        NavChartsPrototype.navS = navS;
        int type = getChartType(formula);
        switch (type) {
            case 0:
                type0Chart(obj, attr, subGroup);
                break;
            case 1:
                type1Chart(obj, attr, subGroup, formula);
                break;
            case 2:
                type2Chart(obj, attr, subGroup);
                break;
            case 3:
                type3Chart(obj, attr, subGroup);
                break;
            case 4:
                FormulaConfiguration.getInstance().setCutEvalAtIfCond(true);
                type4Chart(obj);
                FormulaConfiguration.getInstance().setCutEvalAtIfCond(false);
                break;
            default:
                obj.put("chartType", type);
                break;
        }
        NavChartsPrototype.model = null;
        NavChartsPrototype.navS = null;
    }

    private int getChartType(String formulaStr) {
        return chartType.getOrDefault(formulaStr, -1);
    }

    private void type0Chart(Map<String, Object> obj, int attr, Bucket<String> subGroup) {
        obj.put("chartType", 0);
        List<Double> chartData = new ArrayList<>();
        for (String formula : type0Stat) {
            List<String> emptyList = new ArrayList<>();
            emptyList.add("");
            Map<String, Object> res = navS.getBucketAggWithMemoization(model, subGroup, attr, formula, emptyList);
            chartData.add((Double) res.get("value"));
        }
        obj.put("chartData", chartData);
    }

    private void type1Chart(Map<String, Object> obj, int attr, Bucket<String> subGroup, String formula) {
        if (formula.equals("RANK")) {
            NavigationHistogram hist = new NavigationHistogram(navS.collectDoubleValues(attr, subGroup));
            obj.put("chartType", 1); // must be called before formattedOutput, which might overwrite chartType
            hist.formattedOutput(obj, null);
            int rank = ((Double) obj.get("value")).intValue();
            obj.put("valueIndex", hist.queryBinByRank(rank));
        } else {
            type1Chart(obj, attr, subGroup);
        }
    }

    private void type1Chart(Map<String, Object> obj, int attr, Bucket<String> subGroup) {
        NavigationHistogram hist = new NavigationHistogram(navS.collectDoubleValues(attr, subGroup));
        obj.put("chartType", 1); // must be called before formattedOutput, which might overwrite chartType
        hist.formattedOutput(obj, obj.get("value"));
    }

    private void type2Chart(Map<String, Object> obj, int attr, Bucket<String> subGroup) {
        obj.put("chartType", 2);
        NavigationHistogram hist = new NavigationHistogram(navS.collectDoubleValues(attr, subGroup));
        hist.formattedOutput(obj, null);

        HashMap<String, Object> chartData = (HashMap) obj.get("chartData");
        for (String formula : type2Stat) {
            List<String> emptyList = new ArrayList<>();
            emptyList.add("");
            Map<String, Object> res = navS.getBucketAggWithMemoization(model, subGroup, attr, formula, emptyList);
            chartData.put(formula, res.get("value"));
        }
    }

    private void type3Chart(Map<String, Object> obj, int attr, Bucket<String> subGroup) {
        obj.put("chartType", 3);
        NavigationHistogram hist = new NavigationHistogram(navS.collectDoubleValues(attr, subGroup));
        hist.formattedOutput(obj, null);
    }

    private void type4Chart(Map<String, Object> obj) {
        try {
            obj.put("chartType", 4); // Could be changed to -1 by the exception handler
            String formula = (String) obj.get("formula");
            FormulaEngine engine = EngineFactory.getInstance().createFormulaEngine();
            FormulaExpression expr = engine.parse(formula, new FormulaParseContext(navS.currentSheet, null));
            if (expr.hasError()) {
                throw new RuntimeException(expr.getErrorMessage());
            }
            new FormulaResultCellValue(engine.evaluate(expr, new FormulaEvaluationContext(navS.currentSheet, null)));
            throw new RuntimeException("Unexpected control flow, formula cut failed."); // Should not reach here.
        } catch (Countif.CutFormulaEvaluationException expected) {
            /**
             * Thrown by {@link org.zkoss.poi.ss.formula.functions.Countif}
             */
            expected.fillInIfCondition(obj);
        }
    }

    /**
     * The private constructor that can only be called by the {@link #getPrototype()} function.
     */
    private NavChartsPrototype() {
        String[] valueType = new String[]{
                "AVERAGE", "MAX", "MAXA", "MIN", "MINA", "MEDIAN"
        };
        String[] freqType = new String[]{
                "MODE", "LARGE", "SMALL", "RANK"
        };
        String[] spreadType = new String[]{
                "VARIANCE", "STDEV"
        };
        String[] conditionType = new String[]{
                "COUNTIF", "SUMIF"
        };
        String[] otherType = new String[]{
                "COUNT", "COUNTA", "COUNTBLANK", "SUM"
        };

        chartType = new HashMap<>();
        for (String x : valueType) {
            chartType.put(x, 0);
        }
        for (String x : freqType) {
            chartType.put(x, 1);
        }
        for (String x : spreadType) {
            chartType.put(x, 2);
        }
        for (String x : conditionType) {
            chartType.put(x, 4);
        }
        for (String x : otherType) {
            chartType.put(x, 3);
        }
    }
}
