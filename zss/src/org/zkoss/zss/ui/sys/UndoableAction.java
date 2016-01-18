/* UndoableAction.java

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

import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
/**
 * 
 * @author dennis
 *
 */
public interface UndoableAction{

	/**
	 * 
	 * @return the label of this action
	 */
	public String getLabel();
	
	/**
	 * do the action, either first time or redo
	 */
	public void doAction();
	
	/**
	 * Check if still undoable or not
	 * @return
	 */
	public boolean isUndoable();
	
	/**
	 * Check if still redoable or not
	 * @return
	 */
	public boolean isRedoable();
	
	/**
	 * Undo the action
	 */
	public void undoAction();
	
	/**
	 * 
	 * @return Selection after undo of this action, null if doesn't provided;
	 */
	public AreaRef getUndoSelection();
	
	/**
	 * 
	 * @return Selection after redo of this action, null if doesn't provided;
	 */
	public AreaRef getRedoSelection();
	
	/**
	 * @return target sheet of undo action, null if doesn't provided
	 * @return
	 */
	public Sheet getUndoSheet();
	
	/**
	 * @return target sheet of redo action, null if doesn't provided
	 * @return
	 */
	public Sheet getRedoSheet();
}
