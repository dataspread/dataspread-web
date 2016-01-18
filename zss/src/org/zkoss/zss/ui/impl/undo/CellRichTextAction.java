/* CellRichTextAction.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/08/20, Created by henrichen
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl.undo;

import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.UnitUtil;
import org.zkoss.zss.api.CellOperationUtil.CellStyleApplier;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.SRichText;
import org.zkoss.zss.model.SRichText.Segment;
/**
 * 
 * @author henrichen
 * @since 3.6.0
 */
public class CellRichTextAction extends AbstractEditTextAction {
	
	private final String _richText;
	private final CellStyleApplier _applier;
	
	public CellRichTextAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,String editText){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._richText = editText;
		this._applier = null;
	}

	public CellRichTextAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn, CellStyleApplier applier) {
		super(label,sheet,row,column,lastRow,lastColumn);
		this._richText = null;
		this._applier = applier;
	}

	protected void applyAction(){
		boolean protect = isSheetProtected();
		if(!protect){
			Range r = Ranges.range(_sheet,_row,_column,_lastRow,_lastColumn);
			if (_richText != null) {
				r.setCellRichText((String)_richText);
				CellOperationUtil.fitFontHeightPoints(r);
			}
			else if (_applier != null)
				_applier.apply(r);
		}
	}
}
