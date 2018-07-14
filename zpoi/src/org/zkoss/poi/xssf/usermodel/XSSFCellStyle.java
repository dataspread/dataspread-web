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

package org.zkoss.poi.xssf.usermodel;

import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;
import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.ss.usermodel.BorderStyle;
import org.zkoss.poi.ss.usermodel.BuiltinFormats;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Color;
import org.zkoss.poi.ss.usermodel.FillPatternType;
import org.zkoss.poi.ss.usermodel.Font;
import org.zkoss.poi.ss.usermodel.HorizontalAlignment;
import org.zkoss.poi.ss.usermodel.IndexedColors;
import org.zkoss.poi.ss.usermodel.VerticalAlignment;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.xssf.model.StylesTable;
import org.zkoss.poi.xssf.model.ThemesTable;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellAlignment;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;


/**
 *
 * High level representation of the the possible formatting information for the contents of the cells on a sheet in a
 * SpreadsheetML document.
 *
 * @see org.zkoss.poi.xssf.usermodel.XSSFWorkbook#createCellStyle()
 * @see org.zkoss.poi.xssf.usermodel.XSSFWorkbook#getCellStyleAt(short)
 * @see org.zkoss.poi.xssf.usermodel.XSSFCell#setCellStyle(org.zkoss.poi.ss.usermodel.CellStyle)
 */
public class XSSFCellStyle implements CellStyle {

    private int _cellXfId;
    private StylesTable _stylesSource;
    private CTXf _cellXf;
    private CTXf _cellStyleXf;
    private XSSFFont _font;
    private XSSFCellAlignment _cellAlignment;
    private ThemesTable _theme;

    /**
     * Creates a Cell Style from the supplied parts
     * @param cellXfId The main XF for the cell. Must be a valid 0-based index into the XF table
     * @param cellStyleXfId Optional, style xf. A value of <code>-1</code> means no xf.
     * @param stylesSource Styles Source to work off
     */
    public XSSFCellStyle(int cellXfId, int cellStyleXfId, StylesTable stylesSource, ThemesTable theme) {
        _cellXfId = cellXfId;
        _stylesSource = stylesSource;
        _cellXf = this._cellXfId < 0 ? null : stylesSource.getCellXfAt(this._cellXfId);
        _cellStyleXf = cellStyleXfId == -1 ? null : stylesSource.getCellStyleXfAt(cellStyleXfId);
        _theme = theme;
    }
    
    //ZSS-854: for export
    public XSSFCellStyle(CTXf cellStyleXf, StylesTable stylesSource, ThemesTable theme) {
    	_cellXfId = -1;
    	_stylesSource = stylesSource;
    	_cellXf = null;
    	_cellStyleXf = cellStyleXf;
    	_theme = theme;
    }
    
    /**
     * Used so that StylesSource can figure out our location
     */
    @Internal
    public CTXf getCoreXf() {
        return _cellXf;
    }

    /**
     * Used so that StylesSource can figure out our location
     */
    @Internal
    public CTXf getStyleXf() {
        return _cellStyleXf;
    }

    /**
     * Creates an empty Cell Style
     */
    public XSSFCellStyle(StylesTable stylesSource) {
        _stylesSource = stylesSource;
        // We need a new CTXf for the main styles
        // TODO decide on a style ctxf
        _cellXf = CTXf.Factory.newInstance();
        _cellStyleXf = null;
    }

    /**
     * Verifies that this style belongs to the supplied Workbook
     *  Styles Source.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     *  cell, to ensure the cell and the style are from the same
     *  workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToStylesSource(StylesTable src) {
        if(this._stylesSource != src) {
            throw new IllegalArgumentException("This Style does not belong to the supplied Workbook Stlyes Source. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
        }
    }

    /**
     * Clones all the style information from another
     *  XSSFCellStyle, onto this one. This
     *  XSSFCellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this XSSFCellStyle will be lost!
     *
     * The source XSSFCellStyle could be from another
     *  XSSFWorkbook if you like. This allows you to
     *  copy styles from one XSSFWorkbook to another.
     */
    public void cloneStyleFrom(CellStyle source) {
        if(source instanceof XSSFCellStyle) {
            XSSFCellStyle src = (XSSFCellStyle)source;
            
            // Is it on our Workbook?
            if(src._stylesSource == _stylesSource) {
               // Nice and easy
               _cellXf.set(src.getCoreXf());
               _cellStyleXf.set(src.getStyleXf());
            } else {
               // Copy the style
               try {
                  // Remove any children off the current style, to
                  //  avoid orphaned nodes
                  if(_cellXf.isSetAlignment())
                     _cellXf.unsetAlignment();
                  if(_cellXf.isSetExtLst())
                     _cellXf.unsetExtLst();
                  
                  // Create a new Xf with the same contents
                  _cellXf = CTXf.Factory.parse(
                        src.getCoreXf().toString()
                  );
                  // Swap it over
                  _stylesSource.replaceCellXfAt(_cellXfId, _cellXf);
               } catch(XmlException e) {
                  throw new POIXMLException(e);
               }
               
               // Copy the format
               String fmt = src.getDataFormatString();
               setDataFormat(
                     (new XSSFDataFormat(_stylesSource)).getFormat(fmt)
               );
               
               // Copy the font
               try {
                  CTFont ctFont = CTFont.Factory.parse(
                        src.getFont().getCTFont().toString()
                  );
                  XSSFFont font = new XSSFFont(ctFont);
                  font.registerTo(_stylesSource);
                  setFont(font);
               } catch(XmlException e) {
                  throw new POIXMLException(e);
               }
               
				// 20130306, samchuang@zkoss.org, ZSS-210
				// Copy the fill
               	if (src.getCoreXf().getApplyFill()) {
					try {
						CTFill ctFill = CTFill.Factory.parse(src.getCTFill().toString());
						int index = _stylesSource.putFill(new XSSFCellFill(ctFill));
						_cellXf.setFillId(index);
						_cellXf.setApplyFill(true);
					} catch (XmlException e) {
						throw new POIXMLException(e);
					}
				}
               
				// 20130306, samchuang@zkoss.org, ZSS-210
				// Copy the border
				if (src.getCoreXf().getApplyBorder()) {
					try {
						CTBorder ctBorder = CTBorder.Factory.parse(src.getCTBorder().toString());
						int index = _stylesSource.putBorder(new XSSFCellBorder(ctBorder, _theme));
						_cellXf.setBorderId(index);
						_cellXf.setApplyBorder(true);
					} catch (XmlException e) {
						throw new POIXMLException(e);
					}
				}
            }
            
            // Clear out cached details
            _font = null;
            _cellAlignment = null;
        } else {
            throw new IllegalArgumentException("Can only clone from one XSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle");
        }
    }

