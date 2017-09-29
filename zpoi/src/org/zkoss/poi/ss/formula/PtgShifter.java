/* FormulaShifter.java

	Purpose:
		
	Description:
		
	History:
		Jun 2, 2010 2:31:25 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

*/

package org.zkoss.poi.ss.formula;

import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.formula.ptg.*;

/**
 * @author henrichen
 *
 */
public class PtgShifter {

	/**
	 * Extern sheet index of sheet where moving is occurring
	 */
	private final int _externSheetIndex;
	private final int _firstRow;
	private final int _lastRow;
	private final int _rowAmount;
	private final int _firstCol;
	private final int _lastCol;
	private final int _colAmount;
	private final SpreadsheetVersion _ver;

	public PtgShifter(int externSheetIndex, int firstRow, int lastRow, int rowAmount, int firstCol, int lastCol, int colAmount, SpreadsheetVersion ver) {
		if (firstRow > lastRow) {
			throw new IllegalArgumentException("firstRow("+firstRow+") and lastRow("+lastRow+") out of order");
		}
		if (firstCol > lastCol) {
			throw new IllegalArgumentException("firstCol("+firstCol+") and lastCol("+lastCol+") out of order");
		}
		_externSheetIndex = externSheetIndex;
		_firstRow = firstRow;
		_lastRow = lastRow;
		_rowAmount = rowAmount;
		_firstCol = firstCol;
		_lastCol = lastCol;
		_colAmount = colAmount;
		_ver = ver;
	}

	public static Ptg createDeletedRef(Ptg ptg) {
		if (ptg instanceof RefPtg) {
			return new RefErrorPtg();
		}
		if (ptg instanceof Ref3DPtg) {
			Ref3DPtg rptg = (Ref3DPtg) ptg;
			return new DeletedRef3DPtg(rptg.getExternSheetIndex());
		}
		if (ptg instanceof AreaPtg) {
			return new AreaErrPtg();
		}
		if (ptg instanceof Area3DPtg) {
			Area3DPtg area3DPtg = (Area3DPtg) ptg;
			return new DeletedArea3DPtg(area3DPtg.getExternSheetIndex());
		}
		//ZSS-985
		if (ptg instanceof TablePtg) {
			return new AreaErrPtg();
		}

		throw new IllegalArgumentException("Unexpected ref ptg class (" + ptg.getClass().getName() + ")");
	}

	//ZSS-759
	public static Ptg createDeletedRef3d(String bookName, Ptg ptg) {
		if (ptg instanceof Ref3DPtg) {
			Ref3DPtg rptg = (Ref3DPtg) ptg;
			return new DeletedRef3DPtg(rptg.getExternSheetIndex(), rptg, bookName);
		}
		if (ptg instanceof Area3DPtg) {
			Area3DPtg area3DPtg = (Area3DPtg) ptg;
			return new DeletedArea3DPtg(area3DPtg.getExternSheetIndex(), area3DPtg, bookName);
		}

		throw new IllegalArgumentException("Unexpected ref ptg class (" + ptg.getClass().getName() + ")");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(getClass().getName());
		sb.append(" [");
		sb.append(_firstRow);
		sb.append(",").append(_lastRow);
		sb.append(",").append(_rowAmount);
		sb.append(",").append(_firstCol);
		sb.append(",").append(_lastCol);
		sb.append(",").append(_colAmount);
		return sb.toString();
	}

	/**
	 * @param ptgs - if necessary, will get modified by this method
	 * @param currentExternSheetIx - the extern sheet index of the sheet that contains the formula being adjusted
	 * @return <code>true</code> if a change was made to the formula tokens
	 */
	public boolean adjustFormula(Ptg[] ptgs, int currentExternSheetIx) {
		if (_rowAmount == 0 && _colAmount == 0) {
			return false;
		}
		boolean refsWereChanged = false;
		for(int i=0; i<ptgs.length; i++) {
			Ptg newPtg = adjustPtg(ptgs[i], currentExternSheetIx);
			if (newPtg != null) {
				refsWereChanged = true;
				ptgs[i] = newPtg;
			}
		}
		return refsWereChanged;
	}

