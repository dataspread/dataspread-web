/* CellSelectionUpdateEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 19, 2007 2:18:10 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.CellSelectionType;
/**
 * Event about selection update of cells
 * @author Dennis.Chen
 * @since 3.0.0
 */
public class CellSelectionUpdateEvent extends CellSelectionEvent{

	private static final long serialVersionUID = 1L;

	
	private CellSelectionAction _action;
	private int _olCol;
	private int _otRow;
	private int _orCol;
	private int _obRow;

	public CellSelectionUpdateEvent(String name, Component target,Sheet sheet,
			int tRow, int lCol, int bRow, int rCol,
			int otRow, int olCol, int obRow, int orCol,CellSelectionType type, CellSelectionAction action) {
		super(name, target, sheet,tRow,lCol,bRow,rCol,type);
		_action = action;
		_olCol = olCol;
		_otRow = otRow;
		_orCol = orCol;
		_obRow = obRow;
	}

	/**
	 * Returns the action of this event.
	 */
	public CellSelectionAction getAction() {
		return _action;
	}
	
	/**
	 * @deprecated use {@link #getOrigColumn()}
	 */
	@Deprecated
	public int getOrigleft() {
		return getOrigColumn();
	}

	/**
	 * @deprecated use {@link #getOrigRow()}
	 */
	@Deprecated
	public int getOrigtop() {
		return getOrigRow();
	}

	/**
	 * @deprecated use {@link #getOrigLastColumn()}
	 */
	@Deprecated
	public int getOrigright() {
		return getOrigLastColumn();
	}

	/**
	 * @deprecated use {@link #getOrigLastRow()}
	 */
	@Deprecated
	public int getOrigbottom() {
		return getOrigLastRow();
	}
	
	
	public int getOrigRow() {
		return _otRow;
	}

	public int getOrigColumn() {
		return _olCol;
	}

	public int getOrigLastRow() {
		return _obRow;
	}

	public int getOrigLastColumn() {
		return _orCol;
	}	
	
}
