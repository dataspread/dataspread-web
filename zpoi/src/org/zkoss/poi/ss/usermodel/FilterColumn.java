/* FilterColumn.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		May 7, 2011 6:28:58 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.poi.ss.usermodel;

import java.util.List;
import java.util.Set;

/**
 * Represent a filtered column.
 * @author henrichen
 *
 */
public interface FilterColumn {
	/**
	 * Returns the column id of this FilterColumn. 
	 * @return the column id of this FilterColumn.
	 */
	public int getColId();
	
	/**
	 * Returns the filter String list of this FilterColumn.
	 * @return the filter String list of this FilterColumn.
	 */
	public List<String> getFilters();
	
	public Set getCriteria1();
	
	public Set getCriteria2();
	
	public boolean isOn();
	
	public int getOperator();
}
