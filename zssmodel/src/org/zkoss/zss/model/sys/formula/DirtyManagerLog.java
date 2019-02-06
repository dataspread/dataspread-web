package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
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

    public void groupPrint(Collection<CellRegion> sheetCells, long controlReturnedTime, long initTime) {
        boolean firstNotDone = true;
        SortedMap<Long, Integer> dirtyCellsCounts = new TreeMap<>();
        Set<CellRegion> dirtyCells = new HashSet<>();
        Set<CellRegion> sheetCellsSet = new HashSet<>(sheetCells);
        long lastTimestamp = -1;

        for (DirtyRecordEntry e : dirtyRecordLog) {
            //System.out.println("["+e.timestamp+"] "+(e.action == Action.MARK_DIRTY ? "DIRTY" : "CLEAN")+" "+e.cellRegion);
            if (e.timestamp != lastTimestamp && lastTimestamp != -1) {
                dirtyCellsCounts.put(e.timestamp, dirtyCells.size());
            }
            lastTimestamp = e.timestamp;
            if (e.cellRegion.getCellCount() == 1) {
                if (sheetCellsSet.contains(e.cellRegion)) {
                    CellRegion r1 = e.cellRegion;
                    if (e.action == Action.MARK_DIRTY) {
                        dirtyCells.add(r1);
                    } else {
                        dirtyCells.remove(r1);
                    }
                }
            } else {
                for (CellRegion r1 : sheetCells) {
                    if (e.cellRegion.contains(r1)) {
                        if (e.action == Action.MARK_DIRTY) {
                            dirtyCells.add(r1);
                        } else {
                            dirtyCells.remove(r1);
                        }
                    }
                }
            }
        }
        if (lastTimestamp != -1) {
            dirtyCellsCounts.put(lastTimestamp, dirtyCells.size());
        }

        for (Map.Entry<Long, Integer> d : dirtyCellsCounts.entrySet()) {
            if (d.getKey() > controlReturnedTime) {
                if (firstNotDone) {
                    System.out.println(0 + "\t" + sheetCells.size());
                    System.out.println(controlReturnedTime - initTime + "\t" + sheetCells.size());
                    System.out.println(controlReturnedTime - initTime + "\t" + d.getValue());
                    firstNotDone = false;
                }
                System.out.println((d.getKey() - initTime) + "\t" + d.getValue());
            }
        }
    }

    public void print() {
        dirtyRecordLog.stream().forEach(System.out::println);
    }
}
