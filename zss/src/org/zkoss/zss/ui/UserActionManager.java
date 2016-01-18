/* UserActionManager.java

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

/**
 * @author dennis
 *
 */
public interface UserActionManager {

	/**
	 * Register a handler to the handler list of category/action, append to the last handler of same category/action.
	 * @param category the category of the handler
	 * @param action the action of the handler
	 * @param handler the handler
	 */
	public void registerHandler(String category,String action, UserActionHandler handler);
	
	/**
	 * set a handler to the handler list of category/action, it will remove all other handlers in same category/action
	 * @param category the category of the handler
	 * @param action the action of the handler
	 * @param handler the handler
	 */
	public void setHandler(String category,String action, UserActionHandler handler);
}
