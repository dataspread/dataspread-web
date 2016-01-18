/* STableStyle.java

	Purpose:
		
	Description:
		
	History:
		Dec 9, 2014 6:11:34 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

/**
 * Table style info.
 * @author henri
 * @since 3.8.0 
 */
public interface STableStyleInfo {
	String getName();
	void setName(String name);
	
	boolean isShowColumnStripes();
	void setShowColumnStripes(boolean b);
	
	boolean isShowRowStripes();
	void setShowRowStripes(boolean b);
	
	boolean isShowLastColumn();
	void setShowLastColumn(boolean b);
	
	boolean isShowFirstColumn();
	void setShowFirstColumn(boolean b);
	
	/**
	 * Returns styles used in this TableStyleInfo.
	 * @return
	 */
	public STableStyle getTableStyle();
}
