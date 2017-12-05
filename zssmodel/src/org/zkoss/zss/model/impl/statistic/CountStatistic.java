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


    public int findIndex(AbstractStatistic obj, Type type){
        int lo = 0, hi = this.count.size();
        int remain = ((CountStatistic) obj).count;
        while (hi != lo) {
            if (remain >  this.count.get(lo)) {
                remain -= this.count.get(lo);
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

    public AbstractStatistic getAggregation(Type type) {
        int aggregate = 0;
        for(int i = 0; i < this.count.size(); i++){
            aggregate += this.count.get(i);
        }
        return new CountStatistic(aggregate);
    }

    public AbstractStatistic getLowerStatistic(AbstractStatistic obj, int limit, Type type) {
        int new_count = ((CountStatistic) obj).count.get(0);
        for(int i = 0; i < limit; i++){
            new_count -= this.count.get(i);
        }
        return new CountStatistic(new_count);
    }


    public AbstractStatistic updateStatistic(Mode mode){
        int new_count = (mode == Mode.ADD)? (this.count + 1) : (this.count - 1);
        return new CountStatistic(new_count);
    }
}
