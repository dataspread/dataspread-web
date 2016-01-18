/* AbstractCellStyleAction.java

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


import org.zkoss.zss.api.IllegalFormulaException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.impl.undo.ReserveUtil.ReservedResult;
/**
 * abstract class handle src content/style reservation
 * @author dennis
 *
 */
public abstract class AbstractCellDataStyleAction extends AbstractUndoableAction {

	public static final int RESERVE_CONTENT = ReserveUtil.RESERVE_CONTENT;
	public static final int RESERVE_STYLE = ReserveUtil.RESERVE_STYLE;
	public static final int RESERVE_MERGE = ReserveUtil.RESERVE_MERGE;
	public static final int RESERVE_ALL = ReserveUtil.RESERVE_ALL;
	
	/**
	 * 
	 * @see #RESERVE_ALL
	 * @see #RESERVE_STYLE
	 * @see #RESERVE_CONTENT
	 * @see #RESERVE_MERGE
	 */
	private final int _reserveType;
	
	ReservedResult _oldReserve;
	ReservedResult _newReserve;
	
	public AbstractCellDataStyleAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,int reserveType){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._reserveType=reserveType;
	}
	
	protected int getReservedRow(){
		return _row;
	}
	protected int getReservedColumn(){
		return _column;
	}
	protected int getReservedLastRow(){
		return _lastRow;
	}
	protected int getReservedLastColumn(){
		return _lastColumn;
	}
	protected Sheet getReservedSheet(){
		return _sheet;
	}

	@Override
	public void doAction() {
		if(isSheetProtected()) return;
		//keep old style

		int row = getReservedRow();
		int column = getReservedColumn();
		int lastRow = getReservedLastRow();
		int lastColumn = getReservedLastColumn();
		Sheet sheet = getReservedSheet();
		
		_oldReserve = ReserveUtil.reserve(sheet.getInternalSheet(), row, column, lastRow, lastColumn, _reserveType);
		
		if(_newReserve!=null){//reuse the style
			_newReserve.restore();
			_newReserve = null;
		}else{
			//first time
			applyAction();
		}
	}

	
	protected abstract void applyAction();
	
	@Override
	public boolean isUndoable() {
		return _oldReserve!=null && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public boolean isRedoable() {
		return _oldReserve==null && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public void undoAction() {
		if(isSheetProtected()) return;
		
		int row = getReservedRow();
		int column = getReservedColumn();
		int lastRow = getReservedLastRow();
		int lastColumn = getReservedLastColumn();
		Sheet sheet = getReservedSheet();
		//keep last new style, so if redo-again, we will reuse it.
		
		_newReserve = ReserveUtil.reserve(sheet.getInternalSheet(), row, column, lastRow, lastColumn, _reserveType);
		
		_oldReserve.restore();
		_oldReserve = null;
	}
	
	@Override
	protected boolean isSheetProtected(){
		return super.isSheetProtected() && !Ranges.range(_sheet).getSheetProtection().isFormatCellsAllowed();
	}

}
