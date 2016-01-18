/* CellSortAction.java

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
import org.zkoss.zss.api.Range.SortDataOption;
import org.zkoss.zss.api.model.Sheet;
/**
 * 
 * @author dennis
 *
 */
public class SortCellAction extends AbstractCellDataStyleAction {
	
	private boolean _simpleMode;
	private final boolean _desc;
	
	private final Range _index1;
	private final boolean _desc1;
	private final SortDataOption _dataOption1;
	private final Range _index2;
	private final boolean _desc2;
	private final SortDataOption _dataOption2;
	private final Range _index3;
	private final boolean _desc3;
	private final SortDataOption _dataOption3;
	boolean _header;
	boolean _matchCase; 
	boolean _sortByRows; 
	
	
	public SortCellAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,boolean desc){
		super(label,sheet,row,column,lastRow,lastColumn,RESERVE_ALL);
		_simpleMode = true;
		this._desc = desc;
		
		_index1 = null;
		_desc1 = false;
		_dataOption1 = null;
		_index2 = null;
		_desc2 = false;
		_dataOption2 = null;
		_index3 = null;
		_desc3 = false;
		_dataOption3 = null;
		_header = false;
		_matchCase = false;
		_sortByRows = false;
		
	}
	public SortCellAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,
			Range index1,boolean desc1,SortDataOption dataOption1,
			Range index2,boolean desc2,SortDataOption dataOption2,
			Range index3,boolean desc3,SortDataOption dataOption3,
			boolean header,
			boolean matchCase, 
			boolean sortByRows 
			){
		super(label,sheet,row,column,lastRow,lastColumn,RESERVE_ALL);
		_simpleMode = false;
		_desc = false;
		
		_index1 = index1;
		_desc1 = desc1;
		_dataOption1 = dataOption1;
		_index2 = index2;
		_desc2 = desc2;
		_dataOption2 = dataOption2;
		_index3 = index3;
		_desc3 = desc3;
		_dataOption3 = dataOption3;
		_header = header;
		_matchCase = matchCase;
		_sortByRows = sortByRows;
	}

	protected void applyAction(){
		Range r = Ranges.range(_sheet,_row,_column,_lastRow,_lastColumn);
		if(_simpleMode){
			CellOperationUtil.sort(r,_desc);
		}else{
			CellOperationUtil.sort(r, _index1, _desc1, _dataOption1, _index2,
					_desc2, _dataOption2, _index3, _desc3, _dataOption3,
					_header, _matchCase, _sortByRows);
		}
	}
	
	@Override
	protected boolean isSheetProtected(){
		return super.isSheetProtected() && !Ranges.range(_sheet).getSheetProtection().isSortAllowed();
	}
}
