package org.zkoss.zss.model.impl;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

public class NavigationHistogram {
    public static void main(String[] args) {
        int n = 10000;
        ArrayList<Double> numData = new ArrayList<>();
        DoubleStream inStream = (new Random()).doubles(n);
        inStream.forEach(numData::add);
//        numData.clear();
        NavigationHistogram test = new NavigationHistogram(numData);
        System.out.println(test.getBins());
        System.out.println(test.getFrequencies());
    }

    private static final int MAX_BARS = 6; // corresponding to 27 unique values
    private ArrayList<Double> numData;
    private static final double OFFSET = 0.05;
    private int numberOfBins;
    private List<Double> bins;
    private TreeMap<Double, Integer> hist;
    private Double minV;
    private Double maxV;
    private Map<Double, Integer> visitedMap;
    private int visitingPointer;

    NavigationHistogram(ArrayList<Double> numData) {
        this.numData = numData;
    }

    private void findMinMax() {
        minV = Double.POSITIVE_INFINITY;
        maxV = Double.NEGATIVE_INFINITY;
        int offSet = (int) (OFFSET * numData.size());
        if (offSet > 0) {
            QuickSelect selector = new QuickSelect();
            minV = selector.quickSelect(numData, offSet);
            maxV = selector.quickSelect(numData, numData.size() - offSet);
        }
    }

    private static class QuickSelect {
        Double quickSelect(ArrayList<Double> G, int k) {
            int first = 0, last = G.size() - 1;
            while (true) {
                if (first > last)
                    return Double.NEGATIVE_INFINITY;
                int pivot = partition(G, first, last);
                if (pivot == k) {
                    return G.get(k);
                }
                if (pivot > k) {
                    last = pivot - 1;
                } else {
                    first = pivot + 1;
                }
            }
        }

        private int partition(ArrayList<Double> G, int first, int last) {
            int pivot = first + new Random().nextInt(last - first + 1);
            swap(G, last, pivot);
            for (int i = first; i < last; i++) {
                if (G.get(i) < G.get(last)) {
                    swap(G, i, first);
                    first++;
                }
            }
            swap(G, first, last);
            return first;
        }

        private void swap(ArrayList<Double> G, int x, int y) {
            Double tmp = G.get(x);
            G.set(x, G.get(y));
            G.set(y, tmp);
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
            if (key > maxV || key < minV)
                return;
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

    int queryBinByRank(int rank) {
        int sum = (int) (numData.size() * OFFSET);
        if (sum >= rank)
            return -1;
        for (int i = 0; i < bins.size() - 1; i++) {
            sum += hist.get(bins.get(i));
            if (sum >= rank)
                return i;
        }
        return bins.size() - 1;
    }

    void formattedOutput(Map<String, Object> obj, Object queryValue) {
        List<Integer> freq = getFrequencies();
        if (freq.size() == 0) {
            obj.put("chartType", -1);
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
