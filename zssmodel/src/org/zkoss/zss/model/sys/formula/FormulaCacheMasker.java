package org.zkoss.zss.model.sys.formula;

import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.FormulaResultCellValue;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.TransactionManager;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.*;

/**
 * Created by zekun.fan@gmail.com on 8/16/17.
 */

public enum FormulaCacheMasker {
    INSTANCE;
    private Map<SSheet,Collection<MaskArea>> mapping;
    private FormulaCacheMasker(){
        mapping=new HashMap<>();
    }

    public void mask(Ref target){
        SBook book;
        SSheet sheet;
        if (target.getType()!= Ref.RefType.CELL && target.getType()!=Ref.RefType.AREA)
            return;
        if (!TransactionManager.INSTANCE.isInTransaction(null))
            throw new RuntimeException("Masking not within transaction!");
        book=BookBindings.get(target.getBookName());
        if (book==null)
            return;
        sheet=book.getSheetByName(target.getSheetName());
        if (sheet==null)
            return;
        Collection<MaskArea> records = mapping.computeIfAbsent(sheet, k -> new ArrayList<>());
        MaskArea newRec=new MaskArea(TransactionManager.INSTANCE.getXid(null),new CellRegion(target.getRow(),target.getColumn(),target.getLastRow(),target.getLastColumn()));
        records.add(newRec);
    }

    //Assumption: Each cell in each transaction is unmask once and only once
    //Otherwise, the area has to be expanded
    //Supported by caller
    public void unmask(Ref target,int xid){
        SBook book;
        SSheet sheet;
        if (target.getType()!=Ref.RefType.CELL && target.getType()!=Ref.RefType.AREA)
            return;
        //This code is duplicated for too many time
        book=BookBindings.get(target.getBookName());
        if (book==null)
            return;
        sheet=book.getSheetByName(target.getSheetName());
        if (sheet==null)
            return;
        Collection<MaskArea> records=mapping.get(sheet);
        for (Iterator<MaskArea> iter=records.iterator();iter.hasNext();){
            MaskArea maskArea=iter.next();
            if (xid==maskArea.xid) {
                //Intersection - Once and only once.
                 int intersect=Math.max(0,1+
                         Math.min(maskArea.region.lastColumn,target.getLastColumn())-
                                 Math.max(maskArea.region.column,target.getColumn()))
                         * Math.max(0,1+
                         Math.min(maskArea.region.lastRow,target.getLastRow()-
                         Math.max(maskArea.region.row,target.getRow())));
                 maskArea.updateCnt+=intersect;
                 if (maskArea.updateCnt==maskArea.region.getCellCount()){
                     iter.remove();
                 }
            }
        }
    }

    public int isMaskedUntil(Ref target){
        SBook book;
        SSheet sheet;
        //This code is duplicated for too many time
        int result=-1;
        if (target.getType()!=Ref.RefType.CELL && target.getType()!=Ref.RefType.AREA)
            return result;
        book=BookBindings.get(target.getBookName());
        if (book==null)
            return result;
        sheet=book.getSheetByName(target.getSheetName());
        if (sheet==null)
            return result;
        Collection<MaskArea> records=mapping.get(sheet);
        if (records==null)
            return result;
        for (MaskArea maskArea:records){
            if (result<maskArea.xid && !(
                    maskArea.region.column>target.getLastColumn() ||
                    maskArea.region.lastColumn<target.getColumn()||
                    maskArea.region.row>target.getLastRow()||
                    maskArea.region.lastRow<target.getRow()))
                //Intersection
                    result=maskArea.xid;
        }
        return result;
    }

    public FormulaResultCellValue getMaskedVal(){
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

    private class MaskArea{
        int xid;
        int updateCnt;
        CellRegion region;

        MaskArea(int xid, CellRegion region) {
            this.xid = xid;
            this.region = region;
            this.updateCnt=0;
        }
    }
}
