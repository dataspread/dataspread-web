package org.zkoss.zss.model.impl.statistic;

import java.util.ArrayList;

public interface AbstractStatistic {
    /**
     * Indicate the operation
     */
    enum Mode {
        ADD,
        DELETE
    }

    enum Type {
        KEY,
        COUNT,
        SHIFT
    }

    /**
     * Check if the statistic require update
     * @return True if require update
     */
    boolean requireUpdate();

    /**
     * Compare this statistic with the specified statistic
     * @param obj the statistic to compare to
     * @return a negative integer, zero, or a positive integer as this statistic is less than, equal to, or greater than the specified statistic.
     */
    int compareTo(AbstractStatistic obj, Type type);

    /**
     * Lookup the index of the current node based on the statistic
     * @param stat_list an array of statistics
     * @return a integer between 0 and the length of stat_list
     */
    int findIndex(ArrayList<AbstractStatistic> stat_list, Type type, boolean isLeaf, boolean isAdd);

    /**
     * Get the aggregation value of the stat_list
     * @param stat_list an array of statistics
     * @return the aggregated statistic
     */
    AbstractStatistic getAggregation(ArrayList<AbstractStatistic> stat_list, Type type);

    /**
     * Get the next level lookup statistic
     * @param stat_list the stat_list to lookup in
     * @param limit the end of the index to aggregate
     * @return new statistic for lookup
     */
    AbstractStatistic getLowerStatistic(ArrayList<AbstractStatistic> stat_list, int limit, Type type);

    /**
     *
     * @param type
     * @return
     */
    AbstractStatistic getLeafStatistic(int count, Type type);

    /**
     * Update the current statistic affected by the operation
     * @param mode indicate ADD or DELETE operation
     * @return the updated statistic
     */
    AbstractStatistic updateStatistic(Mode mode);

    /**
     * Check if the statistic is the exact match
     * @param stat_list
     * @param index the statistic to match with
     * @param type
     * @return true if this statistic matches the designated statistic
     */
    boolean match(ArrayList<AbstractStatistic> stat_list, int index, Type type);

    /**
     *
     * @param statistic
     * @param type
     * @return
     */
    int splitIndex(ArrayList<AbstractStatistic> statistic, Type type);

    /**
     *
     * @return
     */
    String toString();
}
