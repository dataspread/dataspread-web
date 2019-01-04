package org.zkoss.zss.model.impl;

import org.model.DBContext;

import java.util.ArrayList;

public interface PosMappingWithValue {
    void dropSchema(DBContext context);

    // LookUp: Get IDs by index, this should not change since by index does not make sense
    ArrayList getIDs(DBContext context, int pos, int count);

    // Delete: remove count ID by its value and return the deleted ID
    // delete one at a time
    ArrayList deleteIDs(DBContext context, ArrayList<Integer> values);

    // Insert: Add and return count IDs of values given
    void insertIDs(DBContext context, ArrayList<Integer> ids);

    //this is a Hack?
    ArrayList createIDs(DBContext context, int pos, int count);

    // Rollback and Flush Cache
    void clearCache(DBContext context);

    PosMapping clone(DBContext context, String tableName);

    int size(DBContext context);

    String getTableName();
}
