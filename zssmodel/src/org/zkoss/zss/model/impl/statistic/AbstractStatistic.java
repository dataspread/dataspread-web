package org.zkoss.zss.model.impl.statistic;

import java.util.ArrayList;

public interface AbstractStatistic {
    enum Mode {
        ADD,
        DELETE
    }
    boolean requireUpdate();
    int compareTo(AbstractStatistic abstractStatistic);
    int findIndex(ArrayList<AbstractStatistic> stat_list);
    AbstractStatistic getStatistic(ArrayList<AbstractStatistic> stat_list, Mode mode);
    AbstractStatistic getStatistic(ArrayList<AbstractStatistic> stat_list, int offset, Mode mode);
    AbstractStatistic updateStatistic(Mode mode);
}
