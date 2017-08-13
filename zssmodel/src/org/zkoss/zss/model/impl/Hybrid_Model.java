package org.zkoss.zss.model.impl;

import org.model.BlockStore;
import org.model.DBContext;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;

public class Hybrid_Model extends RCV_Model {
    BlockStore bs;
    private int block_row = 100000;
    //Tables on the sheet

    private Logger logger = Logger.getLogger(Hybrid_Model.class.getName());
    private MetaDataBlock metaDataBlock;

    // This list is synchronized with modelEntryList in metaDataBlock
    private List<Model> tableModels;

    Hybrid_Model(DBContext context, SSheet sheet, String tableName) {
        super(context, sheet, tableName);
        loadMetaData(context);
    }


    private void loadMetaData(DBContext context) {
        tableModels = new ArrayList<>();
        bs = new BlockStore(context, tableName + "_hb_meta");
        metaDataBlock = bs.getObject(context, 0, MetaDataBlock.class);
        if (metaDataBlock == null) {
            metaDataBlock = new MetaDataBlock();
            bs.putObject(0, metaDataBlock);
            bs.flushDirtyBlocks(context);
        } else {
            // Create models
            metaDataBlock.modelEntryList
                    .stream()
                    .sequential()
                    .forEach(e -> tableModels.add(
                            Model.CreateModel(context, sheet, e.modelType, e.tableName)));
        }
    }

    @Override
    public void dropSchema(DBContext context) {
        tableModels.stream().forEach(e -> e.dropSchema(context));
        super.dropSchema(context);
        bs.dropSchemaAndClear(context);
    }

    /* Fix later
    public void converttoRCV(DBContext context, CellRegion range) {
        for (int i = range.getLastRow() / block_row + 1; i > 0; i--) {
            int min_row = range.getRow() + (i - 1) * block_row;
            int max_row = range.getRow() + i * block_row;
            if (i > range.getLastRow() / block_row) max_row = range.getLastRow();
            CellRegion work_range = new CellRegion(min_row, range.getColumn(), max_row, range.getLastColumn());
            Collection<AbstractCellAdv> cells = getCells(context, work_range)
                    .stream()
                    //.map(e -> e.shiftedCell(-range.getRow(), -range.getColumn())) // Translate
                    .collect(Collectors.toList());
            // logger.info("Done - Loading Cells in Range " + work_range.toString());

            // Do a RCV delete
            deleteCells(context, work_range);
            //logger.info("Done - Delete from RCV in Range " + work_range.toString());
            // Insert data into new table
            super.updateCellsCopy(context, cells);
            // model.updateCellsCopy(context, cells);
            //  logger.info("Done - Insert into new model in CellRegion " + work_range.toString());
        }


    } */


    //Construct rom_table on the selected range get from getRomtables
    // Convert to ROM or COM.

