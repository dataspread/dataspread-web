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
package org.zkoss.zss.model;
/**
 * A color which might relate to a text or a border.
 * @author dennis
 * @since 3.5.0
 */
public interface SColor {

	/**
	 * Gets the color html string expression, for example "#FF6622"
	 * @return
	 */
	public String getHtmlColor();
	
	/**
	 * Gets the color types array, order by Red,Green,Blue
	 * @return
	 */
	public byte[] getRGB();
	
	
}
