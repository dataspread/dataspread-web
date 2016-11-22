package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.CellRegion;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.stream.Collectors;

/* COM is a transpose of ROM */
public class COM_Model extends Model {
    ROM_Model rom_model;

    //Create a ROM_model and a new empty btree
    COM_Model(DBContext context, String tableName) {
        this.tableName = tableName;
        rom_model = new ROM_Model(context, tableName);
    }

    @Override
    public void dropSchema(DBContext context) {
        rom_model.dropSchema(context);
    }

    @Override
    public void insertRows(DBContext context, int row, int count) {
        rom_model.insertCols(context, row, count);
    }

    @Override
    public void insertCols(DBContext context, int col, int count) {
        rom_model.insertRows(context, col, count);
    }

    @Override
    public void deleteRows(DBContext context, int row, int count) {
        rom_model.deleteCols(context, row, count);
    }

    @Override
    public void deleteCols(DBContext context, int col, int count) {
        rom_model.deleteRows(context, col, count);
    }

    @Override
    public void updateCells(DBContext context, Collection<AbstractCellAdv> cells) {
        rom_model.updateCells(context,
                cells.stream()
                        .map(c ->  CellImpl.fromBytes(c.getColumnIndex(), c.getRowIndex(), c.toBytes()))
                        .collect(Collectors.toList()));
    }

    @Override
    public void deleteCells(DBContext context, CellRegion range) {
        rom_model.deleteCells(context, transpose(range));
    }

    @Override
    public void deleteCells(DBContext context, Collection<AbstractCellAdv> cells) {
        rom_model.deleteCells(context,
                cells.stream()
                        .map(c -> CellImpl.fromBytes(c.getColumnIndex(), c.getRowIndex(), c.toBytes()))
                        .collect(Collectors.toList()));
    }

    @Override
    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion fetchRange) {
        return rom_model.getCells(context, transpose(fetchRange))
                .stream()
                .map(c -> CellImpl.fromBytes(c.getColumnIndex(), c.getRowIndex(), c.toBytes()))
                .collect(Collectors.toList());
    }

    @Override
    public CellRegion getBounds(DBContext context) {
        return transpose(rom_model.getBounds(context));
    }

    @Override
    public void clearCache(DBContext context) {
        rom_model.clearCache(context);
    }

    @Override
    public void importSheet(Reader reader, char delimiter) throws IOException {
        throw new RuntimeException("Not Supported");
    }

    private CellRegion transpose(CellRegion range) {
        return new CellRegion(range.getColumn(),
                range.getRow(),
                range.getLastColumn(),
                range.getLastRow());
    }
}