	private Ptg adjustPtg(Ptg ptg, int currentExternSheetIx) {
		if (_rowAmount == 0 && _colAmount == 0) {
			return null;
		}
		if(ptg instanceof RefPtg) {
			if (currentExternSheetIx != _externSheetIndex) {
				// local refs on other sheets are unaffected
				return null;
			}
			final RefPtg rptg = (RefPtg)ptg;
			if (_rowAmount != 0 && _colAmount != 0) {
				return bothMoveRefPtg(rptg);
			} else if (_rowAmount != 0) {
				return rowMoveRefPtg(rptg);
			} else {
				return colMoveRefPtg(rptg);
			}
		}
		if(ptg instanceof Ref3DPtg) {
			final Ref3DPtg rptg = (Ref3DPtg)ptg;
			if (_externSheetIndex != rptg.getExternSheetIndex()) {
				// only move 3D refs that refer to the sheet with cells being moved
				// (currentExternSheetIx is irrelevant)
				return null;
			}
			if (_rowAmount != 0 && _colAmount != 0) {
				return bothMoveRefPtg(rptg);
			} else if (_rowAmount != 0) {
				return rowMoveRefPtg(rptg);
			} else {
				return colMoveRefPtg(rptg);
			}
		}
		if(ptg instanceof Area2DPtgBase) {
			if (currentExternSheetIx != _externSheetIndex) {
				// local refs on other sheets are unaffected
				return null;
			}
			final Area2DPtgBase aptg = (Area2DPtgBase)ptg;
			if (_rowAmount != 0 && _colAmount != 0) {
				return bothMoveAreaPtg(aptg);
			} else if (_rowAmount != 0) {
				return rowMoveAreaPtg(aptg);
			} else {
				return colMoveAreaPtg(aptg);
			}
		}
		if(ptg instanceof Area3DPtg) {
			final Area3DPtg aptg = (Area3DPtg)ptg;
			if (_externSheetIndex != aptg.getExternSheetIndex()) {
				// only move 3D refs that refer to the sheet with cells being moved
				// (currentExternSheetIx is irrelevant)
				return null;
			}
			if (_rowAmount != 0 && _colAmount != 0) {
				return bothMoveAreaPtg(aptg);
			} else if (_rowAmount != 0) {
				return rowMoveAreaPtg(aptg);
			} else {
				return colMoveAreaPtg(aptg);
			}
		}
		//ZSS-985
		if(ptg instanceof TablePtg) {
			if (currentExternSheetIx != _externSheetIndex) {
				// local refs on other sheets are unaffected
				return null;
			}
			final TablePtg aptg = (TablePtg)ptg;
			if (_rowAmount != 0 && _colAmount != 0) {
				return bothMoveAreaPtg(aptg);
			} else if (_rowAmount != 0) {
				return rowMoveAreaPtg(aptg);
			} else {
				return colMoveAreaPtg(aptg);
			}
		}
		return null;
	}

	private Ptg rowMoveRefPtg(RefPtgBase rptg) {
		final int refCol = rptg.getColumn();
		if (_firstCol > refCol || refCol > _lastCol) { //out of the boundary
			return null;
		}
		int refRow = rptg.getRow();
		if (_firstRow <= refRow && refRow <= _lastRow) {
			// Rows being moved completely enclose the ref.
			// - move the area ref along with the rows regardless of destination
			//rptg.setRow(refRow + _amountToMove);
			//return rptg;
			return rptgSetRow(rptg, refRow + _rowAmount);
		}
		// else rules for adjusting area may also depend on the destination of the moved rows

		int destFirstRowIndex = _firstRow + _rowAmount;
		int destLastRowIndex = _lastRow + _rowAmount;

		// ref is outside source rows
		// check for clashes with destination

		if (destLastRowIndex < refRow || refRow < destFirstRowIndex) {
			// destination rows are completely outside ref
			return null;
		}

		if (destFirstRowIndex <= refRow && refRow <= destLastRowIndex) {
			// destination rows enclose the area (possibly exactly)
			return createDeletedRef(rptg);
		}
		throw new IllegalStateException("Situation not covered: (" + _firstRow + ", " +
					_lastRow + ", " + _rowAmount + ", " + refRow + ")");
	}
	
