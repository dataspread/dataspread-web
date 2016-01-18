/* SBorder.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 4:45:11 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

import org.zkoss.zss.model.SBorderLine;

/**
 * Border object
 * @author henri
 * @since 3.8.0
 */
public interface SBorder {

	/**
	 * The border type
	 * @since 3.5.0
	 */
	public static enum BorderType {
		NONE, THIN, MEDIUM, DASHED, HAIR, THICK, DOUBLE, DOTTED, 
		MEDIUM_DASHED, DASH_DOT, MEDIUM_DASH_DOT, DASH_DOT_DOT, 
		MEDIUM_DASH_DOT_DOT, SLANTED_DASH_DOT;
	}
	
	/**
	 * @return left border
	 */
	public BorderType getBorderLeft();
	public void setBorderLeft(BorderType type);

	/**
	 * @return top border
	 */
	public BorderType getBorderTop();
	public void setBorderTop(BorderType type);

	/**
	 * @return right border
	 */
	public BorderType getBorderRight();
	public void setBorderRight(BorderType type);

	/**
	 * @return bottom border
	 */
	public BorderType getBorderBottom();
	public void setBorderBottom(BorderType type);

	/**
	 * @return vertical border
	 */
	public BorderType getBorderVertical();
	public void setBorderVertical(BorderType type);
	
	/**
	 * @return horizontal border
	 */
	public BorderType getBorderHorizontal();
	public void setBorderHorizontal(BorderType type);
	
	/**
	 * @return diagonal border
	 */
	public BorderType getBorderDiagonal();
	public void setBorderDiagonal(BorderType type);
	
	/**
	 * @return top border color
	 */
	public SColor getBorderTopColor();
	public void setBorderTopColor(SColor color);

	/** 
	 * @return left border color
	 */
	public SColor getBorderLeftColor();
	public void setBorderLeftColor(SColor color);

	/**
	 * 
	 * @return bottom border color
	 */
	public SColor getBorderBottomColor();
	public void setBorderBottomColor(SColor color);

	/**
	 * @return right border color
	 */
	public SColor getBorderRightColor();
	public void setBorderRightColor(SColor color);

	/**
	 * @return vertical border color
	 */
	public SColor getBorderVerticalColor();
	public void setBorderVerticalColor(SColor color);
	
	/**
	 * @return horizontal border color
	 */
	public SColor getBorderHorizontalColor();
	public void setBorderHorizontalColor(SColor color);
	
	/**
	 * @return diagonal border color
	 */
	public SColor getBorderDiagonalColor();
	public void setBorderDiagonalColor(SColor color);
	
    /**
     * Whether show diagonalUp diagonal border
     * @return
     */
    public boolean isShowDiagonalUpBorder();
	public void setShowDiagonalUpBorder(boolean show);
    
    /**
     * Whether show diagonalDown diagonal border
     * @return
     */
    public boolean isShowDiagonalDownBorder();
	public void setShowDiagonalDownBorder(boolean show);
	
	/**
	 * Returns left border line.
	 * @return
	 */
	public SBorderLine getLeftLine();
	/**
	 * Returns top border line
	 * @return
	 */
	public SBorderLine getTopLine();
	/**
	 * Returns right border line
	 * @return
	 */
	public SBorderLine getRightLine();
	/**
	 * Returns bottom border line
	 * @return
	 */
	public SBorderLine getBottomLine();
	/**
	 * Returns diagonal border line
	 * @return
	 */
	public SBorderLine getDiagonalLine();
	
	/**
	 * Returns vertical border line
	 * @return
	 */
	public SBorderLine getVerticalLine();
	
	/**
	 * Returns horizontal border line
	 * @return
	 */
	public SBorderLine getHorizontalLine();
	
}
