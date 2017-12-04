package org.zkoss.zss.model.impl.statistic;

import java.util.ArrayList;
/**
 * Created by Stan on 12/3/2017.
 */
public class CombinedStatistic<T extends Comparable<T>> implements AbstractStatistic {
    CountStatistic ct;
    BinarySearch<T> key;

    public boolean requireUpdate(){
        return true;
    }
    public int compareTo(AbstractStatistic abstractStatistic){
        return 0;
    }
    public int findIndex(ArrayList<AbstractStatistic> stat_list){
        return 0;
    }
    public AbstractStatistic getAggregation(ArrayList<AbstractStatistic> stat_list){

    }
    public AbstractStatistic getLowerStatistic(ArrayList<AbstractStatistic> stat_list, int limit){

    }
    public AbstractStatistic updateStatistic(Mode mode){

    }
}
