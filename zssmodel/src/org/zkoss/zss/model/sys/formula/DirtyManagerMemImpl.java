package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/* Simple in-memory implementation for DirtyManager */
public class DirtyManagerMemImpl extends DirtyManager {
    LinkedBlockingQueue<DirtyRecord> dirtyRecordPriorityBlockingQueue;
    ConcurrentSkipListSet<DirtyRecord> dirtyRecords;

    DirtyManagerMemImpl()
    {
        dirtyRecords = new ConcurrentSkipListSet<>();
        dirtyRecordPriorityBlockingQueue = new LinkedBlockingQueue<>();
    }

    private boolean overlaps(Ref region1, Ref region2)
    {
        if (!region1.getBookName().equals(region2.getBookName()))
            return false;
        if (!region1.getSheetName().equals(region2.getSheetName()))
            return false;

        CellRegion cellRegion1 = new CellRegion(region1);
        CellRegion cellRegion2 = new CellRegion(region2);
        return cellRegion1.overlaps(cellRegion2);
    }

    @Override
    public int getDirtyTrxId(Ref region) {
         return dirtyRecords.stream().filter(e->overlaps(e.region, region))
                .map(e->e.trxId)
                .reduce(Integer::max)
                 .orElse(-1);
    }

    @Override
    public void addDirtyRegion(Ref region, int trxId) {
        DirtyRecord dirtyRecord = new DirtyRecord();

        // For now only consider cell dependencies.
        // Later we need to consider sheet and book level as well.
        if (region.getType()== Ref.RefType.AREA || region.getType()== Ref.RefType.CELL) {
            dirtyRecord.region = region;
            dirtyRecord.trxId = trxId;
            dirtyRecords.add(dirtyRecord);
            dirtyRecordPriorityBlockingQueue.add(dirtyRecord);
        }
        DirtyManagerLog.instance.markDirty(region);
    }

    @Override
    public boolean isEmpty() {
        return dirtyRecords.isEmpty();
    }

    @Override
    public void removeDirtyRegion(Ref region, int trxId) {
        DirtyRecord dirtyRecord = new DirtyRecord();
        dirtyRecord.region = region;
        dirtyRecord.trxId = trxId;

        dirtyRecords.remove(dirtyRecord);
    }

    @Override
    public DirtyRecord getDirtyRegionFromQueue() {
        return dirtyRecordPriorityBlockingQueue.poll();
    }

    @Override
    public DirtyRecord getDirtyRegionFromQueue(long waitTime) {
        try {
            return dirtyRecordPriorityBlockingQueue.poll(waitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }
}
