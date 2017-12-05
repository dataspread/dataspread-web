package org.zkoss.zss.model.impl.statistic;

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
     * Lookup the index of the current node based on the statistic
     * @param stat_list an array of statistics of the current node
     * @return a integer between 0 and the length of stat_list
     */
    int findIndex(AbstractStatistic stat_list, Type type);

    /**
     * Get the aggregation value of the stat_list
     * @return the aggregated statistic
     */
    AbstractStatistic getAggregation(Type type);

    /**
     * Get the next level lookup statistic
     * @param stat_list an array of statistics of a node
     * @param limit the end of the index to aggregate
     * @return new statistic for lookup
     */
    AbstractStatistic getLowerStatistic(AbstractStatistic stat_list, int limit, Type type);

    /**
     * Update the current statistic affected by the operation
     * @param mode indicate ADD or DELETE operation
     * @return the updated statistic
     */
    AbstractStatistic updateStatistic(Mode mode);
}