    /**
     * Get the type of horizontal alignment for the cell
     *
     * @return short - the type of alignment
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_GENERAL
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_LEFT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_CENTER
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_RIGHT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_FILL
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_JUSTIFY
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_CENTER_SELECTION
     */
    public short getAlignment() {
        return (short)(getAlignmentEnum().ordinal());
    }

    /**
     * Get the type of horizontal alignment for the cell
     *
     * @return HorizontalAlignment - the type of alignment
     * @see org.zkoss.poi.ss.usermodel.HorizontalAlignment
     */
    public HorizontalAlignment getAlignmentEnum() {
    	if (_cellXf != null && _cellXf.isSetAlignment()) {
	        CTCellAlignment align = _cellXf.getAlignment();
	        if(align != null && align.isSetHorizontal()) {
	            return HorizontalAlignment.values()[align.getHorizontal().intValue()-1];
	        }
    	}
    	CTCellAlignment align = _cellStyleXf == null ? 
    			null : _cellStyleXf.getAlignment();
        if(align != null && align.isSetHorizontal()) {
            return HorizontalAlignment.values()[align.getHorizontal().intValue()-1];
        }
        return HorizontalAlignment.GENERAL;
    }
    
    /**
     * Get the type of border to use for the bottom border of the cell
     *
     * @return short - border type
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderBottom() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetBottom() ? ct.getBottom().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	int idx = _cellStyleXf == null ? -1 : (int) _cellStyleXf.getBorderId();
    	if (idx >= 0) {
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetBottom() ? ct.getBottom().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
    	}
    	return BORDER_NONE;
    }

    /**
     * Get the type of border to use for the bottom border of the cell
     *
     * @return border type as Java enum
     * @see BorderStyle
     */
    public BorderStyle getBorderBottomEnum() {
        int style  = getBorderBottom();
        return BorderStyle.values()[style];
    }

    /**
     * Get the type of border to use for the left border of the cell
     *
     * @return short - border type, default value is {@link org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE}
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderLeft() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }

        int idx = _cellStyleXf == null ? -1 : (int)_cellStyleXf.getBorderId();
        if (idx >= 0) {
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
    }

    /**
     * Get the type of border to use for the left border of the cell
     *
     * @return border type, default value is {@link org.zkoss.poi.ss.usermodel.BorderStyle#NONE}
     */
    public BorderStyle getBorderLeftEnum() {
        int style  = getBorderLeft();
        return BorderStyle.values()[style];
    }

    /**
     * Get the type of border to use for the right border of the cell
     *
     * @return short - border type, default value is {@link org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE}
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderRight() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetRight() ? ct.getRight().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }

        int idx = _cellStyleXf == null ? -1 : (int)_cellStyleXf.getBorderId();
        if (idx >= 0) {
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetRight() ? ct.getRight().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
    }

    /**
     * Get the type of border to use for the right border of the cell
     *
     * @return border type, default value is {@link org.zkoss.poi.ss.usermodel.BorderStyle#NONE}
     */
    public BorderStyle getBorderRightEnum() {
        int style  = getBorderRight();
        return BorderStyle.values()[style];
    }

    /**
     * Get the type of border to use for the top border of the cell
     *
     * @return short - border type, default value is {@link org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE}
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle #BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public short getBorderTop() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }

        int idx = _cellStyleXf == null ? -1 : (int)_cellStyleXf.getBorderId();
        if (idx >= 0) {
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
    }

     /**
     * Get the type of border to use for the top border of the cell
     *
     * @return border type, default value is {@link org.zkoss.poi.ss.usermodel.BorderStyle#NONE}
     */
    public BorderStyle getBorderTopEnum() {
         int style  = getBorderTop();
         return BorderStyle.values()[style];
    }

    /**
     * Get the color to use for the bottom border
     * <br/>
     * Color is optional. When missing, IndexedColors.AUTOMATIC is implied.
     * @return the index of the color definition, default value is {@link org.zkoss.poi.ss.usermodel.IndexedColors#AUTOMATIC}
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public short getBottomBorderColor() {
        XSSFColor clr = getBottomBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }

    /**
     * Get the color to use for the bottom border as a {@link XSSFColor}
     *
     * @return the used color or <code>null</code> if not set
     */
    public XSSFColor getBottomBorderXSSFColor() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            XSSFCellBorder border = _stylesSource.getBorderAt(idx);

