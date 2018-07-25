package org.zkoss.zss.model.impl;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

public class NavigationHistogram {
    public static void main(String[] args) {
        int n = 10000;
        List<Double> numData = new ArrayList<>();
        DoubleStream inStream = (new Random()).doubles(n);
        inStream.forEach(numData::add);
        numData.clear();
        NavigationHistogram test = new NavigationHistogram(numData);
        System.out.println(test.getBins());
        System.out.println(test.getFrequencies());
    }

    private static final int MAX_BARS = 6; // corresponding to 27 unique values
    private List<Double> numData;
    private int numberOfBins;
    private List<Double> bins;
    private TreeMap<Double, Integer> hist;
    private Double minV;
    private Double maxV;
    private Map<Double, Integer> visitedMap;
    private int visitingPointer;

    NavigationHistogram(List<Double> numData) {
        this.numData = numData;
    }

    private void findMinMax() {
        minV = Double.POSITIVE_INFINITY;
        maxV = Double.NEGATIVE_INFINITY;
        for (Double v : numData) {
            if (v > maxV)
                maxV = v;
            if (v < minV)
                minV = v;
        }
    }

    private int estimateUniqueLowerBound() {
        visitingPointer = -1;
        visitedMap = new TreeMap<>();
        int maxUniques = barsToUniques();
        for (int i = 0; i < numData.size(); i++) {
            Double v = numData.get(i);
            visitedMap.put(v, 1 + visitedMap.getOrDefault(v, 0));
            if (visitedMap.size() > maxUniques) {
                visitingPointer = i;
                return maxUniques;
            }
        }
        visitingPointer = numData.size() - 1;
        return visitedMap.size();
    }

    private int uniquesToBars(int uniques) {
        return (int) (2 * Math.cbrt(uniques));
    }

    private int barsToUniques() {
        return (int) (Math.pow(NavigationHistogram.MAX_BARS / 2., 3));
    }

    private void determineNumberOfBins() {
        int lowerBound = estimateUniqueLowerBound();
        numberOfBins = Math.min(uniquesToBars(lowerBound), MAX_BARS);
    }

    private void generateBins() {
        bins = new ArrayList<>();
        double inc = (maxV - minV) / numberOfBins;
        double val = minV;
        for (int i = 0; i <= numberOfBins; i++) {
            bins.add(val);
            val += inc;
        }
    }

    private void initHist() {
        hist = new TreeMap<>();
        // Don't need the last val (maxV), so that # of bins = # of hist + 1
        bins.subList(0, bins.size() - 1).forEach(key -> hist.put(key, 0));
    }

    private void fillInHist() {
        BiConsumer<Double, Integer> addToKey = (key, inc) -> {
            Double eKey = hist.floorKey(key);
            //noinspection ConstantConditions
            hist.compute(eKey, (_ignored, val) -> val + inc);
        };
        Consumer<Double> addOneToKey = key -> addToKey.accept(key, 1);
        int startIndex;
        visitedMap.forEach(addToKey);
        startIndex = visitingPointer + 1;
        numData.subList(startIndex, numData.size()).forEach(addOneToKey);
    }

    private void generateHistogram() {
        findMinMax();
        determineNumberOfBins();
        generateBins();
        initHist();
        fillInHist();
    }

    private List<Integer> getFrequencies() {
        if (hist == null) {
            generateHistogram();
        }
        return new ArrayList<>(hist.values());
    }

    private List<Double> getBins() {
        if (bins == null) {
            generateHistogram();
        }
        return bins;
    }

    void formattedOutput(Map<String, Object> obj, Object queryValue) {
        List<Integer> freq = getFrequencies();
        if (freq.size() == 0) {
            obj.put("chartType", 3);
            return;
        }
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("bins", getBins());
        chartData.put("counts", getFrequencies());
        obj.put("chartData", chartData);
        if (queryValue instanceof Double) {
            Double key = hist.floorKey((Double) queryValue);
            obj.put("valueIndex", bins.indexOf(key));
        }
    }
}
