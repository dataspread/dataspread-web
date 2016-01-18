/* DxfCellStyle.java

	Purpose:
		
	Description:
		
	History:
		Dec 11, 2014 11:57:28 AM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.ss.usermodel;

/**
 * Cell style for Dxfs
 * @author henri
 */
public interface DxfCellStyle extends CellStyle {
    /**
     * set the type of border to use for the horizontal border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     * @since 3.9.5
     */
    public void setBorderHorizontal(short border);

    /**
     * get the type of border to use for the horizontal border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     * @since 3.9.6
     */
    short getBorderHorizontal();

    /**
     * 
     * @return
     * @since 3.9.6
     */
    Color getHorizontalBorderColorColor();

    /**
     * set the color to use for the horizontal border
     * @param color The index of the color definition
     * @since 3.9.6
     */
    void setHorizontalBorderColor(short color);

    /**
     * get the color to use for the horizontal border
     * @since 3.9.6
     */
    short getHorizontalBorderColor();

    /**
     * set the type of border to use for the horizontal border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     * @since 3.9.5
     */
    public void setBorderVertical(short border);

    /**
     * get the type of border to use for the vertical border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     * @since 3.9.6
     */
    short getBorderVertical();

    /**
     * 
     * @return
     * @since 3.9.6
     */
    Color getVerticalBorderColorColor();

    /**
     * set the color to use for the vertical border
     * @param color The index of the color definition
     * @since 3.9.6
     */
    void setVerticalBorderColor(short color);

    /**
     * get the color to use for the vertical border
     * @since 3.9.6
     */
    short getVerticalBorderColor();
}
