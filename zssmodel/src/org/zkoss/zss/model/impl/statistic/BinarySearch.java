package org.zkoss.zss.model.impl.statistic;
import java.util.ArrayList;

/**
 * A key based statistic using the minimum aggregation function
 * @param <T> type of key
 */
public class BinarySearch<T extends Comparable<T>> implements AbstractStatistic {
    T key;

    /**
     * Compare this key with the specified key
     * @param obj the specified key to compare to
     * @returna negative integer, zero, or a positive integer as this key is less than, equal to, or greater than the specified key.
     */
    @Override
    public int compareTo(AbstractStatistic obj) {
        if (obj instanceof BinarySearch)
            return this.key.compareTo(((BinarySearch<T>) obj).key);
        else return this.compareTo(obj);
    }

    /**
     * Find the index, i, where this key should be inserted into the sorted array stat_list
     * Using minimum as the aggregation function
     * @param keys a sorted array of keys
     * @return children index
     */
    @Override
    public int findIndex(ArrayList<AbstractStatistic> keys) {
        int lo = 0, hi = keys.size();
        while (hi > lo) {
            int m = (hi + lo) / 2;
            if (this.compareTo(keys.get(m)) < 0)
                hi = m - 1;     // look in first half
            else if (this.compareTo(keys.get(m)) > 0)
                lo = m;     // look in second half
            else
                return m;   // found the index
        }
        return lo;
    }

    /**
     * Key statistic doesn't require update
     * @return always false
     */
    @Override
    public boolean requireUpdate() {
        return false;
    }

    /**
     * Get the minimum key of the stat_list
     * @param keys an array of keys of a node
     * @return always the first key
     */
    @Override
    public BinarySearch<T> getAggregation(ArrayList<AbstractStatistic> keys) {
        return (BinarySearch<T>) keys.get(0);
    }

    /**
     * Get the next level lookup key
     * @param keys an array of keys of a node
     * @param limit the end index of the array to find
     * @return always this key
     */
    @Override
    public BinarySearch<T> getLowerStatistic(ArrayList<AbstractStatistic> keys, int limit) {
        return this;
    }

    /**
     * Update the current key which is not required for this class
     * @param mode ADD or DELETE operation
     * @return always this key
     */
    @Override
    public BinarySearch<T> updateStatistic(Mode mode) {
        return this;
    }
}
