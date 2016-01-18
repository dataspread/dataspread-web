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

import org.zkoss.zss.model.SCell.CellType;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public interface InputResult {


	public String getEditText();

	public Object getValue();

	public CellType getType();
	
	/**
	 * Format for the edit text
	 * @return
	 */
	public String getFormat();

}
