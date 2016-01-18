/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 21, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.ui.impl;

import java.util.List;

import org.zkoss.zss.api.CellVisitor;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.CellOperationUtil.CellStyleApplier;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.ui.impl.undo.CellRichTextAction;
import org.zkoss.zss.ui.sys.UndoableAction;

/**
 * @author henri
 * @since 3.6.0
 */
public class ActionHelper {

	/**
	 * Scan the given range and collect rich text cells actions which will be applied with the applier.
	 * @param range
	 * @param applier
	 * @param actions
	 */
	//ZSS-752
	public static void collectRichTextStyleActions(Range range, final CellStyleApplier applier, final List<UndoableAction> actions) {
		//scan each cell of the range and prepare RichTextAction for cells with richtext
		range.visit(new CellVisitor() {
			@Override
			public boolean visit(Range cellRange) {
				final int row = cellRange.getRow();
				final int column = cellRange.getColumn();
				final Sheet sheet = cellRange.getSheet();
				final SCell cell = sheet.getInternalSheet().getCell(row, column);
				if (!((AbstractCellAdv)cell).isRichTextValue())
					return true;
				
				final UndoableAction action =
						new CellRichTextAction("",sheet,row,column,row,column, applier);
				actions.add(action);
				return true;
			}
			

			@Override
			public boolean ignoreIfNotExist(int row, int column) {
				return true;
			}

			@Override
			public boolean createIfNotExist(int row, int column) {
				return false;
			}
		});
	}
}
