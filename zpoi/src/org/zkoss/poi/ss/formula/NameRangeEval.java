/* NameRangeEval.java

	Purpose:
		
	Description:
		
	History:
		Nov 21, 2014 3:41:17 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.ss.formula;

import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.ptg.NamePtg;

/**
 * @author henri
 * @since 3.9.6
 */
public class NameRangeEval implements ValueEval {
	private NamePtg _ptg;
	public NameRangeEval(NamePtg ptg) {
		_ptg = ptg;
	}
	public NamePtg getNamePtg() {
		return _ptg;
	}
}
