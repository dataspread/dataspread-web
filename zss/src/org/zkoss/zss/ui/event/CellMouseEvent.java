/* CellMouseEvent.java

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
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zss.api.model.Sheet;

/**
 * A class from handle event which about mouse event on a cell
 * @author Dennis.Chen
 */
public class CellMouseEvent extends MouseEvent{
	
	private Sheet _sheet;
	private int _row;
	private int _col;
	
	@Deprecated
	public CellMouseEvent(String name, Component target, int x,int y, int keys,Sheet sheet, int row ,int col,int clientx,int clienty) {
		super(name, target, x, y, clientx, clienty, keys);
		_sheet = sheet;
		this._row = row;
		this._col = col;
	}
	
	/**
	 * @since 3.0.0
	 */
	public CellMouseEvent(String name, Component target, Sheet sheet, int row ,int col, int x,int y, int keys,int clientx,int clienty) {
		super(name, target, x, y, clientx, clienty, keys);
		_sheet = sheet;
		this._row = row;
		this._col = col;
	}
	
	/*
	public CellMouseEvent(String name, Component target, int x,int y,String sheetId, int row ,int col) {
		super(name, target, x, y);
		_sheetId = sheetId;
		this._row = row;
		this._col = col;
	}
	public CellMouseEvent(String name, Component target,String sheetId, int row ,int col) {
		super(name, target);
		_sheetId = sheetId;
		this._row = row;
		this._col = col;
	}
	public CellMouseEvent(String name, Component target, String area,String sheetId, int row ,int col) {
		super(name, target, area);
		_sheetId = sheetId;
		this._row = row;
		this._col = col;
	}
	*/
	
	
	
	/**
	 * get Sheet
	 * @return sheet the related sheet 
	 */
	public Sheet getSheet(){
		return _sheet;
	}
	
	/**
	 * get row index
	 * @return row index
	 */
	public int getRow(){
		return _row;
	}
	
	/**
	 * get column index
	 * @return column index
	 */
	public int getColumn(){
		return _col;
	}
	
	/**
	 * get x position of client  
	 * @return x position
	 */
	public int getClientx(){
		return super.getPageX();
	}
	
	/**
	 * get y position of client
	 * @return y position
	 */
	public int getClienty(){
		return super.getPageY();
	}
	
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append("[").append("x:").append(getX()).append(",y:").append(getY()).append("]");
		sb.append("[").append("alt:").append((getKeys()&ALT_KEY)==ALT_KEY)
			.append(",ctrl:").append((getKeys()&CTRL_KEY)==CTRL_KEY)
			.append(",shift:").append((getKeys()&SHIFT_KEY)==SHIFT_KEY)
			.append("]");
		sb.append("[").append("row:").append(_row).append(",col:").append(_col).append("]");
		return sb.toString();
	}

}
