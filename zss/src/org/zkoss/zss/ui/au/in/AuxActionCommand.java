/* AuxActionCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Feb 13, 2012 12:37:46 PM , Created by sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import java.util.Map;

import org.zkoss.lang.Objects;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.AuxAction;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.AuxActionEvent;
import org.zkoss.zss.ui.event.Events;

/**
 * @author sam
 * @author dennis
 */
public class AuxActionCommand extends AbstractCommand implements Command {

	@Override
	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, AuxActionCommand.class);
		
		final Map data = (Map) request.getData();
		if (data == null || data.size() < 2)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), AuxActionCommand.class });
		
		Spreadsheet spreadsheet = ((Spreadsheet) comp);
		String tag = (String) data.get("tag");
		String action = (String) data.get("action");
		AreaRef selection = getSelectionIfAny(data);
		Sheet sheet = null;
		
		if(selection==null){
			selection = spreadsheet.getSelection();
		}
		
		//old code logic refer to ActionCommand in 2.6.0
		if ("sheet".equals(tag) && spreadsheet.getSBook() != null) {
			String sheetId = (String) data.get("sheetId");
			//don't get form spreadsheet, it (should be)is possible doing on non-selected sheet
			sheet = getSheetByUuid(spreadsheet.getBook(), sheetId);
			
			if(sheet==null){
				//not found, it is possible been deleted.?
				return;
			}

			// client's act doesn't follow the Action, so I have to remap it.
			// TODO make client use correct key directly?
			if ("add".equals(action)) {
				action = AuxAction.ADD_SHEET.getAction();
			} else if ("delete".equals(action)) {
				action = AuxAction.DELETE_SHEET.getAction();
			} else if ("rename".equals(action)) {
				action = AuxAction.RENAME_SHEET.getAction();
			} else if ("copy".equals(action)) {
				action = AuxAction.COPY_SHEET.getAction();
			} else if ("protect".equals(action)) {
				action = AuxAction.PROTECT_SHEET.getAction();
			} else if ("hide".equals(action)) {
				action = AuxAction.HIDE_SHEET.getAction();
			} else if ("unhide".equals(action)) {
				action = AuxAction.UNHIDE_SHEET.getAction();
			} else if ("moveLeft".equals(action)) {
				action = AuxAction.MOVE_SHEET_LEFT.getAction();
			} else if ("moveRight".equals(action)) {
				action = AuxAction.MOVE_SHEET_RIGHT.getAction();
			} else {
				throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
						new Object[] { Objects.toString(data),
								AuxActionCommand.class });
			}
		}else if ("toolbar".equals(tag)) {
			sheet = spreadsheet.getSelectedSheet();
		}else if ("column".equals(tag) && spreadsheet.getSBook() != null) { //ZSS-1082
			String sheetId = (String) data.get("sheetId");
			//don't get form spreadsheet, it (should be)is possible doing on non-selected sheet
			sheet = getSheetByUuid(spreadsheet.getBook(), sheetId);
			
			if(sheet==null){
				//not found, it is possible been deleted.?
				return;
			}

			// client's act doesn't follow the Action, so I have to remap it.
			// TODO make client use correct key directly?
			if ("add".equals(action)) {
				action = AuxAction.ADD_COLUMN.getAction();
			}
		}else if ("row".equals(tag)) { //ZSS-1082
			String sheetId = (String) data.get("sheetId");
			//don't get form spreadsheet, it (should be)is possible doing on non-selected sheet
			sheet = getSheetByUuid(spreadsheet.getBook(), sheetId);
			
			if(sheet==null){
				//not found, it is possible been deleted.?
				return;
			}

			// client's act doesn't follow the Action, so I have to remap it.
			// TODO make client use correct key directly?
			if ("add".equals(action)) {
				action = AuxAction.ADD_ROW.getAction();
			}
		}else{
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), AuxActionCommand.class });
		}
		
		AuxActionEvent evt = new AuxActionEvent(Events.ON_AUX_ACTION, spreadsheet, sheet, action, selection,data);
		
		org.zkoss.zk.ui.event.Events.postEvent(evt);
	}
	
	private AreaRef getSelectionIfAny(Map data) {
		if(data.containsKey("tRow") && data.containsKey("tRow") && data.containsKey("tRow") && data.containsKey("tRow")){
			int tRow = (Integer) data.get("tRow");
			int bRow = (Integer) data.get("bRow");
			int lCol = (Integer) data.get("lCol");
			int rCol = (Integer) data.get("rCol");
			AreaRef r = new AreaRef(tRow, lCol, bRow, rCol);
			return r;
		}else{
			return null;
		}
	}
}
