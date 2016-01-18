/* AuRemoveRowColumn.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 25, 2010 6:32:14 PM , Created by Sam
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
public class AuRemoveRowColumn extends AuResponse {
	public AuRemoveRowColumn(Component comp, String token, String sheetid, Object data) {
		super("setAttr", comp, new Object[] {comp.getUuid(), "removeRowColumn",  new Object[]{token, sheetid, data}});
	}
}
