package org.zkoss.zss.model.sys.formula;

import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.impl.FormulaResultCellValue;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.List;

public abstract class DirtyManager {
    //static public DirtyManager dirtyManagerInstance = new DirtyManagerPGImpl();
    static public DirtyManager dirtyManagerInstance = new DirtyManagerMemImpl();

    abstract public void reset();

    /* Check if the given region is dirty
    *  Return the trxId that made this region dirty
    *  -1 if the target is not dirty */
    abstract public int getDirtyTrxId(Ref target);

    /* Add a new dirty region */
    abstract public void addDirtyRegion(Ref target, int trxId);

    /* Remove a dirty region.
       All the regions with trxId <= input trxId
       and region encapsulated with inout target are removed.
     */

    abstract public boolean isEmpty();

    abstract public void removeDirtyRegion(Ref target, int trxId);

    abstract public List<DirtyRecord> getAllDirtyRegions();

    public static FormulaResultCellValue getDirtyValue(){
        return _val;
    }

    private static FormulaResultCellValue _val=new FormulaResultCellValue(new EvaluationResult() {
        @Override
        public ResultType getType() {
            return ResultType.SUCCESS;
        }

        @Override
        public Object getValue() {
            return "...";
        }

        @Override
        public ValueEval getValueEval() {
            return null;
        }
    });

    public static class DirtyRecord implements Comparable {
        public Ref region;
        public int trxId;

        @Override
        public int compareTo(Object o) {
            return this.toString().compareTo(o.toString());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + trxId;
            result = prime * result + region.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof DirtyRecord))
                return false;
            DirtyRecord dirtyRecord = (DirtyRecord) o;
            return region.equals(dirtyRecord.region) &&
                    trxId == dirtyRecord.trxId;
        }

        @Override
        public String toString() {
            return region + " " + trxId;
        }
    }
}
