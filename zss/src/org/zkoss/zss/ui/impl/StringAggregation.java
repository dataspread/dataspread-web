/* StringCache.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 13, 2012 11:11:45 AM , Created by sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.zkoss.json.JSONArray;

/**
 * @author sam
 *
 */
public class StringAggregation {
	
	private LinkedHashMap<String, Integer> _values = new LinkedHashMap<String, Integer>();
	
	/**
	 * Add string to aggregation
	 * 
	 * @return int the index of the string in aggregation
	 */
	public int add(String value) {
		if (_values.containsKey(value)) {
			return _values.get(value);
		} else {
			int strIndex = _values.size();
			_values.put(value, strIndex);
			return strIndex;
		}
	}
	
	/**
	 * Returns {@link JSONArray} representation of aggregation
	 * 
	 * @return JSONArray
	 */
	public JSONArray getJSONArray() {
		JSONArray strs = new JSONArray();
		
		Iterator<String> iter = _values.keySet().iterator();
		while (iter.hasNext()) {
			strs.add(iter.next());
		}
		return strs;
	}
}
