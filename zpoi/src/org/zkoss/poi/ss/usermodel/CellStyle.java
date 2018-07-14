/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.ss.usermodel;

public interface CellStyle {

    /**
     * general (normal) horizontal alignment
     */

    public final static short ALIGN_GENERAL = 0x0;

    /**
     * left-justified horizontal alignment
     */

    public final static short ALIGN_LEFT = 0x1;

    /**
     * center horizontal alignment
     */

    public final static short ALIGN_CENTER = 0x2;

    /**
     * right-justified horizontal alignment
     */

    public final static short ALIGN_RIGHT = 0x3;

    /**
     * fill? horizontal alignment
     */

    public final static short ALIGN_FILL = 0x4;

    /**
     * justified horizontal alignment
     */

    public final static short ALIGN_JUSTIFY = 0x5;

    /**
     * center-selection? horizontal alignment
     */

    public final static short ALIGN_CENTER_SELECTION = 0x6;

    /**
     * top-aligned vertical alignment
     */

    public final static short VERTICAL_TOP = 0x0;

    /**
     * center-aligned vertical alignment
     */

    public final static short VERTICAL_CENTER = 0x1;

    /**
     * bottom-aligned vertical alignment
     */

    public final static short VERTICAL_BOTTOM = 0x2;

    /**
     * vertically justified vertical alignment
     */

    public final static short VERTICAL_JUSTIFY = 0x3;

    /**
     * No border
     */

    public final static short BORDER_NONE = 0x0;

    /**
     * Thin border
     */

    public final static short BORDER_THIN = 0x1;

    /**
     * Medium border
     */

    public final static short BORDER_MEDIUM = 0x2;

    /**
     * dash border
     */

    public final static short BORDER_DASHED = 0x3;

    /**
     * dot border
     */

    public final static short BORDER_DOTTED = 0x4;

    /**
     * Thick border
     */

    public final static short BORDER_THICK = 0x5;

    /**
     * double-line border
     */

    public final static short BORDER_DOUBLE = 0x6;

    /**
     * hair-line border
     */

    public final static short BORDER_HAIR = 0x7;

    /**
     * Medium dashed border
     */

    public final static short BORDER_MEDIUM_DASHED = 0x8;

    /**
     * dash-dot border
     */

    public final static short BORDER_DASH_DOT = 0x9;

    /**
     * medium dash-dot border
     */

    public final static short BORDER_MEDIUM_DASH_DOT = 0xA;

    /**
     * dash-dot-dot border
     */

    public final static short BORDER_DASH_DOT_DOT = 0xB;

    /**
     * medium dash-dot-dot border
     */

    public final static short BORDER_MEDIUM_DASH_DOT_DOT = 0xC;

    /**
     * slanted dash-dot border
     */

    public final static short BORDER_SLANTED_DASH_DOT = 0xD;

    /**  No background */
    public final static short NO_FILL = 0;

    /**  Solidly filled */
    public final static short SOLID_FOREGROUND = 1;

    /**  Small fine dots */
    public final static short FINE_DOTS = 2;

    /**  Wide dots */
    public final static short ALT_BARS = 3;

    /**  Sparse dots */
    public final static short SPARSE_DOTS = 4;

    /**  Thick horizontal bands */
    public final static short THICK_HORZ_BANDS = 5;

    /**  Thick vertical bands */
    public final static short THICK_VERT_BANDS = 6;

    /**  Thick backward facing diagonals */
    public final static short THICK_BACKWARD_DIAG = 7;

    /**  Thick forward facing diagonals */
    public final static short THICK_FORWARD_DIAG = 8;

    /**  Large spots */
    public final static short BIG_SPOTS = 9;

    /**  Brick-like layout */
    public final static short BRICKS = 10;

    /**  Thin horizontal bands */
    public final static short THIN_HORZ_BANDS = 11;

    /**  Thin vertical bands */
    public final static short THIN_VERT_BANDS = 12;

    /**  Thin backward diagonal */
    public final static short THIN_BACKWARD_DIAG = 13;

    /**  Thin forward diagonal */
    public final static short THIN_FORWARD_DIAG = 14;

    /**  Squares */
    public final static short SQUARES = 15;

    /**  Diamonds */
    public final static short DIAMONDS = 16;

    /**  Less Dots */
    public final static short LESS_DOTS = 17;

    /**  Least Dots */
    public final static short LEAST_DOTS = 18;

    /**
     * get the index within the Workbook (sequence within the collection of ExtnededFormat objects)
     * @return unique index number of the underlying record this style represents (probably you don't care
     *  unless you're comparing which one is which)
     */

    short getIndex();

    /**
     * set the data format (must be a valid format)
     * @see DataFormat
     */

    void setDataFormat(short fmt);

    /**
     * get the index of the format
     * @see DataFormat
     */
    short getDataFormat();

    /**
     * Get the format string
     */
    public String getDataFormatString();
    
    //20140213, dennischen@zkoss.org get the data format string that doesn't depends on locale
    /**
     * Get the raw data format string that are not transfered by ZSS Context Locale
     * @return
     */
    public String getRawDataFormatString();
    
