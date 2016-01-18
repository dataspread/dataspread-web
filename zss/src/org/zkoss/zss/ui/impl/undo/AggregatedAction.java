/* AggregatedAction.java

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
package org.zkoss.zss.ui.impl.undo;

import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.sys.UndoableAction;
/**
 * 
 * @author dennis
 *
 */
public class AggregatedAction implements UndoableAction {

	private final UndoableAction[] _actions;
	private final String _label;
	
	public AggregatedAction(String label,UndoableAction[] actions){
		this._label = label;
		this._actions = actions;
		
	}

	@Override
	public String getLabel() {
		return _label;
	}

	@Override
	public void doAction() {
		for(int i=0;i<_actions.length;i++){
			try{
				_actions[i].doAction();
			}catch(RuntimeException x){
				//TODO, how to keep atomic?
			}
		}
	}

	@Override
	public boolean isUndoable() {
		for(UndoableAction a:_actions){
			if(!a.isUndoable()){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isRedoable() {
		for(UndoableAction a:_actions){
			if(!a.isRedoable()){
				return false;
			}
		}
		return true;
	}

	@Override
	public void undoAction() {
		for(int i=_actions.length-1;i>=0;i--){
			_actions[i].undoAction();
		}
	}

	@Override
	public AreaRef getUndoSelection() {
		for(int i=0;i<_actions.length;i++){
			AreaRef sel = _actions[i].getUndoSelection();
			if(sel!=null){
				return sel;
			}
		}
		return null;
	}
	@Override
	public AreaRef getRedoSelection() {
		for(int i=_actions.length-1;i>=0;i--){
			AreaRef sel = _actions[i].getRedoSelection();
			if(sel!=null){
				return sel;
			}
		}
		return null;
	}

	@Override
	public Sheet getUndoSheet() {
		for(int i=0;i<_actions.length;i++){
			Sheet sheet = _actions[i].getUndoSheet();
			if(sheet!=null){
				return sheet;
			}
		}
		return null;
	}

	@Override
	public Sheet getRedoSheet() {
		for(int i=_actions.length-1;i>=0;i--){
			Sheet sheet = _actions[i].getRedoSheet();
			if(sheet!=null){
				return sheet;
			}
		}
		return null;
	}
	
}
