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
package org.zkoss.zss.model.impl;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public interface LinkedModelObject {
	/**
	 * Destroy / release this model object, for example all the dependency, parent linking.
	 * this method has to be called before remove this linking from parent object 
	 */
	public void destroy();
	
	public void checkOrphan();
}
