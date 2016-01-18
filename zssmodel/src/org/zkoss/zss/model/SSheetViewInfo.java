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
 * Store those visual statuses of a sheet, e.g. freeze status of row (column), or grid-line display.  
 * @author dennis
 * @since 3.5.0
 */
public interface SSheetViewInfo {
	public int getNumOfRowFreeze();

	public int getNumOfColumnFreeze();

	public void setNumOfRowFreeze(int num);

	public void setNumOfColumnFreeze(int num);

	public boolean isDisplayGridlines();

	public void setDisplayGridlines(boolean enable);
	
	public SHeader getHeader();
	
	public SFooter getFooter();
	
	/**
	 * @return row indexes of all the horizontal page breaks
	 * @see #setRowBreaks(int[])
	 */
	public int[] getRowBreaks();

	/**
	 * @see #getRowBreaks()
	 */
	public void setRowBreaks(int[] breaks);
	
	/**
	 * @see #getRowBreaks()
	 */
	public void addRowBreak(int rowIdx);
	
	/**
	 * @return column indexes of all the vertical page breaks
	 * @see #setColumnBreaks(int[])
	 */
	public int[] getColumnBreaks();
	
	/**
	 * @see #getColumnBreaks()
	 */
	public void setColumnBreaks(int[] breaks);
	
	/**
	 * @see #getColumnBreaks()
	 */
	public void addColumnBreak(int columnIdx);
}
