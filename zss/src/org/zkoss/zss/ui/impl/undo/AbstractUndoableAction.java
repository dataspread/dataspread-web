/* AbstractUndoableAction.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/7/25, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl.undo;

import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.sys.UndoableAction;

/**
 * 
 * @author dennis
 *
 */
abstract public class AbstractUndoableAction implements UndoableAction {

	protected final String _label;
	protected final Sheet _sheet;
	protected final int _row,_column,_lastRow,_lastColumn;
	public AbstractUndoableAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn){
		this._label = label;
		this._sheet = sheet;
		this._row = row;
		this._column = column;
		this._lastRow = lastRow;
		this._lastColumn = lastColumn;
	}
	
	public String getLabel(){
		return _label;
	}

	/**
	 * Check if sheet still available
	 * @return
	 */
	protected boolean isSheetAvailable(){
		try{
			Book book = _sheet.getBook();
			return book.getSheetIndex(_sheet)>=0;
		}catch(Exception x){}
		return false;
	}
	
	/**
	 * Check if sheet is protected
	 * @return
	 */
	protected boolean isSheetProtected(){
		try{
			return _sheet.isProtected();
		}catch(Exception x){}
		return true;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getLabel()+": ["+_row+","+_column+","+_lastRow+","+_lastColumn+"]").append(super.toString());
		return sb.toString();
	}
	
	@Override
	public AreaRef getUndoSelection(){
		return new AreaRef(_row,_column,_lastRow,_lastColumn);
	}
	@Override
	public AreaRef getRedoSelection(){
		return new AreaRef(_row,_column,_lastRow,_lastColumn);
	}
	@Override
	public Sheet getUndoSheet(){
		return _sheet;
	}
	@Override
	public Sheet getRedoSheet(){
		return _sheet;
	}
}
