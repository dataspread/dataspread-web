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
import org.zkoss.poi.ss.formula.ptg.ArrayPtg;

/**
 * Constant value relational table eval.
 * @author tana
 *
 */
public class RelTableEval implements TwoDEval {

	private final int _nRows;
	private final int _nColumns;
	
	private final ValueEval[][] _values;
	private final String[] _attributes;

	public RelTableEval(ValueEval[][] values, String[] attributes, int nRows, int nColumns) {
		_nRows = nRows;
		_nColumns = nColumns;

		_values = values;
		_attributes = attributes;
	}

	/**
	 * assumes first row of region is the schema
	 * @param region
	 * @param nRows
	 * @param nColumns
	 */
	public RelTableEval(ValueEval[][] region, int nRows, int nColumns) {
		_nRows = nRows;
		_nColumns = nColumns;

		_values = new ValueEval[nRows][];
		for (int i = 0; i < nRows; i++) {
			_values[i] = new ValueEval[nColumns];
			System.arraycopy(region[i+1], 0, _values[i], 0, nColumns);
		}
		_attributes = new String[nColumns];
		for (int i = 0; i < nColumns; i++) {
			ValueEval e = region[0][i];
			if (e instanceof StringEval) {
				_attributes[i] = ((StringEval) e).getStringValue();
			}
		}
	}

	@Override
	public ValueEval getValue(int rowIndex, int columnIndex) {
		return _values[rowIndex][columnIndex];
	}

	@Override
	public int getWidth() {
		return _nColumns;
	}

	@Override
	public int getHeight() {
		return _nRows;
	}

	public String[] getAttributes() {
		return _attributes;
	}

	@Override
	public boolean isRow() {
		return _nRows == 1;
	}

	@Override
	public boolean isColumn() {
		return _nColumns == 1;
	}

	@Override
	public TwoDEval getRow(int rowIndex) {
		int nRows = 1;
		int nColumns = getWidth();
		final ValueEval[][] tgtvalues = new ValueEval[nRows][];
		final ValueEval[] dst = new ValueEval[nColumns];
		tgtvalues[0] = dst;
		System.arraycopy(_values[rowIndex], 0, dst, 0, nColumns);
		final String[] dstatt = new String[nColumns];
		System.arraycopy(_attributes, 0, dstatt, 0, nColumns);
		return new RelTableEval(tgtvalues, dstatt, nRows, nColumns);
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
		final String[] dstatt = new String[nColumns];
		dstatt[0] = _attributes[columnIndex];
		return new RelTableEval(tgtvalues, dstatt, nRows, nColumns);
	}

	@Override
	public boolean isSubTotal(int rowIndex, int columnIndex) {
		return false;
	}

	//ZSS-962
	@Override
	public boolean isHidden(int rowIndex, int columnIndex) {
		return false;
	}
}
