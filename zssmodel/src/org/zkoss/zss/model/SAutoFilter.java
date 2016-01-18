/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
/**
 * Contains autofilter's setting.
 * @author Dennis
 * @since 3.5.0
 */
public interface SAutoFilter {

	/**
	 * A filter column contains information for filtering, e.g. criteria.
	 * A filter column only exists when users apply a criteria on a column. 
	 * @author Dennis
	 * @since 3.5.0
	 */
	public interface NFilterColumn{
		/**
		 * @return the nth column (1st column in the filter range is 0)
		 */
		int getIndex();
		
		public List<String> getFilters();
		
		/**
		 * @return main criteria used on a column
		 */
		public Set getCriteria1();

		public Set getCriteria2();
				
		public boolean isShowButton(); //show filter button
		
		public FilterOp getOperator();
		
		public void setProperties(FilterOp filterOp, Object criteria1, Object criteria2, Boolean showButton);

	}
	
	/**
	 * 
	 * @author Dennis
	 * @since 3.5.0
	 */
	public enum FilterOp{
		AND, BOTTOM10, BOTOOM10_PERCENT, OR, TOP10, TOP10_PERCENT, VALUES;
	}
	

	/**
	 * Returns the filtered Region.
	 */
	public CellRegion getRegion();
	
	/**
	 * Return filter setting of each filtered column.
	 */
	public Collection<NFilterColumn> getFilterColumns();
	
	/**
	 * Returns the column filter information of the specified column; null if the column is not filtered.
	 * @param index the nth column (1st column in the filter range is 0)
	 * @return the column filter information of the specified column; null if the column is not filtered.
	 */
	public NFilterColumn getFilterColumn(int index, boolean create);
	
	public void clearFilterColumn(int index);
	
	public void clearFilterColumns();
}
