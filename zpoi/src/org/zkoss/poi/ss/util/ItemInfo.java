/* ItemInfo.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		May 15, 2012 9:51:32 AM , Created by Sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.poi.ss.util;

import org.zkoss.poi.ss.usermodel.PivotField.Item;
import org.zkoss.poi.ss.usermodel.PivotField.Item.Type;

/**
 * @author Sam
 *
 */
public class ItemInfo {

	private Object _value;
	private int _depth;
	private Item.Type _type;
	private int _index = -1;
	
	public ItemInfo(Type type, Object value, int depth) {
		this(type, value, depth, -1);
	}
	
	public ItemInfo(Type type, Object value, int depth, int index) {
		_type = type;
		_value = value;
		_depth = depth;
		_index = index;
	}
	
	/**
	 * Returns the type.
	 * 
	 * @return
	 */
	public Type getType () {
		return _type;
	}

	/**
	 * Returns the value of item.
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return _value;
	}

	/**
	 * Returns the depth of the item.
	 * 
	 * @return the depth
	 */
	public int getDepth() {
		return _depth;
	}
	
	public int getIndex() {
		return _index;
	}
}
