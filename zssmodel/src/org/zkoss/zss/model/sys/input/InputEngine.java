/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.sys.input;


/**
 * Determine a cell's type and value by parsing editing text with predefined patterns. 
 * The parsing process considers the locale for decimal separator, thousands separator, and date format.  
 * @author dennis
 * @since 3.5.0
 */
public interface InputEngine {

	public InputResult parseInput(String editText,String format, InputParseContext context);
}
