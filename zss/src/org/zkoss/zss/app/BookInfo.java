/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app;

import java.io.Serializable;
import java.util.Date;
/**
 * Book information
 * @author dennis
 *
 */
public interface BookInfo extends Serializable {
	
	public final static String STATE_EMPTY = "empty";			// no book loaded
	public final static String STATE_UNSAVED = "unsaved";		// doesn't save yet
	public final static String STATE_SAVED = "saved";			// all modified saved
	
	String getName();
	
	Date getLastModify();
}