	private Ptg rowMoveAreaPtg(AreaPtgBase aptg) {
		final int aFirstCol = aptg.getFirstColumn();
		final int aLastCol = aptg.getLastColumn();
		if (aFirstCol < _firstCol|| aLastCol > _lastCol) { //not total cover
			return null;
		}
		int aFirstRow = aptg.getFirstRow();
		int aLastRow = aptg.getLastRow();
		if (_firstRow <= aFirstRow && aLastRow <= _lastRow) {
			// Rows being moved completely enclose the area ref.
			// - move the area ref along with the rows regardless of destination
			//aptg.setFirstRow(aFirstRow + _amountToMove);
			//aptg.setLastRow(aLastRow + _amountToMove);
			//return aptg;
			return aptgSetFirstLastRow(aptg, aFirstRow + _rowAmount, aLastRow + _rowAmount);
		}
		// else rules for adjusting area may also depend on the destination of the moved rows

		int destFirstRowIndex = _firstRow + _rowAmount;
		int destLastRowIndex = _lastRow + _rowAmount;

		if (aFirstRow < _firstRow && _lastRow < aLastRow) {
			// Rows moved were originally *completely* within the area ref

			// If the destination of the rows overlaps either the top
			// or bottom of the area ref there will be a change
			if (destFirstRowIndex < aFirstRow && aFirstRow <= destLastRowIndex) {
				// truncate the top of the area by the moved rows
				//aptg.setFirstRow(destLastRowIndex+1);
				//return aptg;
				return aptgSetFirstRow(aptg, destLastRowIndex+1);
			} else if (destFirstRowIndex <= aLastRow && aLastRow < destLastRowIndex) {
				// truncate the bottom of the area by the moved rows
				//aptg.setLastRow(destFirstRowIndex-1);
				return aptgSetLastRow(aptg, destFirstRowIndex-1);
			}
			// else - rows have moved completely outside the area ref,
			// or still remain completely within the area ref
			return null; // - no change to the area
		}
		if (_firstRow <= aFirstRow && aFirstRow <= _lastRow) {
			// Rows moved include the first row of the area ref, but not the last row
			// btw: (aLastRow > _lastMovedIndex)
			if (_rowAmount < 0) {
				// simple case - expand area by shifting top upward
				//aptg.setFirstRow(aFirstRow + _amountToMove);
				//return aptg;
				return aptgSetFirstRow(aptg, aFirstRow + _rowAmount);
			}
			if (destFirstRowIndex > aLastRow) {
				// in this case, excel ignores the row move
				return null;
			}
			int newFirstRowIx = aFirstRow + _rowAmount;
			if (destLastRowIndex < aLastRow) {
				// end of area is preserved (will remain exact same row)
				// the top area row is moved simply
				//aptg.setFirstRow(newFirstRowIx);
				//return aptg;
				return aptgSetFirstRow(aptg, newFirstRowIx);
			}
			// else - bottom area row has been replaced - both area top and bottom may move now
			int areaRemainingTopRowIx = _lastRow + 1;
			if (destFirstRowIndex > areaRemainingTopRowIx) {
				// old top row of area has moved deep within the area, and exposed a new top row
				newFirstRowIx = areaRemainingTopRowIx;
			}
			//aptg.setFirstRow(newFirstRowIx);
			//aptg.setLastRow(Math.max(aLastRow, destLastRowIndex));
			//return aptg;
			return aptgSetFirstLastRow(aptg, newFirstRowIx, Math.max(aLastRow, destLastRowIndex));
		}
		if (_firstRow <= aLastRow && aLastRow <= _lastRow) {
			// Rows moved include the last row of the area ref, but not the first
			// btw: (aFirstRow < _firstMovedIndex)
			if (_rowAmount > 0) {
				// simple case - expand area by shifting bottom downward
				//aptg.setLastRow(aLastRow + _amountToMove);
				return aptgSetLastRow(aptg, aLastRow + _rowAmount);
			}
			if (destLastRowIndex < aFirstRow) {
				// in this case, excel ignores the row move
				return null;
			}
			int newLastRowIx = aLastRow + _rowAmount;
			if (destFirstRowIndex > aFirstRow) {
				// top of area is preserved (will remain exact same row)
				// the bottom area row is moved simply
				//aptg.setLastRow(newLastRowIx);
				return aptgSetLastRow(aptg, newLastRowIx);
			}
			// else - top area row has been replaced - both area top and bottom may move now
			int areaRemainingBottomRowIx = _firstRow - 1;
			if (destLastRowIndex < areaRemainingBottomRowIx) {
				// old bottom row of area has moved up deep within the area, and exposed a new bottom row
				newLastRowIx = areaRemainingBottomRowIx;
			}
			//aptg.setFirstRow(Math.min(aFirstRow, destFirstRowIndex));
			//aptg.setLastRow(newLastRowIx);
			//return aptg;
			return aptgSetFirstLastRow(aptg, Math.min(aFirstRow, destFirstRowIndex), newLastRowIx);
		}
		// else source rows include none of the rows of the area ref
		// check for clashes with destination

		if (destLastRowIndex < aFirstRow || aLastRow < destFirstRowIndex) {
			// destination rows are completely outside area ref
			return null;
		}

		if (destFirstRowIndex <= aFirstRow && aLastRow <= destLastRowIndex) {
			// destination rows enclose the area (possibly exactly)
			return createDeletedRef(aptg);
		}

		if (aFirstRow <= destFirstRowIndex && destLastRowIndex <= aLastRow) {
			// destination rows are within area ref (possibly exact on top or bottom, but not both)
			return null; // - no change to area
		}

		if (destFirstRowIndex < aFirstRow && aFirstRow <= destLastRowIndex) {
			// dest rows overlap top of area
			// - truncate the top
			//aptg.setFirstRow(destLastRowIndex+1);
			//return aptg;
			return aptgSetFirstRow(aptg, destLastRowIndex+1);
		}
		if (destFirstRowIndex <= aLastRow && aLastRow < destLastRowIndex) { //20101201, henrichen@zkoss.org: correct the logic
			// dest rows overlap bottom of area
			// - truncate the bottom
			//aptg.setLastRow(destFirstRowIndex-1);
			return aptgSetLastRow(aptg, destFirstRowIndex-1);
		}
		throw new IllegalStateException("Situation not covered: (" + _firstRow + ", " +
					_lastRow + ", " + _rowAmount + ", " + aFirstRow + ", " + aLastRow + ", "+ destFirstRowIndex + ", " + destLastRowIndex + ")");
	}
	
