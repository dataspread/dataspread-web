/* CellVisitor.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api;

/**
 * The visitor to help you to visit cells of a {@link Range}.
 * @author dennis
 * @see Range#visit(CellVisitor)
 * @since 3.0.0
 */
public interface CellVisitor {

	/**
	 * Should ignore to visit a cell on (row,column) if the cell doesn't existed.
	 * This method is called only when the cell of (row,column) is null. 
	 * @param row the row of the cell
	 * @param column the column of the cell
	 * @return true if should ignore visiting the cell if it is not exist.
	 */
	public boolean ignoreIfNotExist(int row, int column);
	
	/**
	 * Should create the cell on (row,column) if the cell doesn't existed. this method is called after {@link #ignoreIfNotExist(int, int)} return false
	 * @param row the row of the cell
	 * @param column the column of the cell
	 * @return true if should create the cell if it is not exist.
	 * @deprecated since 3.5, it use proxy when you edit a null cell , it is more smarter 
	 */
	public boolean createIfNotExist(int row, int column);

	/**
	 * Visits the cell
	 * @param cellRange the range of a cell
	 * @return true if should continue visit next cell.
	 */
	public boolean visit(Range cellRange);
}
