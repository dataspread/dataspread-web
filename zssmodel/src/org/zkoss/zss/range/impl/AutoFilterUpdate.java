/* AutoFitlerChange.java

	Purpose:
		
	Description:
		
	History:
		May 1, 2015 5:08:27 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.range.impl;

import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.STable;

/**
 * Keep the information of Table/Sheet filter change
 * @author henri
 * @since 3.8.0
 */
public class AutoFilterUpdate {
	final SSheet sheet;
	final STable table;
	
	public AutoFilterUpdate(SSheet sheet, STable table) {
		this.sheet = sheet;
		this.table = table;
	}
	
	public SSheet getSheet() {
		return sheet;
	}

	public STable getTable() {
		return table;
	}
}
