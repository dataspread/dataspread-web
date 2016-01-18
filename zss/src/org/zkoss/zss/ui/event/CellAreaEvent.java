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
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;

/**
 * Event class about selection of cell
 * @author Dennis.Chen
 */
public class CellAreaEvent extends CellEvent{
	private static final long serialVersionUID = 1L;
	private int _bRow;
	private int _rCol;

	public CellAreaEvent(String name, Component target,Sheet sheet, int tRow, int lCol, int bRow, int rCol) {
		super(name, target,sheet,tRow,lCol,null);
		_bRow = bRow;
		_rCol = rCol;
	}

	public int getLastColumn(){
		return _rCol;
	}
	
	public int getLastRow(){
		return _bRow;
	}
	
	public AreaRef getArea(){
		return new AreaRef(getRow(),getColumn(),getLastRow(),getLastColumn());
	}
	
	
}
