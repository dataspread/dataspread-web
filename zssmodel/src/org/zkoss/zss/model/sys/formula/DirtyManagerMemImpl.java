package org.zkoss.zss.model.sys.formula;

import com.google.common.collect.ConcurrentHashMultiset;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.max;

/* Simple in-memory implementation for DirtyManager */
public class DirtyManagerMemImpl extends DirtyManager {
    Set<DirtyRecord> dirtyRecordsSingle;
    Set<DirtyRecord> dirtyRecordsMult;
    ConcurrentHashMap<Ref, ConcurrentHashMultiset<Integer>> dirtyRecordsSingleMap;

    DirtyManagerMemImpl()
    {
        reset();
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
    public void reset() {
        dirtyRecordsSingle = ConcurrentHashMap.newKeySet();
        dirtyRecordsMult = ConcurrentHashMap.newKeySet();
        dirtyRecordsSingleMap = new ConcurrentHashMap<>();
    }

    @Override
    public int getDirtyTrxId(Ref region) {
        int overlapMultiple = dirtyRecordsMult.stream().filter(e->overlaps(e.region, region))
                .map(e->e.trxId)
                .reduce(Integer::max)
                .orElse(-1);
        int overlapSingle = -1;
        if (region.getCellCount() == 1) {
            if (dirtyRecordsSingleMap.containsKey(region)) {
                overlapSingle = dirtyRecordsSingleMap.get(region).stream().reduce(Integer::max).orElse(-1);
            }
        } else {
            overlapSingle = dirtyRecordsSingle.stream().filter(e->overlaps(e.region, region))
                    .map(e->e.trxId)
                    .reduce(Integer::max)
                    .orElse(-1);
        }
        return max(overlapMultiple, overlapSingle);
    }

    @Override
    public synchronized void addDirtyRegion(Ref region, int trxId) {
        DirtyRecord dirtyRecord = new DirtyRecord();
        dirtyRecord.region = region;
        dirtyRecord.trxId = trxId;

        // For now only consider cell dependencies.
        // Later we need to consider sheet and book level as well.
        if (region.getType() == Ref.RefType.CELL) {
            dirtyRecordsSingle.add(dirtyRecord);
            if (dirtyRecordsSingleMap.containsKey(region)) {
                dirtyRecordsSingleMap.get(region).add(trxId);
            }
        } else if (region.getType() == Ref.RefType.AREA) {
            dirtyRecordsMult.add(dirtyRecord);
        }
        notifyAll();
        DirtyManagerLog.instance.markDirty(region);
    }

    @Override
    public boolean isEmpty() {
        return dirtyRecordsSingle.isEmpty() && dirtyRecordsMult.isEmpty();
    }

    @Override
    public void removeDirtyRegion(Ref region, int trxId) {
        DirtyRecord dirtyRecord = new DirtyRecord();
        dirtyRecord.region = region;
        dirtyRecord.trxId = trxId;

        if (region.getType() == Ref.RefType.CELL) {
            dirtyRecordsSingle.remove(dirtyRecord);
            if (dirtyRecordsSingleMap.containsKey(region)) {
                dirtyRecordsSingleMap.get(region).remove(trxId);
            }
        } else {
            dirtyRecordsMult.remove(dirtyRecord);
        }
    }

    // Call in a single thread.
    public synchronized List<DirtyRecord> getAllDirtyRegions() {
        List<DirtyRecord> ret = new ArrayList<>(dirtyRecordsSingle);
        List<DirtyRecord> ret2 = new ArrayList<>(dirtyRecordsMult);
        ret.addAll(ret2);
        if (ret.isEmpty()) {
            try {
                wait(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