    //20140213, dennischen@zkoss.org the way to know a format is a builtin format
    /**
     * Check if the data format is build in format
     * @return
     */
    public boolean isBuiltinDataFormat();

    /**
     * set the font for this style
     * @param font  a font object created or retreived from the Workbook object
     * @see Workbook#createFont()
     * @see Workbook#getFontAt(short)
     */

    void setFont(Font font);

    /**
     * gets the index of the font for this style
     * @see Workbook#getFontAt(short)
     */
    short getFontIndex();

    /**
     * set the cell's using this style to be hidden
     * @param hidden - whether the cell using this style should be hidden
     */

    void setHidden(boolean hidden);

    /**
     * get whether the cell's using this style are to be hidden
     * @return hidden - whether the cell using this style should be hidden
     */

    boolean getHidden();

    /**
     * set the cell's using this style to be locked
     * @param locked - whether the cell using this style should be locked
     */

    void setLocked(boolean locked);

    /**
     * get whether the cell's using this style are to be locked
     * @return hidden - whether the cell using this style should be locked
     */

    boolean getLocked();

    /**
     * set the type of horizontal alignment for the cell
     * @param align - the type of alignment
     * @see #ALIGN_GENERAL
     * @see #ALIGN_LEFT
     * @see #ALIGN_CENTER
     * @see #ALIGN_RIGHT
     * @see #ALIGN_FILL
     * @see #ALIGN_JUSTIFY
     * @see #ALIGN_CENTER_SELECTION
     */

    void setAlignment(short align);

    /**
     * get the type of horizontal alignment for the cell
     * @return align - the type of alignment
     * @see #ALIGN_GENERAL
     * @see #ALIGN_LEFT
     * @see #ALIGN_CENTER
     * @see #ALIGN_RIGHT
     * @see #ALIGN_FILL
     * @see #ALIGN_JUSTIFY
     * @see #ALIGN_CENTER_SELECTION
     */

    short getAlignment();

    /**
     * Set whether the text should be wrapped.
     * Setting this flag to <code>true</code> make all content visible
     * whithin a cell by displaying it on multiple lines
     *
     * @param wrapped  wrap text or not
     */

    void setWrapText(boolean wrapped);

    /**
     * get whether the text should be wrapped
     * @return wrap text or not
     */

    boolean getWrapText();

    /**
     * set the type of vertical alignment for the cell
     * @param align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */

    void setVerticalAlignment(short align);

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */

    short getVerticalAlignment();

    /**
     * set the degree of rotation for the text in the cell
     * @param rotation degrees (between -90 and 90 degrees)
     */

    void setRotation(short rotation);

    /**
     * get the degree of rotation for the text in the cell
     * @return rotation degrees (between -90 and 90 degrees)
     */

    short getRotation();

    /**
     * set the number of spaces to indent the text in the cell
     * @param indent - number of spaces
     */

    void setIndention(short indent);

    /**
     * get the number of spaces to indent the text in the cell
     * @return indent - number of spaces
     */

    short getIndention();

    /**
     * set the type of border to use for the left border of the cell
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
     */

    void setBorderLeft(short border);

    /**
     * get the type of border to use for the left border of the cell
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
     */

    short getBorderLeft();

    /**
     * set the type of border to use for the right border of the cell
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
     */

    void setBorderRight(short border);

    /**
     * get the type of border to use for the right border of the cell
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
     */

    short getBorderRight();

    /**
     * set the type of border to use for the top border of the cell
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
     */

    void setBorderTop(short border);

    /**
     * get the type of border to use for the top border of the cell
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
     */

    short getBorderTop();

    /**
     * set the type of border to use for the bottom border of the cell
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
     */

    void setBorderBottom(short border);

    /**
     * get the type of border to use for the bottom border of the cell
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
     */
    short getBorderBottom();

    /**
     * set the color to use for the left border
     * @param color The index of the color definition
     */
    void setLeftBorderColor(short color);

    /**
     * get the color to use for the left border
     */
    short getLeftBorderColor();

    /**
     * set the color to use for the right border
     * @param color The index of the color definition
     */
    void setRightBorderColor(short color);

    /**
     * get the color to use for the left border
     * @return the index of the color definition
     */
    short getRightBorderColor();

    /**
     * set the color to use for the top border
     * @param color The index of the color definition
     */
    void setTopBorderColor(short color);

    /**
     * get the color to use for the top border
     * @return hhe index of the color definition
     */
    short getTopBorderColor();

    /**
     * set the color to use for the bottom border
     * @param color The index of the color definition
     */
    void setBottomBorderColor(short color);

    /**
     * get the color to use for the left border
     * @return the index of the color definition
     */
    short getBottomBorderColor();

