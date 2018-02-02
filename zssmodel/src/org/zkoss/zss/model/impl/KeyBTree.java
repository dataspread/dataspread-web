package org.zkoss.zss.model.impl;

import org.model.BlockStore;
import org.model.DBContext;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;
import org.zkoss.zss.model.impl.statistic.KeyStatistic;

import java.util.ArrayList;

public class KeyBTree{
    BTree<KeyStatistic> btree;

    public KeyBTree(DBContext context, String tableName, BlockStore sourceBlockStore) {
        KeyStatistic emptyStatistic = new KeyStatistic(0);
        btree = new BTree<>(context, tableName, sourceBlockStore, emptyStatistic, false);
    }

    public KeyBTree(DBContext context, String tableName) {
        KeyStatistic emptyStatistic = new KeyStatistic(0);
        btree = new BTree<>(context, tableName, emptyStatistic, false);
        btree.updateMaxValue(context, 0);
    }

    public KeyBTree(DBContext context, String tableName, boolean useKryo) {
        KeyStatistic emptyStatistic = new KeyStatistic(0);
        btree = new BTree<>(context, tableName, emptyStatistic, useKryo);
        btree.updateMaxValue(context, 0);
    }


    public void dropSchema(DBContext context) {
        btree.dropSchema(context);
    }


    public ArrayList getIDs(DBContext context, KeyStatistic statistic, int count) {
        //if ((pos + count) > size(context))
        //    createIDs(context, size(context), pos + count - size(context));
        return btree.getIDs(context, statistic, count, AbstractStatistic.Type.KEY);
    }


    public ArrayList deleteIDs(DBContext context, ArrayList<KeyStatistic> statistics) {
        return btree.deleteIDs(context, statistics, AbstractStatistic.Type.KEY);
    }


    public ArrayList createIDs(DBContext context, int pos, int count) {
        Integer max_value = btree.getMaxValue();
        KeyStatistic statistic = new KeyStatistic(pos);
        ArrayList<Integer> ids = new ArrayList<>();
        btree.createIDs(context, statistic, max_value + 1, count, false, AbstractStatistic.Type.KEY);
        for (int i = 0; i < count; i++) {
            ids.add(++max_value);
        }
        btree.updateMaxValue(context, max_value);
        return ids;
    }


    public void clearCache(DBContext context) {
        btree.clearCache(context);
    }


    public PosMapping clone(DBContext context, String tableName) {
        return new CountedBTree(context, tableName, btree.bs);
    }


    public int size(DBContext context) {
        return btree.size(context);
    }


    public String getTableName() {
        return btree.getTableName();
    }


    public void insertIDs(DBContext context, ArrayList<KeyStatistic> statistics, ArrayList ids) {
        btree.insertIDs(context, statistics, ids, AbstractStatistic.Type.KEY);
    }

    public void useKryo(boolean useKryo) {
        btree.useKryo(useKryo);
    }

    public void setBlockSize(int b) {
        btree.setB(b);
    }
}

