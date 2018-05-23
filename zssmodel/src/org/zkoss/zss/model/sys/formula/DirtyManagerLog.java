package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.*;

/* Simple in-memory implementation for DirtyManager */
public class DirtyManagerLog {
    static public DirtyManagerLog instance = new DirtyManagerLog();
    List<DirtyRecordEntry> dirtyRecordLog;

    enum Action{MARK_DIRTY, MARK_CLEAN}

    static private class DirtyRecordEntry
    {
        CellRegion cellRegion;
        Action action;
        long timestamp;

        @Override
        public String toString() {
            return timestamp + "\t" + action + "\t" + cellRegion;
        }
    }

    DirtyManagerLog()
    {
        init();
    }

    public void init()
    {
        dirtyRecordLog = Collections.synchronizedList(new ArrayList());
    }

    public long getDirtyTime(CellRegion cellRegion)
    {
        long dirtyTime=0;
        long startTime=0;
        if (cellRegion != null)
            for (DirtyRecordEntry e:dirtyRecordLog) {
                if (cellRegion.overlaps(e.cellRegion)) {
                    if (e.action==Action.MARK_DIRTY) {
                        startTime = e.timestamp;
                    } else if (e.action==Action.MARK_CLEAN) {
                        dirtyTime+=e.timestamp - startTime;
                    }
                }
            }
        return dirtyTime;
    }

    public void markDirty(Ref region) {

        // For now only consider cell dependencies.
        // Later we need to consider sheet and book level as well.
        if (region.getType()== Ref.RefType.AREA || region.getType()== Ref.RefType.CELL) {

            DirtyRecordEntry dirtyRecordEntry = new DirtyRecordEntry();

            dirtyRecordEntry.cellRegion = new CellRegion(region);
            dirtyRecordEntry.action = Action.MARK_DIRTY;
            dirtyRecordEntry.timestamp = System.currentTimeMillis();

            dirtyRecordLog.add(dirtyRecordEntry);
        }
    }

    public void markClean(CellRegion region) {

        // For now only consider cell dependencies.
        // Later we need to consider sheet and book level as well.

        DirtyRecordEntry dirtyRecordEntry = new DirtyRecordEntry();

        dirtyRecordEntry.cellRegion = region;
        dirtyRecordEntry.action = Action.MARK_CLEAN;
        dirtyRecordEntry.timestamp = System.currentTimeMillis();

        dirtyRecordLog.add(dirtyRecordEntry);

    }

    public void groupPrint(Collection<CellRegion> sheetCells) {
        int runningTotal = 0;
        SortedMap<Long, Integer> dirtyCellsCounts = new TreeMap<>();
        for (DirtyRecordEntry e : dirtyRecordLog) {
            dirtyCellsCounts.putIfAbsent(e.timestamp, 0);
            int cellCount = 0;
            for (CellRegion r1 : sheetCells)
                if (e.cellRegion.contains(r1))
                    cellCount++;

            if (e.action == Action.MARK_DIRTY)
                dirtyCellsCounts.put(e.timestamp, dirtyCellsCounts.get(e.timestamp) + cellCount);
            else
                dirtyCellsCounts.put(e.timestamp, dirtyCellsCounts.get(e.timestamp) - cellCount);
        }
        for (Map.Entry<Long, Integer> d : dirtyCellsCounts.entrySet()) {
            runningTotal += d.getValue();

            System.out.println(d.getKey() + "\t" + runningTotal);
        }
    }

    public void print() {
        dirtyRecordLog.stream().forEach(System.out::println);
    }
}
