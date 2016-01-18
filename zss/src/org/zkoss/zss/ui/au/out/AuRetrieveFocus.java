/* AuRetrieveFocus.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2007 12:11:22 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.out;

import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.ui.Component;

/**
 * A AuResponse(server to client) for handling cell updating.
 * @author Dennis.Chen
 *
 */
public class AuRetrieveFocus extends AuResponse {

	public AuRetrieveFocus(Component comp, String data) {
		/**
		 * rename zssretfocus -> retrieveFocus
		 */
		//super("zssretfocus", comp, new String[] {comp.getUuid(),data});
		super("setAttr", comp, new Object[] {comp.getUuid(), "retrieveFocus", data});
	}
}
