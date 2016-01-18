/* FontFamilyAction.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/5 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
 */
package org.zkoss.zss.ui.impl.ua;

import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.UnitUtil;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.CellSelectionType;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.impl.undo.ClearCellAction;
import org.zkoss.zss.ui.impl.undo.FontStyleAction;
import org.zkoss.zss.ui.impl.undo.ClearCellAction.Type;
import org.zkoss.zss.ui.sys.UndoableActionManager;

/**
 * @author dennis
 * 
 */
public class ClearCellHandler extends AbstractHandler {
	private static final long serialVersionUID = -3759122452949257087L;
	ClearCellAction.Type _type;

	public ClearCellHandler(Type type) {
		this._type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.zkoss.zss.ui.sys.ua.impl.AbstractHandler#processAction(org.zkoss.
	 * zss.ui.UserActionContext)
	 */
	@Override
	protected boolean processAction(UserActionContext ctx) {
		Sheet sheet = ctx.getSheet();
		AreaRef selection = ctx.getSelection();
		CellSelectionType type = ctx.getSelectionType();
		Range range = Ranges.range(sheet, selection);
		//ZSS-576
		if(range.isProtected()){
			switch(type) {
			case ROW:
				if (range.getSheetProtection().isFormatRowsAllowed()) { 
					showProtectMessage();
					return true;
				}
				break;
			case COLUMN:
			case ALL:
				if (!range.getSheetProtection().isFormatColumnsAllowed()) {
					showProtectMessage();
					return true;
				}
				break;
			case CELL:
				if (!range.getSheetProtection().isFormatCellsAllowed()) {
					showProtectMessage();
					return true;
				}
				break;
			}
		}

		//TODO support zss-623, the implementation of ClearCellAction doesn't support we do this here
		switch(type){
		case ROW:
			range = range.toRowRange();
			break;
		case COLUMN:
			range = range.toColumnRange();
			break;
		case ALL:
			//we don't allow to set whole sheet style, use column range instead 
			range = range.toColumnRange();
		}
		selection = new AreaRef(range.getRow(),range.getColumn(),range.getLastRow(),range.getLastColumn());
		
		String label = null;
		switch (_type) { //ZSS-692
		case ALL:
			label = Labels.getLabel("zss.undo.clearAll");
			break;
		case CONTENT:
			label = Labels.getLabel("zss.undo.clearContents");
			break;
		case STYLE:
			label = Labels.getLabel("zss.undo.clearStyles");
			break;
		}
		UndoableActionManager uam = ctx.getSpreadsheet()
				.getUndoableActionManager();
		uam.doAction(new ClearCellAction(label, sheet, selection.getRow(),
				selection.getColumn(), selection.getLastRow(), selection
						.getLastColumn(), _type));
		return true;
	}

}
