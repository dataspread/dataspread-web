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

import org.zkoss.poi.ss.formula.ptg.AreaI;
import org.zkoss.poi.ss.formula.ptg.AreaI.OffsetArea;
import org.zkoss.poi.ss.formula.eval.AreaEval;
import org.zkoss.poi.ss.formula.eval.AreaEvalBase;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.util.CellReference;

import org.zkoss.poi.ss.formula.eval.HyperlinkEval;
import org.zkoss.poi.ss.usermodel.Hyperlink;
/**
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference, HYPERLINK function
 */
public final class LazyAreaEval extends AreaEvalBase implements HyperlinkEval {

	private final SheetRefEvaluator _evaluator;

	LazyAreaEval(AreaI ptg, SheetRefEvaluator evaluator) {
		super(ptg);
		_evaluator = evaluator;
	}

	public LazyAreaEval(
		int firstRowIndex, int firstColumnIndex, 
		int lastRowIndex, int lastColumnIndex,
		boolean firstRowRel, boolean firstColRel, 
		boolean lastRowRel, boolean lastColRel,
		SheetRefEvaluator evaluator) {
		super(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex,
				firstRowRel, firstColRel, lastRowRel, lastColRel);
		_evaluator = evaluator;
	}

	public ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex) {

		//20100915, henrichen@zkoss.org: will not work in Excel 2007 with larger rows and columns
		//int rowIx = (relativeRowIndex + getFirstRow() ) & 0xFFFF;
		//int colIx = (relativeColumnIndex + getFirstColumn() ) & 0x00FF;
		int rowIx = (relativeRowIndex + getFirstRow() );
		int colIx = (relativeColumnIndex + getFirstColumn() );

		return _evaluator.getEvalForCell(rowIx, colIx);
	}

	public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {
		AreaI area = new OffsetArea(getFirstRow(), getFirstColumn(),
				relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);

		return new LazyAreaEval(area, _evaluator);
	}
	public LazyAreaEval getRow(int rowIndex) {
		if (rowIndex >= getHeight()) {
			throw new IllegalArgumentException("Invalid rowIndex " + rowIndex
					+ ".  Allowable range is (0.." + getHeight() + ").");
		}
		int absRowIx = getFirstRow() + rowIndex;
		return new LazyAreaEval(absRowIx, getFirstColumn(), absRowIx, getLastColumn(), 
				isFirstRowRelative(), isFirstColRelative(), isLastRowRelative(), isLastColRelative(), _evaluator);
	}
	public LazyAreaEval getColumn(int columnIndex) {
		if (columnIndex >= getWidth()) {
			throw new IllegalArgumentException("Invalid columnIndex " + columnIndex
					+ ".  Allowable range is (0.." + getWidth() + ").");
		}
		int absColIx = getFirstColumn() + columnIndex;
		return new LazyAreaEval(getFirstRow(), absColIx, getLastRow(), absColIx, 
				isFirstRowRelative(), isFirstColRelative(), isLastRowRelative(), isLastColRelative(), _evaluator);
	}

	public String getSheetName() {
		return _evaluator.getSheetName();
	}
	
	public String getLastSheetName() {
		return _evaluator.getLastSheetName();
	}
	
	public String getBookName() {
		return _evaluator.getBookName();
	}
	
	public String toString() {
		CellReference crA = new CellReference(getFirstRow(), getFirstColumn(), !isFirstRowRelative(), !isFirstColRelative());
		CellReference crB = new CellReference(getLastRow(), getLastColumn(), !isLastRowRelative(), !isLastColRelative());
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append("[");
		final String bookName = _evaluator.getBookName();
		if (bookName != null) {
			sb.append('[').append(bookName).append(']');
		}
		sb.append(_evaluator.getSheetName());
		if (!_evaluator.getSheetName().equals(_evaluator.getLastSheetName())) {
			sb.append(':').append(_evaluator.getLastSheetName());
		}
		sb.append('!');
		sb.append(crA.formatAsString());
		sb.append(':');
		sb.append(crB.formatAsString());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * @return whether cell at rowIndex and columnIndex is a subtotal
	 */
	public boolean isSubTotal(int rowIndex, int columnIndex) {
		// delegate the query to the sheet evaluator which has access to internal ptgs
        return _evaluator.isSubTotal(getFirstRow() + rowIndex, getFirstColumn() + columnIndex);
    }

	//20100720, henrichen@zkoss.org: handle HYPERLINK function
	private Hyperlink _hyperlink;

	public Hyperlink getHyperlink() {
		return _hyperlink;
	}

	public void setHyperlink(Hyperlink hyperlink) {
		_hyperlink = hyperlink;
	}

	//20111125, henrichen@zkoss.org: sheet depth
	@Override
	public int getDepth() {
		return _evaluator.getSheetCount();
	}
	
	//ZSS-962
	@Override
	public boolean isHidden(int rowIndex, int columnIndex) {
		return _evaluator.isHidden(_evaluator.getSheetIndex(), getFirstRow()+rowIndex, getFirstColumn()+columnIndex);
	}

	//ZSS-962
	@Override
	public SheetRefEvaluator getRefEvaluator() {
		return _evaluator;
	}
}
