/* MoveSheetAction.java

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
public class MoveSheetHandler extends AbstractSheetHandler{
	private static final long serialVersionUID = 758336150863254712L;
	boolean _left;
	
	public MoveSheetHandler(boolean left){
		_left = left;
	}
	
	@Override
	public boolean isEnabled(Book book, Sheet sheet) {
		if(book!=null && sheet!=null){
			int sheetnum = book.getNumberOfSheets();
			if(sheetnum>1){
				int index = book.getSheetIndex(sheet); 
				if(index>0 && _left){
					return true;
				}else if(index<sheetnum-1 && !_left){
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	protected boolean processAction(UserActionContext ctx) {
		Book book = ctx.getBook();
		Sheet sheet = ctx.getSheet();
		
		int sheetnum = book.getNumberOfSheets();
		if(sheetnum>1){
			int index = book.getSheetIndex(sheet); 
			if(index>0 && _left){
				Range range = Ranges.range(sheet);
				SheetOperationUtil.setSheetOrder(range,index-1);
			}else if(index<sheetnum-1 && !_left){
				Range range = Ranges.range(sheet);
				SheetOperationUtil.setSheetOrder(range,index+1);
			}
		}
		return true;
	}

}
