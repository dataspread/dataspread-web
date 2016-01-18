/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;
/**
 * It stores first and last column index of a group of column that has same width and cell style.
 * @author Dennis
 * @since 3.5.0
 */
public interface SColumnArray extends CellStyleHolder{

	public int getIndex();
	public int getLastIndex();
	
	public SSheet getSheet();
	
	public int getWidth();
	public boolean isHidden();
	public boolean isCustomWidth();
	
	public void setWidth(int width);
	public void setHidden(boolean hidden);
	public void setCustomWidth(boolean custom);
}
