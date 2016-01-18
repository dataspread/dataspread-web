/* DeleteSheetHandler.java

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
import org.zkoss.zss.api.SheetOperationUtil;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.UserActionContext;

/**
 * @author dennis
 * @since 3.0.0
 */
public class DeleteSheetHandler extends AbstractSheetHandler{
	private static final long serialVersionUID = 1721402477934763045L;

	@Override
	public boolean isEnabled(Book book, Sheet sheet) {
		if(book!=null && sheet!=null){
			int sheetnum = book.getNumberOfSheets();
			return sheetnum>1;
		}
		return false;
	}
	
	@Override
	protected boolean processAction(UserActionContext ctx) {
		Book book = ctx.getBook();
		Sheet sheet = ctx.getSheet();
		
		int num = book.getNumberOfSheets();
		if(num<=1){
			showWarnMessage(Labels.getLabel("zss.actionhandler.msg.cant_delete_last_sheet"));
			return true;
		}
		
		int index = book.getSheetIndex(sheet);
		
		Range range = Ranges.range(sheet);
		SheetOperationUtil.deleteSheet(range);
		
		if(index==num-1){
			index--;
		}
		
		ctx.getSpreadsheet().setSelectedSheet(book.getSheetAt(index).getSheetName());
		
		return true;
	}

}
