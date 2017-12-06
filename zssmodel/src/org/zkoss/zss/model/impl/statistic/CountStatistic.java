package org.zkoss.zss.model.impl.statistic;
import java.util.ArrayList;

public class CountStatistic implements AbstractStatistic {
    int count;

    public CountStatistic() {
        this.count = 0;
    }

    public CountStatistic(int Count) {
        this.count = Count;
    }


    public int findIndex(ArrayList<AbstractStatistic> counts, Type type){
        int lo = 0, hi = counts.size();
        int remain = this.count;
        while (hi != lo) {
            remain -= ((CountStatistic) counts.get(lo)).count;
            if (remain <= 0) return lo;
        }
        return lo;
    }

    public boolean requireUpdate(){
        return true;
    }

    @Override
    public int compareTo(AbstractStatistic obj, Type type) {
        return this.count - ((CountStatistic) obj).count;
    }

    @Override
    public CountStatistic getAggregation(ArrayList<AbstractStatistic> counts, Type type) {
        int aggregate = 0;
        for(int i = 0; i < counts.size(); i++){
            aggregate += ((CountStatistic) counts.get(i)).count;
        }
        return new CountStatistic(aggregate);
    }

    @Override
    public CountStatistic getLowerStatistic(ArrayList<AbstractStatistic> counts, int limit, Type type) {
        int new_count = this.count;
        for(int i = 0; i < limit; i++){
            new_count -= ((CountStatistic) counts.get(i)).count;
        }
        return new CountStatistic(new_count);
    }

    public CountStatistic updateStatistic(Mode mode){
        int new_count = (mode == Mode.ADD)? (this.count + 1) : (this.count - 1);
        return new CountStatistic(new_count);
    }

    @Override
    public boolean match(ArrayList<AbstractStatistic> counts, int index, Type type) {
        int total = 0;
        for(int i = 0; i < index; i++){
            total += ((CountStatistic) counts.get(i)).count;
        }
        return this.count == total;
    }
}
