package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;

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
        dirtyRecordLog = Collections.synchronizedList(new ArrayList<>());
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
        groupPrint(sheetCells, controlReturnedTime, initTime, false);
    }

    public double groupPrint(Collection<CellRegion> sheetCells,
                             long controlReturnedTime, long initTime, boolean getAreaUnderCurve) {
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
        double areaUnderCurve = 0;
        double prevTime = controlReturnedTime;
        double prevNumDirty = 0;
        for (Map.Entry<Long, Integer> d : dirtyCellsCounts.entrySet()) {
            if (Math.abs(d.getKey() - controlReturnedTime) <= Math.pow(10, -5)) {
                prevNumDirty = d.getValue();
            }
            if (d.getKey() > controlReturnedTime) {
                if (getAreaUnderCurve) {
                    // calculate area
                    if (prevTime == controlReturnedTime){
                        prevNumDirty = d.getValue();
                        prevTime = d.getKey();
                        continue;
                    }
                    double barWidth = d.getKey() - prevTime;
                    prevTime = d.getKey();
                    areaUnderCurve += barWidth * prevNumDirty;
                    prevNumDirty = d.getValue();
                }
                if (firstNotDone) {
                    System.out.println(0 + "\t" + sheetCells.size());
                    System.out.println(controlReturnedTime - initTime + "\t" + sheetCells.size());
                    System.out.println(controlReturnedTime - initTime + "\t" + d.getValue());
                    firstNotDone = false;
                }
                System.out.println((d.getKey() - initTime) + "\t" + d.getValue());
            }
        }
        if (getAreaUnderCurve)
            return areaUnderCurve;
        else
            return 0;
    }

    public double getAreaUnderCurve (Collection<CellRegion> sheetCells, long controlReturnedTime, long initTime, List<Long> times, List<Integer> cells) {
        boolean firstNotDone = true;
        SortedMap<Long, Integer> dirtyCellsCounts = new TreeMap<>();
        Set<CellRegion> dirtyCells = new HashSet<>();
        Set<CellRegion> sheetCellsSet = new HashSet<>(sheetCells);
        long lastTimestamp = -1;

        for (DirtyRecordEntry e : dirtyRecordLog) {
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
        double areaUnderCurve = 0;
        double prevTime = controlReturnedTime;
        double prevNumDirty = 0;
        for (Map.Entry<Long, Integer> d : dirtyCellsCounts.entrySet()) {
            if (Math.abs(d.getKey() - controlReturnedTime) <= Math.pow(10, -5)) {
                prevNumDirty = d.getValue();
            }
            if (d.getKey() > controlReturnedTime) {
                if (prevTime == controlReturnedTime){
                    prevNumDirty = d.getValue();
                    prevTime = d.getKey();
                    continue;
                }
                double barWidth = d.getKey() - prevTime;
                prevTime = d.getKey();
                areaUnderCurve += barWidth * prevNumDirty;
                prevNumDirty = d.getValue();
                if (firstNotDone) {
                    times.add(0L);
                    times.add(controlReturnedTime - initTime);
                    times.add(controlReturnedTime - initTime);
                    cells.add(sheetCells.size());
                    cells.add(sheetCells.size());
                    cells.add(d.getValue());
                    firstNotDone = false;
                }
                times.add(d.getKey() - initTime);
                cells.add(d.getValue());
            }
        }
        return areaUnderCurve;
    }

    public void print() {
        dirtyRecordLog.forEach(System.out::println);
    }
}
