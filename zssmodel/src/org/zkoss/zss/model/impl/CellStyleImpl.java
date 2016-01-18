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
package org.zkoss.zss.model.impl;

import org.zkoss.lang.Objects;
import org.zkoss.zss.model.SBorder;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SColor;
import org.zkoss.zss.model.SFill;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.SBorder.BorderType;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class CellStyleImpl extends AbstractCellStyleAdv {
	private static final long serialVersionUID = 1L;

	private AbstractFontAdv _font;
	
	//SFill
	private AbstractFillAdv _fill;
	
	//SBorder
	private AbstractBorderAdv _border;
	
	private Alignment _alignment = Alignment.GENERAL;
	private VerticalAlignment _verticalAlignment = VerticalAlignment.BOTTOM;
	private boolean _wrapText = false;

	private String _dataFormat = FORMAT_GENERAL;
	private boolean _directFormat = false;
	private boolean _locked = true;// default locked as excel.
	private boolean _hidden = false;
	private int _rotation; //ZSS-918
	private int _indention; //ZSS-915

	public CellStyleImpl(AbstractFontAdv font){
		this._font = font;
	}
	
	public CellStyleImpl(AbstractFontAdv font, AbstractFillAdv fill, AbstractBorderAdv border){
		this._font = font;
		this._fill = fill;
		this._border = border;
	}
	public SFont getFont(){
		return _font;
	}
	
	public void setFont(SFont font){
		Validations.argInstance(font, AbstractFontAdv.class);
		this._font = (AbstractFontAdv)font;
	}

	@Override
	public SColor getFillColor() {
		return _fill == null ? ColorImpl.BLACK : _fill.getFillColor();
	}

	@Override
	public void setFillColor(SColor fillColor) {
		Validations.argNotNull(fillColor);
		if (_fill == null) {
			_fill = new FillImpl();
		}
		_fill.setFillColor(fillColor);
	}

	@Override
	public FillPattern getFillPattern() {
		return _fill == null ? FillPattern.NONE : _fill.getFillPattern();
	}

	@Override
	public void setFillPattern(FillPattern fillPattern) {
		Validations.argNotNull(fillPattern);
		if (_fill == null) {
			_fill = new FillImpl();
		}
		_fill.setFillPattern(fillPattern);
	}

	@Override
	public Alignment getAlignment() {
		return _alignment;
	}

	@Override
	public void setAlignment(Alignment alignment) {
		Validations.argNotNull(alignment);
		this._alignment = alignment;
	}

	@Override
	public VerticalAlignment getVerticalAlignment() {
		return _verticalAlignment;
	}

	@Override
	public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
		Validations.argNotNull(verticalAlignment);
		this._verticalAlignment = verticalAlignment;
	}

	@Override
	public boolean isWrapText() {
		return _wrapText;
	}

	@Override
	public void setWrapText(boolean wrapText) {
		this._wrapText = wrapText;
	}

	@Override
	public BorderType getBorderLeft() {
		return _border == null ? BorderType.NONE : _border.getBorderLeft();
	}

	@Override
	public void setBorderLeft(BorderType borderLeft) {
		Validations.argNotNull(borderLeft);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderLeft(borderLeft);
	}

	@Override
	public BorderType getBorderTop() {
		return _border == null ? BorderType.NONE : _border.getBorderTop();
	}

	@Override
	public void setBorderTop(BorderType type) {
		Validations.argNotNull(type);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderTop(type);
	}

	@Override
	public BorderType getBorderRight() {
		return _border == null ? BorderType.NONE : _border.getBorderRight();
	}

	@Override
	public void setBorderRight(BorderType type) {
		Validations.argNotNull(type);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderRight(type);
	}

	@Override
	public BorderType getBorderBottom() {
		return _border == null ? BorderType.NONE : _border.getBorderBottom();
	}

	@Override
	public void setBorderBottom(BorderType type){
		Validations.argNotNull(type);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderBottom(type);
	}

	@Override
	public SColor getBorderTopColor() {
		return _border == null ? ColorImpl.BLACK : _border.getBorderTopColor();
	}

	@Override
	public void setBorderTopColor(SColor color) {
		Validations.argNotNull(color);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderTopColor(color);
	}

	@Override
	public SColor getBorderLeftColor() {
		return _border == null ? ColorImpl.BLACK : _border.getBorderLeftColor();
	}

	@Override
	public void setBorderLeftColor(SColor color) {
		Validations.argNotNull(color);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderLeftColor(color);
	}

	@Override
	public SColor getBorderBottomColor() {
		return _border == null ? ColorImpl.BLACK : _border.getBorderBottomColor();
	}

	@Override
	public void setBorderBottomColor(SColor color) {
		Validations.argNotNull(color);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderBottomColor(color);
	}

	@Override
	public SColor getBorderRightColor() {
		return _border == null ? ColorImpl.BLACK : _border.getBorderRightColor();
	}

	@Override
	public void setBorderRightColor(SColor color) {
		Validations.argNotNull(color);
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderRightColor(color);
	}

	@Override
	public String getDataFormat() {
		return _dataFormat;
	}
	
	@Override
	public boolean isDirectDataFormat(){
		return _directFormat;
	}

	@Override
	public void setDataFormat(String dataFormat) {
		//set to general if null to compatible with 3.0
		if(dataFormat==null || "".equals(dataFormat.trim())){
			dataFormat = FORMAT_GENERAL;
		}
		this._dataFormat = dataFormat;
		_directFormat = false;
	}
	
	@Override
	public void setDirectDataFormat(String dataFormat){
		setDataFormat(dataFormat);
		_directFormat = true;
	}

	@Override
	public boolean isLocked() {
		return _locked;
	}

	@Override
	public void setLocked(boolean locked) {
		this._locked = locked;
	}

	@Override
	public boolean isHidden() {
		return _hidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		this._hidden = hidden;
	}

	@Override
	public void copyFrom(SCellStyle src) {
		if (src == this)
			return;
		Validations.argInstance(src, CellStyleImpl.class);
		setFont(src.getFont());//assign directly
		
		setFillColor(src.getFillColor());
		setBackColor(src.getBackColor());
		setFillPattern(src.getFillPattern());
		setAlignment(src.getAlignment());
		setVerticalAlignment(src.getVerticalAlignment());
		setWrapText(src.isWrapText());
		setRotation(src.getRotation()); //ZSS-918
		setIndention(src.getIndention()); //ZSS-915

		setBorderLeft(src.getBorderLeft());
		setBorderTop(src.getBorderTop());
		setBorderRight(src.getBorderRight());
		setBorderBottom(src.getBorderBottom());
		setBorderTopColor(src.getBorderTopColor());
		setBorderLeftColor(src.getBorderLeftColor());
		setBorderBottomColor(src.getBorderBottomColor());
		setBorderRightColor(src.getBorderRightColor());

		setDataFormat(src.getDataFormat());
		setLocked(src.isLocked());
		setHidden(src.isHidden());
	}
	
	@Override
	String getStyleKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(_font == null ? "" : _font.getStyleKey())
		.append(".").append(_fill == null ? "" : _fill.getStyleKey())
		.append(".").append(_border == null ? "" : _border.getStyleKey())
		.append(".").append(_alignment.ordinal())
		.append(".").append(_verticalAlignment.ordinal())
		.append(".").append(_wrapText?"T":"F")
		.append(".").append(_dataFormat)
		.append(".").append(_locked?"T":"F")
		.append(".").append(_hidden?"T":"F")
		.append(".").append(_rotation);
		return sb.toString();
	}

	@Override
	public void setBorderLeft(BorderType borderLeft, SColor color) {
		setBorderLeft(borderLeft);
		setBorderLeftColor(color);
	}

	@Override
	public void setBorderTop(BorderType borderTop, SColor color) {
		setBorderTop(borderTop);
		setBorderTopColor(color);
	}

	@Override
	public void setBorderRight(BorderType borderRight, SColor color) {
		setBorderRight(borderRight);
		setBorderRightColor(color);
	}

	@Override
	public void setBorderBottom(BorderType borderBottom, SColor color) {
		setBorderBottom(borderBottom);
		setBorderBottomColor(color);
	}

	//ZSS-780
	@Override
	public SColor getBackColor() {
		return _fill == null ? ColorImpl.WHITE : _fill.getBackColor();
	}

	//ZSS-780
	@Deprecated
	@Override
	public void setBackgroundColor(SColor backColor) {
		setBackColor(backColor);
	}
	@Override
	public void setBackColor(SColor backColor) {
		Validations.argNotNull(backColor);
		if (_fill == null) {
			_fill = new FillImpl();
		}
		_fill.setBackColor(backColor);
	}

	//ZSS-841
	@Override
	public String getFillPatternHtml() {
		return _fill == null ? "" : ((FillImpl)_fill).getFillPatternHtml();
	}
	
	//--Object--//
	public int hashCode() {
		int hash = _font == null ? 0 : _font.hashCode();
		hash = hash * 31 + (_fill == null ? 0 : _fill.hashCode());
		hash = hash * 31 + (_alignment == null ? 0 : _alignment.hashCode());
		hash = hash * 31 + (_verticalAlignment == null ? 0 : _verticalAlignment.hashCode());
		hash = hash * 31 + (_wrapText ? 1 : 0);
		hash = hash * 31 + (_border == null ? 0 : _border.hashCode());
		hash = hash * 31 + (_dataFormat == null ? 0 : _dataFormat.hashCode());
		hash = hash * 31 + (_directFormat ? 1 : 0);
		hash = hash * 31 + (_locked ? 1 : 0);
		hash = hash * 31 + (_hidden ? 1 : 0);
		hash = hash * 31 + _rotation;
		hash = hash * 31 + _indention;
		
		return hash;
	}
	
	public boolean equals(Object other) {
		if (other == this) return true;
		if (!(other instanceof CellStyleImpl)) return false;
		CellStyleImpl o = (CellStyleImpl) other;
		return Objects.equals(this._font, o._font)
				&& Objects.equals(this._fill, o._fill)
				&& Objects.equals(this._alignment, o._alignment)
				&& Objects.equals(this._verticalAlignment, o._verticalAlignment)
				&& Objects.equals(this._wrapText, o._wrapText)
				&& Objects.equals(this._border, o._border)
				&& Objects.equals(this._dataFormat, o._dataFormat)
				&& Objects.equals(this._directFormat, o._directFormat)
				&& Objects.equals(this._locked, o._locked)
				&& Objects.equals(this._hidden, o._hidden)
				&& Objects.equals(this._rotation, o._rotation)
				&& Objects.equals(this._indention, o._indention);
		
	}

	//ZSS-918
	@Override
	public int getRotation() {
		return _rotation;
	}

	//ZSS-918
	@Override
	public void setRotation(int rotation) {
		_rotation = rotation;
	}

	//ZSS-915
	@Override
	public int getIndention() {
		return _indention;
	}

	//ZSS-915
	@Override
	public void setIndention(int indention) {
		_indention = indention;
	}

	//ZSS-977
	@Override
	public SBorder getBorder() {
		return _border;
	}

	//ZSS-977
	@Override
	public SFill getFill() {
		return _fill;
	}

	//ZSS-977
	@Override
	public void setBorderVertical(BorderType type) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderVertical(type);
	}

	//ZSS-977
	@Override
	public void setBorderVertical(BorderType type, SColor color) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderVertical(type);
		_border.setBorderVerticalColor(color);
	}

	//ZSS-977
	@Override
	public void setBorderHorizontal(BorderType type) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderHorizontal(type);
	}

	//ZSS-977
	@Override
	public void setBorderHorizontal(BorderType type, SColor color) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderHorizontal(type);
		_border.setBorderHorizontalColor(color);
	}

	//ZSS-977
	@Override
	public void setBorderDiagonal(BorderType type) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderDiagonal(type);
	}

	//ZSS-977
	@Override
	public void setBorderDiagonal(BorderType type, SColor color) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderDiagonal(type);
		_border.setBorderDiagonalColor(color);
	}

	//ZSS-977
	@Override
	public void setBorderVerticalColor(SColor color) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderVerticalColor(color);
	}

	//ZSS-977
	@Override
	public void setBorderHorizontalColor(SColor color) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderHorizontalColor(color);
	}

	//ZSS-977
	@Override
	public void setBorderDiagonalColor(SColor color) {
		if (_border == null) {
			_border = new BorderImpl();
		}
		_border.setBorderDiagonalColor(color);
	}

	//ZSS-977
	@Override
	public BorderType getBorderVertical() {
		return _border == null ? BorderType.NONE : _border.getBorderVertical();
	}

	//ZSS-977
	@Override
	public BorderType getBorderHorizontal() {
		return _border == null ? BorderType.NONE : _border.getBorderHorizontal();
	}

	//ZSS-977
	@Override
	public BorderType getBorderDiagonal() {
		return _border == null ? BorderType.NONE : _border.getBorderDiagonal();
	}

	//ZSS-977
	@Override
	public SColor getBorderVerticalColor() {
		return _border == null ? ColorImpl.BLACK : _border.getBorderVerticalColor();
	}

	//ZSS-977
	@Override
	public SColor getBorderHorizontalColor() {
		return _border == null ? ColorImpl.BLACK : _border.getBorderHorizontalColor();
	}

	//ZSS-977
	@Override
	public SColor getBorderDiagonalColor() {
		return _border == null ? ColorImpl.BLACK : _border.getBorderDiagonalColor();
	}
	
	//ZSS-977
	protected void setBorder(SBorder border) {
		_border = (AbstractBorderAdv)border;
	}
	
	//ZSS-977
	protected void setFill(SFill fill) {
		_fill = (AbstractFillAdv) fill;
	}
}
