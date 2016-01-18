/* AuRangeUpdate.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 12, 2011 11:13:58 AM , Created by sam
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.out;

import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;

/**
 * @author sam
 *
 */
public class AuUpdateData extends AuInvoke {
	
	public final static String UPDATE_RANGE_FUNCTION = "setActiveRange";
	public final static String UPDATE_FROZEN_LEFT_FUNCTION = "setFrozenLeftRange";
	public final static String UPDATE_FROZEN_TOP_RANGE_FUNCTION = "setFrozenTopRange";
	
	public final static String UPDATE_ROW_HEADER_FUNCTION  = "setRowHeaders";
	public final static String UPDATE_COLUMN_HEADER_FUNCTION  = "setColHeaders";
	
	public AuUpdateData(Component comp, String function, String token, String sheetid, String data) {
		super(comp, function, data);
	}
}
