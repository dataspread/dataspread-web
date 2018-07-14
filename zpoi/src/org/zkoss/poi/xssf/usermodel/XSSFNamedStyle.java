/* XSSFNamedStyle.java

	Purpose:
		
	Description:
		
	History:
		Dec 10, 2014 2:57:46 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellStyle;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Color;
import org.zkoss.poi.ss.usermodel.Font;
import org.zkoss.poi.ss.usermodel.NamedStyle;
import org.zkoss.poi.xssf.model.StylesTable;
import org.zkoss.poi.xssf.model.ThemesTable;

/**
 * @author henri
 * @since 3.9.6
 */
public class XSSFNamedStyle implements NamedStyle {
//    private StylesTable _stylesSource;
    private String name; 
    private int _cellStyleXfId;
    private int _builtinId;
    private CellStyle _inner;
    private CTCellStyle _cellStyle;
    private boolean _custom;

    public XSSFNamedStyle(String name, boolean custom, int builtinId, int xfId, StylesTable stylesSource, ThemesTable theme) {
    	this.name = name;
//    	this._stylesSource = stylesSource;
    	this._cellStyleXfId = xfId;
    	this._builtinId = builtinId;
    	this._cellStyle = stylesSource.getCellStyle(name);
    	this._custom = custom;
    	if (_cellStyle == null) {
    		_cellStyle = CTCellStyle.Factory.newInstance();
    		_cellStyle.setName(name);
			_cellStyle.setXfId((long)xfId);
			if (custom) {
				_cellStyle.setCustomBuiltin(true);
			}
    		if (builtinId >= 0) {
    			_cellStyle.setBuiltinId(builtinId);
    		}
    	}
    	_inner = new XSSFCellStyle(-1, xfId, stylesSource, theme);
    }
    
    public CTCellStyle getCellStyle() {
    	return _cellStyle;
    }
    
    //--NamedStyle--//
    public String getName() {
    	return name;
    }
    
    public boolean isCustomBuiltin() {
    	return _builtinId >= 0;
    }
    
    public int getBuiltinId() {
    	return _builtinId;
    }
  
    //--CellStyle--//
	@Override
	public short getIndex() {
		return (short) _cellStyleXfId;
	}

	@Override
	public void setDataFormat(short fmt) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getDataFormat() {
		return _inner.getDataFormat();
	}

	@Override
	public String getDataFormatString() {
		return _inner.getDataFormatString();
	}

	@Override
	public String getRawDataFormatString() {
		return _inner.getRawDataFormatString();
	}

	@Override
	public boolean isBuiltinDataFormat() {
		return _inner.isBuiltinDataFormat();
	}

	@Override
	public void setFont(Font font) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getFontIndex() {
		return _inner.getFontIndex();
	}

	@Override
	public void setHidden(boolean hidden) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getHidden() {
		return _inner.getHidden();
	}

	@Override
	public void setLocked(boolean locked) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getLocked() {
		return _inner.getLocked();
	}

	@Override
	public void setAlignment(short align) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getAlignment() {
		return _inner.getAlignment();
	}

	@Override
	public void setWrapText(boolean wrapped) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getWrapText() {
		return _inner.getWrapText();
	}

	@Override
	public void setVerticalAlignment(short align) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getVerticalAlignment() {
		return _inner.getVerticalAlignment();
	}

	@Override
	public void setRotation(short rotation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getRotation() {
		return _inner.getRotation();
	}

	@Override
	public void setIndention(short indent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getIndention() {
		return _inner.getIndention();
	}

	@Override
	public void setBorderLeft(short border) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getBorderLeft() {
		return _inner.getBorderLeft();
	}

	@Override
	public void setBorderRight(short border) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getBorderRight() {
		return _inner.getBorderRight();
	}

	@Override
	public void setBorderTop(short border) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getBorderTop() {
		return _inner.getBorderTop();
	}

	@Override
	public void setBorderBottom(short border) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getBorderBottom() {
		return _inner.getBorderBottom();
	}

	@Override
	public void setLeftBorderColor(short color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getLeftBorderColor() {
		return _inner.getLeftBorderColor();
	}

	@Override
	public void setRightBorderColor(short color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getRightBorderColor() {
		return _inner.getRightBorderColor();
	}

	@Override
	public void setTopBorderColor(short color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getTopBorderColor() {
		return _inner.getTopBorderColor();
	}

	@Override
	public void setBottomBorderColor(short color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getBottomBorderColor() {
		return _inner.getBottomBorderColor();
	}

	@Override
	public void setFillPattern(short fp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getFillPattern() {
		return _inner.getFillPattern();
	}

	@Override
	public void setFillBackgroundColor(short bg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getFillBackgroundColor() {
		return _inner.getFillBackgroundColor();
	}

	@Override
	public Color getFillBackgroundColorColor() {
		return _inner.getFillBackgroundColorColor();
	}

	@Override
	public void setFillForegroundColor(short bg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getFillForegroundColor() {
		return _inner.getFillForegroundColor();
	}

	@Override
	public Color getFillForegroundColorColor() {
		return _inner.getFillForegroundColorColor();
	}

	@Override
	public void cloneStyleFrom(CellStyle source) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Color getTopBorderColorColor() {
		return _inner.getTopBorderColorColor();
	}

	@Override
	public Color getBottomBorderColorColor() {
		return _inner.getBottomBorderColorColor();
	}

	@Override
	public Color getRightBorderColorColor() {
		return _inner.getRightBorderColorColor();
	}

	@Override
	public Color getLeftBorderColorColor() {
		return _inner.getLeftBorderColorColor();
	}

	@Override
	public void setFontColorColor(Color fontColor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBorder(short left, Color leftColor, short top,
			Color topColor, short right, Color rightColor, short bottom,
			Color bottomColor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFill(Color fillColor, Color backColor, short pattern) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void setCellAlignment(short hAlign, short vAlign, boolean wrapText) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProtection(boolean locked, boolean hidden) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBorderDiagonal(short border) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getBorderDiagonal() {
		return _inner.getBorderDiagonal();
	}

	@Override
	public Color getDiagonalBorderColorColor() {
		return _inner.getDiagonalBorderColorColor();
	}

	@Override
	public void setDiagonalBorderColor(short color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getDiagonalBorderColor() {
		return _inner.getDiagonalBorderColor();
	}

	@Override
	public boolean isShowDiagonalUpBorder() {
		return _inner.isShowDiagonalUpBorder();
	}

	@Override
	public boolean isShowDiagonalDownBorder() {
		return _inner.isShowDiagonalDownBorder();
	}


	@Override
	public void setShowDiagonalUpBorder(boolean up) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShowDiagonalDownBorder(boolean down) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setBorder(short left, Color leftColor, short top,
			Color topColor, short right, Color rightColor, short bottom,
			Color bottomColor, short diagonal, Color diagonalColor,
			short horizontal, Color horizontalColor, short vertical,
			Color verticalColor, boolean diaUp, boolean diaDown) {
		throw new UnsupportedOperationException();
	}

	//ZSS-1020
	@Override
	public void setCellAlignment(short hAlign, short vAlign, boolean wrapText,
			short rotation) {
		throw new UnsupportedOperationException();
	}
}
