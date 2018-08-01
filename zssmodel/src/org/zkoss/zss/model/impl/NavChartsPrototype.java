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

    /**
     * Get the globally unique instance of this class.
     *
     * @return The unique instance.
     */
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
            type1Chart(obj, attr, subGroup, formula);
        else if (type == 2)
            type2Chart(obj, attr, subGroup);
        else if (type == 3)
            type3Chart(obj, attr, subGroup);
        else
            obj.put("chartType", type);
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
        String[] otherType = new String[]{
                "COUNT", "COUNTIF", "COUNTA", "COUNTBLANK", "SUM", "SUMIF"
        }; // TODO: Put SUM, SUMIF, RANK into freqType (with highlight)

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
        for (String x : otherType) {
            chartType.put(x, 3);
        }
    }
}