    public boolean convert(DBContext context, ModelType modelType, CellRegion range, String name) {
        logger.info("Start - Converting HYBRID " + range.toString());

        //create a new rom_model or tom_model
        // Need to update to a better way to handle delete tables
        String newTableName;
        Model model;

        if (modelType == ModelType.TOM_Model) {
            newTableName = name.toLowerCase();
            model = Model.CreateModel(context, sheet, modelType, newTableName);
            CellRegion tableHeaderRow = new CellRegion(range.getRow(), range.getColumn(), range.getRow(), range.getLastColumn());
            List<AbstractCellAdv> tableHeaderCells = getCells(context, tableHeaderRow).stream().collect(Collectors.toList());

            SortedMap<Integer, AbstractCellAdv> _row = new TreeMap<>();

            for(int i = 0; i<tableHeaderCells.size(); i++)
            {
                _row.put(tableHeaderCells.get(i).getColumnIndex(), tableHeaderCells.get(i));
            }

            ArrayList<String> columnsNames = new ArrayList<>();

            for (int i = 0; i < _row.size(); i++) {
                String colName = _row.get(_row.firstKey()+i).getStringValue();
                if (colName == "")
                    colName = "column_" + (i + 1);

                columnsNames.add(colName);
            }

            TOM_Model tomModel = (TOM_Model) model;
            model.insertRows(context, 0, range.getLastRow() - range.getRow());

            ++range.row;

            deleteCells(context, tableHeaderRow);

        } else {
            newTableName = tableName + "_" + Integer.toHexString(++metaDataBlock.romTableIdentifier);
            model = Model.CreateModel(context, sheet ,modelType, newTableName);
            model.insertRows(context, 0, range.getLastRow() - range.getRow() + 1);
        }

        // Migrate data to the new table
        model.insertCols(context, 0, range.getLastColumn() - range.getColumn() + 1);

        // logger.info("Start - Loading Cells");

        // Work in small range
        for (int i = range.getLastRow() / block_row + 1; i > 0; i--) {
            int min_row = range.getRow() + (i - 1) * block_row;
            int max_row = range.getRow() + i * block_row;
            if (i > range.getLastRow() / block_row) max_row = range.getLastRow();
            CellRegion work_range = new CellRegion(min_row, range.getColumn(), max_row, range.getLastColumn());
            Collection<AbstractCellAdv> cells = getCells(context, work_range)
                    .stream()
//                    .map(e -> e.shiftedCell(-range.getRow(), -range.getColumn())) // Translate
                    .collect(Collectors.toList());

            // Do a RCV delete
            deleteCells(context, work_range);
            // Insert data into new table
//             model.updateCellsCopy(context, cells);
            model.updateCells(context, cells);
        }

        if (modelType == ModelType.TOM_Model)
            --range.row;

        // Collect a list of models to be deleted
        tableModels.add(model);
        MetaDataBlock.ModelEntry modelEntry = new MetaDataBlock.ModelEntry();
        modelEntry.range = range;
        modelEntry.modelType = modelType;
        modelEntry.tableName = newTableName;
        metaDataBlock.modelEntryList.add(modelEntry);
        bs.putObject(0, metaDataBlock);
        bs.flushDirtyBlocks(context);

//        super.vaccumTable(context);

        return true;
    }

    public CellRegion linkTable(DBContext context, String tableName, CellRegion range){
        TOM_Model model = (TOM_Model) Model.CreateModel(context, sheet, ModelType.TOM_Model, tableName);
        range = model.preload(context, range);

        tableModels.add(model);
        MetaDataBlock.ModelEntry modelEntry = new MetaDataBlock.ModelEntry();
        modelEntry.range = range;
        modelEntry.modelType = ModelType.TOM_Model;
        modelEntry.tableName = tableName;
        metaDataBlock.modelEntryList.add(modelEntry);
        bs.putObject(0, metaDataBlock);
        bs.flushDirtyBlocks(context);
        return range;
    }

    @Override
    public void insertRows(DBContext context, int row, int count) {
        //TODO: Max Values ...
        CellRegion rowRange = new CellRegion(row, 1, row, Integer.MAX_VALUE);
        for (int i = 0; i < metaDataBlock.modelEntryList.size(); ++i) {
            CellRegion tableRange = metaDataBlock.modelEntryList.get(i).range;
            Model tableModel = tableModels.get(i);
            if (tableRange.overlaps(rowRange)) {
                if (!(tableModel instanceof TOM_Model)) {
                    tableModel.insertRows(context, row - tableRange.getRow(), count);
                    // Extend range
                    metaDataBlock.modelEntryList.get(i).range = tableRange.extendRange(count, 0);
                }
            } else if (tableRange.getRow() > row) {
                // Shift tables
                metaDataBlock.modelEntryList.get(i).range = tableRange.shiftedRange(count, 0);
            }
        }
        bs.putObject(0, metaDataBlock);
        bs.flushDirtyBlocks(context);

        super.insertRows(context, row, count);
    }

