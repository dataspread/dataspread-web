package org.zkoss.zss.model.impl;

import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* Keep a mapping of UI and TOM models */
public class TOM_Mapping {
    public static TOM_Mapping instance=new TOM_Mapping();

    // Table Name and Reference
    private Map<String, Set<Pair<Ref, TOM_Model>>> tableMapping;

    TOM_Mapping()
    {
        tableMapping = new HashMap<>();
    }

    void addMapping(String tableName, TOM_Model tomModel,  Ref reference)
    {
        tableMapping.computeIfAbsent(tableName, e->new HashSet<>()).add(new Pair<>(reference, tomModel));
    }

    void pushUpdates(DBContext dbContext, String tableName)
    {
            for (Pair<Ref, TOM_Model> reference:tableMapping.get(tableName))
            {
                AbstractBookAdv book =  (AbstractBookAdv) BookBindings.get(reference.x.getBookName());
                SSheet sheet = book.getSheetByName(reference.x.getSheetName());
                CellRegion tableRegion = new CellRegion(reference.x.getRow(), reference.x.getColumn(),
                        reference.x.getLastRow(), reference.x.getLastColumn());
                sheet.clearCache(tableRegion);
                reference.y.clearCache(dbContext);

                book.sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CELL_CONTENT_CHANGE,
                        sheet,tableRegion));
            }
    }
}
