/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.ss.formula;

import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.poi.ss.SpreadsheetVersion;


/**
 * @author Josh Micich
 */
public final class FormulaShifter {

    static enum ShiftMode {
        Row,
        Sheet
    }

	/**
	 * Extern sheet index of sheet where moving is occurring
	 */
	private final int _externSheetIndex;
	private final int _firstMovedIndex;
	private final int _lastMovedIndex;
	private final int _amountToMove;
	private final boolean _isRow;

    private final int _srcSheetIndex;
    private final int _dstSheetIndex;

    private final ShiftMode _mode;

    /**
     * Create an instance for shifting row.
     *
     * For example, this will be called on {@link org.zkoss.poi.hssf.usermodel.HSSFSheet#shiftRows(int, int, int)} }
     */
	private FormulaShifter(int externSheetIndex, int firstMovedIndex, int lastMovedIndex, int amountToMove, boolean isRow) {
		if (amountToMove == 0) {
			throw new IllegalArgumentException("amountToMove must not be zero");
		}
		if (firstMovedIndex > lastMovedIndex) {
			throw new IllegalArgumentException("firstMovedIndex, lastMovedIndex out of order:"+firstMovedIndex+","+lastMovedIndex);
		}
		_externSheetIndex = externSheetIndex;
		_firstMovedIndex = firstMovedIndex;
		_lastMovedIndex = lastMovedIndex;
		_amountToMove = amountToMove;
        _mode = ShiftMode.Row;

        _srcSheetIndex = _dstSheetIndex = -1;
		_isRow = isRow;
	}

    /**
     * Create an instance for shifting sheets.
     *
     * For example, this will be called on {@link org.zkoss.poi.hssf.usermodel.HSSFWorkbook#setSheetOrder(String, int)}  
     */
    private FormulaShifter(int srcSheetIndex, int dstSheetIndex) {
        _externSheetIndex = _firstMovedIndex = _lastMovedIndex = _amountToMove = -1;
        _isRow = true;

        _srcSheetIndex = srcSheetIndex;
        _dstSheetIndex = dstSheetIndex;
        _mode = ShiftMode.Sheet;
    }

	public static FormulaShifter createForRowShift(int externSheetIndex, int firstMovedRowIndex, int lastMovedRowIndex, int numberOfRowsToMove) {
		return new FormulaShifter(externSheetIndex, firstMovedRowIndex, lastMovedRowIndex, numberOfRowsToMove, true);
	}
	
