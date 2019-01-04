package org.zkoss.zss.model.impl;

import org.model.BlockStore;
import org.model.DBContext;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;
import org.zkoss.zss.model.impl.statistic.CountStatistic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

//copy of CountedBTree with the addition of a reverser BTree.
public class CountedBTreeWithReverse implements PosMappingWithValue{
    BTree<CountStatistic> btree;
    //Reverse Tree that maps a value(Tuple pointer) to the leaf node where this resides
    //Actually we can use any tree structure that have logarithmic run time
    //We will just use java's TreeMap for a map of key to Node.
    TreeMap<Integer, BTree.Node> reverseTree;

    //need to save at database as well. whenever the reverseTree is changed
    public CountedBTreeWithReverse(DBContext context, String tableName, BlockStore sourceBlockStore) {
        CountStatistic emptyStatistic = new CountStatistic();
        btree = new BTree<>(context, tableName, sourceBlockStore, emptyStatistic, false);
        reverseTree = new TreeMap<>();
    }

    public CountedBTreeWithReverse(DBContext context, String tableName) {
        CountStatistic emptyStatistic = new CountStatistic();
        btree = new BTree<>(context, tableName, emptyStatistic, true);
        reverseTree = new TreeMap<>();
    }

    public CountedBTreeWithReverse(DBContext context, String tableName, boolean useKryo) {
        CountStatistic emptyStatistic = new CountStatistic();
        btree = new BTree<>(context, tableName, emptyStatistic, useKryo);
        reverseTree = new TreeMap<>();
    }

    @Override
    public void dropSchema(DBContext context) {
        btree.dropSchema(context);
    }

    @Override
    //For lookup, we keep it the same
    public ArrayList<Integer> getIDs(DBContext context, int pos, int count) {
        //if ((pos + count) > size(context))
        //    createIDs(context, size(context), pos + count - size(context));
        CountStatistic statistic = new CountStatistic(pos);
        return btree.getIDs(context, statistic, count, AbstractStatistic.Type.COUNT);
    }

    @Override
    public ArrayList<Integer> deleteIDs(DBContext context, ArrayList<Integer> values) {
        //if ((pos + count) > size(context))
        //    createIDs(context, size(context), pos + count - size(context));
        ArrayList<Integer> deletedIDs = new ArrayList<>();
        for(int i = 0;i < values.size();i++){
            deletedIDs.add(deleteID(context, values.get(i)));
        }
        return deletedIDs;
    }

    public int deleteID(DBContext context, int value){
        //lookup in the reverseTree
        BTree.Node node = reverseTree.get(value);
        if(node.leafNode == false){
            //this should not happen
            throw new RuntimeException("Not a leafnode where it should be");
        }
        int pos = 0;
        for(int i = 0;i < node.values.size();i++){
            if((int)node.values.get(i) == value){
                //TODO: Verify this pos is correct for the deleteIDs method below
                pos = i;
                //remove from reverse tree
                reverseTree.remove(value);
            }
        }
        //remove from Btree
        ArrayList<CountStatistic> statistics = new ArrayList<>();
        //for (int i = 0; i < count; i++)
        statistics.add(new CountStatistic(pos));
        //only one value is returned.
        return btree.deleteIDs(context, statistics, AbstractStatistic.Type.COUNT).get(0);
    }

    @Override
    public void insertIDs(DBContext context, ArrayList<Integer> ids) {
        int count = ids.size();
        for (int i = 0; i < count; i++) {
            insertID(context, ids.get(i));
        }
    }

    public void insertID(DBContext context, int id){
        ArrayList<CountStatistic> statistics = new ArrayList<>();
        //TODO: Verify this is correct: end pos is size of context
        statistics.add(new CountStatistic(size(context)+1));
        BTree.Node inserted = btree.insertIDs(context, statistics,
                new ArrayList<>(Arrays.asList(id)), AbstractStatistic.Type.COUNT);

        reverseTree.put(id, inserted);
    }

    @Override
    public ArrayList<Integer> createIDs(DBContext context, int pos, int count) {
        Integer max_value = btree.getMaxValue();
        CountStatistic statistic = new CountStatistic(pos);
        ArrayList<Integer> ids = new ArrayList<>();

        //update the createID to return the leafNode
        BTree.Node leafNode = btree.createIDs(
                context, statistic, max_value + 1, count, false, AbstractStatistic.Type.COUNT);

        for (int i = 0; i < count; i++) {
            //insert at reverseTree as well
            reverseTree.put(max_value+1, leafNode);
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


    public void useKryo(boolean useKryo) {
        btree.useKryo(useKryo);
    }

    public void setBlockSize(int b) {
        btree.setB(b);
    }
}
