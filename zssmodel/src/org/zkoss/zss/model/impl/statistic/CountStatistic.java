package org.zkoss.zss.model.impl.statistic;

import java.util.ArrayList;
/**
 * Created by Stan on 11/11/2017.
 */
public class CountStatistic implements AbstractStatistic {
    int count;

    public CountStatistic() {
        this.count = 0;
    }

    public CountStatistic(int Count) {
        this.count = Count;
    }

    public int compareTo(AbstractStatistic obj){
        if (obj instanceof CountStatistic){
            if(this.count > ((CountStatistic) obj).count)
                return 1;
            else if(this.count > ((CountStatistic) obj).count)
                return -1;
            else
                return 0;
        }
        else return this.compareTo(obj);
    }

    public int findIndex(ArrayList<AbstractStatistic> stat_list){
        int lo = 0, hi = stat_list.size();
        int remain = this.count;
        while (hi != lo) {
            if (remain > ((CountStatistic) stat_list.get(lo)).count) {
                remain -= ((CountStatistic) stat_list.get(lo)).count;
                lo++;
            } else {
                return lo;
            }
        }
        return lo - 1;
    }

    public boolean requireUpdate(){
        return true;
    }

    public AbstractStatistic getAggregation(ArrayList<AbstractStatistic> stat_list) {
        CountStatistic aggregate = new CountStatistic();
        for(int i = 0; i < stat_list.size(); i++){
            aggregate.count += ((CountStatistic) stat_list.get(i)).count;
        }
        return aggregate;
    }

    public AbstractStatistic getLowerStatistic(ArrayList<AbstractStatistic> stat_list, int limit) {
        int new_count = this.count;
        for(int i = 0; i < limit; i++){
            new_count -= ((CountStatistic) stat_list.get(i)).count;
        }
        return new CountStatistic(new_count);
    }


    public AbstractStatistic updateStatistic(Mode mode){
        int new_count = (mode == Mode.ADD)? (this.count + 1) : (this.count - 1);
        return new CountStatistic(new_count);
    }
}
