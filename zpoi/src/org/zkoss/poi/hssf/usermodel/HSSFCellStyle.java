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


package org.zkoss.poi.hssf.usermodel;

import org.zkoss.poi.hssf.model.InternalWorkbook;
import org.zkoss.poi.hssf.record.ExtendedFormatRecord;
import org.zkoss.poi.hssf.record.FontRecord;
import org.zkoss.poi.hssf.record.FullColorExt;
import org.zkoss.poi.hssf.record.StyleRecord;
import org.zkoss.poi.hssf.record.XFExtRecord;
import org.zkoss.poi.hssf.util.HSSFColor;
import org.zkoss.poi.hssf.util.HSSFColorExt;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Color;
import org.zkoss.poi.ss.usermodel.Font;

/**
 * High level representation of the style of a cell in a sheet of a workbook.
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#createCellStyle()
 * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#getCellStyleAt(short)
 * @see org.zkoss.poi.hssf.usermodel.HSSFCell#setCellStyle(HSSFCellStyle)
 */
public final class HSSFCellStyle implements CellStyle {
    private ExtendedFormatRecord _format                     = null;
    private short                _index                      = 0;
    private InternalWorkbook             _workbook                   = null;


    /** Creates new HSSFCellStyle why would you want to do this?? */
    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, HSSFWorkbook workbook)
    {
    	this(index, rec, workbook.getWorkbook());
    }
    protected HSSFCellStyle(short index, ExtendedFormatRecord rec, InternalWorkbook workbook)
    {
        _workbook = workbook;
        _index = index;
        _format     = rec;
    }

    /**
     * get the index within the HSSFWorkbook (sequence within the collection of ExtnededFormat objects)
     * @return unique index number of the underlying record this style represents (probably you don't care
     *  unless you're comparing which one is which)
     */
    public short getIndex()
    {
        return _index;
    }

    /**
     * Return the parent style for this cell style.
     * In most cases this will be null, but in a few
     *  cases there'll be a fully defined parent.
     */
    public HSSFCellStyle getParentStyle() {
        short parentIndex = _format.getParentIndex();
        // parentIndex equal 0xFFF indicates no inheritance from a cell style XF (See 2.4.353 XF)
    	if(parentIndex == 0 || parentIndex == 0xFFF) {
    		return null;
    	}
    	return new HSSFCellStyle(
    			parentIndex,
    			_workbook.getExFormatAt(parentIndex),
    			_workbook
    	);
    }

    /**
     * set the data format (must be a valid format)
     * @see org.zkoss.poi.hssf.usermodel.HSSFDataFormat
     */
    public void setDataFormat(short fmt)
    {
        _format.setFormatIndex(fmt);
    }

    /**
     * get the index of the format
     * @see org.zkoss.poi.hssf.usermodel.HSSFDataFormat
     */

    public short getDataFormat()
    {
        return _format.getFormatIndex();
    }

    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the bound workbook
     * @see org.zkoss.poi.hssf.usermodel.HSSFDataFormat
     * @return the format string or "General" if not found
     */
    public String getDataFormatString() {
        return getDataFormatString(_workbook);
    }
    
    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the bound workbook
     * @see org.zkoss.poi.hssf.usermodel.HSSFDataFormat
     * @return the format string or "General" if not found
     */
    //20140213 dennischen@zkoss.org
    public String getRawDataFormatString() {
    	HSSFDataFormat format = new HSSFDataFormat( _workbook );
        return format.getRawFormat(getDataFormat());
    }
    //20140213, dennischen@zkoss.org the way to know a format is a builtin format
    public boolean isBuiltinDataFormat(){
    	HSSFDataFormat format = new HSSFDataFormat( _workbook );
        return format.isBuiltinFormat(getDataFormat());
    }
    
    
    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the supplied workbook
     * @see org.zkoss.poi.hssf.usermodel.HSSFDataFormat
     *
     * @return the format string or "General" if not found
     */
    public String getDataFormatString(org.zkoss.poi.ss.usermodel.Workbook workbook) {
    	HSSFDataFormat format = new HSSFDataFormat( ((HSSFWorkbook)workbook).getWorkbook() );

        int idx = getDataFormat();
        return idx == -1 ? "General" : format.getFormat(getDataFormat());
    }
    /**
     * Get the contents of the format string, by looking up
     *  the DataFormat against the supplied low level workbook
     * @see org.zkoss.poi.hssf.usermodel.HSSFDataFormat
     */
    public String getDataFormatString(org.zkoss.poi.hssf.model.InternalWorkbook workbook) {
    	HSSFDataFormat format = new HSSFDataFormat( workbook );

        return format.getFormat(getDataFormat());
    }

    /**
     * set the font for this style
     * @param font  a font object created or retreived from the HSSFWorkbook object
     * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#createFont()
     * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public void setFont(Font font) {
		setFont((HSSFFont)font);
	}
	public void setFont(HSSFFont font) {
        _format.setIndentNotParentFont(true);
        short fontindex = font.getIndex();
        _format.setFontIndex(fontindex);
    }

    /**
     * gets the index of the font for this style
     * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public short getFontIndex()
    {
        return _format.getFontIndex();
    }

    /**
     * gets the font for this style
     * @param parentWorkbook The HSSFWorkbook that this style belongs to
     * @see org.zkoss.poi.hssf.usermodel.HSSFCellStyle#getFontIndex()
     * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#getFontAt(short)
     */
    public HSSFFont getFont(org.zkoss.poi.ss.usermodel.Workbook parentWorkbook) {
    	return ((HSSFWorkbook) parentWorkbook).getFontAt(getFontIndex());
    }

    /**
     * set the cell's using this style to be hidden
     * @param hidden - whether the cell using this style should be hidden
     */
    public void setHidden(boolean hidden)
    {
        _format.setIndentNotParentCellOptions(true);
        _format.setHidden(hidden);
    }

    /**
     * get whether the cell's using this style are to be hidden
     * @return hidden - whether the cell using this style should be hidden
     */
    public boolean getHidden()
    {
        return _format.isHidden();
    }

    /**
     * set the cell's using this style to be locked
     * @param locked - whether the cell using this style should be locked
     */
    public void setLocked(boolean locked)
    {
        _format.setIndentNotParentCellOptions(true);
        _format.setLocked(locked);
    }

    /**
     * get whether the cell's using this style are to be locked
     * @return hidden - whether the cell using this style should be locked
     */
    public boolean getLocked()
    {
        return _format.isLocked();
    }

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
    public void setAlignment(short align)
    {
        _format.setIndentNotParentAlignment(true);
        _format.setAlignment(align);
    }

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
    public short getAlignment()
    {
        return _format.getAlignment();
    }

    /**
     * set whether the text should be wrapped
     * @param wrapped  wrap text or not
     */
    public void setWrapText(boolean wrapped)
    {
        _format.setIndentNotParentAlignment(true);
        _format.setWrapText(wrapped);
    }

    /**
     * get whether the text should be wrapped
     * @return wrap text or not
     */
    public boolean getWrapText()
    {
        return _format.getWrapText();
    }

    /**
     * set the type of vertical alignment for the cell
     * @param align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */
    public void setVerticalAlignment(short align)
    {
        _format.setVerticalAlignment(align);
    }

    /**
     * get the type of vertical alignment for the cell
     * @return align the type of alignment
     * @see #VERTICAL_TOP
     * @see #VERTICAL_CENTER
     * @see #VERTICAL_BOTTOM
     * @see #VERTICAL_JUSTIFY
     */
    public short getVerticalAlignment()
    {
        return _format.getVerticalAlignment();
    }

    /**
     * set the degree of rotation for the text in the cell
     * @param rotation degrees (between -90 and 90 degrees, of 0xff for vertical)
     */
    public void setRotation(short rotation)
    {
      if (rotation == 0xff) {
          // Special cases for vertically aligned text
      } 
      else if ((rotation < 0)&&(rotation >= -90)) {
        //Take care of the funny 4th quadrant issue
        //The 4th quadrant (-1 to -90) is stored as (91 to 180)
        rotation = (short)(90 - rotation);
      }
      else if ((rotation < -90)  ||(rotation > 90)) {
        //Do not allow an incorrect rotation to be set
        throw new IllegalArgumentException("The rotation must be between -90 and 90 degrees, or 0xff");
      }
      _format.setRotation(rotation);
    }

    /**
     * get the degree of rotation for the text in the cell
     * @return rotation degrees (between -90 and 90 degrees, or 0xff for vertical)
     */
    public short getRotation()
    {
      short rotation = _format.getRotation();
      if (rotation == 0xff) {
         // Vertical aligned special case
         return rotation;
      }
      if (rotation > 90) {
        //This is actually the 4th quadrant
        rotation = (short)(90-rotation);
      }
      return rotation;
    }

    /**
     * set the number of spaces to indent the text in the cell
     * @param indent - number of spaces
     */
    public void setIndention(short indent)
    {
        _format.setIndent(indent);
    }

    /**
     * get the number of spaces to indent the text in the cell
     * @return indent - number of spaces
     */
    public short getIndention()
    {
        return _format.getIndent();
    }

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
    public void setBorderLeft(short border)
    {
        _format.setIndentNotParentBorder(true);
        _format.setBorderLeft(border);
    }

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
    public short getBorderLeft()
    {
        return _format.getBorderLeft();
    }

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
    public void setBorderRight(short border)
    {
        _format.setIndentNotParentBorder(true);
        _format.setBorderRight(border);
    }

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
    public short getBorderRight()
    {
        return _format.getBorderRight();
    }

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
    public void setBorderTop(short border)
    {
        _format.setIndentNotParentBorder(true);
        _format.setBorderTop(border);
    }

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
    public short getBorderTop()
    {
        return _format.getBorderTop();
    }

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
    public void setBorderBottom(short border)
    {
        _format.setIndentNotParentBorder(true);
        _format.setBorderBottom(border);
    }

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
    public short getBorderBottom()
    {
        return _format.getBorderBottom();
    }

    /**
     * set the color to use for the left border
     * @param color The index of the color definition
     */
    public void setLeftBorderColor(short color)
    {
        _format.setLeftBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @see org.zkoss.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public short getLeftBorderColor()
    {
        return _format.getLeftBorderPaletteIdx();
    }

    /**
     * set the color to use for the right border
     * @param color The index of the color definition
     */
    public void setRightBorderColor(short color)
    {
        _format.setRightBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @see org.zkoss.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public short getRightBorderColor()
    {
        return _format.getRightBorderPaletteIdx();
    }

    /**
     * set the color to use for the top border
     * @param color The index of the color definition
     */
    public void setTopBorderColor(short color)
    {
        _format.setTopBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the top border
     * @see org.zkoss.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public short getTopBorderColor()
    {
        return _format.getTopBorderPaletteIdx();
    }

    /**
     * set the color to use for the bottom border
     * @param color The index of the color definition
     */
    public void setBottomBorderColor(short color)
    {
        _format.setBottomBorderPaletteIdx(color);
    }

    /**
     * get the color to use for the left border
     * @see org.zkoss.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public short getBottomBorderColor()
    {
        return _format.getBottomBorderPaletteIdx();
    }

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
    public void setFillPattern(short fp)
    {
        _format.setAdtlFillPattern(fp);
    }

    /**
     * get the fill pattern (??) - set to 1 to fill with foreground color
     * @return fill pattern
     */
    public short getFillPattern()
    {
        return _format.getAdtlFillPattern();
    }

    /**
     * Checks if the background and foreground fills are set correctly when one
     * or the other is set to the default color.
     * <p>Works like the logic table below:</p>
     * <p>BACKGROUND   FOREGROUND</p>
     * <p>NONE         AUTOMATIC</p>
     * <p>0x41         0x40</p>
     * <p>NONE         RED/ANYTHING</p>
     * <p>0x40         0xSOMETHING</p>
     */
    private void checkDefaultBackgroundFills() {
      if (_format.getFillForeground() == org.zkoss.poi.hssf.util.HSSFColor.AUTOMATIC.index) {
    	  //JMH: Why +1, hell why not. I guess it made some sense to someone at the time. Doesnt
    	  //to me now.... But experience has shown that when the fore is set to AUTOMATIC then the
    	  //background needs to be incremented......
    	  if (_format.getFillBackground() != (org.zkoss.poi.hssf.util.HSSFColor.AUTOMATIC.index+1))
    		  setFillBackgroundColor((short)(org.zkoss.poi.hssf.util.HSSFColor.AUTOMATIC.index+1));
      } else if (_format.getFillBackground() == org.zkoss.poi.hssf.util.HSSFColor.AUTOMATIC.index+1)
    	  //Now if the forground changes to a non-AUTOMATIC color the background resets itself!!!
    	  if (_format.getFillForeground() != org.zkoss.poi.hssf.util.HSSFColor.AUTOMATIC.index)
    		  setFillBackgroundColor(org.zkoss.poi.hssf.util.HSSFColor.AUTOMATIC.index);
    }

    /**
     * set the background fill color.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundColor(new HSSFColor.RED().getIndex());
     * </pre>
     * optionally a Foreground and background fill can be applied:
     * <i>Note: Ensure Foreground color is set prior to background</i>
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(new HSSFColor.BLUE().getIndex());
     * cs.setFillBackgroundColor(new HSSFColor.RED().getIndex());
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(new HSSFColor.RED().getIndex());
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param bg  color
     */
    public void setFillBackgroundColor(short bg)
    {
        _format.setFillBackground(bg);
        checkDefaultBackgroundFills();
    }

    /**
     * Get the background fill color.
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * @see org.zkoss.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return fill color
     */
    public short getFillBackgroundColor()
    {
    	short result = _format.getFillBackground();
    	//JMH: Do this ridiculous conversion, and let HSSFCellStyle
    	//internally migrate back and forth
    	if (result == (HSSFColor.AUTOMATIC.index+1)) {
			return HSSFColor.AUTOMATIC.index;
		}
    	return result;
    }
    
    public HSSFColor getFillBackgroundColorColor() {
    	final XFExtRecord xfExt = getXFExt(); //20110119, henrichen@zkoss.org: handle XFExt record
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getFillBackgroundColor();
    	if (colorExt == null || colorExt.isIndex()) {
    		HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
    		return pallette.getColor(getFillBackgroundColor());
    	} else {
    		return new HSSFColorExt(colorExt);
    	}
    }

    /**
     * set the foreground fill color
     * <i>Note: Ensure Foreground color is set prior to background color.</i>
     * @param bg  color
     */
    public void setFillForegroundColor(short bg)
    {
        _format.setFillForeground(bg);
        checkDefaultBackgroundFills();
    }

    /**
     * Get the foreground fill color.
     * Many cells are filled with this, instead of a
     *  background color ({@link #getFillBackgroundColor()})
     * @see org.zkoss.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return fill color
     */
    public short getFillForegroundColor()
    {
        return _format.getFillForeground();
    }

    public HSSFColor getFillForegroundColorColor() {
    	final XFExtRecord xfExt = getXFExt(); //20110119, henrichen@zkoss.org: handle XFExt record
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getFillForegroundColor();
    	if (colorExt == null || colorExt.isIndex()) {
		    HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
		    return pallette.getColor(getFillForegroundColor());
    	} else {
    		return new HSSFColorExt(colorExt);
    	}
    }

    /**
     * Gets the name of the user defined style.
     * Returns null for built in styles, and
     *  styles where no name has been defined
     */
    public String getUserStyleName() {
    	StyleRecord sr = _workbook.getStyleRecord(_index);
    	if(sr == null) {
    		return null;
    	}
    	if(sr.isBuiltin()) {
    		return null;
    	}
    	return sr.getName();
    }

    /**
     * Sets the name of the user defined style.
     * Will complain if you try this on a built in style.
     */
    public void setUserStyleName(String styleName) {
    	StyleRecord sr = _workbook.getStyleRecord(_index);
    	if(sr == null) {
    		sr = _workbook.createStyleRecord(_index);
    	}
    	// All Style records start as "builtin", but generally
    	//  only 20 and below really need to be
    	if(sr.isBuiltin() && _index <= 20) {
    		throw new IllegalArgumentException("Unable to set user specified style names for built in styles!");
    	}
    	sr.setName(styleName);
    }

    /**
     * Verifies that this style belongs to the supplied Workbook.
     * Will throw an exception if it belongs to a different one.
     * This is normally called when trying to assign a style to a
     *  cell, to ensure the cell and the style are from the same
     *  workbook (if they're not, it won't work)
     * @throws IllegalArgumentException if there's a workbook mis-match
     */
    public void verifyBelongsToWorkbook(HSSFWorkbook wb) {
		if(wb.getWorkbook() != _workbook) {
			throw new IllegalArgumentException("This Style does not belong to the supplied Workbook. Are you trying to assign a style from one workbook to the cell of a differnt workbook?");
		}
	}

    /**
     * Clones all the style information from another
     *  HSSFCellStyle, onto this one. This
     *  HSSFCellStyle will then have all the same
     *  properties as the source, but the two may
     *  be edited independently.
     * Any stylings on this HSSFCellStyle will be lost!
     *
     * The source HSSFCellStyle could be from another
     *  HSSFWorkbook if you like. This allows you to
     *  copy styles from one HSSFWorkbook to another.
     */
    public void cloneStyleFrom(CellStyle source) {
		if(source instanceof HSSFCellStyle) {
			this.cloneStyleFrom((HSSFCellStyle)source);
		} else {
		    throw new IllegalArgumentException("Can only clone from one HSSFCellStyle to another, not between HSSFCellStyle and XSSFCellStyle");
		}
	}
    public void cloneStyleFrom(HSSFCellStyle source) {
    	// First we need to clone the extended format
    	//  record
    	_format.cloneStyleFrom(source._format);
    	//20110119, henrichen@zkoss.org: then clone XFExt record
    	//ZSS-33: Nullpointer when change the color of cell
    	//20110819, henrichen@zkoss.org: sometimes, no such XFExt record at all  
    	final XFExtRecord extRecord = source._workbook.getXFExtAt(source._index);
    	if (extRecord != null) {
    		XFExtRecord target = _workbook.getXFExtAt(_index);
    		if (target == null) {
    			target = _workbook.createCellXFExt(_index);
    		}
    		target.cloneXFExtFrom(extRecord);
    	}
    	
    	// Handle matching things if we cross workbooks
    	if(_workbook != source._workbook) {
			// Then we need to clone the format string,
			//  and update the format record for this
    		short fmt = (short)_workbook.createFormat(source.getDataFormatString() );
    		setDataFormat(fmt);

			// Finally we need to clone the font,
			//  and update the format record for this
    		FontRecord fr = _workbook.createNewFont();
    		fr.cloneStyleFrom(
    				source._workbook.getFontRecordAt(
    						source.getFontIndex()
    				)
    		);

    		HSSFFont font = new HSSFFont(
    				(short)_workbook.getFontIndex(fr), fr
    		);
    		setFont(font);
    	}
    }


	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_format == null) ? 0 : _format.hashCode());
		result = prime * result + _index;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj instanceof HSSFCellStyle) {
			final HSSFCellStyle other = (HSSFCellStyle) obj;
			if (_format == null) {
				if (other._format != null)
					return false;
			} else if (!_format.equals(other._format))
				return false;
			if (_index != other._index)
				return false;
			return true;
		}
		return false;
	}
	
	//20100921, henrichen@zkoss.org: fetch border color object
    public HSSFColor getLeftBorderColorColor()
    {
    	final XFExtRecord xfExt = getXFExt(); //20110119, henrichen@zkoss.org: handle XFExt record
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getLeftBorderColor();
    	if (colorExt == null || colorExt.isIndex()) {
	        HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
	        return pallette.getColor(getLeftBorderColor());
    	} else {
    		return new HSSFColorExt(colorExt);
    	}
    }

	//20100921, henrichen@zkoss.org: fetch border color object
    public HSSFColor getRightBorderColorColor()
    {
    	final XFExtRecord xfExt = getXFExt(); //20110119, henrichen@zkoss.org: handle XFExt record
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getRightBorderColor();
    	if (colorExt == null || colorExt.isIndex()) {
	        HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
	        return pallette.getColor(getRightBorderColor());
    	} else {
    		return new HSSFColorExt(colorExt);
    	}
    }
	//20100921, henrichen@zkoss.org: fetch border color object
    public HSSFColor getTopBorderColorColor()
    {
    	final XFExtRecord xfExt = getXFExt(); //20110119, henrichen@zkoss.org: handle XFExt record
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getTopBorderColor();
    	if (colorExt == null || colorExt.isIndex()) {
	        HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
	        return pallette.getColor(getTopBorderColor());
		} else {
			return new HSSFColorExt(colorExt);
		}
    }
	//20100921, henrichen@zkoss.org: fetch border color object
    public HSSFColor getBottomBorderColorColor()
    {
    	final XFExtRecord xfExt = getXFExt(); //20110119, henrichen@zkoss.org: handle XFExt record
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getBottomBorderColor();
    	if (colorExt == null || colorExt.isIndex()) {
	        HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
	        return pallette.getColor(getBottomBorderColor());
    	} else {
    		return new HSSFColorExt(colorExt);
    	}
    }
    
    //20100923, henrichen@zkoss.org: handle Color
    public void setFillForegroundColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	
    	if (color instanceof HSSFColorExt) { //20110119, henrichen@zkoss.org: handle XFExt record
    		getOrCreateXFExt().setFillForegroundColor(((HSSFColorExt) color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
        setFillForegroundColor(color.getIndex());
    }
    //20100923, henrichen@zkoss.org: handle Color
    public void setFillBackgroundColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	if (color instanceof HSSFColorExt) { //20110119, henrichen@zkoss.org: handle XFExt record
    		getOrCreateXFExt().setFillBackgroundColor(((HSSFColorExt) color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
		setFillBackgroundColor(color.getIndex());
    }

    //20110118, henrichen@zkoss.org: handle XFExtRecord text color
    public HSSFColor getFontColorColor() {
    	final XFExtRecord xfExt = getXFExt();
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getTextColor();
    	if (colorExt == null || colorExt.isIndex()) {
    	    final FontRecord frec = _workbook.getFontRecordAt(getFontIndex());
    	    final short fontColor = frec.getColorPaletteIndex();
	        HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
	        return pallette.getColor(fontColor);
		} else {
			return new HSSFColorExt(colorExt);
    	}
    }
    
    //20110119, henrichen@zkoss.org: handle seting XFExtRecord text color
    public void setFontColorColor(Color color) {
    	setFontColorColor((HSSFColor)color);
    }
    //20110119, henrichen@zkoss.org: handle seting XFExtRecord text color
    private void setFontColorColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	
    	if (color instanceof HSSFColorExt) {
    		getOrCreateXFExt().setTextColor(((HSSFColorExt)color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
    	final FontRecord frec = _workbook.getFontRecordAt(getFontIndex());
    	frec.setColorPaletteIndex(color.getIndex());
    }

	//20131011, kuroridoplayer@gmail.com: choose similar color in palette.
    public void setTopBorderColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	if (color instanceof HSSFColorExt) {
    		getOrCreateXFExt().setTopBorderColor(((HSSFColorExt) color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
        setTopBorderColor(color.getIndex());  	
    }
    
    //20131011, kuroridoplayer@gmail.com: choose similar color in palette.
    public void setBottomBorderColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	if (color instanceof HSSFColorExt) {
    		getOrCreateXFExt().setBottomBorderColor(((HSSFColorExt) color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
        setBottomBorderColor(color.getIndex());  	
    }
    
    //20131011, kuroridoplayer@gmail.com: choose similar color in palette.
    public void setRightBorderColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	if (color instanceof HSSFColorExt) {
    		getOrCreateXFExt().setRightBorderColor(((HSSFColorExt) color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
        setRightBorderColor(color.getIndex());  	
    }
    
    //20131011, kuroridoplayer@gmail.com: choose similar color in palette.
    public void setLeftBorderColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	if (color instanceof HSSFColorExt) {
    		getOrCreateXFExt().setLeftBorderColor(((HSSFColorExt) color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
        setLeftBorderColor(color.getIndex());  	
    }    
    
    //20110118, henrichen@zkoss.org: handle XFExtRecord
    private XFExtRecord _xfext;
    private XFExtRecord getXFExt() {
    	if (_xfext == null) {
    		_xfext = _workbook.getXFExtAt(_index);
    	}
    	return _xfext;
    }
  //20110119, henrichen@zkoss.org: handle XFExt record
    private XFExtRecord getOrCreateXFExt() {
    	XFExtRecord xfExt = getXFExt();
    	if (xfExt == null) {
    		_xfext = _workbook.createCellXFExt(_index);
    	}
    	return _xfext;
    }
    
    //ZSS-787
	@Override
	public void setBorder(short left, Color leftColor, short top,
			Color topColor, short right, Color rightColor, short bottom,
			Color bottomColor) {
		setBorderRight(right);
		setBorderLeft(left);
		setBorderTop(top);
		setBorderBottom(bottom);
		setBottomBorderColor((HSSFColor)bottomColor);
		setTopBorderColor((HSSFColor)topColor);
		setRightBorderColor((HSSFColor)rightColor);
		setLeftBorderColor((HSSFColor)leftColor);
	}
	
    //ZSS-787
	@Override
	public void setFill(Color fillColor, Color backColor, short pattern) {
		setFillForegroundColor((HSSFColor)fillColor);
		setFillBackgroundColor((HSSFColor)backColor);
		setFillPattern(pattern);
	}
	
    //ZSS-787
	@Override
	@Deprecated
	public void setCellAlignment(short hAlign, short vAlign, boolean wrapText) {
		setCellAlignment(hAlign, vAlign, wrapText, (short)0);
	}

    //ZSS-1020
	@Override
	public void setCellAlignment(short hAlign, short vAlign, boolean wrapText, short rotation) {
		setAlignment(hAlign);
		setVerticalAlignment(vAlign);
		setWrapText(wrapText);
		setRotation(rotation);
	}

    //ZSS-787
	@Override
	public void setProtection(boolean locked, boolean hidden) {
		setLocked(locked);
		setHidden(hidden);
	}
	
	//ZSS-856
	@Override
	public void setBorder(short left, Color leftColor, short top,
			Color topColor, short right, Color rightColor, short bottom,
			Color bottomColor, short diagonal, Color diagonalColor,
			short horizontal, Color horizontalColor, short vertical,
			Color verticalColor, boolean diaUp, boolean diaDown) {
		setBorderRight(right);
		setBorderLeft(left);
		setBorderTop(top);
		setBorderBottom(bottom);
		setBorderDiagonal(diagonal);
		
		setBottomBorderColor((HSSFColor)bottomColor);
		setTopBorderColor((HSSFColor)topColor);
		setRightBorderColor((HSSFColor)rightColor);
		setLeftBorderColor((HSSFColor)leftColor);
		setDiagonalBorderColor((HSSFColor)diagonalColor);
	}
	
	//ZSS-856
	@Override
	public void setBorderDiagonal(short border) {
        _format.setIndentNotParentBorder(true);
        _format.setAdtlDiagLineStyle(border);
	}
	//ZSS-856
	@Override
	public short getBorderDiagonal() {
		return _format.getAdtlDiagLineStyle();
	}
	//ZSS-856
	@Override
	public Color getDiagonalBorderColorColor() {
    	final XFExtRecord xfExt = getXFExt(); //20110119, henrichen@zkoss.org: handle XFExt record
    	final FullColorExt colorExt = xfExt == null ? null : xfExt.getDiagonalBorderColor();
    	if (colorExt == null || colorExt.isIndex()) {
	        HSSFPalette pallette = new HSSFPalette(_workbook.getCustomPalette());
	        return pallette.getColor(getDiagonalBorderColor());
    	} else {
    		return new HSSFColorExt(colorExt);
    	}
	}
	//ZSS-856
	@Override
	public void setDiagonalBorderColor(short color) {
        _format.setAdtlDiag(color);
	}
	//ZSS-856
	@Override
	public short getDiagonalBorderColor() {
		return _format.getAdtlDiag();
	}
	//ZSS-856
	@Override
	public boolean isShowDiagonalUpBorder() {
		return (_format.getDiag() & 0x02) != 0;
	}
	//ZSS-856
	@Override
	public void setShowDiagonalUpBorder(boolean up) {
		int diag = _format.getDiag();
		diag = up ? (diag | 0x02) : (diag & 0x01);
		_format.setDiag((short)diag);
	}

	//ZSS-856
	@Override
	public boolean isShowDiagonalDownBorder() {
		return (_format.getDiag() & 0x01) != 0;
	}

	//ZSS-856
	@Override
	public void setShowDiagonalDownBorder(boolean up) {
		int diag = _format.getDiag();
		diag = up ? (diag | 0x01) : (diag & 0x02);
		_format.setDiag((short)diag);
	}

    //ZSS-856
    public void setDiagonalBorderColor(HSSFColor color) {
    	//20131126, dennischen@zkoss.org, ZSS-517 use xffext color if there is xffext already
    	XFExtRecord ext = getXFExt();
    	if(ext!=null && !(color instanceof HSSFColorExt)){
    		short[] rgb = color.getTriplet();
    		color = new HSSFColorExt(new FullColorExt(rgb[0], rgb[1], rgb[2]));
    	}
    	if (color instanceof HSSFColorExt) {
    		getOrCreateXFExt().setDiagonalBorderColor(((HSSFColorExt) color).getFullColorExt());
    		color = ((HSSFColorExt)color).getSimilarColor(new HSSFPalette(_workbook.getCustomPalette()));
    	}
        setDiagonalBorderColor(color.getIndex());  	
    }
}
