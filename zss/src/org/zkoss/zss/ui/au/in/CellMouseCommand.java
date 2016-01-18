/* CellMouseCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2007 12:10:40 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under Lesser GPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;


import static org.zkoss.zss.ui.au.in.Commands.parseKeys;

import java.util.Map;

import org.zkoss.lang.Objects;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.CellFilterEvent;
import org.zkoss.zss.ui.event.CellMouseEvent;

/**
 * A Command (client to server) for handling user(client) start editing a cell
 * @author Dennis.Chen
 *
 */
public class CellMouseCommand extends AbstractCommand implements Command {
	
	//-- super --//
	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		//final String[] data = request.getData();
		//TODO a little patch, "af" might shall be fired by a separate command?
		final Map data = (Map) request.getData();
		String type = (String) data.get("type");//command type
		if (data == null 
			|| (!"af".equals(type) && data.size() != 9) 
			|| ("af".equals(type) && data.size() != 10)){
			
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), this});
		}
		
		int shx = (Integer) data.get("shx");//x offset against spreadsheet
		int shy = (Integer) data.get("shy");
		int key = parseKeys((String) data.get("key"));
		String sheetId = (String) data.get("sheetId");
		int row = (Integer) data.get("row");
		int col = (Integer) data.get("col");
		
		//ZSS-440, might get double in IE 10 when room
		int mx = AuDataUtil.getInt(data,"mx");//x offset against body
		int my = AuDataUtil.getInt(data,"my");
		
		Spreadsheet spreadsheet = (Spreadsheet) comp;
		Sheet sheet = ((Spreadsheet) comp).getSelectedSheet();
		if (!getSheetUuid(sheet).equals(sheetId))
			return;
		
		if ("lc".equals(type)) {
			type = org.zkoss.zss.ui.event.Events.ON_CELL_CLICK;
		} else if ("rc".equals(type)) {
			type = org.zkoss.zss.ui.event.Events.ON_CELL_RIGHT_CLICK;
		} else if ("dbc".equals(type)) {
			type = org.zkoss.zss.ui.event.Events.ON_CELL_DOUBLE_CLICK;
		} else if ("af".equals(type)) {
			type = org.zkoss.zss.ui.event.Events.ON_CELL_FILTER;
		} else if ("dv".equals(type)) {
			type = org.zkoss.zss.ui.event.Events.ON_CELL_VALIDATOR;
		} else {
			throw new UiException("unknow type : " + type);
		}

		if (org.zkoss.zss.ui.event.Events.ON_CELL_FILTER.equals(type)) {
			int field = (Integer) data.get("field");
			//handling auto filter when user click on the auto-fitler icon on the cell
			//TODO possible to let user override it?
			AreaRef filterArea = new AutoFilterDefaultHandler().processFilter(spreadsheet, sheet, row, col, field);
			//consider to remove  ON_CELL_FILTER , it is useless for user if he can't override it

			Events.postEvent(new CellFilterEvent(type, comp, sheet, row, col, filterArea, field, shx, shy, key, mx, my));
		} else if (org.zkoss.zss.ui.event.Events.ON_CELL_VALIDATOR.equals(type)) {
			//consider to remove  ON_CELL_VALIDATOR event , it is useless for user if he can't override it
			Events.postEvent(new CellMouseEvent(type, comp, sheet, row, col, shx, shy, key, mx, my));
		} else {
			Events.postEvent(new CellMouseEvent(type, comp, sheet, row, col, shx, shy, key, mx, my));
		}
	}
}