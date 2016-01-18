/* UndoableActionManager.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/7/25, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package org.zkoss.zss.ui.sys;

import org.zkoss.zss.ui.Spreadsheet;

/**
 * the manager to control undoable action
 * 
 * @author dennis
 * 
 */
public interface UndoableActionManager {

	/**
	 * Do the action and put it into history
	 * @param action
	 */
	public void doAction(UndoableAction action);

	/**
	 * Is undoable
	 * @return
	 */
	public boolean isUndoable();

	/**
	 * Get undo label, only available if {@link #isUndoable()} 
	 * @return
	 */
	public String getUndoLabel();

	/**
	 * undo last action, only available if {@link #isUndoable()}
	 */
	public void undoAction();

	/**
	 * Is redoable
	 * @return
	 */
	public boolean isRedoable();

	/**
	 * Get redo label, only available if {@link #isRedoable()}
	 * @return
	 */
	public String getRedoLabel();

	/**
	 * redo the last undo action, only availabel if {@link #isRedoable()}
	 */
	public void redoAction();

	/**
	 * clear the history
	 */
	public void clear();
	
	/**
	 * Sets the maximun history size
	 * @param size
	 */
	public void setMaxHsitorySize(int size);
	
	
	/**
	 * Will be called when a manager create 
	 * @param sparedsheet
	 */
	void bind(Spreadsheet spreadsheet);
}
