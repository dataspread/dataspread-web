/* NamedStyle.java

	Purpose:
		
	Description:
		
	History:
		Dec 10, 2014 4:38:45 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.ss.usermodel;

/**
 * CellStyle with a name.
 * @author henri
 * @since 3.9.6
 */
public interface NamedStyle extends CellStyle {
    public String getName();
    
    public boolean isCustomBuiltin();
    
    public int getBuiltinId();
}
