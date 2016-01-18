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
 * A column of a sheet. But you cannot get cells from it. You should get cell via {@link SSheet#getCell(int, int)}. 
 * @author dennis
 * @since 3.5.0
 */
public interface SColumn extends CellStyleHolder{

	public int getIndex();
	public SSheet getSheet();

	/**
	 * @return TRUE if this is a blank column whose cells has no data, otherwise returns FALSE.
	 */
	public boolean isNull();
	
	public int getWidth();
	public boolean isHidden();
	public boolean isCustomWidth();
	
	public void setWidth(int width);
	public void setHidden(boolean hidden);
	public void setCustomWidth(boolean custom);
	
}