    /**
     * setting to one fills the cell with the foreground color... No idea about
     * other values
     *
     * @see #NO_FILL
     * @see #SOLID_FOREGROUND
     * @see #FINE_DOTS
     * @see #ALT_BARS
     * @see #SPARSE_DOTS
     * @see #THICK_HORZ_BANDS
     * @see #THICK_VERT_BANDS
     * @see #THICK_BACKWARD_DIAG
     * @see #THICK_FORWARD_DIAG
     * @see #BIG_SPOTS
     * @see #BRICKS
     * @see #THIN_HORZ_BANDS
     * @see #THIN_VERT_BANDS
     * @see #THIN_BACKWARD_DIAG
     * @see #THIN_FORWARD_DIAG
     * @see #SQUARES
     * @see #DIAMONDS
     *
     * @param fp  fill pattern (set to 1 to fill w/foreground color)
     */
    void setFillPattern(short fp);

    /**
     * get the fill pattern (??) - set to 1 to fill with foreground color
     * @return fill pattern
     */

    short getFillPattern();

    /**
     * set the background fill color.
     *
     * @param bg  color
     */

    void setFillBackgroundColor(short bg);

    /**
     * get the background fill color, if the fill
     *  is defined with an indexed color.
     * @return fill color index, or 0 if not indexed (XSSF only)
     */
    short getFillBackgroundColor();
    
    /**
     * Gets the color object representing the current
     *  background fill, resolving indexes using
     *  the supplied workbook.
     * This will work for both indexed and rgb
     *  defined colors. 
     */
    Color getFillBackgroundColorColor();

    /**
     * set the foreground fill color
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     * @param bg  color
     */
    void setFillForegroundColor(short bg);

    /**
     * get the foreground fill color, if the fill  
     *  is defined with an indexed color.
     * @return fill color, or 0 if not indexed (XSSF only)
     */
    short getFillForegroundColor();
    
    /**
     * Gets the color object representing the current
     *  foreground fill, resolving indexes using
     *  the supplied workbook.
     * This will work for both indexed and rgb
     *  defined colors. 
     */
    Color getFillForegroundColorColor();

    /**
     * Clones all the style information from another
     *  CellStyle, onto this one. This 
     *  CellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this CellStyle will be lost! 
     *  
     * The source CellStyle could be from another
     *  Workbook if you like. This allows you to
     *  copy styles from one Workbook to another.
     *
     * However, both of the CellStyles will need
     *  to be of the same type (HSSFCellStyle or
     *  XSSFCellStyle)
     */
    public void cloneStyleFrom(CellStyle source);

    //20100921, henrichen@zkoss.org: fetch boarder color object
    public Color getTopBorderColorColor();
    public Color getBottomBorderColorColor();
    public Color getRightBorderColorColor();
    public Color getLeftBorderColorColor();
    public void setFontColorColor(Color fontColor);

    //20141007, henrichen@zkoss.org: must do it in a whole, cannot call 
    //  setBorderBottom() .. one by one or there will be half-baked <border/>
    //ZSS-787
    public void setBorder(short left, Color leftColor, short top, Color topColor, 
    	short right, Color rightColor, short bottom, Color bottomColor);
    
    //@since 3.9.6
    public void setBorder(short left, Color leftColor, short top, Color topColor, 
        	short right, Color rightColor, short bottom, Color bottomColor, 
        	short diagonal, Color diagonalColor, 
        	short horizontal, Color horizontalColor, 
        	short vertical, Color verticalColor,
        	boolean diaUp, boolean diaDown);

    //ZSS-787
    public void setFill(Color fillColor, Color backColor, short pattern);
    
    //ZSS-787
    @Deprecated
    public void setCellAlignment(short hAlign, short vAlign, boolean wrapText);
    
    //ZSS-1020
    //@since 3.9.8 
    public void setCellAlignment(short hAlign, short vAlign, boolean wrapText, short rotation);

    //ZSS-787
    public void setProtection(boolean locked, boolean hidden);

    /**
     * set the type of border to use for the diagonal border of the cell
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
    public void setBorderDiagonal(short border);

    /**
     * get the type of border to use for the diagonal border of the cell
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
    short getBorderDiagonal();

    /**
     * 
     * @return
     * @since 3.9.6
     */
    Color getDiagonalBorderColorColor();

    /**
     * set the color to use for the diagonal border
     * @param color The index of the color definition
     * @since 3.9.6
     */
    void setDiagonalBorderColor(short color);

    /**
     * get the color to use for the diagonal border
     * @since 3.9.6
     */
    short getDiagonalBorderColor();
    
    /**
     * Whether show diagonalUp diagonal border
     * @return
     * @since 3.9.6
     */
    boolean isShowDiagonalUpBorder();
    
    /**
     * Whether show diagonalUp diagonal border
     * @return
     * @since 3.9.6
     */
    void setShowDiagonalUpBorder(boolean up);
    
    /**
     * Whether show diagonalDown diagonal border
     * @return
     * @since 3.9.6
     */
    boolean isShowDiagonalDownBorder();

    /**
     * Whether show diagonalDown diagonal border
     * @return
     * @since 3.9.6
     */
    void setShowDiagonalDownBorder(boolean down);
}
