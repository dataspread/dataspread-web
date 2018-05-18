package org.zkoss.zss.model.impl.statistic;
import java.util.ArrayList;

/**
 * A key based statistic using the minimum aggregation function
 * @param <T> type of key
 */
public class KeyStatistic<T extends Comparable<T>> implements AbstractStatistic {
    public T key;

    public KeyStatistic(){

    }

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
    public int compareTo(AbstractStatistic obj, Type type) {
        return this.key.compareTo(((KeyStatistic<T>) obj).key);
    }

    /**
     * Find the index, i, where the key should be inserted into the sorted array keys
     * Using minimum as the aggregation function
     * @param obj the key to lookup
     * @return children index
     */
    @Override
    public int findIndex(ArrayList<AbstractStatistic> keys, Type type, boolean isLeaf, boolean isAdd) {
        int lo = 0, hi = keys.size() - 1;
        int index = lo;
        if (hi < 0) return index;
        while (hi >= lo) {
            int m = (hi + lo) / 2;
            if (this.compareTo(keys.get(m), type) < 0)
                hi = m - 1;     // look in first half
            else if (this.compareTo(keys.get(m), type) > 0) {
                index = m;
                lo = m + 1;     // look in second half
            } else {
                index = m;   // found the index
                break;
            }
        }
        if (isLeaf) {
            if (this.compareTo(keys.get(index), type) <= 0)
                return index;
            else
                return index + 1;
        }
        return index;
    }

    @Override
    public int splitIndex(ArrayList<AbstractStatistic> statistic, Type type) {
        return 0;
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


    @Override
    public KeyStatistic<T> getLeafStatistic(int count, Type type) {
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

    /**
     * Check if the two keys are the same
     * @param keys
     * @param index the index of the key to match with
     * @param type
     * @return
     */
    @Override
    public boolean match(ArrayList<AbstractStatistic> keys, int index, Type type) {
        return this.compareTo(keys.get(index), type) == 0;
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
