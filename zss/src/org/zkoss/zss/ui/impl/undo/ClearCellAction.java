/* ClearCellAction.java

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
import org.zkoss.zss.api.CellOperationUtil.CellStyleApplier;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.Sheet;
/**
 * 
 * @author dennis
 *
 */
public class ClearCellAction extends AbstractCellDataStyleAction {

	
	public enum Type{
		CONTENT,
		STYLE,
		ALL
	}
	
	private CellStyle[][] oldStyles = null;
	private String[][] oldEditTexts = null;
	private Type _type;
	
	public ClearCellAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,Type type){
		super(label,sheet,row,column,lastRow,lastColumn,toReserveType(type));
		this._type = type;
	}

	private static int toReserveType(Type type) {
		switch(type){
		case CONTENT:
			return RESERVE_CONTENT;
		case STYLE:
			// ZSS-553: According to ZSS-298 - we remove merge when clear style, so we need to reserve merge when clear cell style.
			return RESERVE_STYLE | RESERVE_MERGE;
		case ALL:
			return RESERVE_ALL;
		}
		return RESERVE_ALL;
	}

	protected void applyAction(){
		Range r = Ranges.range(_sheet,_row,_column,_lastRow,_lastColumn);
		switch(_type){
		case CONTENT:
			CellOperationUtil.clearContents(r);
			break;
		case STYLE:
			CellOperationUtil.clearStyles(r);
			break;
		case ALL:
			CellOperationUtil.clearAll(r);
			break;
		}
	}
}