	private Ptg bothMoveRefPtg(RefPtgBase rptg) {
		final int refCol = rptg.getColumn();
		final int refRow = rptg.getRow();
		if (_firstRow <= refRow && refRow <= _lastRow && _firstCol <= refCol && refCol <= _lastCol) {
			// ptg being moved completely enclose the ref.
			// - move the area ref along with the rows/columns regardless of destination
			rptgSetRowCol(rptg, refRow + _rowAmount, refCol + _colAmount);
 			return rptg;
		}
		// else rules for adjusting area may also depend on the destination of the moved rows

		final int destFirstRowIndex = _firstRow + _rowAmount;
		final int destLastRowIndex = _lastRow + _rowAmount;
		final int destFirstColIndex = _firstCol + _colAmount;
		final int destLastColIndex = _lastCol + _colAmount;

		// ref is outside source area
		// check for clashes with destination

		if (destLastRowIndex < refRow || refRow < destFirstRowIndex
			|| destLastColIndex < refCol || refCol < destFirstColIndex) {
			// destination row/col are completely outside ref
			return null;
		}

		if (destFirstRowIndex <= refRow && refRow <= destLastRowIndex
			&& destFirstColIndex <= refCol && refCol <= destLastColIndex) {
			// destination rows enclose the area (possibly exactly)
			return createDeletedRef(rptg);
		}
		throw new IllegalStateException("Situation not covered: row(" + _firstRow + ", " +
					_lastRow + ", " + _rowAmount + ", " + refRow + "), column(" + _firstCol + ", " +
					_lastCol + ", " + _colAmount + ", " + refCol + ")");
	}

