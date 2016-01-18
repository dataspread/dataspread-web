/* DeleteCellAction.java

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

import org.zkoss.lang.Objects;
import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.IllegalOpArgumentException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Range.DeleteShift;
import org.zkoss.zss.api.Range.InsertCopyOrigin;
import org.zkoss.zss.api.Range.InsertShift;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.impl.undo.ReserveUtil.ReservedResult;
/**
 * 
 * @author dennis
 *
 */
public class DeleteCellAction extends AbstractUndoableAction {
	
	private DeleteShift _shift;
	private ReservedResult _reserve = null;
	private boolean _doFlag;
	
	
	public DeleteCellAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,DeleteShift shift){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._shift = shift;
	}

	@Override
	public void doAction() {
		if(isSheetProtected()) return;
		_doFlag = true;
		Range r = Ranges.range(_sheet, _row, _column, _lastRow, _lastColumn);
		if(r.isWholeRow() && r.isWholeColumn()){
			throw new IllegalOpArgumentException("doesn't support to delete all");
		}
		
		_reserve = ReserveUtil.reserve(_sheet.getInternalSheet(), _row, _column, _lastRow, _lastColumn,ReserveUtil.RESERVE_ALL);
		
		CellOperationUtil.delete(r,_shift);
	}

	@Override
	public boolean isUndoable() {
		return _doFlag && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public boolean isRedoable() {
		return !_doFlag && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public void undoAction() {
		if(isSheetProtected()) return;
		Range r = Ranges.range(_sheet, _row, _column, _lastRow, _lastColumn);
		switch(_shift){
			case UP:
				CellOperationUtil.insert(r,InsertShift.DOWN,InsertCopyOrigin.FORMAT_NONE);
				break;
			case LEFT:
				CellOperationUtil.insert(r,InsertShift.RIGHT,InsertCopyOrigin.FORMAT_NONE);
				break;
			case DEFAULT:
				CellOperationUtil.insert(r,InsertShift.DEFAULT,InsertCopyOrigin.FORMAT_NONE);
				break;
		}
		
		_reserve.restore();
		_reserve = null;
		_doFlag = false;
	}
	
	@Override
	protected boolean isSheetProtected(){
		final Range range = Ranges.range(_sheet);
		return super.isSheetProtected() && 
				!(Objects.equals(Labels.getLabel("zss.undo.deleteColumn"), getLabel()) &&
						range.getSheetProtection().isDeleteColumnsAllowed()) &&
				!(Objects.equals(Labels.getLabel("zss.undo.deleteRow"), getLabel()) &&
						range.getSheetProtection().isDeleteRowsAllowed());
	}

}
