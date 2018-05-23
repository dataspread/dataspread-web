package org.zkoss.zss.model.impl.statistic;
import org.zkoss.zss.model.impl.CombinedBTree;

import java.util.ArrayList;


public class CombinedStatistic<T extends Comparable<T>> implements AbstractStatistic {
    public CountStatistic count;
    public KeyStatistic<T> key;

    public CombinedStatistic(){

    }

    public CombinedStatistic(KeyStatistic<T> key){
        this.key = key;
        this.count = new CountStatistic();
    }

    public CombinedStatistic(KeyStatistic<T> key, CountStatistic count){
        this.key = key;
        this.count = count;
    }

    @Override
    public boolean requireUpdate(){
        return true;
    }

    @Override
    public int compareTo(AbstractStatistic obj, Type type) {
        if (obj instanceof CombinedStatistic){
            if (type == Type.KEY) {
                return this.key.compareTo(((CombinedStatistic)obj).key, type);
            } else {
                return this.count.compareTo(((CombinedStatistic)obj).count, type);
            }
        }
        else {
            //TODO
            return 0;
        }
    }

    @Override
    public int findIndex(ArrayList<AbstractStatistic> stat_list, Type type, boolean isLeaf, boolean isAdd){
        if (type == Type.KEY){
            ArrayList<AbstractStatistic> new_list = new ArrayList<>();
            for (int i = 0; i < stat_list.size(); i++){
                new_list.add(((CombinedStatistic<T>) stat_list.get(i)).key);
            }
            return this.key.findIndex(new_list, type, isLeaf, isAdd);
        }
        else {
            ArrayList<AbstractStatistic> new_list = new ArrayList<>();
            for (int i = 0; i < stat_list.size(); i++){
                new_list.add(((CombinedStatistic<T>) stat_list.get(i)).count);
            }
            return this.count.findIndex(new_list, type, isLeaf, isAdd);
        }
    }

    @Override
    public int splitIndex(ArrayList<AbstractStatistic> stat_list, Type type) {
        if (type == Type.KEY) {
            ArrayList<AbstractStatistic> new_list = new ArrayList<>();
            for (int i = 0; i < stat_list.size(); i++){
                new_list.add(((CombinedStatistic<T>) stat_list.get(i)).key);
            }
            return this.key.splitIndex(new_list, type);
        } else {
            ArrayList<AbstractStatistic> new_list = new ArrayList<>();
            for (int i = 0; i < stat_list.size(); i++){
                new_list.add(((CombinedStatistic<T>) stat_list.get(i)).count);
            }
            return this.count.splitIndex(new_list, type);
        }
    }

    @Override
    public CombinedStatistic<T> getAggregation(ArrayList<AbstractStatistic> stat_list, Type type){
        ArrayList<AbstractStatistic> key_list = new ArrayList<>();
        ArrayList<AbstractStatistic> count_list = new ArrayList<>();
        for (int i = 0; i < stat_list.size(); i++){
            key_list.add(((CombinedStatistic<T>) stat_list.get(i)).key);
            count_list.add(((CombinedStatistic<T>) stat_list.get(i)).count);
        }
        return new CombinedStatistic<>(this.key.getAggregation(key_list, type), this.count.getAggregation(count_list, type));
    }

    @Override
    public CombinedStatistic<T> getLowerStatistic(ArrayList<AbstractStatistic> stat_list, int limit, Type type){
        ArrayList<AbstractStatistic> key_list = new ArrayList<>();
        ArrayList<AbstractStatistic> count_list = new ArrayList<>();
        for (int i = 0; i < stat_list.size(); i++){
            key_list.add(((CombinedStatistic<T>) stat_list.get(i)).key);
            count_list.add(((CombinedStatistic<T>) stat_list.get(i)).count);
        }
        return new CombinedStatistic<>(this.key.getLowerStatistic(key_list, limit, type), this.count.getLowerStatistic(count_list, limit, type));
    }

    @Override
    public AbstractStatistic getLeafStatistic(int count, Type type) {
        return new CombinedStatistic<>(this.key.getLeafStatistic(count, type), this.count.getLeafStatistic(count, type));
    }

    @Override
    public CombinedStatistic<T> updateStatistic(Mode mode){
        return new CombinedStatistic<>(this.key.updateStatistic(mode), this.count.updateStatistic(mode));
    }

    @Override
    public boolean match(ArrayList<AbstractStatistic> stat_list, int index, Type type) {
        if (type == Type.KEY){
            ArrayList<AbstractStatistic> new_list = new ArrayList<>();
            for (int i = 0; i < stat_list.size(); i++){
                new_list.add(((CombinedStatistic<T>) stat_list.get(i)).key);
            }
            return this.key.match(new_list, index, type);
        }
        else {
            ArrayList<AbstractStatistic> new_list = new ArrayList<>();
            for (int i = 0; i < stat_list.size(); i++){
                new_list.add(((CombinedStatistic<T>) stat_list.get(i)).count);
            }
            return this.count.match(new_list, index, type);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key.toString());
        sb.append(',');
        sb.append(count.toString());
        return sb.toString();
    }
}
