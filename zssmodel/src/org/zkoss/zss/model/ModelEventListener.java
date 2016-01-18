/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.io.Serializable;


/**
 * A listener to handle {@link ModelEvent}.
 * @author dennis
 * @since 3.5.0
 */
public interface ModelEventListener extends Serializable{

	/**
	 * Override this method to handle the model event.
	 * @param event
	 */
	public void onEvent(ModelEvent event);
}
