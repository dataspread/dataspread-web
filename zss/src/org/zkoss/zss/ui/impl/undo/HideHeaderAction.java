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
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.impl.undo.ResizeHeaderAction.Type;
/**
 * 
 * @author dennis
 *
 */
public class HideHeaderAction extends AbstractUndoableAction {

	public enum Type{
		ROW,COLUMN
	}
	
	boolean _oldHiddens[];
	
	final Type _type;
	final boolean _hidden;
	
	
	
	public HideHeaderAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,Type type,boolean hidden){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._type = type;
		this._hidden = hidden;
	}

	@Override
	public void doAction() {
		if(isSheetProtected()) return;
		//keep old size
		if(_type==Type.ROW){
			_oldHiddens = new boolean[_lastRow-_row+1];
			for(int i=_row;i<=_lastRow;i++){
				_oldHiddens[i-_row] = _sheet.isRowHidden(i);
			}
			Range r = Ranges.range(_sheet,_row,0,_lastRow,0).toRowRange();
			if(_hidden){
				CellOperationUtil.hide(r);
			}else{
				CellOperationUtil.unhide(r);
			}
			
		}else{
			_oldHiddens = new boolean[_lastColumn-_column+1];
			for(int i=_column;i<=_lastColumn;i++){
				_oldHiddens[i-_column] = _sheet.isColumnHidden(i);
			}
			Range r = Ranges.range(_sheet,0,_column,0,_lastColumn).toColumnRange();
			if(_hidden){
				CellOperationUtil.hide(r);
			}else{
				CellOperationUtil.unhide(r);
			}
			
		}
	}

	@Override
	public boolean isUndoable() {
		return _oldHiddens!=null && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public boolean isRedoable() {
		return _oldHiddens==null && isSheetAvailable() && !isSheetProtected();
	}

	@Override
	public void undoAction() {
		if(isSheetProtected()) return;
		if(_oldHiddens!=null){
			if(_type==Type.ROW){
				for(int i=_row;i<=_lastRow;i++){
					Range r = Ranges.range(_sheet,i,0,i,0).toRowRange();
					if(_oldHiddens[i-_row]){
						CellOperationUtil.hide(r);
					}else{
						CellOperationUtil.unhide(r);
					}
				}
			}else{
				for(int i=_column;i<=_lastColumn;i++){
					Range r = Ranges.range(_sheet,0,i,0,i).toColumnRange();
					if(_oldHiddens[i-_column]){
						CellOperationUtil.hide(r);
					}else{
						CellOperationUtil.unhide(r);
					}
				}
			}
			_oldHiddens = null;
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
