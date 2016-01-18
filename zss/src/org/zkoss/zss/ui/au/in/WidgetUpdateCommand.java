/* WidgetCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 18, 2011 5:20:37 PM , Created by sam
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import java.util.Map;

import org.zkoss.lang.Objects;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.api.SheetAnchor;
import org.zkoss.zss.api.model.Book.BookType;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Picture;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.Events;
import org.zkoss.zss.ui.event.WidgetAction;
import org.zkoss.zss.ui.event.WidgetUpdateEvent;

/**
 * @author sam
 *
 */
public class WidgetUpdateCommand extends AbstractCommand implements Command {

	@Override
	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		final Map data = request.getData();
		if (data == null || data.size() != 12)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
				new Object[] {Objects.toString(data), this});
		
		Sheet sheet = ((Spreadsheet) comp).getSelectedSheet();
		
		String sheetId= (String) data.get("sheetId");
		if (!getSheetUuid(sheet).equals(sheetId))
			return;
		
		String act = (String) data.get("action");
		WidgetAction action = null;
		if("move".equals(act)){
			action = WidgetAction.MOVE;
		}else if("resize".equals(act)){
			action = WidgetAction.RESIZE;
		}else{
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
					new Object[] {Objects.toString(data), this});
		}
		
		
		SheetAnchor anchor = getAnchor(data);
		if(anchor==null){
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
					new Object[] {Objects.toString(data), this});
		}
		
		Object widgetData = getWidgetData(sheet,data);
		if(widgetData==null){
			//TODO ignore it or throw exception?
			return;
		}


		WidgetUpdateEvent event = new WidgetUpdateEvent(Events.ON_WIDGET_UPDATE, comp, sheet, action, widgetData, anchor);
		
		org.zkoss.zk.ui.event.Events.postEvent(event);
	}

	private SheetAnchor getAnchor(Map data) {
		int dx1 = AuDataUtil.getInt(data,"dx1");
		int dy1 = AuDataUtil.getInt(data,"dy1");
		int dx2 = AuDataUtil.getInt(data,"dx2");
		int dy2 = AuDataUtil.getInt(data,"dy2");
		int col1 = (Integer) data.get("col1");
		int row1 = (Integer) data.get("row1");
		int col2 = (Integer) data.get("col2");
		int row2 = (Integer) data.get("row2");
		return new SheetAnchor(row1, col1, dx1, dy1, row2, col2, dx2, dy2);
	}

	private Object getWidgetData(Sheet sheet,Map data) {
		String id = (String) data.get("wgtId");
		String widgetType = (String) data.get("wgtType");
		if ("image".equals(widgetType)) {
			for(Picture p:sheet.getPictures()){
				if(p.getId().equals(id)){
					return p;
				}
			}
			
		} else if ("chart".equals(widgetType)) {
			for(Chart c:sheet.getCharts()){
				if(c.getId().equals(id)){
					return c;
				}
			}
		}
		return null;
	}
}
