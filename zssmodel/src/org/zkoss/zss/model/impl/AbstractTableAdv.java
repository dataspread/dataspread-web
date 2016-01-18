/* AbstractTableAdv.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 7:54:30 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.STable;

/**
 * @author henri
 * @since 3.8.0
 */
public abstract class AbstractTableAdv implements STable, Serializable {
	private static final long serialVersionUID = 1L;

	public abstract SCellStyle getCellStyle(int row, int col);
	
	//ZSS-985
	public abstract void deleteRows(int row1, int row2);
	
	//ZSS-985
	public abstract void deleteCols(int col1, int col2);
	
	//ZSS-985
	public abstract boolean shiftCols(int offset);

	//ZSS-985
	public abstract boolean shiftRows(int offset);
	
	//ZSS-986
	public abstract void insertRows(int row1, int row2);
	
	//ZSS-986
	public abstract void insertCols(int col1, int col2, boolean insertLeft);
	
	//ZSS-988
	//delete old filter; shift; add new filter
	public abstract void refreshFilter();
}

