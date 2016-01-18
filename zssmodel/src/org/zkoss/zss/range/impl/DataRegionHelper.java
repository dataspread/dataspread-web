/* DataRegionHelper.java
 * 
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

import java.util.List;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.range.SRange;

/**
 * To help search the data region
 * @author dennis
 * @since 3.5.0
 */
//these code if from XRangeImpl and migrate to new model
/*package*/ class DataRegionHelper extends RangeHelperBase{
	
	public DataRegionHelper(SRange range){
		super(range);
	}

	// ZSS-246: give an API for user checking the auto-filtering range before applying it.
	public CellRegion findAutoFilterDataRegion() {
		
		//The logic to decide the actual affected range to implement autofilter:
		//If it's a multiple cell range, it's the range intersect with largest range of the sheet.
		//If it's a single cell range, it has to be extend to a continuous range by looking up the near 8 cells of the single cell.
		CellRegion currentArea = new CellRegion(getRow(), getColumn(), getLastRow(), getLastColumn());
		FilterRegionHelper frHelper = new FilterRegionHelper();
		//ZSS-199
		if (isWholeRow()) {
			//extend to a continuous range from the top row
			CellRegion cra = frHelper.getRowCurrentRegion(sheet, getRow(), getLastRow());
			return cra; 
			
		} else if (isOneCell(sheet,currentArea)){
			//only one cell selected(include merged one), try to look the max range surround by blank cells 
			CellRegion cra = frHelper.findCurrentRegion(sheet, getRow(), getColumn());
			return cra; 
			
		} else {
			CellRegion largeRange = getLargestRange(sheet); //get the largest range that contains non-blank cells
			if (largeRange == null) {
				return null;
			}
			int left = largeRange.getColumn();
			int top = largeRange.getRow();
			int right = largeRange.getLastColumn();
			int bottom = largeRange.getLastRow();
			if (left < getColumn()) {
				left = getColumn();
			}
			if (right > getLastColumn()) {
				right = getLastColumn();
			}
			if (top < getRow()) {
				top = getRow();
			}
			if (bottom > getLastRow()) {
				bottom = getLastRow();
			}
			if (top > bottom || left > right) {
				return null;
			}
			return new CellRegion(top, left, bottom, right); 
		}
	}	
	
	public static boolean isOneCell(SSheet sheet, CellRegion rng) {
		if (rng.isSingle()) {
			return true;
		}
		final int l = rng.getColumn();
		final int t = rng.getRow();
		final int r = rng.getLastColumn();
		final int b = rng.getLastRow();
		
		int s = sheet.getNumOfMergedRegion();
		for (int i = 0; i < s; i++) {
			final CellRegion ref = sheet.getMergedRegion(i);
			final int top = ref.getRow();
	        final int bottom = ref.getLastRow();
	        final int left = ref.getColumn();
	        final int right = ref.getLastColumn();
	    
	        if (l == left && t == top && r == right && b == bottom) {
	        	return true;
	        }
		}
		return false;
	}
	
	//ZSS-646: apply filter incorrectly when contained merged cells
	/*package*/ static int[] getNullRowMinMax(SSheet sheet, int row, int minc, int maxc) {
		final CellRegion region = new CellRegion(row, minc, row, maxc);
		final List<CellRegion> overlaps = sheet.getOverlapsMergedRegions(region, true);
		int maxr = -1;
		int minr = row;
		for (CellRegion rng: overlaps) {
			final int t = rng.getRow();
			final int l = rng.getColumn();
			final SCell cell = sheet.getCell(t, l);
			if (isBlank(cell)) continue;
			else {
				final int b = rng.getLastRow();
				final int r = rng.getLastColumn();
				if (minc > l) minc = l;
				if (minr > t) minr = t;
				if (maxc < r) maxc = r;
				if (maxr < b) maxr = b;
			}
		}
		return maxr < 0 ? null : new int[] {minc, minr, maxc, maxr}; //l, t, r, b
	}
	private static boolean isEmptyRow(SSheet sheet, SRow rowobj) {
		return rowobj.isNull() || sheet.getStartCellIndex(rowobj.getIndex()) < 0;
	}
	
	
	//returns the largest square range of this sheet that contains non-blank cells
	private CellRegion getLargestRange(SSheet sheet) {
		int t = Math.max(0, sheet.getStartRowIndex());//to ignore -1 (no row)
		int b = sheet.getEndRowIndex();
		
		//ZSS-1073: no data at all
		if (b < 0) return null;
		
		//top row
		int minr = -1;
		for(int r = t; r <= b && minr < 0; ++r) {
			final SRow rowobj = sheet.getRow(r);
			if (!rowobj.isNull()) {
				int ll = sheet.getStartCellIndex(r);
				if (ll < 0) { //empty row
					continue;
				}
				int rr = sheet.getEndCellIndex(r);
				for(int c = ll; c <= rr; ++c) {
					final SCell cell = sheet.getCell(r,c);
					if (!isBlank(cell)) { //first no blank row
						minr = r;
						break;
					}
				}
			}
		}
		//bottom row
		int maxr = -1;
		final int sl = sheet.getStartColumnIndex();
		final int sr = sheet.getEndColumnIndex();
		for(int r = b; r >= minr && maxr < 0; --r) {
			final SRow rowobj = sheet.getRow(r);
			if (!rowobj.isNull()) {
				int ll = sheet.getStartCellIndex(r);
				if (ll < 0) { //empty row
					continue;
				}
				int rr = sheet.getEndCellIndex(r);
				for(int c = ll; c <= rr; ++c) {
					final SCell cell = sheet.getCell(r, c);
					if (!isBlank(cell)) { //first no blank row
						maxr = r;
						break;
					}
				}
			} else { // could be bottom row of merged cell
				int[] ltrb = getNullRowMinMax(sheet, r, sl, sr);
				if (ltrb != null) {
					maxr = ltrb[3];
				}
			}
		}
		//left col
		int minc = Integer.MAX_VALUE;
		for(int r = minr; r <= maxr; ++r) {
			final SRow rowobj = sheet.getRow(r);
			if (!rowobj.isNull()) {
				int ll = sheet.getStartCellIndex(r);
				if (ll < 0) { //empty row
					continue;
				}
				int rr = sheet.getEndCellIndex(r);
				for(int c = ll; c < minc && c <= rr; ++c) {
					final SCell cell = sheet.getCell(r,c);
					if (!isBlank(cell)) { //first no blank row
						minc = c;
						break;
					}
				}
			}
		}
		//right col
		int maxc = -1;
		for(int r = minr; r <= maxr; ++r) {
			final SRow rowobj = sheet.getRow(r);
			if (!rowobj.isNull()) {
				int ll = sheet.getStartCellIndex(r);
				if (ll < 0) { //empty row
					continue;
				}
				int rr =  sheet.getEndCellIndex(r);
				for(int c = rr; c > maxc && c >= ll; --c) {
					final SCell cell = sheet.getCell(r, c);
					if (!isBlank(cell)) { //first no blank row
						CellRegion merged = sheet.getMergedRegion(r, c); //ZSS-646
						maxc = merged == null ? c : merged.getLastColumn();
						break;
					}
				}
			}
		}
		
		if (minr < 0 || maxc < 0) { //all blanks!
			return null;
		}
		return new CellRegion(minr, minc, maxr, maxc);
	}	

	//ZSS-988: consider auto filter surrounded by Table
	public static class FilterRegionHelper {
		//outline region surrounded by Tables 
		int llm, tlm, rlm, blm;
		
		/**
		 * Search adjacent cells and return a range with non-blank cells based on a given cell.
		 * It searches row by row to find minimal and maximal non-blank columns. 
		 * @param sheet target searching sheet
		 * @param row starting cell's row index
		 * @param col starting cell's column index
		 * @return
		 */
		/*package*/ CellRegion findCurrentRegion(SSheet sheet, int row, int col) {
			//ZSS-988: get the outline region surrounded by Tables
			llm = 0;
			rlm = Integer.MAX_VALUE;
			tlm = 0;
			blm = Integer.MAX_VALUE;
			
			// locate the row limits first
			for (STable tb : sheet.getTables()) {
				final CellRegion rgn = tb.getAllRegion().getRegion();
				
				final int l = rgn.getColumn();
				final int r = rgn.getLastColumn();
				final int t = rgn.getRow();
				final int b = rgn.getLastRow();
				
				//Don't consider row limits unless the cell is between the table's first column and last column
				if (l <= col && col <= r) {  
					if (row < t && blm >= t) {
						blm = t - 1;
					}
					if (row > b && tlm <= b) {
						tlm = b + 1;
					}
				}
			}
			
			// then locate the column limits
			for (STable tb : sheet.getTables()) {
				final CellRegion rgn = tb.getAllRegion().getRegion();
				
				final int l = rgn.getColumn();
				final int r = rgn.getLastColumn();
				final int t = rgn.getRow();
				final int b = rgn.getLastRow();

				//Don't consider column limits unless the table overlapped with found row limits
				if (t <= blm && b >= tlm) {
					if (col < l && rlm >= l) {
						rlm = l - 1;
					}
					if (col > r && llm <= r) {
						llm = r + 1;
					}
				}
			}
			
			// check this row
			int ltrb[] = getNonBlankCell(sheet, row, col);

			// check previous row if need
			if (ltrb == null && row > 0) {
				ltrb = getNonBlankCell(sheet, row - 1, col);
			}

			// check next row if need 
			if (ltrb == null) {
				ltrb = getNonBlankCell(sheet, row + 1, col);
			}
			
			return ltrb == null ? null : findCurrentRegion0(sheet, row, ltrb);
		}
		
		private int[] getNonBlankCell(SSheet sheet, int row, int col) {
			//ZSS-988: as long as out of region, deemed as blank
			if (tlm > row || blm < row || llm > col || rlm < col)
				return null;
			
			final SRow rowObj = sheet.getRow(row);
			return !isEmptyRow(sheet, rowObj) ?
				getNonBlankCell0(sheet, row, col) : null; 
		}
		
		private int[] getNonBlankCell0(SSheet sheet, int row, int col) {
			int[] ltrb = getCellMinMax(sheet, row, col);
			if (ltrb == null && col > 0) { // check left col
				ltrb = getCellMinMax(sheet, row, col - 1);
			}
			if (ltrb == null) { // check right col
				ltrb = getCellMinMax(sheet, row, col + 1);
			}
			return ltrb;
		}
		
		private CellRegion findCurrentRegion0(SSheet sheet, int row, int[] ltrb) {
			int minNonBlankColumn = ltrb[0];
			int maxNonBlankColumn = ltrb[2];
			int minNonBlankRow = ltrb[1];
			int maxNonBlankRow = ltrb[3];
			final SRow rowobj = sheet.getRow(row);
			final int[] leftTopRightBottom = getRowMinMax(sheet, rowobj, minNonBlankColumn, maxNonBlankColumn);
			if (leftTopRightBottom != null) {
				minNonBlankColumn = leftTopRightBottom[0];
				minNonBlankRow = leftTopRightBottom[1];
				maxNonBlankColumn = leftTopRightBottom[2];
				maxNonBlankRow = leftTopRightBottom[3];
			}
			
			int rowUp = row > 0 ? row - 1 : row;
			int rowDown = row + 1; //ZSS-646 
			
			boolean stopFindingUp = rowUp == row;
			boolean stopFindingDown = false;
			do {
				//for row above
				if (!stopFindingUp) {
					final SRow rowu = sheet.getRow(rowUp);
					//ZSS-646: could be bottom row of vertical merged cell if rowu == null
					final int[] upperRowLeftTopRightBottom = rowu.isNull() || sheet.getStartCellIndex(rowUp) < 0 ?
							getNullRowMinMax(sheet, rowUp, minNonBlankColumn, maxNonBlankColumn) :
							getRowMinMax(sheet, rowu, minNonBlankColumn, maxNonBlankColumn);
					if (upperRowLeftTopRightBottom != null) {
						if (minNonBlankColumn != upperRowLeftTopRightBottom[0] || maxNonBlankColumn != upperRowLeftTopRightBottom[2]) {  //minc or maxc changed!
							stopFindingDown = false;
							minNonBlankColumn = upperRowLeftTopRightBottom[0];
							maxNonBlankColumn = upperRowLeftTopRightBottom[2];
						}
						if (minNonBlankRow > upperRowLeftTopRightBottom[1]) {
							minNonBlankRow = upperRowLeftTopRightBottom[1];
						}
						if (maxNonBlankRow < upperRowLeftTopRightBottom[3]) { //ZSS-646
							maxNonBlankRow = upperRowLeftTopRightBottom[3];
						}
						if (rowUp > 0) {
							--rowUp;
						} else {
							stopFindingUp = true; //no more row above!
						}
					} else { //blank row
						stopFindingUp = true;
					}
				}

				//for row below
				if (!stopFindingDown) {
					final SRow rowd = sheet.getRow(rowDown);
					final int[] downRowLeftTopRightBottom = rowd.isNull() || sheet.getStartCellIndex(rowDown) < 0 ? 
							getNullRowMinMax(sheet, rowDown, minNonBlankColumn, maxNonBlankColumn) :
							getRowMinMax(sheet, rowd, minNonBlankColumn, maxNonBlankColumn);
					if (downRowLeftTopRightBottom != null) {
						if (minNonBlankColumn != downRowLeftTopRightBottom[0] || maxNonBlankColumn != downRowLeftTopRightBottom[2]) { //minc and maxc changed
							stopFindingUp = false;
							minNonBlankColumn = downRowLeftTopRightBottom[0];
							maxNonBlankColumn = downRowLeftTopRightBottom[2];
						}
						if (maxNonBlankRow < downRowLeftTopRightBottom[3]) {
							maxNonBlankRow = downRowLeftTopRightBottom[3];
						}
						++rowDown;
					} else { //blank row
						stopFindingDown = true;
					}
				}
			} while(!stopFindingUp || !stopFindingDown);
			
			return new CellRegion(minNonBlankRow, minNonBlankColumn, maxNonBlankRow, maxNonBlankColumn);
		}
		
		//[0]: left, [1]: top, [2]: right, [3]: bottom; null mean blank row
		private int[] getRowMinMax(SSheet sheet, SRow rowobj, int minc, int maxc) {
			if (isEmptyRow(sheet, rowobj)) { //check if no cell at all!
				return null;
			}
			
			final int row = rowobj.getIndex();
			int minr = row;
			int maxr = row;
			boolean allblank = true;

			//initial minc
			final int[] minrng = getCellMinMax(sheet, row, minc);
			if (minrng != null) {
				final int l = minrng[0];
				final int t = minrng[1];
				final int b = minrng[3];
				if (minr > t) minr = t;
				if (maxr < b) maxr = b;
				minc = l;
				allblank = false;
			}
			
			//initial maxc
			if (maxc > (minrng != null ? minrng[2] : minc)) {
				final int[] maxrng = getCellMinMax(sheet, row, maxc);
				if (maxrng != null) {
					final int t = maxrng[1];
					final int r = maxrng[2];
					final int b = maxrng[3];
					if (minr > t) minr = t;
					if (maxr < b) maxr = b;
					maxc = r;
					allblank = false;
				}
			} else if (minrng != null) {
				maxc = minrng[2];
			}
			
			final int lc = sheet.getStartCellIndex(row);
			final int rc = sheet.getEndCellIndex(row);
			final int left = minc > 0 ? minc - 1 : 0;
			final int right = maxc + 1;

			//locate new minc to its left
			for(int c = left; c >= lc; --c) {
				final int[] rng = getCellMinMax(sheet, row, c);
				if (rng != null) {
					final int l = rng[0];
					final int t = rng[1];
					final int b = rng[3];
					minc = c = l;
					if (minr > t) minr = t;
					if (maxr < b) maxr = b;
					allblank = false;
				} else {
					break;
				}
			}
			//locate new maxc to its right
			for(int c = right; c <= rc; ++c) {
				final int[] rng = getCellMinMax(sheet, row, c);
				if (rng != null) {
					final int t = rng[1];
					final int r = rng[2];
					final int b = rng[3];
					maxc = c = r;
					if (minr > t) minr = t;
					if (maxr < b) maxr = b;
					allblank = false;
				} else {
					break;
				}
			}
			//ZSS-901
			if (allblank) {
				//allblank = !(at least one cell within minc--maxc)
				//!((minc <= lc && lc <= maxc) || (minc <= rc && rc <= maxc));
				allblank = (minc > lc || lc > maxc) && (minc > rc || rc > maxc); 
			}
			return allblank ? null : new int[] {minc, minr, maxc, maxr};
		}
		
		private int[] getCellMinMax(SSheet sheet, int row, int col) {
			//ZSS-988: as long as out of region, deemed as blank
			if (tlm > row || blm < row || llm > col || rlm < col)
				return null;
			
			CellRegion rng = sheet.getMergedRegion(row,col);
			if(rng==null){
				rng = new CellRegion(row,col,row,col);
			}
			final int t = rng.getRow();
			final int l = rng.getColumn();
			final int b = rng.getLastRow();
			final int r = rng.getLastColumn();
			final SCell cell = sheet.getCell(t, l);
			return isBlank(cell) ? null : new int[] {l, t, r, b};
		}
		
		private CellRegion getRowCurrentRegion(SSheet sheet,int topRow, int btmRow) {
			int minc = 0;
			int maxc = 0;
			int minr = topRow;
			int maxr = btmRow;
			final int lastCellIdx = sheet.getEndCellIndex(topRow);
			for (int c = minc; c <= lastCellIdx; c++) {
				boolean foundMax = false;
				for (int r = minr + 1; r <= sheet.getEndRowIndex(); r++) {
					int[] cellMinMax = getCellMinMax(sheet, r, c);
					if (cellMinMax == null && r >= btmRow) {
						break;
					}
					if (cellMinMax != null) {
						foundMax = true;
						maxr = Math.max(maxr, cellMinMax[3]);
					}
				}
				if (foundMax) {
					maxc = c;
				}
			}

			return new CellRegion(minr, minc, maxr, maxc);
		}
	}
}
