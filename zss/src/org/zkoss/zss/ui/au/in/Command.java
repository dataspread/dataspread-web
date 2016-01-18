/* Command.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Feb 8, 2010 11:30:51 AM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import org.zkoss.zk.au.AuRequest;

/**
 * @author Sam
 *
 */
public interface Command {
	
	public void process(AuRequest request);
}
