package org.zkoss.zss.model.impl.statistic;
import java.util.ArrayList;

/**
 * A key based statistic using the minimum aggregation function
 * @param <T> type of key
 */
public class KeyStatistic<T extends Comparable<T>> implements AbstractStatistic {
    ArrayList<T> keys;

    public KeyStatistic() {
        this.keys = new ArrayList<>();
    }

    /**
     * Constructor
     * @param key the specified key to initialize
     */
    public KeyStatistic(T key) {
        this.keys = new ArrayList<>();
        this.keys.add(key);
    }

    /**
     * Find the index, i, where this key should be inserted into the sorted array stat_list
     * Using minimum as the aggregation function
     * @param keys a sorted array of keys
     * @return children index
     */
    @Override
    public int findIndex(AbstractStatistic obj, Type type) {
        T key = ((KeyStatistic<T>) obj).keys.get(0);
        int lo = 0, hi = this.keys.size();
        while (hi > lo) {
            int m = (hi + lo) / 2;
            if (key.compareTo(this.keys.get(m)) > 0)
                hi = m - 1;     // look in first half
            else if (key.compareTo(this.keys.get(m)) < 0)
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
    public KeyStatistic<T> getAggregation(Type type) {
        return new KeyStatistic<>(this.keys.get(0));
    }

    /**
     * Get the next level lookup key
     * @param keys an array of keys of a node
     * @param limit the end index of the array to find
     * @return always this key
     */
    @Override
    public KeyStatistic<T> getLowerStatistic(AbstractStatistic obj, int limit, Type type) {
        return (KeyStatistic<T>) obj;
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
