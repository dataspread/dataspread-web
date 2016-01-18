/* HeaderCommand.java

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


import java.util.Map;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.HeaderAction;
import org.zkoss.zss.ui.event.HeaderUpdateEvent;
import org.zkoss.zss.ui.event.HeaderType;
import org.zkoss.zss.ui.sys.SpreadsheetInCtrl;


/**
 * A Command (client to server) for handling event about a header(top header or left header) 
 * @author Dennis.Chen
 *
 */
public class HeaderUpdateCommand extends AbstractCommand implements Command {

	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		final Map data = (Map) request.getData();
		
		String type = (String) data.get("type");
		if ("top".equals(type)) {
			processTopHeader((Spreadsheet) comp, data);
		} else if ("left".equals(type)) {
			processLeftHeader((Spreadsheet) comp, data);
		} else {
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {
					"Type:" + type, this });
		}
	}
	
	private void processTopHeader(Spreadsheet spreadsheet, Map data){
		String sheetId = (String) data.get("sheetId");
		SSheet sheet = spreadsheet.getSelectedSSheet();
		if(!sheet.getId().equals(sheetId)) {
			return;
		}
		String action = (String) data.get("action");
		int index = (Integer) data.get("index");
		
		if("resize".equals(action)){
			int newsize = AuDataUtil.getInt(data,"size");
			int id = (Integer) data.get("id");
			boolean hidden = (Boolean) data.get("hidden");
			((SpreadsheetInCtrl)spreadsheet.getExtraCtrl()).setColumnSize(sheetId, index, newsize,id, hidden);
			
			if (Events.isListened(spreadsheet, org.zkoss.zss.ui.event.Events.ON_HEADER_UPDATE, true)){
				HeaderUpdateEvent he = new HeaderUpdateEvent(
						org.zkoss.zss.ui.event.Events.ON_HEADER_UPDATE,
						spreadsheet, sheet, HeaderType.COLUMN, HeaderAction.RESIZE, index, newsize,
						hidden);
				Events.postEvent(he);
			}
		}else{
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {"action:"+action, this});
		}
		
	}
	
	private void processLeftHeader(Spreadsheet spreadsheet, Map data){
		String sheetId = (String) data.get("sheetId");
		SSheet sheet = spreadsheet.getSelectedSSheet();
		if(!sheet.getId().equals(sheetId)) {
			return;
		}
		String action = (String) data.get("action");
		int index = (Integer) data.get("index");
		if("resize".equals(action)){
			//ZSS-440, might get double in IE 10 when room
			int newsize = AuDataUtil.getInt(data,"size");
			int id = (Integer) data.get("id");
			boolean hidden = (Boolean) data.get("hidden");
			boolean isCustom= (Boolean) data.get("custom");
			((SpreadsheetInCtrl)spreadsheet.getExtraCtrl()).setRowSize(sheetId, index, newsize,id, hidden, isCustom);
			if (Events.isListened(spreadsheet,org.zkoss.zss.ui.event.Events.ON_HEADER_UPDATE, true)){
				HeaderUpdateEvent he = new HeaderUpdateEvent(
						org.zkoss.zss.ui.event.Events.ON_HEADER_UPDATE,
						spreadsheet, sheet, HeaderType.ROW, HeaderAction.RESIZE,index, newsize,
						hidden);
				Events.postEvent(he);
			}
		} else {
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {"action:"+action, this});
		}
	}
}