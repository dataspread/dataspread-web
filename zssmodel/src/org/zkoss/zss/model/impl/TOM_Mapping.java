package org.zkoss.zss.model.impl;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.ModelEvents;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.sql.*;
import java.util.*;

/* Keep a mapping of UI and TOM models */
public class TOM_Mapping {
    public static TOM_Mapping instance = new TOM_Mapping();

    // Table Name, TOM model
    private Map<String, TOM_Model> tomModels;
    /* Table Name, Order Name, Order object */
    private Map<String, Map<String, TableOrder>> tableOrders;

    // Table Name and Reference
    private Map<String, Set<Pair<Ref, TOM_Model>>> tableMapping;

    TOM_Mapping() {
        tableMapping = new HashMap<>();
        tomModels = new HashMap<>();
    }

    void addMapping(String tableName, TOM_Model tomModel, Ref reference) {
        tableMapping.computeIfAbsent(tableName, e -> new HashSet<>()).add(new Pair<>(reference, tomModel));
    }

    void pushUpdates(DBContext dbContext, String tableName) {
        for (Pair<Ref, TOM_Model> reference : tableMapping.get(tableName)) {
            AbstractBookAdv book = (AbstractBookAdv) BookBindings.get(reference.x.getBookName());
            SSheet sheet = book.getSheetByName(reference.x.getSheetName());
            CellRegion tableRegion = new CellRegion(reference.x.getRow(), reference.x.getColumn(),
                    reference.x.getLastRow(), reference.x.getLastColumn());
            sheet.clearCache(tableRegion);
            //reference.y.clearCache(dbContext);

            book.sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CELL_CONTENT_CHANGE,
                    sheet, tableRegion));
        }
    }

    public Model createTOMModel(DBContext context, String tableName) {
        return tomModels.computeIfAbsent(tableName,
                e -> {
                    loadTableOrder(tableName);
                    return new TOM_Model(context, tableName);
                });
    }

    public void createTableOrder(String tableName) {
        /* TODO: New ways to order tuples.
        To start with we have a simple order
        Might be later on add other ways to order tuples.
         */

        String orderName = "Order " + (tableOrders.get(tableName).size() + 1);
        String rowIdxTable = tableName + "_row_" + (tableOrders.get(tableName).size() + 1);
        String colIdxTable = tableName + "_col_" + (tableOrders.get(tableName).size() + 1);

    }

    public Pair<PosMapping, PosMapping> getTableOrder() {
        return getTableOrder(null);
    }

    public Pair<PosMapping, PosMapping> getTableOrder(String orderName) {
        // If order name is null, return the first order.
        // If no order exists created one based on OIDs.
        return null;
    }

    private ArrayList<Integer> getOIDs(DBContext context, String tableName) {
        ArrayList<Integer> oids = new ArrayList<>();

        String getOids = (new StringBuffer())
                .append("SELECT oid FROM ")
                .append(tableName)
                .append(" ORDER BY oid") /* TODO allow custom order */
                .toString();

        AutoRollbackConnection connection = context.getConnection();
        try (Statement stmt = connection.createStatement()) {
            ResultSet set = stmt.executeQuery(getOids);

            while (set.next()) {
                oids.add(set.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return oids;
    }

    public void indexOIDs(DBContext context, String tableName) {
        /* Batch processing */
        ArrayList<Integer> oids = getOIDs(context, tableName);
        //   rowMapping.insertIDs(context, 0, oids);
    }


    private void loadTableOrder(String tableName) {
        String selectTableOrder = "SELECT * FROM tableorders WHERE tablename = ?";
        try (AutoRollbackConnection conn = DBHandler.instance.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectTableOrder)) {
            DBContext dbContext = new DBContext(conn);
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableOrders.computeIfAbsent(tableName, e -> new HashMap<>()).put(
                        rs.getString(2),
                        new TableOrder(dbContext, rs.getString(3), rs.getString(4))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRegion(TOM_Model tomModel, String bookName, String sheetName,
                             CellRegion oldRegion, CellRegion newRegion) {
        Set<Pair<Ref, TOM_Model>> tableSet = tableMapping.get(tomModel.getTableName());

        AbstractBookAdv book = (AbstractBookAdv) BookBindings.get(bookName);
        SSheet sheet = book.getSheetByName(sheetName);
        Ref oldRef = new RefImpl(bookName,
                sheetName, oldRegion.getRow(), oldRegion.getColumn(),
                oldRegion.getLastRow(), oldRegion.getLastColumn());
        Ref newRef = new RefImpl(bookName,
                sheetName, newRegion.getRow(), newRegion.getColumn(),
                newRegion.getLastRow(), newRegion.getLastColumn());

        tableSet.remove(new Pair<>(oldRef, tomModel));
        tableSet.add(new Pair<>(newRef, tomModel));
    }

    public void removeMapping(String tableName, Ref tableRange, TOM_Model tomModel) {
        tableMapping.get(tableName).remove(new Pair<>(tableRange, tomModel));
    }

    private class TableOrder {
        final PosMapping rowIdxTable;
        final PosMapping colIdxTable;

        TableOrder(DBContext dbContext, String rowIdxTable, String colIdxTable) {
            this.rowIdxTable = new CountedBTree(dbContext, rowIdxTable);
            this.colIdxTable = new CountedBTree(dbContext, colIdxTable);
        }
    }
}