	private Ptg bothMoveAreaPtg(AreaPtgBase aptg) {
		final int aFirstCol = aptg.getFirstColumn();
		final int aLastCol = aptg.getLastColumn();
		final int aFirstRow = aptg.getFirstRow();
		final int aLastRow = aptg.getLastRow();
		if (_firstRow <= aFirstRow && aLastRow <= _lastRow && _firstCol <= aFirstCol && aLastCol <= _lastCol) {
			// Rows being moved completely enclose the area ref.
			// - move the area ref along with the rows regardless of destination
			return aptgSetRowCol(aptg, aFirstRow + _rowAmount, aFirstCol + _colAmount, aLastRow + _rowAmount, aLastCol + _colAmount);
		}
		// else rules for adjusting area may also depend on the destination of the moved rows

		int destFirstRowIndex = _firstRow + _rowAmount;
		int destLastRowIndex = _lastRow + _rowAmount;
		int destFirstColIndex = _firstCol + _colAmount;
		int destLastColIndex = _lastCol + _colAmount;

		// else source rows include none of the rows of the area ref
		// check for clashes with destination

		if (destLastRowIndex < aFirstRow || aLastRow < destFirstRowIndex
			|| destLastColIndex < aFirstCol || aLastCol < destFirstColIndex) {
			// destination rows are completely outside area ref
			return null;
		}

		if (destFirstRowIndex <= aFirstRow && aLastRow <= destLastRowIndex
			&& destFirstColIndex <= aFirstCol && aLastCol <= destLastColIndex) {
			// destination rows/columns enclose the area (possibly exactly)
			return createDeletedRef(aptg);
		}

		if ((aFirstRow <= destFirstRowIndex && destLastRowIndex <= aLastRow)
			|| (aFirstCol <= destFirstColIndex && destLastColIndex <= aLastCol)) {
			// destination rows are within area ref (possibly exact on top or bottom, but not both)
			return null; // - no change to area
		}

		if (destFirstColIndex <= aFirstCol && aLastCol <= destLastColIndex) {
			if (destFirstRowIndex < aFirstRow && aFirstRow <= destLastRowIndex) {
				// dest rows overlap top of area
				// - truncate the top
				return aptgSetFirstRow(aptg, destLastRowIndex+1);
			}
			if (destFirstRowIndex < aLastRow && aLastRow <= destLastRowIndex) {
				// dest rows overlap bottom of area
				// - truncate the bottom
				return aptgSetLastRow(aptg, destFirstRowIndex-1);
			}
		}
		if( destFirstRowIndex <= aFirstRow && aLastRow <= destLastRowIndex) {
			if (destFirstColIndex < aFirstCol && aFirstCol <= destLastColIndex) {
				// dest columns overlap left of area
				// - truncate the left
				return aptgSetFirstCol(aptg, destLastColIndex+1);
			}
			if (destFirstColIndex < aLastCol && aLastCol <= destLastColIndex) {
				// dest columns overlap right of area
				// - truncate the right
				return aptgSetLastCol(aptg, destFirstColIndex-1);
			}
		}
		return null;
	}

