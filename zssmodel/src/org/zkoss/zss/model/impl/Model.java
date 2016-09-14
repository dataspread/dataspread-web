package org.zkoss.zss.model.impl;

import java.util.Collection;

public abstract class Model {
    protected String tableName;

    public static Model CreateModel(DBContext context, ModelType modelType, String tableName) {
        switch (modelType) {
            case ROM_Model:
                //    return new ROM_Model(context, tableName);
            case COM_Model:
                //    return new COM_Model(context, tableName);
            case RCV_Model:
                return new RCV_Model(context, tableName);
            case HYBRID_Model:
                //    return new Hybrid_Model(context, tableName);
        }
        return null;
    }

    // Drop the tables created.
    public abstract void dropSchema(DBContext context);


    // Schema is created by the constructor, if it does not exists.

    //Insert count empty rows starting at row(inclusive)
    public abstract void insertRows(DBContext context, int row, int count);

    // For all the functions below, use logical row/column number

    //Insert count empty columns starting at col(inclusive)
    public abstract void insertCols(DBContext context, int col, int count);

    //Delete count rows starting from row(inclusive)
    public abstract void deleteRows(DBContext context, int row, int count);

    //Delete count columns starting from col(inclusive)
    public abstract void deleteCols(DBContext context, int col, int count);

    //Update a range of cells -- Cells should exist in the sheet
    public abstract void updateCells(DBContext context, Collection<AbstractCellAdv> cells);

    //Delete cells
    public abstract void deleteCells(DBContext context, Range range);

    public abstract void deleteCells(DBContext context, Collection<AbstractCellAdv> cells);

    //Get a range of cells
    public abstract Collection<AbstractCellAdv> getCells(DBContext context, Range range);

    // Get all Cells
    public Collection<AbstractCellAdv> getCells(DBContext context) {
        return getCells(context, getBounds(context));
    }

    // Get size of sheet
    public abstract Range getBounds(DBContext context);

    // Flush Cache and clearCache DB connection
    public abstract void clearCache(DBContext context);

    public String getTableName() {
        return tableName;
    }

    //
    public enum ModelType {
        ROM_Model, COM_Model, RCV_Model, HYBRID_Model
    }
}