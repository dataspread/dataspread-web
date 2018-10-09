package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/* Simple in-memory implementation for DirtyManager */
public class DirtyManagerMemImpl extends DirtyManager {
    ConcurrentSkipListSet<DirtyRecord> dirtyRecords;

    DirtyManagerMemImpl()
    {
        dirtyRecords = new ConcurrentSkipListSet<>();
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
    public synchronized void addDirtyRegion(Ref region, int trxId) {
        DirtyRecord dirtyRecord = new DirtyRecord();

        // For now only consider cell dependencies.
        // Later we need to consider sheet and book level as well.
        if (region.getType()== Ref.RefType.AREA || region.getType()== Ref.RefType.CELL) {
            dirtyRecord.region = region;
            dirtyRecord.trxId = trxId;
            dirtyRecords.add(dirtyRecord);
        }
        notifyAll();
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

    // Call in a single thread.
    public synchronized List<DirtyRecord> getAllDirtyRegions() {
        List<DirtyRecord> ret = new ArrayList<>(dirtyRecords);
        if (ret.isEmpty()) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
