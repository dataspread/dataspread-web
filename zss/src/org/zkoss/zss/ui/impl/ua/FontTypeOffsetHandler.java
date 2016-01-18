/* FontFamilyAction.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/8/18 , Created by henrichen
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

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
import org.zkoss.zss.api.model.Font.TypeOffset;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.CellSelectionType;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.impl.ActionHelper;
import org.zkoss.zss.ui.impl.undo.AggregatedAction;
import org.zkoss.zss.ui.impl.undo.FontStyleAction;
import org.zkoss.zss.ui.sys.UndoableActionManager;
import org.zkoss.zss.ui.sys.UndoableAction;

/**
 * Handle font super/sub operation.
 * @author henrichen
 * @since 3.6.0
 */
public class FontTypeOffsetHandler extends AbstractCellHandler {
	private static final long serialVersionUID = -8745551581702240409L;

	@Override
	protected boolean processAction(UserActionContext ctx) {
		final String offstr = (String) ctx.getData("typeOffset"); 
		TypeOffset offset = 
			"SUPER".equals(offstr) ? TypeOffset.SUPER :
			"SUB".equals(offstr) ? TypeOffset.SUB : TypeOffset.NONE;

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
		
		UndoableActionManager uam = ctx.getSpreadsheet().getUndoableActionManager();
		List<UndoableAction> actions = new ArrayList<UndoableAction>();
		actions.add(new FontStyleAction("",sheet, selection.getRow(), selection.getColumn(), 
				selection.getLastRow(), selection.getLastColumn(), 
				CellOperationUtil.getFontTypeOffsetApplier(offset)));
		ActionHelper.collectRichTextStyleActions(range, CellOperationUtil.getRichTextFontTypeOffsetApplier(offset), actions);
		AggregatedAction action = new AggregatedAction(Labels.getLabel("zss.undo.fontStyle"), actions.toArray(new UndoableAction[actions.size()]));
		uam.doAction(action);
		return true;
	}
}
