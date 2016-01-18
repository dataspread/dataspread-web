/* AuInsertRowColumn.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 26, 2010 11:14:01 AM , Created by Sam
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
public class AuInsertRowColumn extends AuResponse {
	public AuInsertRowColumn(Component comp, String token, String sheetid, Object data) {
		super("setAttr", comp, new Object[] {comp.getUuid(), "insertRowColumn",  new Object[]{token, sheetid, data}});
	}
}
