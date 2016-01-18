/* Font.java

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
 * This interface allows you to get font style of a cell.
 * @author dennis
 * @since 3.0.0
 */
public interface Font {
	
	public enum TypeOffset{
		NONE, 
		SUPER, 
		SUB
	}
	public enum Underline{
		NONE,
		SINGLE,
		DOUBLE,
		SINGLE_ACCOUNTING,
		DOUBLE_ACCOUNTING
	}
	
	public enum Boldweight{
		NORMAL,
		BOLD
	}
	
	/**
	 * 
	 * @return a font's color
	 */
	public Color getColor();
	
	/**
	 * 
	 * @return a font's name like "Calibri".
	 */
	public String getFontName();
	
	/**
	 * 
	 * @return a font's bold style.
	 */
	public Boldweight getBoldweight();
	
	/**
	 * @return a font's height in twentieth of a point
	 * @deprecated use {@link #getFontHeightInPoint()}
	 */
	public int getFontHeight();
	
	/**
	 * 
	 * @return a font's height in pixel
	 */
	public int getFontHeightInPoint();
	
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
	public TypeOffset getTypeOffset();
	
	/**
	 * 
	 * @return the style of a font's underline
	 */
	public Underline getUnderline();
	
}
