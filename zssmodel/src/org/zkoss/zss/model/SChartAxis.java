/* SChartAxis.java

	Purpose:
		
	Description:
		
	History:
		Nov 11, 2014 10:28:43 AM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

/**
 * Represent an axis in {@link SChart}.
 * @author henri
 */
public interface SChartAxis {

	public enum SChartAxisType {
		VALUE,
		CATEGORY,
	}
	
	long getId();
	
	/**
	 * Returns the type of this axis.
	 * @return
	 */
	SChartAxisType getType();
	
	/**
	 * Returns the maximum value on this axis.
	 */
	Double getMax();
	void setMax(Double max);
	
	
	/**
	 * Returns the minimum value on this axis.
	 */
	Double getMin();
	void setMin(Double min);
	
	/**
	 * Returns the tick label in excel format; null if no such info.
	 */
	String getFormat();
	void setFormat(String format);
}
