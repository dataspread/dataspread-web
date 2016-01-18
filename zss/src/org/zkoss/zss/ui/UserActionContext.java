/* UserActionContext.java

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
package org.zkoss.zss.ui;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;

/**
 * 
 * @author dennis
 *
 */
public interface UserActionContext {

	public Spreadsheet getSpreadsheet();
	
	public Book getBook();
	
	public Sheet getSheet();
	
	public Event getEvent();
	
	public AreaRef getSelection(); //may be highlight area
	
	/**
	 * @since 3.5.0
	 */
	public CellSelectionType getSelectionType();
	
	public Object getData(String key);
	
	public String getCategory();
	
	public String getAction();
	
	
	public interface Clipboard {
		
		public Sheet getSheet();
		public AreaRef getSelection();
		public Object getInfo();
		
		public boolean isCutMode();
		
	}
	
	public Clipboard getClipboard();
	
	public void clearClipboard();
	
	public void setClipboard(Sheet sheet,AreaRef selection,boolean cutMode,Object info);

	
	
}
