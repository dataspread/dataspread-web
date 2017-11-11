package org.zkoss.zss.model.impl.statistic;

import java.util.ArrayList;

public class BinarySearch<T extends Comparable<T>> implements AbstractStatistic {
    T key;

    @Override
    public int compareTo(AbstractStatistic obj) {
        if (obj instanceof BinarySearch)
            return this.key.compareTo(((BinarySearch<T>) obj).key);
        else return this.compareTo(obj);
    }

    /**
     * Find the index, i, at which x should be inserted into the null-padded
     * sorted array, a
     *
     * @param stat_list the sorted array (padded with null entries)
     * @param obj the key to search for
     * @return children index
     */
    @Override
    public int findIndex(ArrayList<AbstractStatistic> stat_list) {
        int lo = 0, hi = stat_list.size();
        // TODO: Need explanation of the shift
        while (hi != lo) {
            int m = (hi + lo) / 2;
            if (this.compareTo(stat_list.get(m)) < 0)
                hi = m;      // look in first half
            else if (this.compareTo(stat_list.get(m)) > 0)
                lo = m + 1;    // look in second half
            else
                return m + 1; // found it
        }
        return lo;
    }

    @Override
    public boolean requireUpdate() {
        return false;
    }

    @Override
    public AbstractStatistic getStatistic(ArrayList<AbstractStatistic> stat_list, Mode mode) {
        AbstractStatistic statistic = stat_list.remove(0);
        return statistic;
    }

    @Override
    public AbstractStatistic updateStatistic(Mode mode) {
        return this;
    }
}
