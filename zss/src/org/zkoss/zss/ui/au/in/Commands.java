/* CommandUtil.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Feb 24, 2010 11:01:48 AM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import org.zkoss.zk.ui.event.MouseEvent;

/**
 * @author Sam
 *
 */
public final class Commands {
	
	/** Parses the key flags of a mouse event.
	 * @return a combination of {@link MouseEvent#ALT_KEY},
	 * {@link MouseEvent#SHIFT_KEY} and {@link MouseEvent#CTRL_KEY},
	 */
	public static int parseKeys(String flags) {
		int keys = 0;
		if (flags != null) {
			if (flags.indexOf("a") >= 0) keys |= MouseEvent.ALT_KEY;
			if (flags.indexOf("c") >= 0) keys |= MouseEvent.CTRL_KEY;
			if (flags.indexOf("s") >= 0) keys |= MouseEvent.SHIFT_KEY;
		}
		return keys;
	}
}
