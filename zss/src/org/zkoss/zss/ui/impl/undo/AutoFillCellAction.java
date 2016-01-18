/* AutofillCellAction.java

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
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.Range.AutoFillType;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.impl.undo.ReserveUtil.ReservedResult;

/**
 * 
 * @author dennis
 * 
 */
public class AutoFillCellAction extends Abstract2DCellDataStyleAction {

	private final AutoFillType _fillType;
	
	public AutoFillCellAction(String label,Sheet sheet,int srcRow, int srcColumn, int srcLastRow,int srcLastColumn,
			Sheet destSheet,int destRow, int destColumn, int destLastRow,int destLastColumn,AutoFillType fillType){
		super(label,sheet,srcRow,srcColumn,srcLastRow,srcLastColumn,destSheet,destRow,destColumn,destLastRow,destLastColumn,RESERVE_ALL);
		_fillType = fillType;
	}

	
	protected void applyAction(){
		Range src = Ranges.range(_sheet,_row,_column,_lastRow,_lastColumn);
		Range dest = Ranges.range(_destSheet,_destRow,_destColumn,_destLastRow,_destLastColumn);
		CellOperationUtil.autoFill(src, dest, _fillType);
	}

}
