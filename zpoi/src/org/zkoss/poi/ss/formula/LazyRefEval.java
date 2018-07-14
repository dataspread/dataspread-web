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
import org.zkoss.poi.ss.formula.eval.HyperlinkEval;
import org.zkoss.poi.ss.formula.eval.RefEvalBase;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.ss.usermodel.Hyperlink;

/**
*
* @author Josh Micich
* @author henrichen@zkoss.org: HYPERLINK function
*/
final public class LazyRefEval extends RefEvalBase implements HyperlinkEval {

	private final SheetRefEvaluator _evaluator;

	public LazyRefEval(int rowIndex, int columnIndex, boolean rowRel, boolean colRel, SheetRefEvaluator sre) {
		super(rowIndex, columnIndex, rowRel, colRel);
		if (sre == null) {
			throw new IllegalArgumentException("sre must not be null");
		}
		_evaluator = sre;
	}
	
	public ValueEval getInnerValueEval() {
		return _evaluator.getEvalForCell(getRow(), getColumn());
	}

	public AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx) {

		AreaI area = new OffsetArea(getRow(), getColumn(),
				relFirstRowIx, relLastRowIx, relFirstColIx, relLastColIx);

		return new LazyAreaEval(area, _evaluator);
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
		CellReference cr = new CellReference(getRow(), getColumn());
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append("[");
		final String bookName = _evaluator.getBookName();
		if (bookName != null) {
			sb.append('[').append(bookName).append(']');
		}
		sb.append(_evaluator.getSheetName());
		if (!getSheetName().equals(getLastSheetName())) {
			sb.append(':').append(_evaluator.getLastSheetName());
		}
		sb.append('!');
		sb.append(cr.formatAsString());
		sb.append("]");
		return sb.toString();
	}

	//20100720, henrichen@zkoss.org: handle HYPERLINK function
	private Hyperlink _hyperlink;
	public void setHyperlink(Hyperlink hyperlink) {
		_hyperlink = hyperlink;
	}
	
	public Hyperlink getHyperlink() {
		return _hyperlink;
	}

	//ZSS-962
	@Override
	public boolean isHidden() {
		return _evaluator.isHidden(_evaluator.getSheetIndex(), getRow(), getColumn());
	}
}
