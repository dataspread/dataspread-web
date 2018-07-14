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

package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.BlankEval;
import org.zkoss.poi.ss.formula.eval.BoolEval;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.EvaluationException;
import org.zkoss.poi.ss.formula.eval.HyperlinkEval;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.OperandResolver;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;

/**
 * Implementation of Excel HYPERLINK function.<p/>
 *
 * In Excel this function has special behaviour - it causes the displayed cell value to behave like
 * a hyperlink in the GUI. From an evaluation perspective however, it is very simple.<p/>
 *
 * <b>Syntax</b>:<br/>
 * <b>HYPERLINK</b>(<b>link_location</b>, friendly_name)<p/>
 *
 * <b>link_location</b> The URL of the hyperlink <br/>
 * <b>friendly_name</b> (optional) the value to display<p/>
 *
 *  Returns last argument.  Leaves type unchanged (does not convert to {@link StringEval}).
 *
 * @author Wayne Clingingsmith
 * @author henrichen@zkoss.org: Have to associate a Hyperlink data model so UI knows that is a hyperlink. 
 */
public final class Hyperlink extends Var1or2ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
		//20100719, henrichen@zkoss.org: handle HYPERLINK function
		if (arg0 instanceof HyperlinkEval) {
			((HyperlinkEval)arg0).setHyperlink(new EvalHyperlink(srcRowIndex, srcColumnIndex, arg0, arg0));
		}
		return arg0;
	}
	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		// note - if last arg is MissingArgEval, result will be NumberEval.ZERO,
		// but WorkbookEvaluator does that translation
		
		//20100719, henrichen@zkoss.org: handle HYPERLINK function
		if (arg1 instanceof HyperlinkEval) {
			((HyperlinkEval)arg1).setHyperlink(new EvalHyperlink(srcRowIndex, srcColumnIndex, arg0, arg1));
		}
		return arg1;
	}
	
	private static class EvalHyperlink implements org.zkoss.poi.ss.usermodel.Hyperlink {
		private int _row;
		private int _col;
		private String _address;
		private String _label;
		private int _type;
		private EvalHyperlink(int row, int col, ValueEval addrEval, ValueEval labelEval) {
			_address = (String) evaluateToString(addrEval, row, col);
			_label = (String) evaluateToString(labelEval, row, col);
			final String addr = _address.toLowerCase(); 
			if (addr.startsWith("http://") || addr.startsWith("https://")) {
				_type = org.zkoss.poi.ss.usermodel.Hyperlink.LINK_URL;
			} else if (addr.startsWith("mailto://")) {
				_type = org.zkoss.poi.ss.usermodel.Hyperlink.LINK_EMAIL;
			} else if (addr.indexOf('!') > 0) {
				_type = org.zkoss.poi.ss.usermodel.Hyperlink.LINK_DOCUMENT;
			} else {
				_type = org.zkoss.poi.ss.usermodel.Hyperlink.LINK_FILE;
			}
			_row = row;
			_col = col;
		}
		@Override
		public int getFirstColumn() {
			return _col;
		}

		@Override
		public int getFirstRow() {
			return _row;
		}

		@Override
		public int getLastColumn() {
			return _col;
		}

		@Override
		public int getLastRow() {
			return _row;
		}

		@Override
		public void setFirstColumn(int col) {
			_col = col;
		}

		@Override
		public void setFirstRow(int row) {
			_row = row;
			
		}

		@Override
		public void setLastColumn(int col) {
			setFirstColumn(col);
		}

		@Override
		public void setLastRow(int row) {
			setFirstRow(row);
		}

		@Override
		public String getAddress() {
			return _address;
		}

		@Override
		public String getLabel() {
			return _label;
		}

		@Override
		public int getType() {
			return _type;
		}

		@Override
		public void setAddress(String address) {
			_address = address;
		}

		@Override
		public void setLabel(String label) {
			_label = label;
		}
		
		private ValueEval dereferenceResult(ValueEval eval, int srcRowNum, int srcColNum) {
			ValueEval value;
			try {
				value = OperandResolver.getSingleValue(eval, srcRowNum, srcColNum);
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			if (value == BlankEval.instance) {
				// Note Excel behaviour here. A blank final final value is converted to zero.
				return NumberEval.ZERO;
				// Formulas _never_ evaluate to blank.  If a formula appears to have evaluated to
				// blank, the actual value is empty string. This can be verified with ISBLANK().
			}
			return value;
		}
		
		private Object evaluateToString(ValueEval eval, int row, int col) {
			ValueEval eval0 = dereferenceResult(eval, row, col);
			if (eval0 instanceof NumberEval) {
				NumberEval ne = (NumberEval) eval0;
				return ne.getStringValue();
			}
			if (eval0 instanceof BoolEval) {
				BoolEval be = (BoolEval) eval0;
				return be.getStringValue();
			}
			if (eval0 instanceof StringEval) {
				StringEval ne = (StringEval) eval0;
				return ne.getStringValue();
			}
			if (eval0 instanceof ErrorEval) {
				return ErrorEval.getText(((ErrorEval)eval0).getErrorCode());
			}
			throw new RuntimeException("Unexpected eval class (" + eval0.getClass().getName() + ")");
		}
	}
}
