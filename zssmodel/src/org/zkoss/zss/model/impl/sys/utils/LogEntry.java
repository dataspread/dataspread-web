package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.sys.dependency.Ref;

public class LogEntry {

    public Ref prec;
    public Ref dep;
    public boolean isInsert;

    public LogEntry(Ref prec, Ref dep, boolean isInsert) {
        this.prec = prec;
        this.dep = dep;
        this.isInsert = isInsert;
    }
}
