/* SelectSheetCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Feb 2, 2012 10:36:09 AM , Created by sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import java.util.Map;

import org.zkoss.lang.Objects;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuRequests;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.Events;
import org.zkoss.zss.ui.event.SheetSelectEvent;
import org.zkoss.zss.ui.sys.SpreadsheetInCtrl;

/**
 * Select sheet from sheet bar click
 * @author sam
 *
 */
public class SelectSheetCommand extends AbstractCommand implements Command {

	@Override
	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
					SelectSheetCommand.class);
		
		final Map data = (Map) request.getData();
		if (data == null || data.size() != 14)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), SelectSheetCommand.class });
		
		Spreadsheet spreadsheet = ((Spreadsheet) comp);
		String sheetId = (String) data.get("sheetId");
		boolean cacheInClient = (Boolean) data.get("cache");
		int row = (Integer)data.get("row");
		int col = (Integer)data.get("col");
		
		//selection
		int top = (Integer)data.get("top");
		int right = (Integer)data.get("right");
		int bottom = (Integer)data.get("bottom");
		int left = (Integer)data.get("left");
		
		//highlight
		int highlightLeft = (Integer)data.get("hleft");
		int highlightTop = (Integer)data.get("htop");
		int highlightRight = (Integer)data.get("hright");
		int highlightBottom = (Integer)data.get("hbottom");
		
		//freeze
		int rowfreeze = AuRequests.getInt(data, "frow", -1);
		int colfreeze = AuRequests.getInt(data, "fcol", -1);
		
		Sheet currSheet = spreadsheet.getSelectedSheet();
		
		Book book = spreadsheet.getBook();
		int len = book.getNumberOfSheets();
		for (int i = 0; i < len; i++) {
			Sheet sheet = book.getSheetAt(i);
			if (getSheetUuid(sheet).equals(sheetId)) {
				if(!currSheet.equals(sheet)){
					((SpreadsheetInCtrl)spreadsheet.getExtraCtrl()).setSelectedSheetDirectly(sheet.getSheetName(), cacheInClient, row, col, 
							left, top, right, bottom,
							highlightLeft, highlightTop, highlightRight, highlightBottom,
							rowfreeze, colfreeze);
					
					Event event = new SheetSelectEvent(Events.ON_SHEET_SELECT, spreadsheet, sheet, currSheet);
					org.zkoss.zk.ui.event.Events.postEvent(event);
				}
				break;
			}
		}
	}
}
