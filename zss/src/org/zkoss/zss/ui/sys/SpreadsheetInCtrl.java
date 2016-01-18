/* SpreadsheetInCtrl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2007 12:18:09 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.sys;



/**
 * Special controller interface for controlling client to server behavior.
 * Only spreadsheet developer need to use this interface. 
 * (All method start should with in prefix to represent a client side update)
 * @author Dennis.Chen
 *
 */
public interface SpreadsheetInCtrl {

	/**
	 * Indicate user set the size of a column
	 * @param sheetId the sheet id
	 * @param column the column index
	 * @param newsize the new size
	 * @param id a unique id of this customized column size
	 * @param hidden whether this column is hidden
	 */
	public void setColumnSize(String sheetId,int column,int newsize,int id, boolean hidden);
	
	/**
	 * Indicate user set the size of a row
	 * @param sheetId the sheet id
	 * @param row the row index
	 * @param newsize the new size
	 * @param id a unique id of this customized column size
	 * @param hidden whether this row is hidden
	 * @param isCustom whether the size is custom (set by users) or not (determined by the component automatically)
	 */
	public void setRowSize(String sheetId,int row,int newsize,int id, boolean hidden, boolean isCustom);
	
	/**
	 * Indicate user change selection rectangle
	 */
	public void setSelectionRect(int left,int top,int right,int bottom);
	
	/**
	 * Indicate user change focus rectangle
	 */
	public void setFocusRect(int left,int top,int right,int bottom);
	
	/**
	 * Indicate user do some scroll, and change loaded rectangle.
	 */
	public void setLoadedRect(int left, int top, int right, int bottom);
	
	/**
	 * Indicate user's visible range 
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setVisibleRect(int left, int top,int right, int bottom);
	
	
	/**
	 * 
	 */
	//20130507, Dennis, move to internal controller api
	public void setSelectedSheetDirectly(String name, boolean cacheInClient, int row, int col, 
			int left, int top, int right, int bottom,
			int highlightLeft, int highlightTop, int highlightRight, int highlightBottom,
			int rowfreeze, int colfreeze);
}
