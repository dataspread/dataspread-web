/* CircularStep.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 29, 2011 2:33:56 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.zss.range.impl.autofill;

import org.zkoss.zss.model.SCell;


/**
 * Circular Step.
 * @author henrichen
 *
 */
/*package*/ class CircularStep implements Step {
	private final int _step;
	private int _current;
	private final CircularData _data;
	private final int _count;
	public CircularStep(int initial, int step, CircularData data) {
		_current = initial;
		_step = step;
		_count = data.getSize();
		_data = data;
	}
	public Object next(SCell srcCell) {
		_current = nextIndex(_current, _step);
		return _data.getData(_current);
	}
	private int nextIndex(int current, int step) {
		current += step;
		if (current < 0) {
			current += _count;
		}
		current %= _count;
		return current;
	}
	@Override
	public int getDataType() { //dummy
		return -1;
	}

}