            return border.getBorderColor(BorderSide.BOTTOM);
        }

        int idx = _cellStyleXf == null ? -1 : (int)_cellStyleXf.getBorderId();
        if (idx >= 0) {
            XSSFCellBorder border = _stylesSource.getBorderAt(idx);
            return border.getBorderColor(BorderSide.BOTTOM);
        }
    	return null;
    }

    /**
     * Get the index of the number format (numFmt) record used by this cell format.
     *
     * @return the index of the number format
     */
    public short getDataFormat() {
        if(_cellXf != null && _cellXf.getApplyNumberFormat()) {
            return (short)_cellXf.getNumFmtId();
        }
        return _cellStyleXf == null ? 0 : (short)_cellStyleXf.getNumFmtId();
    }

    /**
     * Get the contents of the format string, by looking up
     * the StylesSource
     *
     * @return the number format string
     */
    public String getDataFormatString() {
        int idx = getDataFormat();
        return new XSSFDataFormat(_stylesSource).getFormat((short)idx);
    }
    
    //20140213 dennischen@zkoss.org 
    //the implementation depends on XSSFDataFormat.getFormat(short)
    public String getRawDataFormatString(){
    	 int idx = getDataFormat();
         return new XSSFDataFormat(_stylesSource).getRawFormat((short)idx);
    }
    //20140213 dennischen@zkoss.org 
    public boolean isBuiltinDataFormat(){
    	int idx = getDataFormat();
    	return _stylesSource.getNumberFormatAt(idx)==null && BuiltinFormats.getBuiltinFormat(idx)!=null;
    }
    

    /**
     * Get the background fill color.
     * <p>
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * </p>
     * @return fill color, default value is {@link org.zkoss.poi.ss.usermodel.IndexedColors#AUTOMATIC}
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public short getFillBackgroundColor() {
        XSSFColor clr = getFillBackgroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
    }
    
    public XSSFColor getFillBackgroundColorColor() {
       return getFillBackgroundXSSFColor();
    }

    /**
     * Get the background fill color.
     * <p>
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * </p>
     * @see org.zkoss.poi.xssf.usermodel.XSSFColor#getRgb()
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillBackgroundXSSFColor() {
    	int fillIndex = getFillId();
    	if (fillIndex < 0) return null;
        XSSFCellFill fg = _stylesSource.getFillAt(fillIndex);

        XSSFColor fillBackgroundColor = fg.getFillBackgroundColor();
        if (fillBackgroundColor != null && _theme != null) {
            _theme.inheritFromThemeAsRequired(fillBackgroundColor);
        }
        return fillBackgroundColor;
    }

    /**
     * Get the foreground fill color.
     * <p>
     * Many cells are filled with this, instead of a
     *  background color ({@link #getFillBackgroundColor()})
     * </p>
     * @see IndexedColors
     * @return fill color, default value is {@link org.zkoss.poi.ss.usermodel.IndexedColors#AUTOMATIC}
     */
    public short getFillForegroundColor() {
        XSSFColor clr = getFillForegroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
    }

    public XSSFColor getFillForegroundColorColor() {
       return getFillForegroundXSSFColor();
    }
    
    /**
     * Get the foreground fill color.
     *
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillForegroundXSSFColor() {
    	int fillIndex = getFillId();
    	if (fillIndex < 0) return null;
        XSSFCellFill fg = _stylesSource.getFillAt(fillIndex);

        XSSFColor fillForegroundColor = fg.getFillForegroundColor();
        if (fillForegroundColor != null && _theme != null) {
            _theme.inheritFromThemeAsRequired(fillForegroundColor);
        }
        return fillForegroundColor;
    }
    
    //ZSS-780
    //if no applyFill in _cellXf, should use the fillId inside _cellStyleXf(_cellStyleXfs[_cellXf.xfId])
    private int getFillId() {
        if(_cellXf != null && _cellXf.getApplyFill()) {
        	return (int)_cellXf.getFillId();
        }
    	return _cellStyleXf != null ? 
    		(int)_cellStyleXf.getFillId() : -1;
    }
    

    /**
     * Get the fill pattern
     * @return fill pattern, default value is {@link org.zkoss.poi.ss.usermodel.CellStyle#NO_FILL}
     *
     * @see org.zkoss.poi.ss.usermodel.CellStyle#NO_FILL
     * @see org.zkoss.poi.ss.usermodel.CellStyle#SOLID_FOREGROUND
     * @see org.zkoss.poi.ss.usermodel.CellStyle#FINE_DOTS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALT_BARS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#SPARSE_DOTS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_HORZ_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_VERT_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_BACKWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_FORWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BIG_SPOTS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BRICKS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_HORZ_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_VERT_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_BACKWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_FORWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#SQUARES
     * @see org.zkoss.poi.ss.usermodel.CellStyle#DIAMONDS
     */
    public short getFillPattern() {
    	int fillIndex = getFillId();
    	if (fillIndex < 0) return 0;
        XSSFCellFill fill = _stylesSource.getFillAt(fillIndex);

        STPatternType.Enum ptrn = fill.getPatternType();
        if(ptrn == null) return CellStyle.NO_FILL;
        return (short)(ptrn.intValue() - 1);
    }

    /**
     * Get the fill pattern
     *
     * @return the fill pattern, default value is {@link org.zkoss.poi.ss.usermodel.FillPatternType#NO_FILL}
     */
    public FillPatternType getFillPatternEnum() {
        int style  = getFillPattern();
        return FillPatternType.values()[style];
    }

    /**
    * Gets the font for this style
    * @return Font - font
    */
    public XSSFFont getFont() {
        if (_font == null) {
            _font = _stylesSource.getFontAt(getFontId());
        }
        return _font;
    }

    /**
     * Gets the index of the font for this style
     *
     * @return short - font index
     * @see org.zkoss.poi.xssf.usermodel.XSSFWorkbook#getFontAt(short)
     */
    public short getFontIndex() {
        return (short) getFontId();
    }

    /**
     * Get whether the cell's using this style are to be hidden
     *
     * @return boolean -  whether the cell using this style is hidden
     */
    public boolean getHidden() {
    	if (_cellXf != null && _cellXf.isSetProtection() && _cellXf.getProtection().isSetHidden()) {
	        return _cellXf.getProtection().getHidden();
    	}
        if (_cellStyleXf != null && _cellStyleXf.isSetProtection() && _cellStyleXf.getProtection().isSetHidden()) {
            return _cellStyleXf.getProtection().getHidden();
        }
        return false;
    }

    /**
     * Get the number of spaces to indent the text in the cell
     *
     * @return indent - number of spaces
     */
    public short getIndention() {
    	if (_cellXf != null && _cellXf.isSetAlignment()) {
	        CTCellAlignment align = _cellXf.getAlignment();
	        return (short)(align == null ? 0 : align.getIndent());
    	}
        CTCellAlignment align = _cellStyleXf == null ? 
        		null : _cellStyleXf.getAlignment();
        return (short)(align == null ? 0 : align.getIndent());
    }

    /**
     * Get the index within the StylesTable (sequence within the collection of CTXf elements)
     *
     * @return unique index number of the underlying record this style represents
     */
    public short getIndex() {
        return (short)this._cellXfId;
    }

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition, default value is {@link org.zkoss.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public short getLeftBorderColor() {
        XSSFColor clr = getLeftBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getLeftBorderXSSFColor() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
	        int idx = (int)_cellXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.LEFT);
        }
        if (_cellStyleXf != null) {
	        int idx = (int)_cellStyleXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	        return border.getBorderColor(BorderSide.LEFT);
        }
        return null;
    }

    /**
     * Get whether the cell's using this style are locked
     *
     * @return whether the cell using this style are locked
     */
    public boolean getLocked() {
        if (_cellXf != null && _cellXf.isSetProtection() && _cellXf.getProtection().isSetLocked()) {
            return _cellXf.getProtection().getLocked();
        }
        if (_cellStyleXf != null && _cellStyleXf.isSetProtection() && _cellStyleXf.getProtection().isSetLocked()) {
            return _cellStyleXf.getProtection().getLocked();
        }
        return true;
    }

    /**
     * Get the color to use for the right border
     *
     * @return the index of the color definition, default value is {@link org.zkoss.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public short getRightBorderColor() {
        XSSFColor clr = getRightBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }
    /**
     * Get the color to use for the right border
     *
     * @return the used color or <code>null</code> if not set
     */
    public XSSFColor getRightBorderXSSFColor() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
	        int idx = (int)_cellXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.RIGHT);
        }
        if (_cellStyleXf != null) {
	        int idx = (int)_cellStyleXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.RIGHT);
        }
        return null;
    }

    /**
     * Get the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br/>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br/>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @return rotation degrees (between 0 and 180 degrees)
     */
    public short getRotation() {
    	if (_cellXf != null && _cellXf.isSetAlignment()) {
	        CTCellAlignment align = _cellXf.getAlignment();
	        return convertToUIRotation((short)(align == null ? 0 : align.getTextRotation())); //ZSS-1020
    	}
        CTCellAlignment align = 
        		_cellStyleXf == null ? null : _cellStyleXf.getAlignment();
        return convertToUIRotation((short)(align == null ? 0 : align.getTextRotation())); //ZSS-1020
    }
    //ZSS-1020
    private short convertToUIRotation(short rotation) {
        if (90 < rotation && rotation <= 180) {
        	rotation = (short)(90 - rotation);
        }
        return (short) rotation;
    }

    /**
     * Get the color to use for the top border
     *
     * @return the index of the color definition, default value is {@link org.zkoss.poi.ss.usermodel.IndexedColors#BLACK}
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public short getTopBorderColor() {
        XSSFColor clr = getTopBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
    }

    /**
     * Get the color to use for the top border
     *
     * @return the used color or <code>null</code> if not set
     */
    public XSSFColor getTopBorderXSSFColor() {
        if(_cellXf != null  && _cellXf.getApplyBorder()) { 
	        int idx = (int)_cellXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.TOP);
    	}
        if (_cellStyleXf != null) {
	        int idx = (int)_cellStyleXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.TOP);
        }
        return null;
    }

    /**
     * Get the type of vertical alignment for the cell
     *
     * @return align the type of alignment, default value is {@link org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_BOTTOM}
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_TOP
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_CENTER
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_BOTTOM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_JUSTIFY
     */
    public short getVerticalAlignment() {
        return (short) (getVerticalAlignmentEnum().ordinal());
    }

    /**
     * Get the type of vertical alignment for the cell
     *
     * @return the type of alignment, default value is {@link org.zkoss.poi.ss.usermodel.VerticalAlignment#BOTTOM}
     * @see org.zkoss.poi.ss.usermodel.VerticalAlignment
     */
    public VerticalAlignment getVerticalAlignmentEnum() {
    	if (_cellXf != null && _cellXf.isSetAlignment()) {
	        CTCellAlignment align = _cellXf.getAlignment();
	        if(align != null && align.isSetVertical() && align.xgetVertical().validate()) {
	            return VerticalAlignment.values()[align.getVertical().intValue()-1];
	        }
    	}
    	if (_cellStyleXf != null) {
	        CTCellAlignment align = _cellStyleXf.getAlignment();
	        if(align != null && align.isSetVertical() && align.xgetVertical().validate()) {
	            return VerticalAlignment.values()[align.getVertical().intValue()-1];
	        }
    	}
        return VerticalAlignment.BOTTOM;
    }

    /**
     * Whether the text should be wrapped
     *
     * @return  a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public boolean getWrapText() {
    	if (_cellXf != null && _cellXf.isSetAlignment()) {
	        CTCellAlignment align = _cellXf.getAlignment();
	        return align != null && align.getWrapText();
    	}
	    CTCellAlignment align = 
	    		_cellStyleXf == null ? null : _cellStyleXf.getAlignment();
	    return align != null && align.getWrapText();
    }

    /**
     * Set the type of horizontal alignment for the cell
     *
     * @param align - the type of alignment
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_GENERAL
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_LEFT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_CENTER
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_RIGHT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_FILL
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_JUSTIFY
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALIGN_CENTER_SELECTION
     */
    public void setAlignment(short align) {
        getCellAlignment().setHorizontal(HorizontalAlignment.values()[align]);
        _cellXf.setApplyAlignment(true);
    }

    /**
     * Set the type of horizontal alignment for the cell
     *
     * @param align - the type of alignment
     * @see org.zkoss.poi.ss.usermodel.HorizontalAlignment
     */
    public void setAlignment(HorizontalAlignment align) {
        setAlignment((short)align.ordinal());
    }

    /**
     * Set the type of border to use for the bottom border of the cell
     *
     * @param border the type of border to use
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public void setBorderBottom(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if(border == BORDER_NONE) ct.unsetBottom();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of border to use for the bottom border of the cell
     *
     * @param border - type of border to use
     * @see org.zkoss.poi.ss.usermodel.BorderStyle
     */
    public void setBorderBottom(BorderStyle border) {
	    setBorderBottom((short)border.ordinal());
    }

    /**
     * Set the type of border to use for the left border of the cell
     * @param border the type of border to use
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
    public void setBorderLeft(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if(border == BORDER_NONE) ct.unsetLeft();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

     /**
     * Set the type of border to use for the left border of the cell
      *
     * @param border the type of border to use
     */
    public void setBorderLeft(BorderStyle border) {
	    setBorderLeft((short)border.ordinal());
    }

    /**
     * Set the type of border to use for the right border of the cell
     *
     * @param border the type of border to use
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
   public void setBorderRight(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if(border == BORDER_NONE) ct.unsetRight();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

     /**
     * Set the type of border to use for the right border of the cell
      *
     * @param border the type of border to use
     */
    public void setBorderRight(BorderStyle border) {
	    setBorderRight((short)border.ordinal());
    }

    /**
     * Set the type of border to use for the top border of the cell
     *
     * @param border the type of border to use
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_NONE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THIN
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOTTED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_THICK
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DOUBLE
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_HAIR
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASHED
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_MEDIUM_DASH_DOT_DOT
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BORDER_SLANTED_DASH_DOT
     */
   public void setBorderTop(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
        if(border == BORDER_NONE) ct.unsetTop();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of border to use for the top border of the cell
     *
     * @param border the type of border to use
     */
    public void setBorderTop(BorderStyle border) {
	    setBorderTop((short)border.ordinal());
    }

    /**
     * Set the color to use for the bottom border
     * @param color the index of the color definition
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public void setBottomBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setBottomBorderColor(clr);
    }

    /**
     * Set the color to use for the bottom border
     *
     * @param color the color to use, null means no color
     */
    public void setBottomBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetBottom()) return;

        CTBorderPr pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the index of a data format
     *
     * @param fmt the index of a data format
     */
    public void setDataFormat(short fmt) {
    	if (fmt != 0) {
	        _cellXf.setApplyNumberFormat(true);
	        _cellXf.setNumFmtId(fmt);
    	}
    }

    /**
     * Set the background fill color represented as a {@link XSSFColor} value.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundXSSFColor(new XSSFColor(java.awt.Color.RED));
     * </pre>
     * optionally a Foreground and background fill can be applied:
     * <i>Note: Ensure Foreground color is set prior to background</i>
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(new XSSFColor(java.awt.Color.BLUE));
     * cs.setFillBackgroundColor(new XSSFColor(java.awt.Color.GREEN));
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(new XSSFColor(java.awt.Color.GREEN));
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param color - the color to use
     */
    public void setFillBackgroundColor(XSSFColor color) {
        CTFill ct = getCTFill();
        CTPatternFill ptrn = ct.getPatternFill();
        if(color == null) {
            if(ptrn != null) ptrn.unsetBgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setBgColor(color.getCTColor());
        }

        int idx = _stylesSource.putFill(new XSSFCellFill(ct));

        _cellXf.setFillId(idx);
        _cellXf.setApplyFill(true);
    }

    /**
     * Set the background fill color represented as a indexed color value.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundXSSFColor(IndexedColors.RED.getIndex());
     * </pre>
     * optionally a Foreground and background fill can be applied:
     * <i>Note: Ensure Foreground color is set prior to background</i>
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(IndexedColors.BLUE.getIndex());
     * cs.setFillBackgroundColor(IndexedColors.RED.getIndex());
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(IndexedColors.RED.getIndex());
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param bg - the color to use
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public void setFillBackgroundColor(short bg) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(bg);
        setFillBackgroundColor(clr);
    }

    /**
    * Set the foreground fill color represented as a {@link XSSFColor} value.
     * <br/>
    * <i>Note: Ensure Foreground color is set prior to background color.</i>
    * @param color the color to use
    * @see #setFillBackgroundColor(org.zkoss.poi.xssf.usermodel.XSSFColor) )
    */
    public void setFillForegroundColor(XSSFColor color) {
        CTFill ct = getCTFill();

        CTPatternFill ptrn = ct.getPatternFill();
        if(color == null) {
            if(ptrn != null) ptrn.unsetFgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setFgColor(color.getCTColor());
        }

        int idx = _stylesSource.putFill(new XSSFCellFill(ct));

        _cellXf.setFillId(idx);
        _cellXf.setApplyFill(true);
    }

    /**
     * Set the foreground fill color as a indexed color value
     * <br/>
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     * @param fg the color to use
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public void setFillForegroundColor(short fg) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(fg);
        setFillForegroundColor(clr);
    }

    /**
     * Get a <b>copy</b> of the currently used CTFill, if none is used, return a new instance.
     */
    private CTFill getCTFill(){
        CTFill ct;
        if(_cellXf.getApplyFill()) {
        	int fillIndex = getFillId();
        	if (fillIndex < 0) fillIndex = 0;
            XSSFCellFill cf = _stylesSource.getFillAt(fillIndex);

            ct = (CTFill)cf.getCTFill().copy();
        } else {
            ct = CTFill.Factory.newInstance();
        }
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTBorder, if none is used, return a new instance.
     */
    private CTBorder getCTBorder(){
        CTBorder ct;
        if(_cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            XSSFCellBorder cf = _stylesSource.getBorderAt(idx);

            ct = (CTBorder)cf.getCTBorder().copy();
        } else {
            ct = CTBorder.Factory.newInstance();
        }
        return ct;
    }

    /**
     * This element is used to specify cell fill information for pattern and solid color cell fills.
     * For solid cell fills (no pattern),  foregorund color is used.
     * For cell fills with patterns specified, then the cell fill color is specified by the background color.
     *
     * @see org.zkoss.poi.ss.usermodel.CellStyle#NO_FILL
     * @see org.zkoss.poi.ss.usermodel.CellStyle#SOLID_FOREGROUND
     * @see org.zkoss.poi.ss.usermodel.CellStyle#FINE_DOTS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#ALT_BARS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#SPARSE_DOTS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_HORZ_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_VERT_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_BACKWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THICK_FORWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BIG_SPOTS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#BRICKS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_HORZ_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_VERT_BANDS
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_BACKWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#THIN_FORWARD_DIAG
     * @see org.zkoss.poi.ss.usermodel.CellStyle#SQUARES
     * @see org.zkoss.poi.ss.usermodel.CellStyle#DIAMONDS
     * @see #setFillBackgroundColor(short)
     * @see #setFillForegroundColor(short)
     * @param fp  fill pattern (set to {@link org.zkoss.poi.ss.usermodel.CellStyle#SOLID_FOREGROUND} to fill w/foreground color)
     */
   public void setFillPattern(short fp) {
        CTFill ct = getCTFill();
        CTPatternFill ptrn = ct.isSetPatternFill() ? ct.getPatternFill() : ct.addNewPatternFill();
        if(fp == NO_FILL && ptrn.isSetPatternType()) ptrn.unsetPatternType();
        else ptrn.setPatternType(STPatternType.Enum.forInt(fp + 1));

        int idx = _stylesSource.putFill(new XSSFCellFill(ct));

        _cellXf.setFillId(idx);
        _cellXf.setApplyFill(true);
    }

    /**
     * This element is used to specify cell fill information for pattern and solid color cell fills. For solid cell fills (no pattern),
     * foreground color is used is used. For cell fills with patterns specified, then the cell fill color is specified by the background color element.
     *
     * @param ptrn the fill pattern to use
     * @see #setFillBackgroundColor(short)
     * @see #setFillForegroundColor(short)
     * @see org.zkoss.poi.ss.usermodel.FillPatternType
     */
    public void setFillPattern(FillPatternType ptrn) {
	    setFillPattern((short)ptrn.ordinal());
    }

    /**
     * Set the font for this style
     *
     * @param font  a font object created or retreived from the XSSFWorkbook object
     * @see org.zkoss.poi.xssf.usermodel.XSSFWorkbook#createFont()
     * @see org.zkoss.poi.xssf.usermodel.XSSFWorkbook#getFontAt(short)
     */
    public void setFont(Font font) {
        if(font != null){
            long index = font.getIndex();
            if (index != 0) {
	            this._cellXf.setFontId(index);
	            this._cellXf.setApplyFont(true);
            }
        } else if (_cellXf.isSetApplyFont()) {
            this._cellXf.unsetApplyFont();
        }
    }

    /**
     * Set the cell's using this style to be hidden
     *
     * @param hidden - whether the cell using this style should be hidden
     */
    public void setHidden(boolean hidden) {
        if (!_cellXf.isSetProtection()) {
             _cellXf.addNewProtection();
         }
        _cellXf.getProtection().setHidden(hidden);
    }

    /**
     * Set the number of spaces to indent the text in the cell
     *
     * @param indent - number of spaces
     */
    public void setIndention(short indent) {
        getCellAlignment().setIndent(indent);
        _cellXf.setApplyAlignment(true);
    }

    /**
     * Set the color to use for the left border as a indexed color value
     *
     * @param color the index of the color definition
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public void setLeftBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setLeftBorderColor(clr);
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setLeftBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetLeft()) return;

        CTBorderPr pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the cell's using this style to be locked
     *
     * @param locked -  whether the cell using this style should be locked
     */
    public void setLocked(boolean locked) {
        if (!_cellXf.isSetProtection()) {
             _cellXf.addNewProtection();
         }
        _cellXf.getProtection().setLocked(locked);
    }

    /**
     * Set the color to use for the right border
     *
     * @param color the index of the color definition
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public void setRightBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setRightBorderColor(clr);
    }

    /**
     * Set the color to use for the right border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setRightBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetRight()) return;

        CTBorderPr pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the degree of rotation for the text in the cell
     * <p>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br/>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br/>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @param rotation - the rotation degrees (between 0 and 180 degrees)
     */
    public void setRotation(short rotation) {
        getCellAlignment().setTextRotation(rotation);
        _cellXf.setApplyAlignment(true);
    }


    /**
     * Set the color to use for the top border
     *
     * @param color the index of the color definition
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public void setTopBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setTopBorderColor(clr);
    }

    /**
     * Set the color to use for the top border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setTopBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetTop()) return;

        CTBorderPr pr = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the type of vertical alignment for the cell
     *
     * @param align - align the type of alignment
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_TOP
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_CENTER
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_BOTTOM
     * @see org.zkoss.poi.ss.usermodel.CellStyle#VERTICAL_JUSTIFY
     * @see org.zkoss.poi.ss.usermodel.VerticalAlignment
     */
    public void setVerticalAlignment(short align) {
        getCellAlignment().setVertical(VerticalAlignment.values()[align]);
        _cellXf.setApplyAlignment(true);
    }

    /**
     * Set the type of vertical alignment for the cell
     *
     * @param align - the type of alignment
     */
    public void setVerticalAlignment(VerticalAlignment align) {
        getCellAlignment().setVertical(align);
        _cellXf.setApplyAlignment(true);
    }

    /**
     * Set whether the text should be wrapped.
     * <p>
     * Setting this flag to <code>true</code> make all content visible
     * whithin a cell by displaying it on multiple lines
     * </p>
     *
     * @param wrapped a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public void setWrapText(boolean wrapped) {
        getCellAlignment().setWrapText(wrapped);
        _cellXf.setApplyAlignment(true);
    }

    /**
     * Gets border color
     *
     * @param side the border side
     * @return the used color
     */
    public XSSFColor getBorderColor(BorderSide side) {
        switch(side){
            case BOTTOM:
                return getBottomBorderXSSFColor();
            case RIGHT:
                return getRightBorderXSSFColor();
            case TOP:
                return getTopBorderXSSFColor();
            case LEFT:
                return getLeftBorderXSSFColor();
            default:
                throw new IllegalArgumentException("Unknown border: " + side);
        }
    }

    /**
     * Set the color to use for the selected border
     *
     * @param side - where to apply the color definition
     * @param color - the color to use
     */
    public void setBorderColor(BorderSide side, XSSFColor color) {
        switch(side){
            case BOTTOM:
                setBottomBorderColor(color);
                break;
            case RIGHT:
                setRightBorderColor(color);
                break;
            case TOP:
                setTopBorderColor(color);
                break;
            case LEFT:
                setLeftBorderColor(color);
                break;
            case DIAGONAL:
            	setDiagonalBorderColor(color);
            	break;
            case HORIZONTAL:
            	setHorizontalBorderColor(color);
                break;
            case VERTICAL:
            	setVerticalBorderColor(color);
            	break;
        }
    }
    private int getFontId() {
        if (_cellXf != null && _cellXf.isSetFontId()) {
            return (int) _cellXf.getFontId();
        }
        return (int) _cellStyleXf.getFontId();
    }

    /**
     * get the cellAlignment object to use for manage alignment
     * @return XSSFCellAlignment - cell alignment
     */
    protected XSSFCellAlignment getCellAlignment() {
        if (this._cellAlignment == null) {
            this._cellAlignment = new XSSFCellAlignment(getCTCellAlignment());
        }
        return this._cellAlignment;
    }

    /**
     * Return the CTCellAlignment instance for alignment
     *
     * @return CTCellAlignment
     */
    private CTCellAlignment getCTCellAlignment() {
    	if (_cellXf != null) {
	        if (_cellXf.getAlignment() == null) {
	            _cellXf.setAlignment(CTCellAlignment.Factory.newInstance());
	        }
    	}
        return _cellXf.getAlignment();
    }

    /**
     * Returns a hash code value for the object. The hash is derived from the underlying CTXf bean.
     *
     * @return the hash code value for this style
     */
    public int hashCode(){
        return _cellXf != null ? _cellXf.toString().hashCode() : _cellStyleXf.toString().hashCode();
    }

    /**
     * Checks is the supplied style is equal to this style
     *
     * @param o the style to check
     * @return true if the supplied style is equal to this style
     */
    public boolean equals(Object o){
        if(o == null || !(o instanceof XSSFCellStyle)) return false;

        XSSFCellStyle cf = (XSSFCellStyle)o;
        return _cellXf != null ? _cellXf.toString().equals(cf.getCoreXf().toString()) :
        	_cellStyleXf.toString().equals(cf.getStyleXf().toString());
    }

    /**
     * Make a copy of this style. The underlying CTXf bean is cloned,
     * the references to fills and borders remain.
     *
     * @return a copy of this style
     */
    public Object clone(){
        CTXf xf = (CTXf)_cellXf.copy();

        int xfSize = _stylesSource._getStyleXfsSize();
        int indexXf = _stylesSource.putCellXf(xf);
        return new XSSFCellStyle(indexXf-1, xfSize-1, _stylesSource, _theme);
    }

    /**
     * Extracts RGB form theme color.
     * @param originalColor Color that refers to theme.
     */
    private void extractColorFromTheme(XSSFColor originalColor){
        XSSFColor themeColor = _theme.getThemeColor(originalColor.getTheme());
        if (themeColor != null) { //20100927, henrichen@zkoss.org: temeColor can be null 
        	originalColor.setRgb(themeColor.getRgb());
        }
    }
    
    //20100921, henrichen@zkoss.org: add fetching border color object
    public XSSFColor getLeftBorderColorColor() {
        return getLeftBorderXSSFColor();
    }

    //20100921, henrichen@zkoss.org: add fetching border color object
    public XSSFColor getRightBorderColorColor() {
        return getRightBorderXSSFColor();
    }
    
    //20100921, henrichen@zkoss.org: add fetching border color object
    public XSSFColor getTopBorderColorColor() {
        return getTopBorderXSSFColor();
    }
    //20100921, henrichen@zkoss.org: add fetching border color object
    public XSSFColor getBottomBorderColorColor() {
        return getBottomBorderXSSFColor();
    }
    //20110119, henrichen@zkoss.org: handle font color
    public void setFontColorColor(Color color) {
    	setFontColorColor((XSSFColor)color);
    }
    //20110119, henrichen@zkoss.org: handle font color
    private void setFontColorColor(XSSFColor color) {
    	getFont().setColor(color);
    }
    //ZSS-787
    //20141007, henrichen@zkoss.org: give way to set default cell style 
    @Internal
    public CTXf getCellXf() {
        return _cellXf;
    }

    //ZSS-787
    @Internal
    @Override
    public void setBorder(short left, Color leftColor, short top, Color topColor, 
        	short right, Color rightColor, short bottom, Color bottomColor) {
        XSSFCellBorder border = new XSSFCellBorder(getCTBorder()); 
        border.prepareBorder(left, leftColor, top, topColor, 
           	right, rightColor, bottom, bottomColor);
        
        List<XSSFCellBorder> borders = _stylesSource.getBorders();
        int bj = borders.indexOf(border);
        if (bj < 0) {
        	bj = borders.size();
        	borders.add(border);
        } else {
        	border = borders.get(bj);
        }
        
        _cellXf.setBorderId(bj);
        if (bj != 0) {
        	_cellXf.setApplyBorder(true);
        }
    }
        
    //ZSS-787
    public void setFill(Color fillColor, Color backColor, short patternType) {
        CTFill ct = getCTFill();
        XSSFCellFill fill = new XSSFCellFill(ct);
        fill.prepareFill(fillColor, backColor, patternType);
        
        List<XSSFCellFill> fills = _stylesSource.getFills();
        int fj = fills.indexOf(fill);
        if (fj < 0) {
        	fj = fills.size();
    		fills.add(fill);
        } else {
        	fill = fills.get(fj);
        }
        
        _cellXf.setFillId(fj);
        if (fj != 0) {
        	_cellXf.setApplyFill(true);
        }
    }

    //ZSS-787
    @Deprecated
    public void setCellAlignment(short hAlign, short vAlign, boolean wrapText) {
    	setCellAlignment(hAlign, vAlign, wrapText, (short)0);
    }
    
    //ZSS-1020
    public void setCellAlignment(short hAlign, short vAlign, boolean wrapText, short rotation) {
    	short defaultHAlign = CellStyle.ALIGN_GENERAL;
    	short defaultVAlign = CellStyle.VERTICAL_BOTTOM;
    	boolean defaultWrapText = false;
    	short defaultTextRotation = 0;
    	
    	if (_cellStyleXf != null) {
    		if (_cellStyleXf.isSetAlignment()) {
    			CTCellAlignment ctalign = _cellStyleXf.getAlignment();
    			if (ctalign != null) {
	    			if (ctalign.isSetHorizontal() && ctalign.xgetHorizontal().validate()) {
	    				defaultHAlign = (short) (ctalign.getHorizontal().intValue() - 1);
	    			}
	    			if (ctalign.isSetVertical() && ctalign.xgetVertical().validate()) {
	    				defaultVAlign = (short) (ctalign.getVertical().intValue() - 1);
	    			}
	    			if (ctalign.isSetWrapText()) {
	    				defaultWrapText = ctalign.getWrapText();
	    			}
	    			if (ctalign.isSetTextRotation() && ctalign.xgetTextRotation().validate()) { //ZSS-1020
	    				defaultTextRotation = (short) ctalign.getTextRotation();
    					if (defaultTextRotation < 0 && defaultTextRotation >= -90) {
    						defaultTextRotation = (short) (90 - defaultTextRotation);
    					}
	    			}
    			}
    		}
    	}
    	
		if (defaultHAlign != hAlign) {
			setAlignment(hAlign);
		}
		if (defaultVAlign != vAlign) {
			setVerticalAlignment(vAlign);
		}
		if (defaultWrapText != wrapText && wrapText) {
			setWrapText(wrapText);
		}
		//ZSS-1020
		if (defaultTextRotation != rotation) {
			setRotation(rotation);
		}
    }
    
    //ZSS-787
    public void setProtection(boolean locked, boolean hidden) {
    	boolean defaultLocked = true;
    	boolean defaultHidden = false;
    	if (_cellStyleXf != null) {
    		if (_cellStyleXf.isSetProtection()) {
    			CTCellProtection ctprot = _cellStyleXf.getProtection();
    			if (ctprot.isSetHidden()) {
    				defaultHidden = ctprot.getHidden();
    			}
    			if (ctprot.isSetLocked()) {
    				defaultLocked = ctprot.getLocked();
    			}
    		}
    	}
		if (locked != defaultLocked && !locked) {
			setLocked(locked);
		}
		if (hidden != defaultHidden && !hidden) {
			setHidden(hidden);
		}
		if (_cellXf.isSetProtection()) {
			_cellXf.setApplyProtection(true);
		}
    }

	@Override
	public void setBorderDiagonal(short border) {
        CTBorder ct = getCTBorder();
        CTBorderPr pr = ct.isSetDiagonal() ? ct.getDiagonal() : ct.addNewDiagonal();
        if(border == BORDER_NONE) ct.unsetDiagonal();
        else pr.setStyle(STBorderStyle.Enum.forInt(border + 1));

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
	}

	@Override
	public short getBorderDiagonal() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
            int idx = (int)_cellXf.getBorderId();
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }

        int idx = _cellStyleXf == null ? -1 : (int)_cellStyleXf.getBorderId();
        if (idx >= 0) {
            CTBorder ct = _stylesSource.getBorderAt(idx).getCTBorder();
            STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public Color getDiagonalBorderColorColor() {
        return getDiagonalBorderXSSFColor();
	}

	@Override
	public void setDiagonalBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setDiagonalBorderColor(clr);
	}

	@Override
	public short getDiagonalBorderColor() {
        XSSFColor clr = getDiagonalBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getDiagonalBorderXSSFColor() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
	        int idx = (int)_cellXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.DIAGONAL);
        }
        if (_cellStyleXf != null) {
	        int idx = (int)_cellStyleXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	        return border.getBorderColor(BorderSide.DIAGONAL);
        }
        return null;
    }

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getVerticalBorderXSSFColor() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
	        int idx = (int)_cellXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.VERTICAL);
        }
        if (_cellStyleXf != null) {
	        int idx = (int)_cellStyleXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	        return border.getBorderColor(BorderSide.VERTICAL);
        }
        return null;
    }

    /**
     * Get the color to use for the horizontal border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getHorizontalBorderXSSFColor() {
        if(_cellXf != null && _cellXf.getApplyBorder()) {
	        int idx = (int)_cellXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	
	        return border.getBorderColor(BorderSide.HORIZONTAL);
        }
        if (_cellStyleXf != null) {
	        int idx = (int)_cellStyleXf.getBorderId();
	        XSSFCellBorder border = _stylesSource.getBorderAt(idx);
	        return border.getBorderColor(BorderSide.HORIZONTAL);
        }
        return null;
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setDiagonalBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetDiagonal()) return;

        CTBorderPr pr = ct.isSetDiagonal() ? ct.getDiagonal() : ct.addNewDiagonal();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }
    
    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setHorizontalBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetHorizontal()) return;

        CTBorderPr pr = ct.isSetHorizontal() ? ct.getHorizontal() : ct.addNewHorizontal();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setVerticalBorderColor(XSSFColor color) {
        CTBorder ct = getCTBorder();
        if(color == null && !ct.isSetVertical()) return;

        CTBorderPr pr = ct.isSetVertical() ? ct.getVertical() : ct.addNewVertical();
        if(color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct, _theme));

        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

	@Override
	public boolean isShowDiagonalUpBorder() {
		CTBorder ct = getCTBorder();
		return ct.getDiagonalUp();
	}

	@Override
	public boolean isShowDiagonalDownBorder() {
		CTBorder ct = getCTBorder();
		return ct.getDiagonalDown();
	}

	@Override
	public void setShowDiagonalUpBorder(boolean up) {
		CTBorder ct = getCTBorder();
		ct.setDiagonalUp(up);
	}

	@Override
	public void setShowDiagonalDownBorder(boolean down) {
		CTBorder ct = getCTBorder();
		ct.setDiagonalDown(down);
	}

    @Internal
    @Override
    public void setBorder(short left, Color leftColor, short top, Color topColor, 
        	short right, Color rightColor, short bottom, Color bottomColor, 
        	short diagonal, Color diagonalColor, 
        	short horizontal, Color horizontalColor, 
        	short vertical, Color verticalColor,
        	boolean diaUp, boolean diaDown) {
    	
        CTBorder ct = getCTBorder();
        XSSFCellBorder border = new XSSFCellBorder(ct);
        border.setDiagonalUp(diaUp);
        border.setDiagonalDown(diaDown);

        // always generate <left/> ... 
      	border.setBorderStyle(BorderSide.LEFT, left);
    	border.setBorderStyle(BorderSide.TOP, top);
    	border.setBorderStyle(BorderSide.RIGHT, right);
    	border.setBorderStyle(BorderSide.BOTTOM, bottom);
    	border.setBorderStyle(BorderSide.DIAGONAL, diagonal);
    	border.setBorderStyle(BorderSide.HORIZONTAL, horizontal);
    	border.setBorderStyle(BorderSide.VERTICAL, vertical);
        if (left != BORDER_NONE) {
           	border.setBorderColor(BorderSide.LEFT, (XSSFColor)leftColor);
        }
        if (top != BORDER_NONE) {
           	border.setBorderColor(BorderSide.TOP, (XSSFColor)topColor);
        }
        if (right != BORDER_NONE) {
           	border.setBorderColor(BorderSide.RIGHT, (XSSFColor)rightColor);
        }
        if (bottom != BORDER_NONE) {
           	border.setBorderColor(BorderSide.BOTTOM, (XSSFColor)bottomColor);
        }
        if (diagonal != BORDER_NONE) {
           	border.setBorderColor(BorderSide.DIAGONAL, (XSSFColor)diagonalColor);
        }
        if (horizontal != BORDER_NONE) {
           	border.setBorderColor(BorderSide.HORIZONTAL, (XSSFColor)horizontalColor);
        }
        if (vertical != BORDER_NONE) {
           	border.setBorderColor(BorderSide.VERTICAL, (XSSFColor)verticalColor);
        }
        //TODO: border setTheme() ?
        
        List<XSSFCellBorder> borders = _stylesSource.getBorders();
        int bj = borders.indexOf(border);
        if (bj < 0) {
        	bj = borders.size();
        	borders.add(border);
        } else {
        	border = borders.get(bj);
        }
        
        _cellXf.setBorderId(bj);
        if (bj != 0) {
        	_cellXf.setApplyBorder(true);
        }
    }

    //ZSS-854
    public void setDefaultCellAlignment(short hAlign, short vAlign, boolean wrapText) {
    	short defaultHAlign = CellStyle.ALIGN_GENERAL;
    	short defaultVAlign = CellStyle.VERTICAL_BOTTOM;
    	boolean defaultWrapText = false;
    	
    	if (_cellStyleXf != null) {
    		if (_cellStyleXf.isSetAlignment()) {
    			CTCellAlignment ctalign = _cellStyleXf.getAlignment();
    			if (ctalign.isSetHorizontal()) {
    				defaultHAlign = (short) (ctalign.getHorizontal().intValue() - 1);
    			}
    			if (ctalign.isSetVertical()) {
    				defaultVAlign = (short) (ctalign.getVertical().intValue() - 1);
    			}
    			if (ctalign.isSetWrapText()) {
    				defaultWrapText = ctalign.getWrapText();
    			}
    		}
    	}
    	
		if (defaultHAlign != hAlign && hAlign != CellStyle.ALIGN_GENERAL) {
	        if (!_cellStyleXf.isSetAlignment()) {
	            _cellStyleXf.setAlignment(CTCellAlignment.Factory.newInstance());
	        }
	        _cellStyleXf.getAlignment().setHorizontal(STHorizontalAlignment.Enum.forInt(hAlign + 1));
		}
		if (defaultVAlign != vAlign && vAlign != CellStyle.VERTICAL_BOTTOM) {
	        if (!_cellStyleXf.isSetAlignment()) {
	            _cellStyleXf.setAlignment(CTCellAlignment.Factory.newInstance());
	        }
			_cellStyleXf.getAlignment().setVertical(STVerticalAlignment.Enum.forInt(vAlign + 1));
		}
		if (defaultWrapText != wrapText && wrapText) {
	        if (!_cellStyleXf.isSetAlignment()) {
	            _cellStyleXf.setAlignment(CTCellAlignment.Factory.newInstance());
	        }
			_cellStyleXf.getAlignment().setWrapText(wrapText);
		}
    }
    
    //ZSS-854
    public void setDefaultProtection(boolean locked, boolean hidden) {
    	boolean defaultLocked = true;
    	boolean defaultHidden = false;
		if (locked != defaultLocked && !locked) {
	        if (!_cellStyleXf.isSetProtection()) {
	             _cellStyleXf.addNewProtection();
	         }
	        _cellStyleXf.getProtection().setLocked(locked);
		}
		if (hidden != defaultHidden && !hidden) {
	        if (!_cellStyleXf.isSetProtection()) {
	             _cellStyleXf.addNewProtection();
	        }
	        _cellStyleXf.getProtection().setHidden(hidden);
		}
    }
}
