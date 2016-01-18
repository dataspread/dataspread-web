/* FullWeekStep.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 29, 2011 2:37:53 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.zss.range.impl.autofill;

import java.util.Locale;

import org.zkoss.zss.model.SCell;

/**
 * Full week Step.
 * @author henrichen
 *
 */
/*package*/ class FullWeekStep implements Step {
	private final CircularStep _innerStep;
	private final int _type;
	public FullWeekStep(int initial, int step, int type, int datatype, Locale locale) { //ZSS-69
		_innerStep = new CircularStep(initial, step, FullWeekData.getInstance(type, locale));
		_type = datatype;
	}
	@Override
	public int getDataType() {
		return _type;
	}
	@Override
	public Object next(SCell srcCell) {
		return _innerStep.next(srcCell);
	}
}
