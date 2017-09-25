/* ArrayEval.java

	Purpose:
		
	Description:
		
	History:
		Oct 21, 2010 2:19:12 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.ss.formula.eval;

import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.functions.RelTableUtils;

import java.util.Arrays;

/**
 * Constant value relational table eval.
 * @author tana
 *
 */
public class RelTableEval implements TwoDEval {

	private int _nRows;
	private int _nColumns;
	
	private ValueEval[][] _values;
	private String[] _attributes;

	RelTableEval()
	{

	}

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
			try {
				_attributes[i] = RelTableUtils.attributeString(region[0][i]);
			} catch (EvaluationException e) {
				_attributes[i] = new String();
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
		int nColumns = getWidth();
		String[] tgtAttributes = new String[nColumns];
		System.arraycopy(_attributes, 0, tgtAttributes, 0, nColumns);
		return tgtAttributes;
	}

	public int indexOfAttribute(String key) {
		return Arrays.asList(_attributes).indexOf(key);
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
	public RelTableEval getRow(int rowIndex) {
		int nRows = 1;
		int nColumns = getWidth();
		final ValueEval[][] tgtValues = new ValueEval[nRows][];
		final ValueEval[] dst = new ValueEval[nColumns];
		tgtValues[0] = dst;
		System.arraycopy(_values[rowIndex], 0, dst, 0, nColumns);
		final String[] tgtAttributes = getAttributes();
		return new RelTableEval(tgtValues, tgtAttributes, nRows, nColumns);
	}

	@Override
	public RelTableEval getColumn(int columnIndex) {
		int[] columnIndices = {columnIndex};
		return getColumns(columnIndices);
	}

	public RelTableEval getColumns(int[] columnIndices) {
		int nRows = getHeight();
		int nColumns = columnIndices.length;
		final ValueEval[][] tgtValues = new ValueEval[nRows][];
		for (int r = 0; r < nRows; ++r) {
			final ValueEval[] dst = new ValueEval[nColumns];
			tgtValues[r] = dst;
			for (int c = 0; c < nColumns; ++c) {
				dst[c] = _values[r][columnIndices[c]];
			}
		}
		final String[] tgtAttributes = new String[nColumns];
		for (int c = 0; c < nColumns; ++c) {
			tgtAttributes[c] = _attributes[columnIndices[c]];
		}
		return new RelTableEval(tgtValues, tgtAttributes, nRows, nColumns);
	}

	public RelTableEval rename(String[] attributes) {
		int nRows = getHeight();
		int nColumns = getWidth();
		final ValueEval[][] tgtValues = new ValueEval[nRows][];
		for (int r = 0; r < nRows; ++r) {
			final ValueEval[] dst = new ValueEval[nColumns];
			tgtValues[r] = dst;
			System.arraycopy(_values[r], 0, dst, 0, nColumns);
		}
		return new RelTableEval(tgtValues, attributes, nRows, nColumns);
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
