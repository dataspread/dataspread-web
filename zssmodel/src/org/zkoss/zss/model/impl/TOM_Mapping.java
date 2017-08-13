package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.HashMap;
import java.util.Map;

/* Keep a mapping of UI and TOM models */
public class TOM_Mapping {
    public static TOM_Mapping instance=new TOM_Mapping();
    // Table Name and CellRegion
    private Map<String, Ref> tableMapping;

    TOM_Mapping()
    {
        tableMapping = new HashMap<>();
    }

    void addMapping(String tableName, Ref reference)
    {
        tableMapping.put(tableName, reference);
    }

}
