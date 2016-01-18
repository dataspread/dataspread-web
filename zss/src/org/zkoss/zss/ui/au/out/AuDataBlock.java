/* AuDataBlock.java

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

import org.zkoss.json.JSONArray;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.ui.Component;

/**
 * A AuResponse(server to client) for handling Data block updating.
 * @author Dennis.Chen
 *
 */
public class AuDataBlock extends AuResponse {
	public AuDataBlock(Component comp, String token, String sheetid, String bockdata ) {
		
		/**
		 * rename. zssdblock -> dataUpdate -> dataBlockUpdate
		 */
		super("setAttr", comp, new Object[] {comp.getUuid(), "dataBlockUpdate", new String[]{token, sheetid, bockdata}});
	}
}
