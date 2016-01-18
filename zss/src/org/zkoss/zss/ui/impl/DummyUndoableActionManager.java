/* DummyUndoableActionManager.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/5 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.Events;
import org.zkoss.zss.ui.event.UndoableActionManagerEvent;
import org.zkoss.zss.ui.sys.UndoableAction;
import org.zkoss.zss.ui.sys.UndoableActionManager;

/**
 * A dummy implementation of undoable action manager
 * @author dennis
 *
 */
public class DummyUndoableActionManager implements UndoableActionManager {

	
	private Spreadsheet _spreadsheet;
	
	@Override
	public void doAction(UndoableAction action) {
		action.doAction();
		org.zkoss.zk.ui.event.Events.postEvent(new UndoableActionManagerEvent(
				Events.ON_AFTER_UNDOABLE_MANAGER_ACTION, _spreadsheet,
				UndoableActionManagerEvent.Type.DO, action));
	}

	@Override
	public boolean isUndoable() {
		return false;
	}
	@Override
	public String getUndoLabel() {
		return null;
	}


	@Override
	public void undoAction() {
	}

	@Override
	public boolean isRedoable() {
		return false;
	}

	@Override
	public String getRedoLabel() {
		return null;
	}

	@Override
	public void redoAction() {
	}

	@Override
	public void clear() {
	}

	@Override
	public void setMaxHsitorySize(int size) {
	}

	@Override
	public void bind(Spreadsheet spreadsheet) {
		_spreadsheet = spreadsheet;
	}

}
