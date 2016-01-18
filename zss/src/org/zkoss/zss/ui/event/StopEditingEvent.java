/* StopEditingEvent.java

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

import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zk.ui.Component;
//import org.zkoss.zss.model.Sheet;

/**
 * @author Dennis.Chen
 *
 */
public class StopEditingEvent extends CellEvent{

	
	boolean _cancel;
	
	Object _editingValue;

	public StopEditingEvent(String name, Component target, Sheet sheet,	int row, int col, String editingValue) {
		super(name, target, sheet, row, col, editingValue);
		this._editingValue = editingValue;
	}

	public Object getEditingValue() {
		return _editingValue;
	}

	public void setEditingValue(Object editingValue) {
		this._editingValue = editingValue;
	}

	/**
	 * Cancel editing, which means the use editing value will not set to cell
	 */
	public void cancel() {
		_cancel = true;
	}

	public boolean isCancel() {
		return _cancel;
	}

}
