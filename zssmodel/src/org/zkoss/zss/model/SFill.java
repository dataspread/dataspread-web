/* SFill.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 4:45:36 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

/**
 * Fill pattern and colors
 * @author henri
 * @since 3.8.0
 */
public interface SFill {
	/**
	 * The fill pattern
	 * @since 3.7.0
	 */
	public static enum FillPattern {
		NONE, //NO_FILL
		SOLID, //SOLID_FOREGROUND
		MEDIUM_GRAY, //FINE_DOTS
		DARK_GRAY, //ALT_BARS
		LIGHT_GRAY, //SPARSE_DOTS
		DARK_HORIZONTAL, //THICK_HORZ_BANDS
		DARK_VERTICAL, //THICK_VERT_BANDS
		DARK_DOWN, //THICK_BACKWARD_DIAG
		DARK_UP, //THICK_FORWARD_DIAG
		DARK_GRID, //BIG_SPOTS
		DARK_TRELLIS, //BRICKS
		LIGHT_HORIZONTAL, //THIN_HORZ_BANDS
		LIGHT_VERTICAL, //THIN_VERT_BANDS
		LIGHT_DOWN, //THIN_BACKWARD_DIAG
		LIGHT_UP, //THIN_FORWARD_DIAG
		LIGHT_GRID, //SQUARES
		LIGHT_TRELLIS, //DIAMONDS
		GRAY125, //LESS_DOTS 
		GRAY0625 //LEAST_DOTS
	}

	public SColor getFillColor();
	public void setFillColor(SColor fillColor);
	
	public SColor getBackColor();
	public void setBackColor(SColor backColor);

	public FillPattern getFillPattern();
	public void setFillPattern(FillPattern fillPattern);
}
