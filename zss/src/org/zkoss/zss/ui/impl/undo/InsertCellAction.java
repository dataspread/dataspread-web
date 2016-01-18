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
import org.zkoss.zss.api.IllegalFormulaException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.Range.DeleteShift;
import org.zkoss.zss.api.Range.InsertCopyOrigin;
import org.zkoss.zss.api.Range.InsertShift;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.Sheet;
/**
 * 
 * @author dennis
 *
 */
public class InsertCellAction extends AbstractUndoableAction {
	
	InsertShift _shift;
	InsertCopyOrigin _copyOrigin;
	boolean _doFlag;
	
	
	public InsertCellAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,InsertShift shift,
			InsertCopyOrigin copyOrigin){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._shift = shift;
		this._copyOrigin = copyOrigin;
	}

	@Override
	public void doAction() {
		if(isSheetProtected()) return;
		_doFlag = true;
		Range r = Ranges.range(_sheet, _row, _column, _lastRow, _lastColumn);
		CellOperationUtil.insert(r,_shift, _copyOrigin);
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
			case DOWN:
				CellOperationUtil.delete(r,DeleteShift.UP);
				break;
			case RIGHT:
				CellOperationUtil.delete(r,DeleteShift.LEFT);
				break;
			case DEFAULT:
				CellOperationUtil.delete(r,DeleteShift.DEFAULT);
				break;
		}
		_doFlag = false;
	}

	@Override
	protected boolean isSheetProtected(){
		final Range range = Ranges.range(_sheet);
		return super.isSheetProtected() && 
				!(Objects.equals(Labels.getLabel("zss.undo.insertColumn"), getLabel()) &&
						range.getSheetProtection().isInsertColumnsAllowed()) &&
				!(Objects.equals(Labels.getLabel("zss.undo.insertRow"), getLabel()) &&
						range.getSheetProtection().isInsertRowsAllowed());
	}

}
