package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.sys.dependency.Ref;

public class LogEntry {

    public int id;
    public Ref prec;
    public Ref dep;
    public boolean isInsert;

    public LogEntry(int id, Ref prec,
                    Ref dep, boolean isInsert) {
        this.id = id;
        this.prec = prec;
        this.dep = dep;
        this.isInsert = isInsert;
    }
}
