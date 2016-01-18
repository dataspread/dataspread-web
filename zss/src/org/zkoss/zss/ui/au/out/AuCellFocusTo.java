/* AuFocusTo.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 12, 2010 6:07:55 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.out;

import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.ui.Component;

/**
 * @author Sam
 *
 */
public class AuCellFocusTo extends AuResponse {
	
	public AuCellFocusTo(Component comp, Object data) {
		super("setAttr", comp, new Object[] {comp.getUuid(), "retrieveFocus", data});
	}
}
