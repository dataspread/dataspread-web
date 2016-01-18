/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 23, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.model.sys.input;

import java.util.Locale;

/**
 * Responsible for number input mask. 
 * 
 * @author henri
 * @since 3.5.0
 */
public interface NumberInputMask {
	/**
	 * @param txt the input text
	 */
	public Object[] parseNumberInput(String txt, Locale locale);
}
