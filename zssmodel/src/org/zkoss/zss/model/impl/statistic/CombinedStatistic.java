package org.zkoss.zss.model.impl.statistic;

import java.util.ArrayList;
/**
 * Created by Stan on 12/3/2017.
 */
public class CombinedStatistic<T extends Comparable<T>> implements AbstractStatistic {
    CountStatistic ct;
    BinarySearch<T> key;
    public CombinedStatistic(T key){
        this.key = new BinarySearch(key);
        this.ct = new CountStatistic();
    }

    public CombinedStatistic(T key, int count){
        this.key = new BinarySearch(key);
        this.ct = new CountStatistic(count);
    }
    public boolean requireUpdate(){
        return true;
    }
    public int compareTo(AbstractStatistic obj, Class type) {
        if (obj instanceof CombinedStatistic){
            if (type == Class.BINARYSEARCH) {
                return this.key.compareTo(((CombinedStatistic)obj).key, type);
            } else {
                return this.ct.compareTo(((CombinedStatistic)obj).ct, type);
            }
        }
        else {
            //TODO
            return 0;
        }
    }
    public int findIndex(ArrayList<AbstractStatistic> stat_list, Class type){
        if (type == Class.BINARYSEARCH){
            return this.key.findIndex(stat_list, type);
        }
        else{

        }
    }
    public AbstractStatistic getAggregation(ArrayList<AbstractStatistic> stat_list, Class type){

    }
    public AbstractStatistic getLowerStatistic(ArrayList<AbstractStatistic> stat_list, int limit, Class type){

    }
    public AbstractStatistic updateStatistic(Mode mode){

    }
}
