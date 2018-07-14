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
package org.zkoss.poi.xssf.usermodel.extensions;


import org.zkoss.poi.ss.usermodel.BorderStyle;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Color;
import org.zkoss.poi.xssf.model.ThemesTable;
import org.zkoss.poi.xssf.usermodel.XSSFColor;
import org.zkoss.poi.util.Internal;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;



/**
 * This element contains border formatting information, specifying border definition formats (left, right, top, bottom, diagonal)
 * for cells in the workbook.
 * Color is optional.
 */
public class XSSFCellBorder {
    private ThemesTable _theme;
    private CTBorder border;

    /**
     * Creates a Cell Border from the supplied XML definition
     */
    public XSSFCellBorder(CTBorder border, ThemesTable theme) {
        this(border);
        this._theme = theme;
    }

    /**
     * Creates a Cell Border from the supplied XML definition
     */
    public XSSFCellBorder(CTBorder border) {
        this.border = border;
    }

    /**
     * Creates a new, empty Cell Border.
     * You need to attach this to the Styles Table
     */
    public XSSFCellBorder() {
        border = CTBorder.Factory.newInstance();
    }

    /**
     * Records the Themes Table that is associated with
     *  the current font, used when looking up theme
     *  based colours and properties.
     */
    public void setThemesTable(ThemesTable themes) {
       this._theme = themes;
    }
    
    /**
     * The enumeration value indicating the side being used for a cell border.
     */
    public static enum BorderSide {
        TOP, RIGHT, BOTTOM, LEFT, DIAGONAL, HORIZONTAL, VERTICAL
    }

    /**
     * Returns the underlying XML bean.
     *
     * @return CTBorder
     */
    @Internal
    public CTBorder getCTBorder() {
        return border;
    }

    /**
     * Get the type of border to use for the selected border
     *
     * @param side -  - where to apply the color definition
     * @return borderstyle - the type of border to use. default value is NONE if border style is not set.
     * @see BorderStyle
     */
    public BorderStyle getBorderStyle(BorderSide side) {
        CTBorderPr ctBorder = getBorder(side);
        STBorderStyle.Enum border = ctBorder == null ? STBorderStyle.NONE : ctBorder.getStyle();
        return BorderStyle.values()[border.intValue() - 1];
    }

    /**
     * Set the type of border to use for the selected border
     *
     * @param side  -  - where to apply the color definition
     * @param style - border style
     * @see BorderStyle
     */
    public void setBorderStyle(BorderSide side, BorderStyle style) {
    	setBorderStyle(side, style.ordinal());
    }
    public void setBorderStyle(BorderSide side, int ordinal) {
        CTBorderPr bpr = getBorder(side, true);
    	if (ordinal != CellStyle.BORDER_NONE) {
    		bpr.setStyle(STBorderStyle.Enum.forInt(ordinal + 1));
    	}
    }

    /**
     * Get the color to use for the selected border
     *
     * @param side - where to apply the color definition
     * @return color - color to use as XSSFColor. null if color is not set
     */
    public XSSFColor getBorderColor(BorderSide side) {
        CTBorderPr borderPr = getBorder(side);
        
        if(borderPr != null && borderPr.isSetColor()) { 
            XSSFColor clr = new XSSFColor(borderPr.getColor());
            if(_theme != null) {
               _theme.inheritFromThemeAsRequired(clr);
            }
            return clr;
        } else {
           // No border set
           return null;
        }
    }

    /**
     * Set the color to use for the selected border
     *
     * @param side  - where to apply the color definition
     * @param color - the color to use
     */
    public void setBorderColor(BorderSide side, XSSFColor color) {
        CTBorderPr borderPr = getBorder(side, true);
        if (color == null) borderPr.unsetColor();
        else
            borderPr.setColor(color.getCTColor());
    }

    private CTBorderPr getBorder(BorderSide side) {
        return getBorder(side, false);
    }


    private CTBorderPr getBorder(BorderSide side, boolean ensure) {
        CTBorderPr borderPr;
        switch (side) {
            case TOP:
                borderPr = border.getTop();
                if (ensure && borderPr == null) borderPr = border.addNewTop();
                break;
            case RIGHT:
                borderPr = border.getRight();
                if (ensure && borderPr == null) borderPr = border.addNewRight();
                break;
            case BOTTOM:
                borderPr = border.getBottom();
                if (ensure && borderPr == null) borderPr = border.addNewBottom();
                break;
            case LEFT:
                borderPr = border.getLeft();
                if (ensure && borderPr == null) borderPr = border.addNewLeft();
                break;
            case DIAGONAL:
                borderPr = border.getDiagonal();
                if (ensure && borderPr == null) borderPr = border.addNewDiagonal();
                break;
            case HORIZONTAL:
                borderPr = border.getHorizontal();
                if (ensure && borderPr == null) borderPr = border.addNewHorizontal();
                break;
            case VERTICAL:
                borderPr = border.getVertical();
                if (ensure && borderPr == null) borderPr = border.addNewVertical();
                break;
            default:
                throw new IllegalArgumentException("No suitable side specified for the border");
        }
        return borderPr;
    }


    public int hashCode() {
        return border.toString().hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof XSSFCellBorder)) return false;

        XSSFCellBorder cf = (XSSFCellBorder) o;
        return border.toString().equals(cf.getCTBorder().toString());
    }
    
    public void setDiagonalUp(boolean up) {
    	border.setDiagonalUp(up);
    }
    
    public boolean isDiagonalUp() {
    	return border.getDiagonalUp();
    }
    
    public void setDiagonalDown(boolean down) {
    	border.setDiagonalDown(down);
    }
    
    public boolean isDiagonalDown() {
    	return border.getDiagonalDown();
    }
    
    //ZSS-854
    public void prepareBorder(short left, Color leftColor, short top, Color topColor, 
        	short right, Color rightColor, short bottom, Color bottomColor) {
    	
        // always generate <left/> ... 
      	this.setBorderStyle(BorderSide.LEFT, left);
    	this.setBorderStyle(BorderSide.TOP, top);
    	this.setBorderStyle(BorderSide.RIGHT, right);
    	this.setBorderStyle(BorderSide.BOTTOM, bottom);
        if (left != CellStyle.BORDER_NONE) {
           	this.setBorderColor(BorderSide.LEFT, (XSSFColor)leftColor);
        }
        if (top != CellStyle.BORDER_NONE) {
           	this.setBorderColor(BorderSide.TOP, (XSSFColor)topColor);
        }
        if (right != CellStyle.BORDER_NONE) {
           	this.setBorderColor(BorderSide.RIGHT, (XSSFColor)rightColor);
        }
        if (bottom != CellStyle.BORDER_NONE) {
           	this.setBorderColor(BorderSide.BOTTOM, (XSSFColor)bottomColor);
        }
    }
}