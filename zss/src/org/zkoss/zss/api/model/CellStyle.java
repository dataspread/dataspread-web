/* CellStyle.java

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


/**
 * This interface provides access to "style" part of a cell including alignment, border, font, formant, and color.
 * @author dennis
 * @since 3.0.0
 */
public interface CellStyle {

	/**
	 * The fill pattern
	 * @since 3.7.0
	 */
	public enum FillPattern {
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

	/**
	 * The horizontal alignment
	 *
	 */
	public enum Alignment {
		GENERAL, LEFT, CENTER, RIGHT, FILL, JUSTIFY, CENTER_SELECTION
	}

	/**
	 * The vertical alignment
	 */
	public enum VerticalAlignment {
		TOP, CENTER, BOTTOM, JUSTIFY
	}

	/**
	 * The border type
	 */
	public enum BorderType {
		NONE, THIN, MEDIUM, DASHED, HAIR, THICK, DOUBLE, DOTTED, MEDIUM_DASHED, DASH_DOT, MEDIUM_DASH_DOT, DASH_DOT_DOT, MEDIUM_DASH_DOT_DOT, SLANTED_DASH_DOT;
	}

	/**
	 * @return the font
	 */
	public Font getFont();

	/**
	 * @return background-color
	 * @deprecated since 3.5.0 , use {@link #getFillColor()}
	 */
	public Color getBackgroundColor();
	
	/**
	 * 
	 * @return fill foreground color
	 * @since 3.5.0
	 */
	public Color getFillColor();
	
	/**
	 * 
	 * @return fill background color
	 * @since 3.6.0
	 */
	public Color getBackColor();

	/**
	 * Gets the fill/background pattern <br/>
	 * @return the fill pattern
	 */
	public FillPattern getFillPattern();

	/**
	 * Gets the horizontal alignment <br/>
	 * @return the horizontal alignment
	 */
	public Alignment getAlignment();

	/**
	 * Gets vertical alignment <br/>
	 * @return
	 */
	public VerticalAlignment getVerticalAlignment();

	/**
	 * @return true if wrap-text
	 */
	public boolean isWrapText();

	/**
	 * @return left border
	 */
	public BorderType getBorderLeft();

	/**
	 * @return top border
	 */
	public BorderType getBorderTop();

	/**
	 * @return right border
	 */
	public BorderType getBorderRight();

	/**
	 * @return bottom border
	 */
	public BorderType getBorderBottom();

	/**
	 * @return top border color
	 */
	public Color getBorderTopColor();

	/** 
	 * @return left border color
	 */
	public Color getBorderLeftColor();

	/**
	 * 
	 * @return bottom border color
	 */
	public Color getBorderBottomColor();

	/**
	 * @return right border color
	 */
	public Color getBorderRightColor();
	
	/**
	 * @return data format
	 */
	public String getDataFormat();
	
	/**
	 * 
	 * @return true if locked
	 */
	public boolean isLocked();
	
	/**
	 * 
	 * @return true if hidden
	 */
	public boolean isHidden();
	

}
