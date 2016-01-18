/* SheetDeleteEvent.java

	Purpose:
		
	Description:
		
	History:
		Dec 26, 2011 4:54:04 PM, Created by henri

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.zss.ui.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

/**
 * The event to notify a sheet's deleted
 * @author henrichen
 * @see Events#ON_SHEET_DELETE
 *
 */
public class SheetDeleteEvent extends Event{
	private static final long serialVersionUID = 1L;
	
	private String _sheetName;
//	private String _newSheetName;
	public SheetDeleteEvent(String name, Component target, String delSheetName) {
		super(name, target);
		_sheetName = delSheetName;
//		_newSheetName = newSheetName;
	}
	public String getSheetName() {
		return _sheetName;
	}
//	public String getNewSheetName() {
//		return _newSheetName;
//	}
}
