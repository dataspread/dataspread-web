/* JavaScriptValue.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 29, 2012 9:05:25 AM , Created by Sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;


/**
 * @author Sam
 *
 */
public class JavaScriptValue implements org.zkoss.json.JSONAware {
	private final String _js;

	/** Constructor
	 * @param js the snippet of the JavaScript code.
	 * For example, "{what: 123, another: 'a'}".
	 * The content is generated directly to the AU response, so it must be
	 * a valid JavaScript code.
	 */
	public JavaScriptValue(String js) {
		if (js == null)
			throw new IllegalArgumentException();
		_js = js;
	}

	//JSONAware//
	public String toJSONString() {
		return _js;
	}
	//Object//
	public int hashCode() {
		return _js.hashCode();
	}
	public boolean equals(Object o) {
		if (this == o) return true;
		return o instanceof JavaScriptValue && _js.equals(((JavaScriptValue)o)._js);
	}
}
