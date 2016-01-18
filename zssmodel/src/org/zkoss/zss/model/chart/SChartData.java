/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.chart;

import org.zkoss.zss.model.*;
/**
 * An object that can be used to get {@link SChart}. 
 * @author dennis
 * @since 3.5.0
 */
public interface SChartData extends FormulaContent{
	/**
	 * @return the chart that contains the chart data.
	 */
	public SChart getChart();
}
