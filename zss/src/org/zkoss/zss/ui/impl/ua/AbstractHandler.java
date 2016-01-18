/* AbstractUserHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/2 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl.ua;

import java.io.Serializable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.IllegalOpArgumentException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.UserActionHandler;
import org.zkoss.zul.Messagebox;

/**
 * @author dennis
 * @since 3.0.0
 */
public abstract class AbstractHandler implements UserActionHandler, Serializable {
	private static final long serialVersionUID = -3596700452009821943L;

	@Override
	public boolean isEnabled(Book book, Sheet sheet) {
		return true;
	}
	
	
	protected void showProtectMessage() {
		String message = Labels.getLabel("zss.actionhandler.msg.sheet_protected");
		showWarnMessage(message);
	}
	
	protected void showInfoMessage(String message) {
		String title = Labels.getLabel("zss.actionhandler.msg.info_title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.INFORMATION);
	}
	
	protected void showWarnMessage(String message) {
		String title = Labels.getLabel("zss.actionhandler.msg.warn_title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	public boolean process(UserActionContext ctx){
		try{
			return processAction(ctx);
		}catch(IllegalOpArgumentException x){
			showInfoMessage(Labels.getLabel("zss.actionhandler.msg.illegal_range_operation")+" : "+x.getMessage());
			return true;
		}catch(InvalidModelOpException x){
			showInfoMessage(Labels.getLabel("zss.actionhandler.msg.illegal_range_operation")+" : "+x.getMessage());
			return true;
		}
	}

	// ZSS-404: corner panel can't be modified 
	protected boolean checkInCornerFreezePanel(Range range) {
		Sheet sheet = range.getSheet();
		int fzr = sheet.getRowFreeze();	// it's number
		int fzc = sheet.getColumnFreeze();
		// check there is corner freeze panel first
		if(fzr > 0 && fzc > 0) {
			// check range
			if(range.getRow() < fzr && range.getColumn() < fzc) {
				return true;
			}
		}
		return false;
	}
	
	//ZSS-504 , prevent some operation when select all visible
	protected boolean checkSelectAllVisibleRow(UserActionContext ctx) {
		Spreadsheet ss = ctx.getSpreadsheet();
		AreaRef sel = ctx.getSelection();
		
		if(sel.getRow()==0 && sel.getLastRow() >= ss.getCurrentMaxVisibleRows()-1){ //ZSS-1084
			return true;
		}
		return false;
	}
	protected boolean checkSelectAllVisibleColumn(UserActionContext ctx) {
		Spreadsheet ss = ctx.getSpreadsheet();
		AreaRef sel = ctx.getSelection();
		
		if(sel.getColumn()==0 && sel.getLastColumn() >= ss.getCurrentMaxVisibleColumns()-1){ //ZSS-1084
			return true;
		}
		return false;
	}
	protected boolean checkSelectAllVisibleRowColumn(UserActionContext ctx) {
		Spreadsheet ss = ctx.getSpreadsheet();
		AreaRef sel = ctx.getSelection();
		
		if(sel.getRow()==0 && sel.getLastRow() >= ss.getCurrentMaxVisibleRows()-1 &&  //ZSS-1084
				sel.getColumn()==0 && sel.getLastColumn() >= ss.getCurrentMaxVisibleColumns()-1){ //ZSS-1084
			return true;
		}
		return false;
	}
	
	protected abstract boolean processAction(UserActionContext ctx);
}
