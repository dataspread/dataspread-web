/* AA.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/20 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.event;

import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zss.model.SSheet;

/**
 * Event about header update
 * @author Dennis.Chen
 * @since 3.0.0
 */
public class HeaderUpdateEvent extends Event{
	private static final long serialVersionUID = 1L;
//	static public final int TOP_HEADER = 0;
//	static public final int LEFT_HEADER = 1;
	
	private SSheet _sheet;
	private HeaderType _type;
	private HeaderAction _action;
	private int _index;
	private boolean _hidden;
	private int _size;

	public HeaderUpdateEvent(String name, Component target,SSheet sheet, HeaderType type, HeaderAction acton,int index, int size, boolean hidden) {
		super(name, target, size);
		_sheet = sheet;
		this._type = type;
		this._index = index;
		this._hidden = hidden;
		this._size = size;
		this._action = acton;
	}
	
//	public HeaderEvent(String name, Component target,Sheet sheet, int type ,int index, boolean hidden) {
//		this(name,target,sheet,type,index,-1,hidden);
//	}
	
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
	 * get type of this event 
	 * @return the type of header
	 */
	public HeaderType getType(){
		return _type;
	}
	
	/**
	 * Returns whether request hidden(true)/unhidden(false) of this column/row.
	 * @return whether request hidden(true)/unhidden(false) of this column/row.
	 */
	public boolean isHidden() {
		return _hidden;
	}
	
	/**
	 * Returns the new size of this header event 
	 * @return the new size
	 */
	public int getSize(){
		return _size;
	}
	
	/**
	 * Returns the action 
	 * @return
	 */
	public HeaderAction getAction(){
		return _action;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append("[").append("type:").append(_type).append(",index:").append(_index).append(",data:").append(getData()).append(",hidden:").append(isHidden()).append("]");
		return sb.toString();
	}

}
