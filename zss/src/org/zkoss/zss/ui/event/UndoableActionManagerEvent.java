/* UndoableActionEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/6 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zss.ui.sys.UndoableAction;

/**
 * @author dennis
 *
 */
public class UndoableActionManagerEvent extends Event {
	private static final long serialVersionUID = 1L;

	public enum Type{
		DO,UNDO,REDO,CLEAR
	}
	
	private Type _type;
	
	public UndoableActionManagerEvent(String name, Component target, Type type,UndoableAction action) {
		super(name, target, action);
		this._type = type;
	}
	
	public UndoableAction getAction(){
		return (UndoableAction)getData();
	}
	
	public Type getType(){
		return _type;
	}

}
