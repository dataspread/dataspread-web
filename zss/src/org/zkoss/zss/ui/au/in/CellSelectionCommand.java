/* CellSelectionCommand.java
 * 
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		January 10, 2008 03:10:40 PM , Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under Lesser GPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;


import java.util.Map;

import org.zkoss.lang.Objects;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.impl.SheetImpl;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.ui.CellSelectionType;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.CellSelectionEvent;
import org.zkoss.zss.ui.sys.SpreadsheetInCtrl;


/**
 * A Command (client to server) for handling cell selection
 * @author Dennis.Chen
 *
 */
public class CellSelectionCommand extends AbstractCommand implements Command {

	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, CellSelectionCommand.class.getCanonicalName());

		final Map data = (Map) request.getData();
		if (data == null || data.size() != 6)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), this});
			
		String sheetId= (String) data.get("sheetId");
		
		Sheet sheet = ((Spreadsheet)comp).getSelectedSheet();
		
		if(!getSheetUuid(sheet).equals(sheetId))
			return;
		
		//TODO request shall send back maxcol/maxrow (do it in client side)
		final SBook book = ((SheetImpl)sheet).getNative().getBook();
		final int maxcol = book.getMaxColumnIndex();
		final int maxrow = book.getMaxRowIndex();
		
		String t = (String) data.get("type");
		CellSelectionType type = null;
		if("all".equals(t)){
			type = CellSelectionType.ALL;
		}else if("col".equals(t)){
			type = CellSelectionType.COLUMN;
		}else if("row".equals(t)){
			type = CellSelectionType.ROW;
		}else if("cell".equals(t)){
			type = CellSelectionType.CELL;
		}else{
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), this});
		}
		
		int left = (Integer) data.get("left");
		int top = (Integer) data.get("top");
		int right = (type == CellSelectionType.ROW || type == CellSelectionType.ALL) ? maxcol : (Integer) data.get("right");
		int bottom = (type == CellSelectionType.COLUMN || type == CellSelectionType.ALL) ? maxrow : (Integer) data.get("bottom");
		
		SpreadsheetInCtrl ctrl = ((SpreadsheetInCtrl)((Spreadsheet)comp).getExtraCtrl());
		ctrl.setSelectionRect(left, top, right, bottom);	
		
		Events.postEvent(new CellSelectionEvent(
				org.zkoss.zss.ui.event.Events.ON_CELL_SELECTION, comp, sheet,
				top, left, bottom, right,type));
	}
}