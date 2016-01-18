/* PasteHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/3 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl.ua;

import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.UserActionContext.Clipboard;
import org.zkoss.zss.ui.impl.undo.CutCellAction;
import org.zkoss.zss.ui.impl.undo.PasteCellAction;
import org.zkoss.zss.ui.sys.UndoableActionManager;

public class PasteHandler extends AbstractHandler {
	private static final long serialVersionUID = -6262315007795949652L;

	@Override
	protected boolean processAction(UserActionContext ctx) {
		Clipboard cb = ctx.getClipboard();
		if(cb==null){
			//don't handle it , so give a chance to do client paste
			return false;
		}
		
		Book book = ctx.getBook();
		Sheet destSheet = ctx.getSheet();
		Sheet srcSheet = cb.getSheet();
		if(srcSheet==null){
			showInfoMessage(Labels.getLabel("zss.actionhandler.msg.cant_find_sheet_to_paste"));
			ctx.clearClipboard();
			return true;
		}
		AreaRef src = cb.getSelection();
		
		AreaRef selection = ctx.getSelection();
		
		Range srcRange = Ranges.range(srcSheet, src.getRow(),
				src.getColumn(), src.getLastRow(),src.getLastColumn());

		Range destRange = Ranges.range(destSheet, selection.getRow(),
				selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
		
		if (destRange.isProtected()) {
			showProtectMessage();
			return true;
		} else if (cb.isCutMode() && srcRange.isProtected()) {
			showProtectMessage();
			return true;
		}
		
		UndoableActionManager uam = ctx.getSpreadsheet().getUndoableActionManager();
		
		if(cb.isCutMode()){
			uam.doAction(new CutCellAction(Labels.getLabel("zss.undo.cut"),
				srcSheet, src.getRow(), src.getColumn(),src.getLastRow(), src.getLastColumn(), 
				destSheet, selection.getRow(), selection.getColumn(),selection.getLastRow(), selection.getLastColumn()));
			ctx.clearClipboard();
		}else{
			uam.doAction(new PasteCellAction(Labels.getLabel("zss.undo.paste"),
				srcSheet, src.getRow(), src.getColumn(),src.getLastRow(), src.getLastColumn(), 
				destSheet, selection.getRow(), selection.getColumn(),selection.getLastRow(), selection.getLastColumn()));
		}
		return true;
	}

}
