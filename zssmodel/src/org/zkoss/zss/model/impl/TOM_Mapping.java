package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.*;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* Keep a mapping of UI and TOM models */
public class TOM_Mapping {
    public static TOM_Mapping instance=new TOM_Mapping();

    // Table Name and Reference
    private Map<String, Set<Ref>> tableMapping;

    TOM_Mapping()
    {
        tableMapping = new HashMap<>();
    }

    void addMapping(String tableName, Ref reference)
    {
        tableMapping.computeIfAbsent(tableName, e->new HashSet<>()).add(reference);
    }

    void pushUpdates(String tableName)
    {
        for (Ref reference:tableMapping.get(tableName))
        {
            AbstractBookAdv book =  (AbstractBookAdv) BookBindings.get(reference.getBookName());
            SSheet sheet = book.getSheetByName(reference.getSheetName());
            CellRegion tableRegion = new CellRegion(reference.getRow(), reference.getColumn(),
                    reference.getLastRow(), reference.getLastColumn());
            sheet.clearCache(tableRegion);
            book.sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CELL_CONTENT_CHANGE,
                    sheet,tableRegion));
        }
    }
}
