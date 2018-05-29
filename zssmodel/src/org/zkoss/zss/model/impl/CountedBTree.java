package org.zkoss.zss.model.impl;

import org.model.BlockStore;
import org.model.DBContext;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;
import org.zkoss.zss.model.impl.statistic.CountStatistic;

import java.util.ArrayList;

public class CountedBTree implements PosMapping{
    BTree<CountStatistic> btree;

    public CountedBTree(DBContext context, String tableName, BlockStore sourceBlockStore) {
        CountStatistic emptyStatistic = new CountStatistic();
        btree = new BTree<>(context, tableName, sourceBlockStore, emptyStatistic, false);
    }

    public CountedBTree(DBContext context, String tableName) {
        CountStatistic emptyStatistic = new CountStatistic();
        btree = new BTree<>(context, tableName, emptyStatistic, true);
    }

    public CountedBTree(DBContext context, String tableName, boolean useKryo) {
        CountStatistic emptyStatistic = new CountStatistic();
        btree = new BTree<>(context, tableName, emptyStatistic, useKryo);
    }

    @Override
    public void dropSchema(DBContext context) {
        btree.dropSchema(context);
    }

    @Override
    public ArrayList getIDs(DBContext context, int pos, int count) {
        if ((pos + count) > size(context))
            createIDs(context, size(context), pos + count - size(context));
        CountStatistic statistic = new CountStatistic(pos);
        return btree.getIDs(context, statistic, count, AbstractStatistic.Type.COUNT);
    }

    @Override
    public ArrayList deleteIDs(DBContext context, int pos, int count) {
        if ((pos + count) > size(context))
            createIDs(context, size(context), pos + count - size(context));
        ArrayList<CountStatistic> statistics = new ArrayList<>();
        for (int i = 0; i < count; i++)
            statistics.add(new CountStatistic(pos));
        return btree.deleteIDs(context, statistics, AbstractStatistic.Type.COUNT);
    }

    @Override
    public ArrayList createIDs(DBContext context, int pos, int count) {
        Integer max_value = btree.getMaxValue();
        CountStatistic statistic = new CountStatistic(pos);
        ArrayList<Integer> ids = new ArrayList<>();
        btree.createIDs(context, statistic, max_value + 1, count, false, AbstractStatistic.Type.COUNT);
        for (int i = 0; i < count; i++) {
            ids.add(++max_value);
        }
        btree.updateMaxValue(context, max_value);
        return ids;
    }

    @Override
    public void clearCache(DBContext context) {
        btree.clearCache(context);
    }

    @Override
    public PosMapping clone(DBContext context, String tableName) {
        return new CountedBTree(context, tableName, btree.bs);
    }

    @Override
    public int size(DBContext context) {
        return btree.size(context);
    }

    @Override
    public String getTableName() {
        return btree.getTableName();
    }

    @Override
    public void insertIDs(DBContext context, int pos, ArrayList ids) {
        int count = ids.size();
        ArrayList<CountStatistic> statistics = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            statistics.add(new CountStatistic(pos + i));
        }
        btree.insertIDs(context, statistics, ids, AbstractStatistic.Type.COUNT);
    }

    public void useKryo(boolean useKryo) {
        btree.useKryo(useKryo);
    }

    public void setBlockSize(int b) {
        btree.setB(b);
    }
}