	private Ptg colMoveRefPtg(RefPtgBase rptg) {
		final int refRow = rptg.getRow();
		if (_firstRow > refRow || refRow > _lastRow) { //out of the boundary
			return null;
		}
		int refCol = rptg.getColumn();
		if (_firstCol <= refCol && refCol <= _lastCol) {
			// Cols being moved completely enclose the ref.
			// - move the area ref along with the cols regardless of destination
			//rptg.setCol(refCol + _amountToMove);
			//return rptg;
			return rptgSetCol(rptg, refCol + _colAmount);
		}
		// else rules for adjusting area may also depend on the destination of the moved cols

		int destFirstColIndex = _firstCol + _colAmount;
		int destLastColIndex = _lastCol + _colAmount;

		// ref is outside source cols
		// check for clashes with destination

		if (destLastColIndex < refCol || refCol < destFirstColIndex) {
			// destination cols are completely outside ref
			return null;
		}

		if (destFirstColIndex <= refCol && refCol <= destLastColIndex) {
			// destination cols enclose the area (possibly exactly)
			return createDeletedRef(rptg);
		}
		throw new IllegalStateException("Situation not covered: (" + _firstCol + ", " +
					_lastCol + ", " + _colAmount + ", " + refCol + ")");
	}

	private Ptg colMoveAreaPtg(AreaPtgBase aptg) {
		final int aFirstRow = aptg.getFirstRow();
		final int aLastRow = aptg.getLastRow();
		if (aFirstRow < _firstRow || aLastRow > _lastRow) { //not total cover
			return null;
		}
		int aFirstCol = aptg.getFirstColumn();
		int aLastCol = aptg.getLastColumn();
		if (_firstCol <= aFirstCol && aLastCol <= _lastCol) {
			// Cols being moved completely enclose the area ref.
			// - move the area ref along with the cols regardless of destination
			return aptgSetFirstLastCol(aptg, aFirstCol + _colAmount, aLastCol + _colAmount);
		}
		// else rules for adjusting area may also depend on the destination of the moved cols

		int destFirstColIndex = _firstCol + _colAmount;
		int destLastColIndex = _lastCol + _colAmount;

		if (aFirstCol < _firstCol && _lastCol < aLastCol) {
			// Cols moved were originally *completely* within the area ref

			// If the destination of the cols overlaps either the left
			// or right of the area ref there will be a change
			if (destFirstColIndex < aFirstCol && aFirstCol <= destLastColIndex) {
				// truncate the left of the area by the moved cols
				return aptgSetFirstCol(aptg, destLastColIndex+1);
			} else if (destFirstColIndex <= aLastCol && aLastCol < destLastColIndex) {
				// truncate the right of the area by the moved cols
				return aptgSetLastCol(aptg, destFirstColIndex-1);
			}
			// else - cols have moved completely outside the area ref,
			// or still remain completely within the area ref
			return null; // - no change to the area
		}
		if (_firstCol <= aFirstCol && aFirstCol <= _lastCol) {
			// Cols moved include the first col of the area ref, but not the last col
			// btw: (aLastCol > _lastMovedIndex)
			if (_colAmount < 0) {
				// simple case - expand area by shifting left leftward
				return aptgSetFirstCol(aptg, aFirstCol + _colAmount);
			}
			if (destFirstColIndex > aLastCol) {
				// in this case, excel ignores the col move
				return null;
			}
			int newFirstColIx = aFirstCol + _colAmount;
			if (destLastColIndex < aLastCol) {
				// end of area is preserved (will remain exact same col)
				// the left area col is moved simply
				return aptgSetFirstCol(aptg, newFirstColIx);
			}
			// else - right area col has been replaced - both area left and right may move now
			int areaRemainingTopColIx = _lastCol + 1;
			if (destFirstColIndex > areaRemainingTopColIx) {
				// old left col of area has moved deep within the area, and exposed a new left col
				newFirstColIx = areaRemainingTopColIx;
			}
			return aptgSetFirstLastCol(aptg, newFirstColIx, Math.max(aLastCol, destLastColIndex));
		}
		if (_firstCol <= aLastCol && aLastCol <= _lastCol) {
			// Cols moved include the last col of the area ref, but not the first
			// btw: (aFirstCol < _firstMovedIndex)
			if (_colAmount > 0) {
				// simple case - expand area by shifting right rightward
				return aptgSetLastCol(aptg, aLastCol + _colAmount);
			}
			if (destLastColIndex < aFirstCol) {
				// in this case, excel ignores the col move
				return null;
			}
			int newLastColIx = aLastCol + _colAmount;
			if (destFirstColIndex > aFirstCol) {
				// left of area is preserved (will remain exact same col)
				// the right area col is moved simply
				return aptgSetLastCol(aptg, newLastColIx);
			}
			// else - top area col has been replaced - both area top and bottom may move now
			int areaRemainingBottomColIx = _firstCol - 1;
			if (destLastColIndex < areaRemainingBottomColIx) {
				// old right col of area has moved left deep within the area, and exposed a new right col
				newLastColIx = areaRemainingBottomColIx;
			}
			return aptgSetFirstLastCol(aptg, Math.min(aFirstCol, destFirstColIndex), newLastColIx);
		}
		// else source cols include none of the cols of the area ref
		// check for clashes with destination

		if (destLastColIndex < aFirstCol || aLastCol < destFirstColIndex) {
			// destination cols are completely outside area ref
			return null;
		}

		if (destFirstColIndex <= aFirstCol && aLastCol <= destLastColIndex) {
			// destination cols enclose the area (possibly exactly)
			return createDeletedRef(aptg);
		}

		if (aFirstCol <= destFirstColIndex && destLastColIndex <= aLastCol) {
			// destination cols are within area ref (possibly exact on left or right, but not both)
			return null; // - no change to area
		}

		if (destFirstColIndex < aFirstCol && aFirstCol <= destLastColIndex) {
			// dest cols overlap left of area
			// - truncate the left
			return aptgSetFirstCol(aptg, destLastColIndex+1);
		}
		if (destFirstColIndex <= aLastCol && aLastCol < destLastColIndex) { //20101202, henrichen@zkoss.org: correct the logic
			// dest cols overlap right of area
			// - truncate the right
			return aptgSetLastCol(aptg, destFirstColIndex-1);
		}
		throw new IllegalStateException("Situation not covered: (" + _firstCol + ", " +
					_lastRow + ", " + _colAmount + ", " + aFirstCol + ", " + aLastCol + ", " + destFirstColIndex + ", " + destLastColIndex+ ")");
	}

