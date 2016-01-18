/* Chart.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model;

import org.zkoss.zss.api.SheetAnchor;

/**
 * This interface provides the access to a chart of a sheet.
 * @author dennis
 * @since 3.0.0
 */
public interface Chart {

	public enum Type {
		AREA_3D,
		AREA,
		BAR_3D,
		BAR,
		BUBBLE,
		COLUMN,
		COLUMN_3D,
		DOUGHNUT,
		LINE_3D,
		LINE,
		OF_PIE,
		PIE_3D,
		PIE,
		RADAR,
		SCATTER,
		STOCK,
		SURFACE_3D,
		SURFACE
	}
	public enum Grouping {
		STANDARD,
		STACKED,
		PERCENT_STACKED,
		CLUSTERED; //bar only
	}
	public enum LegendPosition {
		BOTTOM,
		LEFT,
		RIGHT,
		TOP,
		TOP_RIGHT
	}
	
	public String getId();
	
	
	public SheetAnchor getAnchor();
}
