/* HeaderSizeAction.java

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

import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.IllegalFormulaException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.Sheet;
/**
 * 
 * @author dennis
 *
 */
public class ResizeHeaderAction extends AbstractUndoableAction {

	public enum Type{
		ROW,COLUMN
	}
	
	int _oldSizes[];
	
	final Type _type;
	final int _size;
	boolean _isCustom = true;
	
	
	
	public ResizeHeaderAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,Type type,int size, boolean isCustom){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._type = type;
		this._size = size;
		_isCustom = isCustom;
	}

	@Override
	public void doAction() {
		if(isSheetProtected()) return;
		//keep old size
		if(_type==Type.ROW){
			_oldSizes = new int[_lastRow-_row+1];
			for(int i=_row;i<=_lastRow;i++){
				_oldSizes[i-_row] = _sheet.getRowHeight(i);
			}
			CellOperationUtil.setRowHeight(Ranges.range(_sheet,_row,0,_lastRow,0).toRowRange(),_size, _isCustom);
		}else{
			_oldSizes = new int[_lastColumn-_column+1];
			for(int i=_column;i<=_lastColumn;i++){
				_oldSizes[i-_column] = _sheet.getColumnWidth(i);
			}
			CellOperationUtil.setColumnWidth(Ranges.range(_sheet,0,_column,0,_lastColumn).toColumnRange(),_size);
		}
	}

	@Override
	public boolean isUndoable() {
		return _oldSizes!=null && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public boolean isRedoable() {
		return _oldSizes==null && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public void undoAction() {
		if(isSheetProtected()) return;
		if(_oldSizes!=null){
			if(_type==Type.ROW){
				for(int i=_row;i<=_lastRow;i++){
					CellOperationUtil.setRowHeight(Ranges.range(_sheet,i,0,i,0).toRowRange(),_oldSizes[i-_row], _isCustom);
				}
			}else{
				for(int i=_column;i<=_lastColumn;i++){
					CellOperationUtil.setColumnWidth(Ranges.range(_sheet,0,i,0,i).toColumnRange(),_oldSizes[i-_column]);
				}
			}
			_oldSizes = null;
		}
	}
	
	@Override
	protected boolean isSheetProtected() {
		final Range range = Ranges.range(_sheet);
		return super.isSheetProtected() &&
				!(_type == Type.COLUMN && range.getSheetProtection().isFormatColumnsAllowed()) &&
				!(_type == Type.ROW && range.getSheetProtection().isFormatRowsAllowed());
	}
}