    @Override
    public void insertCols(DBContext context, int col, int count) {
        CellRegion colRange = new CellRegion(1, col, Integer.MAX_VALUE, col);
        for (int i = 0; i < metaDataBlock.modelEntryList.size(); ++i) {
            CellRegion tableRange = metaDataBlock.modelEntryList.get(i).range;
            Model tableModel = tableModels.get(i);
            if (tableRange.overlaps(colRange)) {
                if (!(tableModel instanceof TOM_Model)) {
                    tableModel.insertCols(context, col - tableRange.getColumn(), count);
                    // Extend range
                    metaDataBlock.modelEntryList.get(i).range = tableRange.extendRange(0, count);
                }
            } else if (tableRange.getColumn() > col) {
                // Shift tables
                metaDataBlock.modelEntryList.get(i).range = tableRange.shiftedRange(0, count);
            }
        }
        bs.putObject(0, metaDataBlock);
        bs.flushDirtyBlocks(context);
        super.insertCols(context, col, count);
    }

    @Override
    public void deleteRows(DBContext context, int row, int count) {
        CellRegion rowRange = new CellRegion(row, 0, row + count - 1, Integer.MAX_VALUE);
        int i = 0;
        while (i < metaDataBlock.modelEntryList.size()) {
            CellRegion tableRange = metaDataBlock.modelEntryList.get(i).range;
            Model tableModel = tableModels.get(i);
            if (rowRange.contains(tableRange)) {
                // Delete table
                tableModel.dropSchema(context);
                metaDataBlock.modelEntryList.remove(i);
                tableModels.remove(i);
                //Continue next table at some i.
                continue;
            } else if (tableRange.overlaps(rowRange)) {
                if (!(tableModel instanceof TOM_Model)) {
                    CellRegion intersection = tableRange.getOverlap(rowRange);
                    tableModel.deleteRows(context, intersection.getRow(), intersection.getHeight());
                    metaDataBlock.modelEntryList.get(i).range = tableRange.extendRange(-intersection.getHeight(), 0);
                }
            } else if (tableRange.getRow() > row) {
                // Shift tables
                metaDataBlock.modelEntryList.get(i).range = tableRange.shiftedRange(-count, 0);
            }
            ++i;
        }
        bs.putObject(0, metaDataBlock);
        bs.flushDirtyBlocks(context);

        super.deleteRows(context, row, count);
    }

    @Override
    public void deleteCols(DBContext context, int col, int count) {
        CellRegion colRange = new CellRegion(0, col, Integer.MAX_VALUE, col + count - 1);
        int i = 0;
        while (i < metaDataBlock.modelEntryList.size()) {
            CellRegion tableRange = metaDataBlock.modelEntryList.get(i).range;
            Model tableModel = tableModels.get(i);
            if (colRange.contains(tableRange)) {
                // Delete table
                tableModel.dropSchema(context);
                metaDataBlock.modelEntryList.remove(i);
                tableModels.remove(i);
                //Continue next table at some i.
                continue;
            } else if (tableRange.overlaps(colRange)) {
                if (!(tableModel instanceof TOM_Model)) {
                    CellRegion intersection = tableRange.getOverlap(colRange);
                    tableModel.deleteCols(context, intersection.getColumn(), intersection.getLength());
                    metaDataBlock.modelEntryList.get(i).range = tableRange.extendRange(0, -intersection.getLength());
                }
            } else if (tableRange.getColumn() > col) {
                // Shift tables
                metaDataBlock.modelEntryList.get(i).range = tableRange.shiftedRange(0, -count);
            }
            ++i;


        }
        bs.putObject(0, metaDataBlock);
        bs.flushDirtyBlocks(context);

        super.deleteCols(context, col, count);
    }

    @Override
    public void updateCells(DBContext context, Collection<AbstractCellAdv> cells) {
        Set<AbstractCellAdv> pendingCells = new HashSet<>(cells);
        for (int i = 0; i < metaDataBlock.modelEntryList.size(); ++i) {
            CellRegion tableRange = metaDataBlock.modelEntryList.get(i).range;
            List<AbstractCellAdv> cellsInRange = pendingCells
                    .stream()
                    .filter(c -> tableRange.contains(c.getRowIndex(), c.getColumnIndex()))
                    .collect(Collectors.toList());
            pendingCells.removeAll(cellsInRange);

            // Translate
            if (!cellsInRange.isEmpty()) {
                cellsInRange.stream()
                        .forEach(e -> e.translate(-tableRange.getRow(), -tableRange.getColumn()));
                tableModels.get(i).updateCells(context, cellsInRange);
                cellsInRange.stream()
                        .forEach(e -> e.translate(tableRange.getRow(), tableRange.getColumn()));
            }

        }
        super.updateCells(context, pendingCells);
    }

