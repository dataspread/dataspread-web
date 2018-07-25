package org.zkoss.zss.model.impl;

import java.util.*;

class NavChartsPrototype {
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

    static NavChartsPrototype getPrototype() {
        if (uniqueInstance == null) {
            uniqueInstance = new NavChartsPrototype();
        }
        return uniqueInstance;
    }

    void generateChartObject(Model model, NavigationStructure navS, Map<String, Object> obj, int attr, Bucket<String> subGroup, String formula) {
        NavChartsPrototype.model = model;
        NavChartsPrototype.navS = navS;
        int type = getChartType(formula);
        if (type == 0)
            type0Chart(obj, attr, subGroup);
        else if (type == 1)
            type1Chart(obj, attr, subGroup);
        else if (type == 2)
            type2Chart(obj, attr, subGroup);
        else
            obj.put("chartType", 3);
        NavChartsPrototype.model = null;
        NavChartsPrototype.navS = null;
    }

    private int getChartType(String formulaStr) {
        return chartType.getOrDefault(formulaStr, 3);
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

    private void type1Chart(Map<String, Object> obj, int attr, Bucket<String> subGroup) {
        NavigationHistogram hist = new NavigationHistogram(navS.collectDoubleValues(attr, subGroup));
        hist.formattedOutput(obj, obj.get("value"));
        obj.put("chartType", 1);
    }

    private void type2Chart(Map<String, Object> obj, int attr, Bucket<String> subGroup) {
        obj.put("chartType", 2);
        List<Double> chartData = new ArrayList<>();
        for (String formula : type2Stat) {
            List<String> emptyList = new ArrayList<>();
            emptyList.add("");
            Map<String, Object> res = navS.getBucketAggWithMemoization(model, subGroup, attr, formula, emptyList);
            chartData.add((Double) res.get("value"));
        }
        obj.put("chartData", chartData);
    }

    private NavChartsPrototype() {
        String[] valueType = new String[]{
                "AVERAGE", "MAX", "MAXA", "MIN", "MINA", "MEDIAN"
        };
        String[] freqType = new String[]{
                "COUNT", "COUNTIF", "COUNTA", "COUNTBLANK", "MODE", "RANK", "LARGE", "SMALL"
        };
        String[] spreadType = new String[]{
                "SUM", "SUMIF"
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
    }
}
