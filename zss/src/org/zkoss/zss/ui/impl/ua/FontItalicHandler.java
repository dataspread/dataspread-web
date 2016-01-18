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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.CellSelectionType;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.impl.ActionHelper;
import org.zkoss.zss.ui.impl.undo.AggregatedAction;
import org.zkoss.zss.ui.impl.undo.FontStyleAction;
import org.zkoss.zss.ui.sys.UndoableAction;
import org.zkoss.zss.ui.sys.UndoableActionManager;

/**
 * @author dennis
 *
 */
public class FontItalicHandler extends AbstractCellHandler {
	private static final long serialVersionUID = 8778194802956921793L;

	@Override
	protected boolean processAction(UserActionContext ctx) {
		Sheet sheet = ctx.getSheet();
		AreaRef selection = ctx.getSelection();
		CellSelectionType type = ctx.getSelectionType();
		Range range = Ranges.range(sheet, selection);
		//ZSS-576
		if (range.isProtected() && !range.getSheetProtection().isFormatCellsAllowed()) {
			showProtectMessage();
			return true;
		}	
		//zss-623, extends to row,column area
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
		
		boolean italic = !range.getCellStyle().getFont().isItalic();
		
		UndoableActionManager uam = ctx.getSpreadsheet().getUndoableActionManager();

		List<UndoableAction> actions = new ArrayList<UndoableAction>();
		actions.add(new FontStyleAction("", sheet, selection.getRow(), selection.getColumn(), 
				selection.getLastRow(), selection.getLastColumn(), 
				CellOperationUtil.getFontItalicApplier(italic)));
		ActionHelper.collectRichTextStyleActions(range, CellOperationUtil.getRichTextFontItalicApplier(italic), actions);
		AggregatedAction action = new AggregatedAction(Labels.getLabel("zss.undo.fontStyle"), actions.toArray(new UndoableAction[actions.size()]));
		
		uam.doAction(action);
		return true;
	}

}
