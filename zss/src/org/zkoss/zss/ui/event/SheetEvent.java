package org.zkoss.zss.ui.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zss.api.model.Sheet;

/**
 * The event to notify a sheet's updated
 * @author dennis
 * @see Events#ON_SHEET_NAME_CHANGE
 * @see Events#ON_SHEET_ORDER_CHANGE
 * @see Events#ON_SHEET_CREATE
 */
public class SheetEvent extends Event{
	private static final long serialVersionUID = 1L;
	private Sheet _sheet;
	
	
	public SheetEvent(String name, Component target, Sheet sheet) {
		super(name, target, sheet);
		_sheet = sheet;
	}
	
	public Sheet getSheet(){
		return _sheet;
	}
	
	public String getSheetName(){
		return _sheet.getSheetName();
	}
}
