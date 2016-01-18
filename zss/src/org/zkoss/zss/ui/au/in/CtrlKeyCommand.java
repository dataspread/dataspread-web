/* CtrlKeyCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 21, 2012 10:25:09 AM , Created by sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import java.util.Map;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuRequests;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zss.api.model.Book.BookType;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Picture;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.KeyEvent;
import org.zkoss.zss.ui.event.WidgetKeyEvent;

/**
 * @author sam
 *
 */
public class CtrlKeyCommand extends AbstractCommand implements Command {

	@Override
	public void process(AuRequest request) {
		Map data = request.getData();
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		
		Sheet sheet = ((Spreadsheet) comp).getSelectedSheet();
		
		String sheetId= (String) data.get("sheetId");
		if (!getSheetUuid(sheet).equals(sheetId))
			return;
		
		Boolean widgetType = (Boolean)data.get("wgt");
		if(widgetType!=null && widgetType){
			//widget keydnown
			handleWidgetKeyDown(request);
		}else{
			handleSpreadsheetKeyDown(request);
		}
	}

	private void handleSpreadsheetKeyDown(AuRequest request) {
		Map data = request.getData();
		org.zkoss.zk.ui.event.KeyEvent evt = KeyEvent.getKeyEvent(request);
		Event zssKeyEvt = new KeyEvent(Events.ON_CTRL_KEY, evt.getTarget(), 
				evt.getKeyCode(), evt.isCtrlKey(), evt.isShiftKey(), evt.isAltKey(), 
				AuRequests.getInt(data, "tRow", -1), AuRequests.getInt(data, "lCol", -1),
				AuRequests.getInt(data, "bRow", -1), AuRequests.getInt(data, "rCol", -1));
		Events.postEvent(zssKeyEvt);
	}
	
	private void handleWidgetKeyDown(AuRequest request) {
		final Map data = request.getData();
		final Component comp = request.getComponent();
		Sheet sheet = ((Spreadsheet) comp).getSelectedSheet();
		
		String widgetType = (String) data.get("wgtType");
		org.zkoss.zk.ui.event.KeyEvent evt = KeyEvent.getKeyEvent(request);
		Object widgetData = null;
		
		String id = (String) data.get("wgtId");
		
		if ("image".equals(widgetType)) {
			for(Picture p:sheet.getPictures()){
				if(p.getId().equals(id)){
					widgetData = p;
					break;
				}
			}
			
		} else if ("chart".equals(widgetType)) {
			for(Chart c:sheet.getCharts()){
				if(c.getId().equals(id)){
					widgetData = c;
					break;
				}
			}
		}
		if(widgetData==null){
			//TODO ignore it or throw exception?
			return;
		}
		Event zssKeyEvt = new WidgetKeyEvent(org.zkoss.zss.ui.event.Events.ON_WIDGET_CTRL_KEY, evt.getTarget(), sheet,widgetData,
				evt.getKeyCode(), evt.isCtrlKey(), evt.isShiftKey(), evt.isAltKey());
		Events.postEvent(zssKeyEvt);
	}
}