    public static FormulaShifter createForSheetShift(int srcSheetIndex, int dstSheetIndex) {
        return new FormulaShifter(srcSheetIndex, dstSheetIndex);
    }

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(getClass().getName());
		sb.append(" [");
		sb.append(_firstMovedIndex);
		sb.append(_lastMovedIndex);
		sb.append(_amountToMove);
		sb.append(_isRow);
		return sb.toString();
	}

	/**
	 * @param ptgs - if necessary, will get modified by this method
	 * @param currentExternSheetIx - the extern sheet index of the sheet that contains the formula being adjusted
	 * @return <code>true</code> if a change was made to the formula tokens
	 */
	public boolean adjustFormula(Ptg[] ptgs, int currentExternSheetIx) {
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
		switch(_mode){
            case Row:
                //return adjustPtgDueToRowMove(ptg, currentExternSheetIx);
        		return _isRow ?	adjustPtgDueToRowMove(ptg, currentExternSheetIx):
					adjustPtgDueToColMove(ptg, currentExternSheetIx); //20100525, henrichen@zkoss.org
            case Sheet:
                return adjustPtgDueToShiftMove(ptg);
            default:
                throw new IllegalStateException("Unsupported shift mode: " + _mode);
        }
	}
	/**
	 * @return <code>true</code> if this Ptg needed to be changed
	 */
	private Ptg adjustPtgDueToRowMove(Ptg ptg, int currentExternSheetIx) {
		if(ptg instanceof RefPtg) {
			if (currentExternSheetIx != _externSheetIndex) {
				// local refs on other sheets are unaffected
				return null;
			}
			RefPtg rptg = (RefPtg)ptg;
			return rowMoveRefPtg(rptg);
		}
		if(ptg instanceof Ref3DPtg) {
			Ref3DPtg rptg = (Ref3DPtg)ptg;
			if (_externSheetIndex != rptg.getExternSheetIndex()) {
				// only move 3D refs that refer to the sheet with cells being moved
				// (currentExternSheetIx is irrelevant)
				return null;
			}
			return rowMoveRefPtg(rptg);
		}
		if(ptg instanceof Area2DPtgBase) {
			if (currentExternSheetIx != _externSheetIndex) {
				// local refs on other sheets are unaffected
				return ptg;
			}
			return rowMoveAreaPtg((Area2DPtgBase)ptg);
		}
		if(ptg instanceof Area3DPtg) {
			Area3DPtg aptg = (Area3DPtg)ptg;
			if (_externSheetIndex != aptg.getExternSheetIndex()) {
				// only move 3D refs that refer to the sheet with cells being moved
				// (currentExternSheetIx is irrelevant)
				return null;
			}
			return rowMoveAreaPtg(aptg);
		}
		return null;
	}

    private Ptg adjustPtgDueToShiftMove(Ptg ptg) {
        Ptg updatedPtg = null;
        if(ptg instanceof Ref3DPtg) {
            Ref3DPtg ref = (Ref3DPtg)ptg;
            if(ref.getExternSheetIndex() == _srcSheetIndex){
                ref.setExternSheetIndex(_dstSheetIndex);
                updatedPtg = ref;
            } else if (ref.getExternSheetIndex() == _dstSheetIndex){
                ref.setExternSheetIndex(_srcSheetIndex);
                updatedPtg = ref;
            }
        }
        return updatedPtg;
    }

	private Ptg rowMoveRefPtg(RefPtgBase rptg) {
		int refRow = rptg.getRow();
		if (_firstMovedIndex <= refRow && refRow <= _lastMovedIndex) {
			// Rows being moved completely enclose the ref.
			// - move the area ref along with the rows regardless of destination
			//rptg.setRow(refRow + _amountToMove);
			//return rptg;
			return rptgSetRow(rptg, refRow + _amountToMove);
		}
		// else rules for adjusting area may also depend on the destination of the moved rows

		int destFirstRowIndex = _firstMovedIndex + _amountToMove;
		int destLastRowIndex = _lastMovedIndex + _amountToMove;

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
		throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
					_lastMovedIndex + ", " + _amountToMove + ", " + refRow + ", " + refRow + ")");
	}
	private Ptg rowMoveAreaPtg(AreaPtgBase aptg) {
		int aFirstRow = aptg.getFirstRow();
		int aLastRow = aptg.getLastRow();
		if (_firstMovedIndex <= aFirstRow && aLastRow <= _lastMovedIndex) {
			// Rows being moved completely enclose the area ref.
			// - move the area ref along with the rows regardless of destination
			//aptg.setFirstRow(aFirstRow + _amountToMove);
			//aptg.setLastRow(aLastRow + _amountToMove);
			//return aptg;
			aptgSetLastRow(aptg, aLastRow + _amountToMove);
			return aptgSetFirstRow(aptg, aFirstRow + _amountToMove);
		}
		// else rules for adjusting area may also depend on the destination of the moved rows

		int destFirstRowIndex = _firstMovedIndex + _amountToMove;
		int destLastRowIndex = _lastMovedIndex + _amountToMove;

		if (aFirstRow < _firstMovedIndex && _lastMovedIndex < aLastRow) {
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
				aptgSetLastRow(aptg, destFirstRowIndex-1);
				return aptg;
			}
			// else - rows have moved completely outside the area ref,
			// or still remain completely within the area ref
			return null; // - no change to the area
		}
		if (_firstMovedIndex <= aFirstRow && aFirstRow <= _lastMovedIndex) {
			// Rows moved include the first row of the area ref, but not the last row
			// btw: (aLastRow > _lastMovedIndex)
			if (_amountToMove < 0) {
				// simple case - expand area by shifting top upward
				//aptg.setFirstRow(aFirstRow + _amountToMove);
				//return aptg;
				return aptgSetFirstRow(aptg, aFirstRow + _amountToMove);
			}
			if (destFirstRowIndex > aLastRow) {
				// in this case, excel ignores the row move
				return null;
			}
			int newFirstRowIx = aFirstRow + _amountToMove;
			if (destLastRowIndex < aLastRow) {
				// end of area is preserved (will remain exact same row)
				// the top area row is moved simply
				//aptg.setFirstRow(newFirstRowIx);
				//return aptg;
				return aptgSetFirstRow(aptg, newFirstRowIx);
			}
			// else - bottom area row has been replaced - both area top and bottom may move now
			int areaRemainingTopRowIx = _lastMovedIndex + 1;
			if (destFirstRowIndex > areaRemainingTopRowIx) {
				// old top row of area has moved deep within the area, and exposed a new top row
				newFirstRowIx = areaRemainingTopRowIx;
			}
			//aptg.setFirstRow(newFirstRowIx);
			//aptg.setLastRow(Math.max(aLastRow, destLastRowIndex));
			//return aptg;
			aptgSetLastRow(aptg, Math.max(aLastRow, destLastRowIndex));
			return aptgSetFirstRow(aptg, newFirstRowIx);
		}
		if (_firstMovedIndex <= aLastRow && aLastRow <= _lastMovedIndex) {
			// Rows moved include the last row of the area ref, but not the first
			// btw: (aFirstRow < _firstMovedIndex)
			if (_amountToMove > 0) {
				// simple case - expand area by shifting bottom downward
				//aptg.setLastRow(aLastRow + _amountToMove);
				aptgSetLastRow(aptg, aLastRow + _amountToMove);
				return aptg;
			}
			if (destLastRowIndex < aFirstRow) {
				// in this case, excel ignores the row move
				return null;
			}
			int newLastRowIx = aLastRow + _amountToMove;
			if (destFirstRowIndex > aFirstRow) {
				// top of area is preserved (will remain exact same row)
				// the bottom area row is moved simply
				//aptg.setLastRow(newLastRowIx);
				aptgSetLastRow(aptg, newLastRowIx);
				return aptg;
			}
			// else - top area row has been replaced - both area top and bottom may move now
			int areaRemainingBottomRowIx = _firstMovedIndex - 1;
			if (destLastRowIndex < areaRemainingBottomRowIx) {
				// old bottom row of area has moved up deep within the area, and exposed a new bottom row
				newLastRowIx = areaRemainingBottomRowIx;
			}
			//aptg.setFirstRow(Math.min(aFirstRow, destFirstRowIndex));
			//aptg.setLastRow(newLastRowIx);
			//return aptg;
			aptgSetLastRow(aptg, newLastRowIx);
			return aptgSetFirstRow(aptg, Math.min(aFirstRow, destFirstRowIndex));
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
		if (destFirstRowIndex < aLastRow && aLastRow <= destLastRowIndex) {
			// dest rows overlap bottom of area
			// - truncate the bottom
			//aptg.setLastRow(destFirstRowIndex-1);
			aptgSetLastRow(aptg, destFirstRowIndex-1);
			return aptg;
		}
		throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
					_lastMovedIndex + ", " + _amountToMove + ", " + aFirstRow + ", " + aLastRow + ")");
	}

	private static Ptg createDeletedRef(Ptg ptg) {
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

		throw new IllegalArgumentException("Unexpected ref ptg class (" + ptg.getClass().getName() + ")");
	}
	
	//20100521, henrichen@zkoss.org: check in-bound
	private Ptg rptgSetRow(RefPtgBase rptg, int rowNum) {
		if (rowNum > SpreadsheetVersion.EXCEL97.getLastRowIndex()) {
			return createDeletedRef(rptg); //out of bound
		} else {
			rptg.setRow(rowNum);
			return rptg;
		}
	}
	//20100521, henrichen@zkoss.org: check in-bound
	private void aptgSetLastRow(AreaPtgBase aptg, int rowNum) {
		if (rowNum > SpreadsheetVersion.EXCEL97.getLastRowIndex()) {
			aptg.setLastRow(SpreadsheetVersion.EXCEL97.getLastRowIndex());
		} else {
			aptg.setLastRow(rowNum);
		}
	}
	//20100521, henrichen@zkoss.org: check in-bound
	private Ptg aptgSetFirstRow(AreaPtgBase aptg, int rowNum) {
		if (rowNum > SpreadsheetVersion.EXCEL97.getLastRowIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else {
			aptg.setFirstRow(rowNum);
			return aptg;
		}
	}
	//20100525, henrichen@zkoss.org: add createForColumnShift
	public static FormulaShifter createForColumnShift(int externSheetIndex, int firstMovedColIndex, int lastMovedColIndex, int numberOfColsToMove) {
		return new FormulaShifter(externSheetIndex, firstMovedColIndex, lastMovedColIndex, numberOfColsToMove, false);
	}
	//20100525, henrichen@zkoss.org: add adjustPtgDueToColMove 
	/**
	 * @return <code>true</code> if this Ptg needed to be changed
	 */
	private Ptg adjustPtgDueToColMove(Ptg ptg, int currentExternSheetIx) {
		if(ptg instanceof RefPtg) {
			if (currentExternSheetIx != _externSheetIndex) {
				// local refs on other sheets are unaffected
				return null;
			}
			RefPtg rptg = (RefPtg)ptg;
			return colMoveRefPtg(rptg);
		}
		if(ptg instanceof Ref3DPtg) {
			Ref3DPtg rptg = (Ref3DPtg)ptg;
			if (_externSheetIndex != rptg.getExternSheetIndex()) {
				// only move 3D refs that refer to the sheet with cells being moved
				// (currentExternSheetIx is irrelevant)
				return null;
			}
			return colMoveRefPtg(rptg);
		}
		if(ptg instanceof Area2DPtgBase) {
			if (currentExternSheetIx != _externSheetIndex) {
				// local refs on other sheets are unaffected
				return ptg;
			}
			return colMoveAreaPtg((Area2DPtgBase)ptg);
		}
		if(ptg instanceof Area3DPtg) {
			Area3DPtg aptg = (Area3DPtg)ptg;
			if (_externSheetIndex != aptg.getExternSheetIndex()) {
				// only move 3D refs that refer to the sheet with cells being moved
				// (currentExternSheetIx is irrelevant)
				return null;
			}
			return colMoveAreaPtg(aptg);
		}
		return null;
	}
	//20100525, henrichen@zkoss.org: add colMoveRefPtg
	private Ptg colMoveRefPtg(RefPtgBase rptg) {
		int refCol = rptg.getColumn();
		if (_firstMovedIndex <= refCol && refCol <= _lastMovedIndex) {
			// Cols being moved completely enclose the ref.
			// - move the area ref along with the cols regardless of destination
			//rptg.setCol(refCol + _amountToMove);
			//return rptg;
			return rptgSetCol(rptg, refCol + _amountToMove);
		}
		// else rules for adjusting area may also depend on the destination of the moved cols

		int destFirstColIndex = _firstMovedIndex + _amountToMove;
		int destLastColIndex = _lastMovedIndex + _amountToMove;

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
		throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
					_lastMovedIndex + ", " + _amountToMove + ", " + refCol + ", " + refCol + ")");
	}
	//20100525, henrichen@zkoss.org: add colMoveAreaPtg
	private Ptg colMoveAreaPtg(AreaPtgBase aptg) {
		int aFirstCol = aptg.getFirstColumn();
		int aLastCol = aptg.getLastColumn();
		if (_firstMovedIndex <= aFirstCol && aLastCol <= _lastMovedIndex) {
			// Cols being moved completely enclose the area ref.
			// - move the area ref along with the cols regardless of destination
			//aptg.setFirstCol(aFirstCol + _amountToMove);
			//aptg.setLastCol(aLastCol + _amountToMove);
			//return aptg;
			aptgSetLastCol(aptg, aLastCol + _amountToMove);
			return aptgSetFirstCol(aptg, aFirstCol + _amountToMove);
		}
		// else rules for adjusting area may also depend on the destination of the moved cols

		int destFirstColIndex = _firstMovedIndex + _amountToMove;
		int destLastColIndex = _lastMovedIndex + _amountToMove;

		if (aFirstCol < _firstMovedIndex && _lastMovedIndex < aLastCol) {
			// Cols moved were originally *completely* within the area ref

			// If the destination of the cols overlaps either the top
			// or bottom of the area ref there will be a change
			if (destFirstColIndex < aFirstCol && aFirstCol <= destLastColIndex) {
				// truncate the top of the area by the moved cols
				//aptg.setFirstCol(destLastColIndex+1);
				//return aptg;
				return aptgSetFirstCol(aptg, destLastColIndex+1);
			} else if (destFirstColIndex <= aLastCol && aLastCol < destLastColIndex) {
				// truncate the bottom of the area by the moved cols
				//aptg.setLastCol(destFirstColIndex-1);
				aptgSetLastCol(aptg, destFirstColIndex-1);
				return aptg;
			}
			// else - cols have moved completely outside the area ref,
			// or still remain completely within the area ref
			return null; // - no change to the area
		}
		if (_firstMovedIndex <= aFirstCol && aFirstCol <= _lastMovedIndex) {
			// Cols moved include the first col of the area ref, but not the last col
			// btw: (aLastCol > _lastMovedIndex)
			if (_amountToMove < 0) {
				// simple case - expand area by shifting top upward
				//aptg.setFirstCol(aFirstCol + _amountToMove);
				//return aptg;
				return aptgSetFirstCol(aptg, aFirstCol + _amountToMove);
			}
			if (destFirstColIndex > aLastCol) {
				// in this case, excel ignores the col move
				return null;
			}
			int newFirstColIx = aFirstCol + _amountToMove;
			if (destLastColIndex < aLastCol) {
				// end of area is preserved (will remain exact same col)
				// the top area col is moved simply
				//aptg.setFirstCol(newFirstColIx);
				//return aptg;
				return aptgSetFirstCol(aptg, newFirstColIx);
			}
			// else - bottom area col has been replaced - both area top and bottom may move now
			int areaRemainingTopColIx = _lastMovedIndex + 1;
			if (destFirstColIndex > areaRemainingTopColIx) {
				// old top col of area has moved deep within the area, and exposed a new top col
				newFirstColIx = areaRemainingTopColIx;
			}
			//aptg.setFirstCol(newFirstColIx);
			//aptg.setLastCol(Math.max(aLastCol, destLastColIndex));
			//return aptg;
			aptgSetLastCol(aptg, Math.max(aLastCol, destLastColIndex));
			return aptgSetFirstCol(aptg, newFirstColIx);
		}
		if (_firstMovedIndex <= aLastCol && aLastCol <= _lastMovedIndex) {
			// Cols moved include the last col of the area ref, but not the first
			// btw: (aFirstCol < _firstMovedIndex)
			if (_amountToMove > 0) {
				// simple case - expand area by shifting bottom downward
				//aptg.setLastCol(aLastCol + _amountToMove);
				aptgSetLastCol(aptg, aLastCol + _amountToMove);
				return aptg;
			}
			if (destLastColIndex < aFirstCol) {
				// in this case, excel ignores the col move
				return null;
			}
			int newLastColIx = aLastCol + _amountToMove;
			if (destFirstColIndex > aFirstCol) {
				// top of area is preserved (will remain exact same col)
				// the bottom area col is moved simply
				//aptg.setLastCol(newLastColIx);
				aptgSetLastCol(aptg, newLastColIx);
				return aptg;
			}
			// else - top area col has been replaced - both area top and bottom may move now
			int areaRemainingBottomColIx = _firstMovedIndex - 1;
			if (destLastColIndex < areaRemainingBottomColIx) {
				// old bottom col of area has moved up deep within the area, and exposed a new bottom col
				newLastColIx = areaRemainingBottomColIx;
			}
			//aptg.setFirstCol(Math.min(aFirstCol, destFirstColIndex));
			//aptg.setLastCol(newLastColIx);
			//return aptg;
			aptgSetLastCol(aptg, newLastColIx);
			return aptgSetFirstCol(aptg, Math.min(aFirstCol, destFirstColIndex));
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
			// destination cols are within area ref (possibly exact on top or bottom, but not both)
			return null; // - no change to area
		}

		if (destFirstColIndex < aFirstCol && aFirstCol <= destLastColIndex) {
			// dest cols overlap top of area
			// - truncate the top
			//aptg.setFirstCol(destLastColIndex+1);
			//return aptg;
			return aptgSetFirstCol(aptg, destLastColIndex+1);
		}
		if (destFirstColIndex < aLastCol && aLastCol <= destLastColIndex) {
			// dest cols overlap bottom of area
			// - truncate the bottom
			//aptg.setLastCol(destFirstColIndex-1);
			aptgSetLastCol(aptg, destFirstColIndex-1);
			return aptg;
		}
		throw new IllegalStateException("Situation not covered: (" + _firstMovedIndex + ", " +
					_lastMovedIndex + ", " + _amountToMove + ", " + aFirstCol + ", " + aLastCol + ")");
	}
	//20100525, henrichen@zkoss.org: check in-bound
	private Ptg rptgSetCol(RefPtgBase rptg, int colNum) {
		if (colNum > SpreadsheetVersion.EXCEL97.getLastColumnIndex()) {
			return createDeletedRef(rptg); //out of bound
		} else {
			rptg.setColumn(colNum);
			return rptg;
		}
	}
	//20100525, henrichen@zkoss.org: check in-bound
	private void aptgSetLastCol(AreaPtgBase aptg, int colNum) {
		if (colNum > SpreadsheetVersion.EXCEL97.getLastColumnIndex()) {
			aptg.setLastColumn(SpreadsheetVersion.EXCEL97.getLastColumnIndex());
		} else {
			aptg.setLastColumn(colNum);
		}
	}
	//20100525, henrichen@zkoss.org: check in-bound
	private Ptg aptgSetFirstCol(AreaPtgBase aptg, int colNum) {
		if (colNum > SpreadsheetVersion.EXCEL97.getLastColumnIndex()) {
			return createDeletedRef(aptg); //out of bound
		} else {
			aptg.setFirstColumn(colNum);
			return aptg;
		}
	}
}
