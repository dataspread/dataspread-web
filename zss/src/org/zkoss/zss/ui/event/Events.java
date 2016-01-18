/* Events.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 19, 2007 12:48:06 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.event;




/**
 * @author Dennis.Chen
 *
 */
public class Events {
	
	/** The onCellFocus event (with {@link CellEvent}).
	 * Sent when cell get focus from client.
	 */
	public static final String ON_CELL_FOUCS = "onCellFocus";
	
	/** The onStartEditing event (with {@link StartEditingEvent}).
	 * Sent when cell start editing.
	 */
	public static final String ON_START_EDITING = "onStartEditing";
	
	
	/** The onStopEditing event (with {@link StopEditingEvent}).
	 * Sent when cell stop editing
	 */
	public static final String ON_STOP_EDITING = "onStopEditing";
	
	/**
	 * The onCellClick event (with {@link CellMouseEvent}).
	 * Sent when user left click on a cell
	 */
	public static final String ON_CELL_CLICK = "onCellClick";
	
	/**
	 * The onCellRightClick event (with {@link CellMouseEvent}).
	 * Sent when user right click on a cell
	 */
	public static final String ON_CELL_RIGHT_CLICK = "onCellRightClick";
	
	/**
	 * The onCellDoubleClick event (with {@link CellMouseEvent}).
	 * Sent when user double click on a cell
	 */
	public static final String ON_CELL_DOUBLE_CLICK = "onCellDoubleClick";
	
	/**
	 * The onCellFilter event (with {@link CellMouseEvent}).
	 * Sent when user click on the cell filter button.
	 */
	public static final String ON_CELL_FILTER = "onCellFilter";
	
	/**
	 * The onCellValidator event (with {@link CellMouseEvent}).
	 * Sent when user click on the cell validation drop down button
	 */
	public static final String ON_CELL_VALIDATOR = "onCellValidator";
	
	/**
	 * The onHeaderClick event (with {@link HeaderMouseEvent}).
	 * Sent when user left click on a header
	 */
	public static final String ON_HEADER_CLICK = "onHeaderClick";
	
	/**
	 * The onHeaderRightClick event (with {@link HeaderMouseEvent}).
	 * Sent when user right click on a header
	 */
	public static final String ON_HEADER_RIGHT_CLICK = "onHeaderRightClick";
	
	/**
	 * The onHeaderDoubleClick event (with {@link HeaderMouseEvent}).
	 * Sent when user double click on a header
	 */
	public static final String ON_HEADER_DOUBLE_CLICK = "onHeaderDoubleClick";
	
	/** 
	 * The onHeaderSzie event (with {@link HeaderUpdateEvent}).
	 * Sent when user resize a header
	 */
	public static final String ON_HEADER_UPDATE = "onHeaderUpdate";
	
	/**
	 * The onCellSelection event (with {@link CellSelectionEvent}).
	 * Sent when user select a row/column or a range of cells
	 */
	public static final String ON_CELL_SELECTION = "onCellSelection";
	
	/**
	 * The onCellSelectionUpdate event (with {@link CellSelectionUpdateEvent}).
	 * Sent when user move or modify the range of a selection
	 */
	public static final String ON_CELL_SELECTION_UPDATE = "onCellSelectionUpdate";
	
	/**
	 * The onEditboxEditing event (with {@link EditboxEditingEvent}).
	 * Sent when user start to typing in the ZSSEditbox
	 */
	public static final String ON_EDITBOX_EDITING = "onEditboxEditing";
	
	/**
	 * The onCellHyperlink event (with {@link CellHyperlinkEvent}).
	 * Sent when user click on the hyperlink of a cell.
	 */
	public static final String ON_CELL_HYPERLINK = "onCellHyperlink";
	/**
	 * The onSheetSelect event (with {@link SheetSelectEvent}
	 * Sent when sheet is selected.
	 */
	public static final String ON_SHEET_SELECT = "onSheetSelect";
	
	/**
	 * The onCtrlKey event (with {@link KeyEvent})
	 */
	public static final String ON_CTRL_KEY = org.zkoss.zk.ui.event.Events.ON_CTRL_KEY;
	
	/**
	 * The onAuxAction event (with {@link AuxActionEvent})
	 */
	public static final String ON_AUX_ACTION = "onAuxAction";
	
	
	/**
	 * The onWidgetCtrlKey event (with {@link WidgetKeyEvent})
	 */
	public static final String ON_WIDGET_CTRL_KEY = "onWidgetCtrlKey";
	
	/**
	 * The onWidgetUpdate event (with {@link WidgetUpdateEvent})
	 */
	public static final String ON_WIDGET_UPDATE = "onWidgetUpdate";//"onZSSMoveWidget";
	
	
	/**
	 * This is event is sent after do/undo/redo a undoable action by a undoable action manager, or clear undo manager
	 * @see UndoableActionManagerEvent
	 * @since 3.0.0
	 */
	public static final String ON_AFTER_UNDOABLE_MANAGER_ACTION = "onAfterUndoableManagerAction";
	
	/* 
	 * Following are events that fire by book SSDataEvent and delegate to Sparedsheet to provide to component user.
	 * TODO consider to let user register listener on book directly or wrap more event 
	 */

	/**
	 * This event is sent after sheet's name is changed by operating the book model.
	 * @see SheetEvent
	 * @since 3.0.0
	 */
	public static final String ON_AFTER_SHEET_NAME_CHANGE = "onAfterSheetNameChange";
	/**
	 * This event is sent after sheet's visible is changed by operating the book model.
	 * @see SheetEvent
	 * @since 3.7.0
	 */
	public static final String ON_AFTER_SHEET_VISIBLE_CHANGE = "onAfterSheetVisibleChange"; //ZSS-832
	
	/**
	 * This event is sent after sheet's order is changed by operating the book model.
	 * @see SheetEvent
	 * @since 3.0.0
	 */
	public static final String ON_AFTER_SHEET_ORDER_CHANGE = "onAfterSheetOrderChange";
	
	/**
	 * This event is sent after sheet is deleted by operating the book model.
	 * @see SheetDeleteEvent
	 * @since 3.0.0
	 */
	public static final String ON_AFTER_SHEET_DELETE = "onAfterSheetDelete";

	/**
	 * This event is sent after sheet is created by operating the book model.
	 * @see SheetEvent
	 * @since 3.0.0
	 */
	public static final String ON_AFTER_SHEET_CREATE = "onAfterSheetCreate";
		
	/** 
	 * This event is sent after cell contents changed by operating the book model.
	 * @see CellAreaEvent
	 * @since 3.0.0
	 */
	public static final String ON_AFTER_CELL_CHANGE = "onAfterCellChange";

	/**
	 * This event is sent after friend focus is added/deleted/moved.
	 * @see SyncFriendFocusEvent
	 * @since 3.8.1
	 */
	public static final String ON_SYNC_FRIEND_FOCUS = "onSyncFriendFocus";
	
	/*
	 * end of book delegation event  
	 */
	
}
