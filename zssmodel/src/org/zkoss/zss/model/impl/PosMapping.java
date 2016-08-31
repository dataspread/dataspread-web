package org.zkoss.zss.model.impl;

/* Store mapping between logical pos and ID */
public interface PosMapping {

    void dropSchema(DBContext context);

    // Get count IDs starting from position pos(inclusive)
    Integer[] getIDs(DBContext context, int pos, int count);

    // Delete count IDs starting from position pos(inclusive), and return deleted IDs
    Integer[] deleteIDs(DBContext context, int pos, int count);

    // Add and return count IDs starting from position pos(inclusive)
    Integer[] createIDs(DBContext context, int pos, int count);

    // Rollback and Flush Cache
    void clearCache(DBContext context);

    int size(DBContext context);

    String getTableName();

}

