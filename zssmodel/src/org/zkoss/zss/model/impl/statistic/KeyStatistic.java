package org.zkoss.zss.model.impl.statistic;
import java.util.ArrayList;

/**
 * A key based statistic using the minimum aggregation function
 * @param <T> type of key
 */
public class KeyStatistic<T extends Comparable<T>> implements AbstractStatistic {
    T key;

    /**
     * Constructor
     * @param key the specified key to initialize
     */
    public KeyStatistic(T key) {
        this.key = key;
    }

    /**
     * Compare this key with the specified key
     * @param obj the specified key to compare to
     * @return a negative integer, zero, or a positive integer as this key is less than, equal to, or greater than the specified key.
     */
    @Override
    public int compareTo(AbstractStatistic obj) {
        return this.key.compareTo(((KeyStatistic<T>) obj).key);
    }

    /**
     * Find the index, i, where the key should be inserted into the sorted array keys
     * Using minimum as the aggregation function
     * @param obj the key to lookup
     * @return children index
     */
    @Override
    public int findIndex(ArrayList<AbstractStatistic> keys, Type type) {
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
     * @return always the first key
     */
    @Override
    public KeyStatistic<T> getAggregation(ArrayList<AbstractStatistic> keys, Type type) {
        return (KeyStatistic<T>) keys.get(0);
    }

    /**
     * Get the next level lookup key
     * @param obj the current lookup key
     * @param limit the end index of the array to find
     * @return always this key
     */
    @Override
    public KeyStatistic<T> getLowerStatistic(ArrayList<AbstractStatistic> keys, int limit, Type type) {
        return this;
    }

    /**
     * Update the current key which is not required for this class
     * @param mode ADD or DELETE operation
     * @return always this key
     */
    @Override
    public KeyStatistic<T> updateStatistic(Mode mode) {
        return this;
    }
}