    @Override
    public void deleteCells(DBContext context, CellRegion range) {
        IntStream.range(0, metaDataBlock.modelEntryList.size())
                .filter(e -> metaDataBlock.modelEntryList.get(e).range.overlaps(range))
                .forEach(e -> deleteCells(context, range.getOverlap(metaDataBlock.modelEntryList.get(e).range)
                        .shiftedRange(
                                -metaDataBlock.modelEntryList.get(e).range.getRow(),
                                -metaDataBlock.modelEntryList.get(e).range.getColumn())));
        super.deleteCells(context, range);
    }

    @Override
    public void deleteCells(DBContext context, Collection<AbstractCellAdv> cells) {
        Set<AbstractCellAdv> pendingCells = new HashSet<>(cells);
        for (int i = 0; i < metaDataBlock.modelEntryList.size(); ++i) {
            CellRegion tableRange = metaDataBlock.modelEntryList.get(i).range;
            List<AbstractCellAdv> cellsInRange = pendingCells
                    .stream()
                    .filter(c -> tableRange.contains(c.getRowIndex(), c.getColumnIndex()))
                    .collect(Collectors.toList());
            tableModels.get(i).deleteCells(context, cellsInRange.stream()
                    .peek(e->e.translate(-tableRange.getRow(), -tableRange.getLastColumn()))
                    .collect(Collectors.toList()));
            pendingCells.removeAll(cellsInRange);
        }
        super.deleteCells(context, pendingCells);
    }

    @Override
    public Collection<AbstractCellAdv> getCells(DBContext context, CellRegion range) {
        List<AbstractCellAdv> cells = new ArrayList<>();

        // System.out.println("size" + metaDataBlock.modelEntryList.size());
        //System.out.println("Queries " + cnt);

        IntStream.range(0, metaDataBlock.modelEntryList.size())
                .filter(e -> metaDataBlock.modelEntryList.get(e).range.overlaps(range))
                .forEach(e -> cells.addAll(
                        tableModels.get(e)
                                .getCells(context, metaDataBlock.modelEntryList.get(e).range.getOverlap(range)
                                        .shiftedRange(
                                                -metaDataBlock.modelEntryList.get(e).range.getRow(),
                                                -metaDataBlock.modelEntryList.get(e).range.getColumn()))
                                .stream()
                                .peek(c->c.translate(metaDataBlock.modelEntryList.get(e).range.getRow(),
                                        metaDataBlock.modelEntryList.get(e).range.getColumn()))
                                .collect(Collectors.toList())));

        boolean encompass = false;
        for (MetaDataBlock.ModelEntry m:metaDataBlock.modelEntryList)
            if (m.range.contains(range))
                encompass=true;

        if (encompass==false) {
            //  System.out.println("RCV executed ");
            cells.addAll(super.getCells(context, range));
        }
        // else
        //   System.out.println("RCV not executed ");


        return Collections.unmodifiableCollection(cells);
    }

    public Collection<CellRegion> getTablesRanges() {
        return metaDataBlock.modelEntryList.stream().map(e -> e.range).collect(Collectors.toList());
    }

    @Override
    public void clearCache(DBContext context) {
        super.clearCache(context);
        loadMetaData(context);
    }

    public void deleteModel(DBContext dbContext, CellRegion modelRange) {
        metaDataBlock.modelEntryList.removeIf(e -> e.range.equals(modelRange));
    }

    private static class MetaDataBlock {
        List<ModelEntry> modelEntryList;
        int romTableIdentifier;
        MetaDataBlock() {
            modelEntryList = new ArrayList<>();
            romTableIdentifier = 1;
        }
        private static class ModelEntry {
            CellRegion range;
            String tableName;
            ModelType modelType;
        }
    }
}
