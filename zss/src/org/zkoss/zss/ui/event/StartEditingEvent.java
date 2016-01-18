/* StartEditingEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 14, 2008 4:33:25 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zss.api.model.Sheet;

/**
 * @author Dennis.Chen
 *
 */
public class StartEditingEvent extends CellEvent{

	boolean _cancel;
	
	Object _editingValue;
	
	boolean _editingSet = false;
	
	Object _clientValue;
	
	public StartEditingEvent(String name, Component target,Sheet sheet, int row ,int col, Object editingValue,Object clientvalue) {
		super(name,target,sheet,row,col,null);
		this._editingValue = editingValue;
		this._clientValue = clientvalue;
	}
	
	public Object getEditingValue(){
		return _editingValue;
	}
	
	/**
	 * Sets the editing value. if editing value is set, then client value will be ignore. 
	 */
	public void setEditingValue(Object editingValue){
		this._editingValue = editingValue;
		_editingSet = true;
	}
	
	/**
	 * Return true if editing value is set by {@link #setEditingValue(Object)}
	 */
	public boolean isEditingSet(){
		return _editingSet;
	}
	
	/**
	 * Return value when client side fire start editing. 
	 * For example if user press on A to start editing, then the value is "A".
	 * If user start editing by F2 or mouse click, then the value is null. 
	 */
	public Object getClientValue(){
		return _clientValue;
	}
	
	/**
	 * Cancel editing, which means the editing will not start;
	 */
	public void cancel(){
		_cancel = true;
	}
	
	public boolean isCancel(){
		return _cancel;
	}
}
