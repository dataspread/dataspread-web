/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBorder.BorderType;
import org.zkoss.zss.range.SRange;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class MergeHelper extends RangeHelperBase{

	public MergeHelper(SRange range) {
		super(range);
	}
	
	//BookHelper, public static ChangeInfo unMerge(XSheet sheet, int tRow, int lCol, int bRow, int rCol,boolean overlapped) {
	public void unmerge(boolean overlapped) {
		int tRow = getRow();
		int lCol = getColumn();
		int bRow = getLastRow();
		int rCol = getLastColumn();
		
		sheet.removeMergedRegion(new CellRegion(tRow,lCol,bRow,rCol),overlapped);
		
//		for(int j = sheet.getNumOfMergedRegion() - 1; j >= 0; --j) {
//        	final CellRegion merged = sheet.getMergedRegion(j);
//        	
//        	final int firstCol = merged.getColumn();
//        	final int lastCol = merged.getLastColumn();
//        	final int firstRow = merged.getRow();
//        	final int lastRow = merged.getLastRow();
//        	
//        	// ZSS-395 unmerge when any cell overlap with merged region
//        	// ZSS-412 use a flag to decide to check overlap or not.
//        	if( (overlapped && overlap(firstRow, firstCol, lastRow, lastCol, tRow, lCol, bRow, rCol)) || 
//        			(!overlapped && contain(tRow, lCol, bRow, rCol,firstRow, firstCol, lastRow, lastCol)) ) {
//				sheet.removeMergedRegion(merged);
//        	}
//		}
	}
	
	//a b are overlapped.
//	private static boolean overlap(int aTopRow, int aLeftCol, int aBottomRow, int aRightCol,
//			int bTopRow, int bLeftCol, int bBottomRow, int bRightCol) {
//		
//		boolean xOverlap = between(aLeftCol, bLeftCol, bRightCol) || between(bLeftCol, aLeftCol, aRightCol);
//		boolean yOverlap = between(aTopRow, bTopRow, bBottomRow) || between(bTopRow, aTopRow, aBottomRow);
//		
//		return xOverlap && yOverlap;
//	}
	
	//a contains b
//	private static boolean contain(int aTopRow, int aLeftCol, int aBottomRow, int aRightCol,
//			int bTopRow, int bLeftCol, int bBottomRow, int bRightCol){
//		return aLeftCol <= bLeftCol && aRightCol >= bRightCol 
//        		&& aTopRow <= bTopRow && aBottomRow >= bBottomRow;
//	}
//	
//	private static boolean between(int value, int min, int max) {
//		return (value >= min) && (value <= max);
//	}
	
	/*
	 * Merge the specified range per the given tRow, lCol, bRow, rCol.
	 * 
	 * @param sheet sheet
	 * @param tRow top row
	 * @param lCol left column
	 * @param bRow bottom row
	 * @param rCol right column
	 * @param across merge across each row.
	 * @return {@link Ref} array where the affected formula cell references in index 1 and to be evaluated formula cell references in index 0.
	 */
//	@SuppressWarnings("unchecked")
	public void merge(boolean across) {
		int tRow = range.getRow();
		int lCol = range.getColumn();
		int bRow = range.getLastRow();
		int rCol = range.getLastColumn();
		
		List<CellRegion> overlaps = sheet.getOverlapsMergedRegions(new CellRegion(tRow,lCol,bRow,rCol),false); 
		
		if(overlaps.size()>0){
			//to backward comparable to old sepc. we should unmerge it
//			throw new InvalidateModelOpException("can't merge an overlapped region "+overlaps.get(0).getReferenceString()+", unmerge it first");
			unmerge(true);
		}
		
		if (across) {
			for(int r = tRow; r <= bRow; ++r) {
				merge0(sheet, r, lCol, r, rCol);
			}
		} else {
			merge0(sheet, tRow, lCol, bRow, rCol);
		}
	}
	
	private void merge0(SSheet sheet, int tRow, int lCol, int bRow, int rCol) {
		if(tRow==bRow && lCol==rCol)
			return;
//		final List<MergeChange> changes = new ArrayList<MergeChange>();
//		final Set<CellRegion> all = new HashSet<CellRegion>();
//		final Set<CellRegion> last = new HashSet<CellRegion>();
		//find the left most non-blank cell.
		SCell target = null;
		for(int r = tRow; target == null && r <= bRow; ++r) {
			for(int c = lCol; c <= rCol; ++c) {
				final SCell cell = sheet.getCell(r, c);
				if (!isBlank(cell)) {
					target = cell;
					break;
				}
			}
		}
		
		SCellStyle style = null;
		if (target != null) { //found the target
			final int tgtRow = target.getRowIndex();
			final int tgtCol = target.getColumnIndex();
			final int nRow = tRow - tgtRow;
			final int nCol = lCol - tgtCol;
			if (nRow != 0 || nCol != 0) { //if target not the left-top one, move to left-top
//				final ChangeInfo info = BookHelper.moveRange(sheet, tgtRow, tgtCol, tgtRow, tgtCol, nRow, nCol);
				sheet.moveCell(tgtRow, tgtCol, tgtRow, tgtCol, nRow, nCol);
//				if (info != null) {
//					changes.addAll(info.getMergeChanges());
//					last.addAll(info.getToEval());
//					all.addAll(info.getAffected());
//				}
			}
			final SCellStyle source = target.getCellStyle();
			style = source.equals(sheet.getBook().getDefaultCellStyle()) ? null : sheet.getBook().createCellStyle(source,true);
			if (style != null) {
//				style.cloneStyleFrom(source);
				style.setBorderLeft(BorderType.NONE);
				style.setBorderTop(BorderType.NONE);
				style.setBorderRight(BorderType.NONE);
				style.setBorderBottom(BorderType.NONE);
				target.setCellStyle(style); //set all cell in the merged range to CellStyle of the target minus border
			}
			//1st row (exclude 1st cell)
			for (int c = lCol + 1; c <= rCol; ++c) {
				final SCell cell = sheet.getCell(tRow, c);
				cell.setCellStyle(style); //set all cell in the merged range to CellStyle of the target minus border
				cell.setValue(null);
//				final Set<Ref>[] refs = BookHelper.setCellValue(cell, (RichTextString) null);
//				if (refs != null) {
//					last.addAll(refs[0]);
//					all.addAll(refs[1]);
//				}
			}
			//2nd row and after
			for(int r = tRow+1; r <= bRow; ++r) {
				for(int c = lCol; c <= rCol; ++c) {
					final SCell cell = sheet.getCell(r, c);
					cell.setCellStyle(style); //set all cell in the merged range to CellStyle of the target minus border
					cell.setValue(null);
//					final Set<Ref>[] refs = BookHelper.setCellValue(cell, (RichTextString) null);
//					if (refs != null) {
//						last.addAll(refs[0]);
//						all.addAll(refs[1]);
//					}
				}
			}
		}
		final CellRegion mergeArea = new CellRegion(tRow, lCol,bRow,rCol);
		sheet.addMergedRegion(mergeArea);
//		final Ref mergeArea = new AreaRefImpl(tRow, lCol, bRow, rCol, BookHelper.getRefSheet((XBook)sheet.getWorkbook(), sheet)); 
//		all.add(mergeArea);//should update the cell in the merge area.
//		changes.add(new MergeChange(null, mergeArea));
		
//		return new ChangeInfo(last, all, changes);
	}	

}
