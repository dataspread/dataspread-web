/* GrowthStep.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 31, 2011 10:24:00 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.zss.range.impl.autofill;

import org.zkoss.zss.model.SCell;

/**
 * Step for growth sequence.
 * @author henrichen
 *
 */
public class GrowthStep implements Step {
	private double _current;
	private final double _ratio;
	private final int _type;
	
	public GrowthStep(double initial, double ratio, int type) {
		_current = initial;
		_ratio = ratio;
		_type = type;
	}
	@Override
	public int getDataType() {
		return _type;
	}
	@Override
	public Object next(SCell cell) {
		return Double.valueOf(_current*= _ratio);
	}
}
