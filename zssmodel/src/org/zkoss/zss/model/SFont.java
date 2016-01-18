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

/**
 * Represents a font used in cells.
 * @author dennis
 * @since 3.5.0
 */
public interface SFont {
	/**
	 * @since 3.5.0
	 */
	public enum TypeOffset{
		NONE, 
		SUPER, 
		SUB
	}
	/**
	 * @since 3.5.0
	 */
	public enum Underline{
		NONE,
		SINGLE,
		DOUBLE,
		SINGLE_ACCOUNTING,
		DOUBLE_ACCOUNTING
	}
	
	/**
	 * @since 3.5.0
	 */
	public enum Boldweight{
		NORMAL,
		BOLD
	}
	
	/**
	 * 
	 * @return a font's color
	 */
	public SColor getColor();
	
	/**
	 * 
	 * @return a font's name like "Calibri".
	 */
	public String getName();
	
	/**
	 * 
	 * @return a font's bold style.
	 */
	public Boldweight getBoldweight();
	
	/**
	 * 
	 * @return a font height point
	 */
	public int getHeightPoints();
	
	/**
	 * 
	 * @return true if the font is italic
	 */
	public boolean isItalic();
	
	/**
	 * 
	 * @return true if the font is strike-out.
	 */
	public boolean isStrikeout();
	
	/**
	 * 
	 * @return
	 */
	public TypeOffset getTypeOffset();
	
	/**
	 * 
	 * @return the style of a font's underline
	 */
	public Underline getUnderline();
	
	
	public void setName(String fontName);

	public void setColor(SColor fontColor);

	public void setBoldweight(Boldweight fontBoldweight);

	public void setHeightPoints(int fontHeightPoint);

	public void setItalic(boolean fontItalic);

	public void setStrikeout(boolean fontStrikeout);

	public void setTypeOffset(TypeOffset fontTypeOffset);

	public void setUnderline(Underline fontUnderline);
	
	public void copyFrom(SFont src);
}
