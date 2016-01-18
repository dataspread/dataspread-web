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

import org.zkoss.zss.model.SColor;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class FontImpl extends AbstractFontAdv {
	private static final long serialVersionUID = 1L;


	public static final String FORMAT_GENERAL = "General";
	/**
     * By default, Microsoft Office Excel 2007 uses the Calibry font in font size 11
     */
	private String _fontName = "Calibri";
	
	 /**
     * By default, Microsoft Office Excel 2007 uses the Calibry font in font size 11
     */
	private int _fontHeightPoint = 11;
	
	private SColor _fontColor = ColorImpl.BLACK;
	
	private Boldweight _fontBoldweight = Boldweight.NORMAL;
	
	private boolean _fontItalic = false;
	private boolean _fontStrikeout = false;
	private TypeOffset _fontTypeOffset = TypeOffset.NONE;
	private Underline _fontUnderline = Underline.NONE;

	public FontImpl(){}
	
	//ZSS-977
	public FontImpl(String fontColor, boolean bold, boolean fontItalic, boolean fontStrikeout, Underline fontUnderline) {
		_fontColor = fontColor != null ? new ColorImpl(fontColor) : ColorImpl.BLACK;
		_fontBoldweight = bold ? Boldweight.BOLD : Boldweight.NORMAL;
		_fontItalic = fontItalic;
		_fontStrikeout = fontStrikeout;
		_fontUnderline = fontUnderline;
	}
	
	@Override
	public String getName() {
		return _fontName;
	}

	@Override
	public void setName(String fontName) {
		this._fontName = fontName;
	}

	@Override
	public SColor getColor() {
		return _fontColor;
	}

	@Override
	public void setColor(SColor fontColor) {
		Validations.argNotNull(fontColor);
		this._fontColor = fontColor;
	}

	@Override
	public Boldweight getBoldweight() {
		return _fontBoldweight;
	}

	@Override
	public void setBoldweight(Boldweight fontBoldweight) {
		Validations.argNotNull(fontBoldweight);
		this._fontBoldweight = fontBoldweight;
	}

	@Override
	public int getHeightPoints() {
		return _fontHeightPoint;
	}

	@Override
	public void setHeightPoints(int fontHeightPoint) {
		this._fontHeightPoint = fontHeightPoint;
	}

	@Override
	public boolean isItalic() {
		return _fontItalic;
	}

	@Override
	public void setItalic(boolean fontItalic) {
		this._fontItalic = fontItalic;
	}

	@Override
	public boolean isStrikeout() {
		return _fontStrikeout;
	}

	@Override
	public void setStrikeout(boolean fontStrikeout) {
		this._fontStrikeout = fontStrikeout;
	}

	@Override
	public TypeOffset getTypeOffset() {
		return _fontTypeOffset;
	}

	@Override
	public void setTypeOffset(TypeOffset fontTypeOffset) {
		Validations.argNotNull(fontTypeOffset);
		this._fontTypeOffset = fontTypeOffset;
	}

	@Override
	public Underline getUnderline() {
		return _fontUnderline;
	}

	@Override
	public void setUnderline(Underline fontUnderline) {
		Validations.argNotNull(fontUnderline);
		this._fontUnderline = fontUnderline;
	}

	

	@Override
	public void copyFrom(SFont src) {
		if (src == this)
			return;
		Validations.argInstance(src, FontImpl.class);
		
		setName(src.getName());
		setColor(src.getColor());
		setBoldweight(src.getBoldweight());
		setHeightPoints(src.getHeightPoints());
		setItalic(src.isItalic());
		setStrikeout(src.isStrikeout());
		setTypeOffset(src.getTypeOffset());
		setUnderline(src.getUnderline());
	}

	@Override
	String getStyleKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(_fontName)
		.append(".").append(_fontColor.getHtmlColor())
		.append(".").append(_fontBoldweight.ordinal())
		.append(".").append(_fontHeightPoint)
		.append(".").append(_fontItalic?"T":"F")
		.append(".").append(_fontStrikeout?"T":"F")
		.append(".").append(_fontTypeOffset.ordinal())
		.append(".").append(_fontUnderline.ordinal());
		return sb.toString();
	}
	
	//ZSS-977
	public int hashCode() {
		return getStyleKey().hashCode();
	}
	
	//ZSS-977
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof FontImpl))
			return false;
		final FontImpl o = (FontImpl) other;
		return this.getStyleKey().equals(o.getStyleKey());
	}
}
