/* CellAttribute.java

	Purpose:
		
	Description:
		
	History:
		Feb 26, 2015 3:59:54 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

/**
 * Specify which attribute is to be updated.
 * @author henri
 * @see RangeImpl.ModelManipulationTask
 */
public enum CellAttribute {
	ALL(1), TEXT(2), STYLE(3), SIZE(4), MERGE(5), COMMENT(6);
	
	public final int value;
	CellAttribute(int value) {
		this.value = value;
	}
	
	public String toString() {
		return "" + value;
	}
}