	private Ptg rptgSetRow(RefPtgBase rptg, int rowNum) {
		if (rowNum > _ver.getLastRowIndex() || rowNum < 0) {
			return createDeletedRef(rptg); //out of bound
		} else {
			rptg.setRow(rowNum);
			return rptg;
		}
	}

	private Ptg rptgSetCol(RefPtgBase rptg, int colNum) {
		if (colNum > _ver.getLastColumnIndex() || colNum < 0) {
			return createDeletedRef(rptg); //out of bound
		} else {
			rptg.setColumn(colNum);
			return rptg;
		}
	}

	private Ptg rptgSetRowCol(RefPtgBase rptg, int rowNum, int colNum) {
		if (rowNum > _ver.getLastRowIndex() || rowNum < 0) {
			return createDeletedRef(rptg); //out of bound
		} else {
			rptg.setRow(rowNum);
		}
		if (colNum > _ver.getLastColumnIndex() || colNum < 0) {
			return createDeletedRef(rptg); //out of bound
		} else {
			rptg.setColumn(colNum);
		}
		return rptg;
	}

	private Ptg aptgSetFirstRow(AreaPtgBase aptg, int rowNum) {
		if (rowNum > _ver.getLastRowIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else if (rowNum < 0) {
			aptg.setFirstRow(0);
		} else {
			aptg.setFirstRow(rowNum);
		}
		return aptg;
	}

	private Ptg aptgSetFirstCol(AreaPtgBase aptg, int colNum) {
		if (colNum > _ver.getLastColumnIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else if (colNum < 0) {
			aptg.setFirstColumn(0);
		} else {
			aptg.setFirstColumn(colNum);
		}
		return aptg;
	}

	private Ptg aptgSetLastRow(AreaPtgBase aptg, int rowNum) {
		if (rowNum < 0) {
			return createDeletedRef(aptg); //out of bound
		} else if (rowNum > _ver.getLastRowIndex()) {
			aptg.setLastRow(_ver.getLastRowIndex());
		} else {
			aptg.setLastRow(rowNum);
		}
		return aptg;
	}

	private Ptg aptgSetLastCol(AreaPtgBase aptg, int colNum) {
		if (colNum < 0) {
			return createDeletedRef(aptg); //out of bound
		} if (colNum > _ver.getLastColumnIndex()) {
			aptg.setLastColumn(_ver.getLastColumnIndex());
		} else {
			aptg.setLastColumn(colNum);
		}
		return aptg;
	}

	private Ptg aptgSetFirstLastRow(AreaPtgBase aptg, int firstRow, int lastRow) {
		if (firstRow > _ver.getLastRowIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else if (firstRow < 0) {
			aptg.setFirstRow(0);
		} else {
			aptg.setFirstRow(firstRow);
		}
		if (lastRow < 0) {
			return createDeletedRef(aptg); //out of bound
		} else if (lastRow > _ver.getLastRowIndex()) {
			aptg.setLastRow(_ver.getLastRowIndex());
		} else {
			aptg.setLastRow(lastRow);
		}
		return aptg;
	}

	private Ptg aptgSetFirstLastCol(AreaPtgBase aptg, int firstCol, int lastCol) {
		if (firstCol > _ver.getLastColumnIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else if (firstCol < 0) {
			aptg.setFirstColumn(0);
		} else {
			aptg.setFirstColumn(firstCol);
		}
		if (lastCol < 0) {
			return createDeletedRef(aptg); //out of bound
		} else if (lastCol > _ver.getLastColumnIndex()) {
			aptg.setLastColumn(_ver.getLastColumnIndex());
		} else {
			aptg.setLastColumn(lastCol);
		}
		return aptg;
	}

	private Ptg aptgSetRowCol(AreaPtgBase aptg, int firstRow, int firstCol, int lastRow, int lastCol) {
		//first & last columns
		if (firstCol > _ver.getLastColumnIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else if (firstCol < 0) {
			aptg.setFirstColumn(0);
		} else {
			aptg.setFirstColumn(firstCol);
		}
		if (lastCol < 0) {
			return createDeletedRef(aptg); //out of bound
		} else if (lastCol > _ver.getLastColumnIndex()) {
			aptg.setLastColumn(_ver.getLastColumnIndex());
		} else {
			aptg.setLastColumn(lastCol);
		}
		//first & last rows
		if (firstRow > _ver.getLastRowIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else if (firstRow < 0) {
			aptg.setFirstRow(0);
		} else {
			aptg.setFirstRow(firstRow);
		}
		if (lastRow < 0) {
			return createDeletedRef(aptg); //out of bound
		} else if (lastRow > _ver.getLastRowIndex()) {
			aptg.setLastRow(_ver.getLastRowIndex());
		} else {
			aptg.setLastRow(lastRow);
		}
		return aptg;
	}
}
