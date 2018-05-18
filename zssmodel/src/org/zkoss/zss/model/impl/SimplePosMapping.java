package org.zkoss.zss.model.impl;

import org.model.DBContext;

import java.util.ArrayList;
import java.util.LinkedList;

public class SimplePosMapping implements PosMapping{
    LinkedList<Integer> mapping;
    int id;

    SimplePosMapping()
    {
        mapping = new LinkedList();
        id = 0;
    }

    @Override
    public void dropSchema(DBContext context) {

    }

    @Override
    public ArrayList getIDs(DBContext context, int pos, int count) {
        ArrayList<Integer> arrayListOut = new ArrayList(count);
        while (mapping.size()<pos+count)
            mapping.add(id++);
        for (int i=0;i<count;i++)
            arrayListOut.set(i,  mapping.get(pos+i));
        return arrayListOut;

    }

    @Override
    public ArrayList deleteIDs(DBContext context, int pos, int count) {
        ArrayList<Integer> arrayListOut = new ArrayList(count);
        for (int i=0;i<count;i++)
            arrayListOut.add(mapping.remove(pos));
        return arrayListOut;

    }

    @Override
    public ArrayList createIDs(DBContext context, int pos, int count) {
        return getIDs(context, pos, count);
    }

    @Override
    public void clearCache(DBContext context) {

    }

    @Override
    public PosMapping clone(DBContext context, String tableName) {
        return null;
    }

    @Override
    public int size(DBContext context) {
        return mapping.size();
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public void insertIDs(DBContext context, int pos, ArrayList ids) {

    }
}
