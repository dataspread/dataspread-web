/* CellSelectionEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 19, 2007 2:18:10 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.CellSelectionType;

/**
 * Event class about selection of cell
 * @author Dennis.Chen
 */
public class CellSelectionEvent extends CellAreaEvent{
	private static final long serialVersionUID = 1L;
//	
//	public static final int SELECT_CELLS = 0x01;
//	public static final int SELECT_ROW = 0x02;
//	public static final int SELECT_COLUMN = 0x03;
//	public static final int SELECT_ALL = 0x04;
	
	
	private CellSelectionType _type;

	public CellSelectionEvent(String name, Component target,Sheet sheet,int tRow, int lCol, int bRow, int rCol,CellSelectionType type) {
		super(name, target, sheet, tRow, lCol, bRow, rCol);
		_type = type;
	}
	
	/**
	 * Returns the selection type
	 * @return the selection type.
	 */
	public CellSelectionType getType(){
		return _type;
	}
	
	/**
	 * @deprecated use {@link #getColumn()}
	 */
	@Deprecated 
	public int getLeft() {
		return getColumn();
	}

	/**
	 * @deprecated use {@link #getRow()}
	 */
	@Deprecated 
	public int getTop() {
		return getRow();
	}

	/**
	 * @deprecated use {@link #getLastColumn()}
	 */
	@Deprecated 
	public int getRight() {
		return getLastColumn();
	}

	/**
	 * @deprecated use {@link #getLastRow()}
	 */
	@Deprecated 
	public int getBottom() {
		return getLastRow();
	}
}
