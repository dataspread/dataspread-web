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
package org.zkoss.zss.model.sys;

import java.util.Date;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public interface CalendarUtil {

	public Date doubleValueToDate(double val, boolean date1904);
	public double dateToDoubleValue(Date value, boolean date1904);
	public Date doubleValueToDate(double val);
	public double dateToDoubleValue(Date value);
}
