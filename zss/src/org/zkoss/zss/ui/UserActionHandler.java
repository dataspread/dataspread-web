/* UserActionHandler.java

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

import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;

/**
 * 
 * @author dennis
 *
 */
public interface UserActionHandler {

	/**
	 * get the enabled state of this action
	 * @param book current book, null if no book selected
	 * @param sheet current sheet, null if no sheet selected 
	 * @return
	 */
	public boolean isEnabled(Book book,Sheet sheet);
	

	/**
	 * Handle the action. 
	 * @param ctx
	 * @return true if the handler has processed and manager should ignore post process (if there is) 
	 */
	public boolean process(UserActionContext ctx);
}
