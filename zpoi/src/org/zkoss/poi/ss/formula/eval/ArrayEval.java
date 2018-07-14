/* ArrayEval.java

	Purpose:
		
	Description:
		
	History:
		Oct 21, 2010 2:19:12 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.ss.formula.eval;

import org.zkoss.poi.ss.formula.SheetRefEvaluator;
import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.constant.ErrorConstant;
import org.zkoss.poi.ss.formula.ptg.AreaI;
import org.zkoss.poi.ss.formula.ptg.ArrayPtg;
import org.zkoss.poi.ss.util.NumberToTextConverter;

/**
 * Constant value array eval.
 * @author henrichen
 *
 */
public class ArrayEval implements AreaEval {

	private SheetRefEvaluator _evaluator; //ZSS-962

	private int _firstRow;
	private int _firstCol;
	private int _lastRow;
	private int _lastCol;
	
	private ValueEval[][] _values; //[row][col] -> value

	ArrayEval()
	{

	}

	private ArrayEval(Object[][] srcvalues, int firstRow, int firstColumn, int lastRow, int lastColumn, SheetRefEvaluator evaluator) {
		_evaluator = evaluator; //ZSS-962
		_firstRow = firstRow;
		_firstCol = firstColumn;
		_lastRow = lastRow;
		_lastCol = lastColumn;

		int nRows = getHeight();
		int nColumns = getWidth();
		_values = new ValueEval[nRows][];
		for(int r = 0; r < nRows; ++r) {
			final ValueEval[] dst = new ValueEval[nColumns]; 
			_values[r] = dst;
			for(int c = 0; c < nColumns; ++c) {
				final Object o = srcvalues[r][c];
				_values[r][c] = getValueEval(o); 
			}
		}
	}
	
	public ArrayEval(ValueEval[][] srcvalues, int firstRow, int firstColumn, int lastRow, int lastColumn, SheetRefEvaluator evaluator) {
		_firstRow = firstRow;
		_firstCol = firstColumn;
		_lastRow = lastRow;
		_lastCol = lastColumn;

		_values = srcvalues;
		_evaluator = evaluator; //ZSS-962
	}
	
	public ArrayEval(ArrayPtg ptg, SheetRefEvaluator evaluator) {
		this(ptg.getTokenArrayValues(), 0, 0, ptg.getRowCount() - 1, ptg.getColumnCount() - 1, evaluator); //ZSS-962
	}
	
	private ValueEval getValueEval(Object o) {
		if (o == null) {
			return BlankEval.instance;
		}
		if (o instanceof String) {
			return new StringEval((String) o);
		}
		if (o instanceof Double) {
			return new NumberEval(((Number)o).doubleValue());
		}
		if (o instanceof Boolean) {
			return BoolEval.valueOf(((Boolean)o).booleanValue());
		}
		if (o instanceof ErrorConstant) {
			return ErrorEval.valueOf(((ErrorConstant)o).getErrorCode());
		}
		throw new IllegalArgumentException("Unexpected constant class (" + o.getClass().getName() + ")");
	}

	@Override
	public boolean contains(int row, int col) {
		return _firstRow <= row && row <= _lastRow && _firstCol <= col && col <= _lastCol;
	}

	@Override
	public boolean containsColumn(int col) {
		return _firstCol <= col && col <= _lastCol;
	}

	@Override
	public boolean containsRow(int row) {
		return _firstRow <= row && row <= _lastRow;
	}

	@Override
	public ValueEval getAbsoluteValue(int row, int col) {
		return getRelativeValue(row - _firstRow, col - _firstCol);
	}

	@Override
	public int getFirstColumn() {
		return _firstCol;
	}

	@Override
	public int getFirstRow() {
		return _firstRow;
	}

	@Override
	public int getHeight() {
		return _lastRow - _firstRow + 1;
	}

	@Override
	public int getLastColumn() {
		return _lastCol;
	}

	@Override
	public int getLastRow() {
		return _lastRow;
	}

	@Override
	public ValueEval getRelativeValue(int r, int c) {
		final int row = r + _firstRow;
		final int col = c + _firstCol;
		if(!containsRow(row)) {
			throw new IllegalArgumentException("Specified row index (" + row
					+ ") is outside the allowed range (" + getFirstRow() + "" + getLastRow() + ")");
		}
		if(!containsColumn(col)) {
			throw new IllegalArgumentException("Specified column index (" + col
					+ ") is outside the allowed range (" + getFirstColumn() + "" + getLastColumn() + ")");
		}
		return _values[r][c];
	}

	@Override
	public int getWidth() {
		return _lastCol - _firstCol + 1;
	}

	@Override
	public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TwoDEval getColumn(int columnIndex) {
		int nRows = getHeight();
		int nColumns = 1;
		final ValueEval[][] tgtvalues = new ValueEval[nRows][];
		for(int r = 0; r < nRows; ++r) {
			final ValueEval[] dst = new ValueEval[nColumns]; 
			tgtvalues[r] = dst;
			dst[0] = _values[r][columnIndex];
		}
		return new ArrayEval(tgtvalues, getFirstRow(), columnIndex, getLastRow(), columnIndex, _evaluator);
	}

	@Override
	public TwoDEval getRow(int rowIndex) {
		int nRows = 1;
		int nColumns = getWidth();
		final ValueEval[][] tgtvalues = new ValueEval[nRows][];
		final ValueEval[] dst = new ValueEval[nColumns]; 
		tgtvalues[0] = dst;
		System.arraycopy(_values[rowIndex], 0, dst, 0, nColumns);
		return new ArrayEval(tgtvalues, rowIndex, getFirstColumn(), rowIndex, getLastColumn(), _evaluator);
	}

	@Override
	public ValueEval getValue(int rowIndex, int columnIndex) {
		return getRelativeValue(rowIndex, columnIndex);
	}

	@Override
	public boolean isColumn() {
		return _firstCol == _lastCol;
	}

	@Override
	public boolean isRow() {
		return _firstRow == _lastRow;
	}

	@Override
	public boolean isSubTotal(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public int getDepth() {
		return 1;
	}

	//ZSS-962
	@Override
	public boolean isHidden(int rowIndex, int columnIndex) {
		return _evaluator.isHidden(_evaluator.getSheetIndex(), rowIndex, columnIndex);
	}

	//ZSS-962
	@Override
	public SheetRefEvaluator getRefEvaluator() {
		return _evaluator;
	}
}
