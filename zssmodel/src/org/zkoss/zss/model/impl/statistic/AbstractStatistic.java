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

    /**
     * Check if the statistic require update
     * @return True if require update
     */
    boolean requireUpdate();

    /**
     * Compare this statistic with the specified statistic
     * @param abstractStatistic the statistic to compare to
     * @return a negative integer, zero, or a positive integer as this statistic is less than, equal to, or greater than the specified statistic.
     */
    int compareTo(AbstractStatistic abstractStatistic);

    /**
     * Lookup the index of the current node based on the statistic
     * @param stat_list an array of statistics of the current node
     * @return a integer between 0 and the length of stat_list
     */
    int findIndex(ArrayList<AbstractStatistic> stat_list);

    /**
     * Get the aggregation value of the stat_list
     * @param stat_list an array of statistics of a node
     * @return the aggregated statistic
     */
    AbstractStatistic getAggregation(ArrayList<AbstractStatistic> stat_list);

    /**
     * Get the next level lookup statistic
     * @param stat_list an array of statistics of a node
     * @param limit the end of the index to aggregate
     * @return new statistic for lookup
     */
    AbstractStatistic getLowerStatistic(ArrayList<AbstractStatistic> stat_list, int limit);

    /**
     * Update the current statistic affected by the operation
     * @param mode indicate ADD or DELETE operation
     * @return the updated statistic
     */
    AbstractStatistic updateStatistic(Mode mode);
}
