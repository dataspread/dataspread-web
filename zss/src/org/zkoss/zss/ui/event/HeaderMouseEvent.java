/* HeaderMouseEvent.java

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

import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zss.model.SSheet;
//import org.zkoss.zss.model.Sheet;
/**
 * A class from handle event which about mouse event on a cell
 * @author Dennis.Chen
 */
public class HeaderMouseEvent extends MouseEvent{
	private SSheet _sheet;
	private int _index;
	private HeaderType _type;
	private int _clientx;
	private int _clienty;


	public HeaderMouseEvent(String name, Component target, int x,int y, int keys,SSheet sheet, HeaderType type, int index,int clientx,int clienty) {
		super(name, target, x, y, clientx, clienty, keys);
		_sheet = sheet;
		this._index = index;
		this._type = type;
		this._clientx = clientx;
		this._clienty = clienty;
	}
	/*
	public HeaderMouseEvent(String name, Component target, int x,int y,String sheetId, int type, int index) {
		super(name, target, x, y);
		_sheetId = sheetId;
		this._index = index;
		this._type = type;
	}
	public HeaderMouseEvent(String name, Component target,String sheetId, int type, int index) {
		super(name, target);
		_sheetId = sheetId;
		this._index = index;
		this._type = type;
	}
	public HeaderMouseEvent(String name, Component target, String area,String sheetId, int type,int index) {
		super(name, target, area);
		_sheetId = sheetId;
		this._index = index;
	}
	*/
	
	
	
	
	/**
	 * get Sheet
	 * @return sheet 
	 */
	public SSheet getSheet(){
		return _sheet;
	}
	
	/**
	 * get index of the header, if the {@link #getType} return @link HeaderEvent#TOP_HEADER} then it is column index, otherwise it is row index 
	 * @return row index
	 */
	public int getIndex(){
		return _index;
	}
	
	
	/**
	 * get type of this event, it will be {@link HeaderUpdateEvent#TOP_HEADER} or (@link HeaderEvent#LEFT_HEADER} 
	 * @return the type of header
	 */
	public HeaderType getType(){
		return _type;
	}
	
	/**
	 * get x position of client  
	 * @return x position
	 */
	public int getClientx(){
		return _clientx;
	}
	
	/**
	 * get y position of client
	 * @return y position
	 */
	public int getClienty(){
		return _clienty;
	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append("[").append("x:").append(getX()).append(",y:").append(getY()).append("]");
		sb.append("[").append("alt:").append((getKeys()&ALT_KEY)==ALT_KEY)
			.append(",ctrl:").append((getKeys()&CTRL_KEY)==CTRL_KEY)
			.append(",shift:").append((getKeys()&SHIFT_KEY)==SHIFT_KEY)
			.append("]");
		sb.append("[").append("type:").append(_type).append(",index:").append(_index).append("]");
		return sb.toString();
	}

}
