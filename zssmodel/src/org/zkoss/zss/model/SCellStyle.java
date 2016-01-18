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
package org.zkoss.zss.model;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.SBorder.BorderType;

/**
 * Represent style information e.g. alignment, border, format pattern, font, color, fill pattern, wrap text, and hidden status. It may associate with a book or cells.
 * @author dennis
 * @since 3.5.0
 */
public interface SCellStyle {

	public static final String FORMAT_GENERAL = "General";	

	/**
	 * The horizontal alignment
	 * @since 3.5.0
	 */
	public enum Alignment {
		GENERAL, LEFT, CENTER, RIGHT, FILL, JUSTIFY, CENTER_SELECTION
	}

	/**
	 * The vertical alignment
	 * @since 3.5.0
	 */
	public enum VerticalAlignment {
		TOP, CENTER, BOTTOM, JUSTIFY
	}
	
	/**
	 * @return fill foreground-color
	 */
	public SColor getFillColor();

	/**
	 * @return fill background-color
	 * @since 3.6.0
	 */
	public SColor getBackColor();

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
	 * @return vertical border
	 * @since 3.8.0
	 */
	public BorderType getBorderVertical();

	/**
	 * @return horizontal border
	 * @since 3.8.0
	 */
	public BorderType getBorderHorizontal();

	/**
	 * @return diagonal border
	 * @since 3.8.0
	 */
	public BorderType getBorderDiagonal();

	/**
	 * @return top border color
	 */
	public SColor getBorderTopColor();

	/** 
	 * @return left border color
	 */
	public SColor getBorderLeftColor();

	/**
	 * 
	 * @return bottom border color
	 */
	public SColor getBorderBottomColor();

	/**
	 * @return right border color
	 */
	public SColor getBorderRightColor();
	
	/**
	 * @return vertical border color
	 * @since 3.8.0
	 */
	public SColor getBorderVerticalColor();

	/**
	 * @return horizontal border color
	 * @since 3.8.0
	 */
	public SColor getBorderHorizontalColor();

	/**
	 * @return diagonal border color
	 * @since 3.8.0
	 */
	public SColor getBorderDiagonalColor();

	/**
	 * @return data format
	 */
	public String getDataFormat();
	
	/**
	 * 
	 * @return true if the data format is direct data format, which mean it will not care Locale when formatting
	 */
	public boolean isDirectDataFormat();
	
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
	

	public void setFillColor(SColor fillColor);
	
	@Deprecated
	public void setBackgroundColor(SColor backColor); //ZSS-780
	public void setBackColor(SColor backColor);

	public void setFillPattern(FillPattern fillPattern);
	
	public void setAlignment(Alignment alignment);
	
	public void setVerticalAlignment(VerticalAlignment verticalAlignment);

	public void setWrapText(boolean wrapText);

	public void setBorderLeft(BorderType borderLeft);
	public void setBorderLeft(BorderType borderLeft,SColor color);

	public void setBorderTop(BorderType borderTop);
	public void setBorderTop(BorderType borderTop,SColor color);

	public void setBorderRight(BorderType borderRight);
	public void setBorderRight(BorderType borderRight,SColor color);

	public void setBorderBottom(BorderType borderBottom);
	public void setBorderBottom(BorderType borderBottom,SColor color);

	public void setBorderVertical(BorderType borderVertical);
	public void setBorderVertical(BorderType borderVertical,SColor color);

	public void setBorderHorizontal(BorderType borderHorizontal);
	public void setBorderHorizontal(BorderType borderHorizontal,SColor color);

	public void setBorderDiagonal(BorderType borderDiagonal);
	public void setBorderDiagonal(BorderType borderDiagonal,SColor color);

	public void setBorderTopColor(SColor borderTopColor);

	public void setBorderLeftColor(SColor borderLeftColor);

	public void setBorderBottomColor(SColor borderBottomColor);

	public void setBorderRightColor(SColor borderRightColor);
	
	public void setBorderVerticalColor(SColor color);
	
	public void setBorderHorizontalColor(SColor color);
	
	public void setBorderDiagonalColor(SColor color);

	public void setDataFormat(String dataFormat);
	
	public void setDirectDataFormat(String dataFormat);
	
	public void setLocked(boolean locked);

	public void setHidden(boolean hidden);
	
	public SFont getFont();
	
	public void setFont(SFont font);

	public void copyFrom(SCellStyle src);
		
    /**
     * get the degree of rotation for the text in the cell
     * @return rotation degrees (between -90 and 90 degrees); 255 means "vertical text"
     * @since 3.8.0
     */
	public int getRotation();
	public void setRotation(int rotation);
	
	public int getIndention();
	public void setIndention(int indent);

	//ZSS-977
	/**
	 * Returns the border
	 * @return
	 * @since 3.8.0
	 */
	public SBorder getBorder();
	
	//ZSS-977
	/**
	 * Returns the fill
	 * @return
	 * @since 3.8.0
	 */
	public SFill getFill();
}
