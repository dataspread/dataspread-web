/* InnerEvts.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 1, 2012 10:25:55 AM , Created by sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.zss.ui.au.in.AuxActionCommand;
import org.zkoss.zss.ui.au.in.BlockSyncCommand;
import org.zkoss.zss.ui.au.in.CellFetchCommand;
import org.zkoss.zss.ui.au.in.CellFocusedCommand;
import org.zkoss.zss.ui.au.in.CellMouseCommand;
import org.zkoss.zss.ui.au.in.CellSelectionCommand;
import org.zkoss.zss.ui.au.in.Command;
import org.zkoss.zss.ui.au.in.CtrlKeyCommand;
import org.zkoss.zss.ui.au.in.EditboxEditingCommand;
import org.zkoss.zss.ui.au.in.FetchActiveRangeCommand;
import org.zkoss.zss.ui.au.in.FilterCommand;
import org.zkoss.zss.ui.au.in.HeaderUpdateCommand;
import org.zkoss.zss.ui.au.in.HeaderMouseCommand;
import org.zkoss.zss.ui.au.in.ShiftPosCommand;
import org.zkoss.zss.ui.au.in.WidgetUpdateCommand;
import org.zkoss.zss.ui.au.in.SelectSheetCommand;
import org.zkoss.zss.ui.au.in.CellSelectionUpdateCommand;
import org.zkoss.zss.ui.au.in.StartEditingCommand;
import org.zkoss.zss.ui.au.in.StopEditingCommand;
import org.zkoss.zss.ui.au.in.CtrlArrowCommand;
import org.zkoss.zss.ui.event.Events;

/**
 * @author sam / Ian
 *
 */
/*package*/ final class InnerEvts {
	private InnerEvts() {}
	
	static final String ON_ZSS_CELL_FETCH = "onZSSCellFetch";
	static final String ON_ZSS_CELL_MOUSE = "onZSSCellMouse";
	static final String ON_ZSS_FETCH_ACTIVE_RANGE = "onZSSFetchActiveRange";
	static final String ON_ZSS_FILTER = "onZSSFilter";
//	static final String ON_ZSS_HEADER_MODIFY = "onZSSHeaderModify";
	static final String ON_ZSS_HEADER_MOUSE = "onZSSHeaderMouse";
	static final String ON_ZSS_SYNC_BLOCK = "onZSSSyncBlock";
	
	static final String ON_ZSS_CTRL_ARROW = "onZSSCtrlArrow"; //ZSS-1000
	static final String ON_ZSS_SHIFT_POS = "onZSSShiftPos"; //ZSS-1085
	
	static final Map<String, Command> CMDS;
	static{
		CMDS = new HashMap<String, Command>();
		
		//onCellSelection, also update component selection 
		//-> onCellSelection
		CMDS.put(Events.ON_CELL_SELECTION, new CellSelectionCommand());
		//onCellSelectionUpdate, update cell selection 
		//-> onCellSelectionUpdate
		CMDS.put(Events.ON_CELL_SELECTION_UPDATE, new CellSelectionUpdateCommand());
				
		//onCellFocus, also update cell focus 
		//-> onCellFocus
		CMDS.put(Events.ON_CELL_FOUCS, new CellFocusedCommand());
		
		//onEditBoxEditing 
		//-> onEditBoxEditing
		CMDS.put(Events.ON_EDITBOX_EDITING, new EditboxEditingCommand());
		
		//onCtrlKey 
		//-> on CtrlKey
		CMDS.put(Events.ON_CTRL_KEY, new CtrlKeyCommand());
		
		//onStartEditing
		//->onStartEditing -> onStartEditingImpl(zss internal listen to)
		CMDS.put(Events.ON_START_EDITING, new StartEditingCommand());
		
		//onStopEditing
		//->onStopEditing -> onStopEditingImpl(zss internal listen to)
		CMDS.put(Events.ON_STOP_EDITING, new StopEditingCommand());
		
		//onAuxAction
		CMDS.put(Events.ON_AUX_ACTION, new AuxActionCommand());
		
		//onWidgetUpdate 
		CMDS.put(Events.ON_WIDGET_UPDATE, new WidgetUpdateCommand());
		
		//onSheetSelected , set selected sheet 
		//-> onSheetSelected
		CMDS.put(Events.ON_SHEET_SELECT, new SelectSheetCommand());
		
		// onZssCellMouse 
		// -> ON_CELL_CLICK,ON_CELL_RIGHT_CLICK,ON_CELL_DOUBLE_CLICK
		// or -> (default processing) - > ON_CELL_FILTER , 
		// or -> ON_CELL_VALIDATOR
		CMDS.put(ON_ZSS_CELL_MOUSE, new CellMouseCommand());
		
		//onZssHaderModify, set row or column size of component 
		//-> onHeaderSize
		CMDS.put(Events.ON_HEADER_UPDATE, new HeaderUpdateCommand());
		
		//onZssHeaderMouse -> ON_HEADER_CLICK,ON_HEADER_RIGHT_CLICK,ON_HEADER_DOUBLE_CLICK
		CMDS.put(ON_ZSS_HEADER_MOUSE, new HeaderMouseCommand());

		//wire event, need to review
		CMDS.put(ON_ZSS_FILTER, new FilterCommand());//TODO review
		
		//internal client au
		CMDS.put(ON_ZSS_CELL_FETCH, new CellFetchCommand());
		CMDS.put(ON_ZSS_FETCH_ACTIVE_RANGE, new FetchActiveRangeCommand());
		CMDS.put(ON_ZSS_SYNC_BLOCK, new BlockSyncCommand());
		
		//ZSS-1000
		CMDS.put(ON_ZSS_CTRL_ARROW, new CtrlArrowCommand());
		//ZSS-1085
		CMDS.put(ON_ZSS_SHIFT_POS, new ShiftPosCommand());
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	static Command getCommand(String name){
		return CMDS.get(name);
	}
}
