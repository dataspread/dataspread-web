/* FontImpl.java

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
package org.zkoss.zss.api.model.impl;

import org.zkoss.zss.api.UnitUtil;
import org.zkoss.zss.api.model.Color;
import org.zkoss.zss.api.model.Font;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SFont;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class FontImpl implements Font{
	
	protected ModelRef<SBook> _bookRef;
	protected ModelRef<SFont> _fontRef;
	
	public FontImpl(ModelRef<SBook> book, ModelRef<SFont> font) {
		this._bookRef = book;
		this._fontRef = font;
	}
	public String getFontName() {
		return getNative().getName();
	}
	public SFont getNative() {
		return _fontRef.get();
	}
	public ModelRef<SFont> getRef(){
		return _fontRef;
	}
	
	public Color getColor(){
		return new ColorImpl(_bookRef,new SimpleRef(getNative().getColor()));
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_fontRef == null) ? 0 : _fontRef.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FontImpl other = (FontImpl) obj;
		if (_fontRef == null) {
			if (other._fontRef != null)
				return false;
		} else if (!_fontRef.equals(other._fontRef))
			return false;
		return true;
	}
	public Boldweight getBoldweight() {
		return EnumUtil.toFontBoldweight(getNative().getBoldweight());
	}
	public int getFontHeight() {
		return UnitUtil.pointToTwip(getFontHeightInPoint());
	}
	public boolean isItalic() {
		return getNative().isItalic();
	}
	public boolean isStrikeout() {
		return getNative().isStrikeout();
	}
	public TypeOffset getTypeOffset() {
		return EnumUtil.toFontTypeOffset(getNative().getTypeOffset());
	}
	public Underline getUnderline() {
		return EnumUtil.toFontUnderline(getNative().getUnderline());
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(getNative()).append("]");
		sb.append(getFontName());
		return sb.toString();
	}

	public int getFontHeightInPoint() {
		return getNative().getHeightPoints();
	}
}
