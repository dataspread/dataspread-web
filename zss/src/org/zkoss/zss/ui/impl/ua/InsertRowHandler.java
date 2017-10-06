/* InserCellRightHandler.java

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
import org.zkoss.zss.api.IllegalOpArgumentException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.Range.InsertCopyOrigin;
import org.zkoss.zss.api.Range.InsertShift;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.impl.undo.InsertCellAction;
import org.zkoss.zss.ui.sys.UndoableActionManager;

/**
 * @author dennis
 *
 */
public class InsertRowHandler extends AbstractHandler {
	private static final long serialVersionUID = -9104949440019029413L;

	/* (non-Javadoc)
	 * @see org.zkoss.zss.ui.sys.ua.impl.AbstractHandler#processAction(org.zkoss.zss.ui.UserActionContext)
	 */
	@Override
	protected boolean processAction(UserActionContext ctx) {
		Sheet sheet = ctx.getSheet();
		AreaRef selection = ctx.getSelection();
		Range range = Ranges.range(sheet, selection);
		range = range.toRowRange();
		//work around for ZSS-404 JS Error after insert column when freeze
		if(checkInCornerFreezePanel(range)){
			throw new IllegalOpArgumentException(Labels.getLabel("zss.msg.operation_not_supported_with_freeze_panel"));
		}
		// work around for ZSS-586: don't allow insert rows when select whole visible rows
		int row = range.getRow();
		int lastRow = range.getLastRow();
		int maxVisibleRows = ctx.getSpreadsheet().getCurrentMaxVisibleRows(); //ZSS-1084
		if(lastRow - row + 1 == maxVisibleRows) {
			throw new IllegalOpArgumentException(Labels.getLabel("zss.msg.operation_not_supported_with_all_row"));
		}
		
		UndoableActionManager uam = ctx.getSpreadsheet().getUndoableActionManager();
		uam.doAction(new InsertCellAction(Labels.getLabel("zss.undo.insertRow"),sheet, range.getRow(), range.getColumn(), 
				range.getLastRow(), range.getLastColumn(), 
				InsertShift.DOWN, InsertCopyOrigin.FORMAT_LEFT_ABOVE)); // ZSS-404, Excel default behavior is left or above 
		ctx.clearClipboard();

		//code for updating the max visible row
		SSheet isheet = ctx.getSheet().getInternalSheet();
		Spreadsheet ss = ctx.getSpreadsheet();
		ss.setSheetMaxVisibleRows(isheet, ss.getSheetMaxVisibleRows(isheet) + range.getRowCount());

		return true;
	}

	@Override
	public boolean isEnabled(Book book, Sheet sheet) {
		return book != null && sheet != null && (!sheet.isProtected() ||
				Ranges.range(sheet).getSheetProtection().isInsertRowsAllowed());
	}

}
