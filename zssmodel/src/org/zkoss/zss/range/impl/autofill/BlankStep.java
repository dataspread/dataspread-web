/* CopyStep.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 29, 2011 2:27:09 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.zss.range.impl.autofill;

import org.zkoss.zss.model.SCell;

/**
 * Copy blank to destination
 * @author henrichen
 * @since 2.1.0
 */
public class BlankStep implements Step {
	public static final Step instance = new BlankStep(); //since CopyStep keeps no state, we can use a singleton to serve all!
	@Override
	public Object next(SCell cell) {
		return null;
	}
	@Override
	public int getDataType() {
		return BLANK;
	}
}
