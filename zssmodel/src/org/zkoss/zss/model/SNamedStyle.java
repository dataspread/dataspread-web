/* SNamedStyle.java

	Purpose:
		
	Description:
		
	History:
		Dec 10, 2014 2:25:26 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

/**
 * Cell Style referred via a name.
 * @author henri
 * @3.7.0
 */
public interface SNamedStyle extends SCellStyle {
	public String getName();

	public int getIndex(); // index refer to defaultCellStyles
	
	public boolean isCustomBuiltin();
	
	public int getBuiltinId();
}
