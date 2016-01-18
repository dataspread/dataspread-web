/* AbstractCommand.java

	Purpose:
		
	Description:
		
	History:
		Jul 22, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.ui.au.in;

import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zul.Messagebox;


/**
 * Abstract command.
 * @author Pao
 */
public abstract class AbstractCommand implements Command {

	protected void showInfoMessage(String message) {
		String title = Labels.getLabel("zss.command.msg.info_title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.INFORMATION);
	}
	
	protected void showWarnMessage(String message) {
		String title = Labels.getLabel("zss.command.msg.warn_title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	protected static String getSheetUuid(Sheet sheet){
		return sheet.getInternalSheet().getId();
	}
	public static Sheet getSheetByUuid(Book book, String uuid) {
		int count = book.getNumberOfSheets();
		for(int j = 0; j < count; ++j) {
			Sheet sheet = book.getSheetAt(j);
			if (uuid.equals(getSheetUuid(sheet))) {
				return sheet;
			}
		}
		return null;
	}	
}
