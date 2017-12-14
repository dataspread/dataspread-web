package org.zkoss.zss.model.impl.statistic;
import java.util.ArrayList;

public class CountStatistic implements AbstractStatistic {
    public int count;

    public CountStatistic() {
        this.count = 0;
    }

    public CountStatistic(int Count) {
        this.count = Count;
    }

    @Override
    public int findIndex(ArrayList<AbstractStatistic> counts, Type type, boolean isLeaf){
        int lo = 0, hi = counts.size();
        int remain = this.count;
        if (remain <= 0) return lo;
        while (hi != lo) {
            remain -= ((CountStatistic) counts.get(lo)).count;
            if (remain <= 0) {
                if (isLeaf)
                    lo++;
                return lo;
            }
            lo++;
        }
        if (isLeaf) lo++;
        return lo;
    }


    @Override
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

    @Override
    public CountStatistic getLeafStatistic(Type type) {
        return new CountStatistic(1);
    }

    @Override
    public CountStatistic updateStatistic(Mode mode){
        int new_count = (mode == Mode.ADD)? (this.count + 1) : (this.count - 1);
        return new CountStatistic(new_count);
    }

    @Override
    public boolean match(ArrayList<AbstractStatistic> counts, int index, Type type) {
        return true;
    }

    @Override
    public String toString() {
        return Integer.toString(count);
    }
}
