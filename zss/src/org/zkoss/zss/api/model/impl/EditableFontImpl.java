/* EditableFontImpl.java

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
import org.zkoss.zss.api.model.EditableFont;
import org.zkoss.zss.api.model.Font;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SFont;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class EditableFontImpl extends FontImpl implements EditableFont{
	

	public EditableFontImpl(ModelRef<SBook> book, ModelRef<SFont> font) {
		super(book,font);
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
		EditableFontImpl other = (EditableFontImpl) obj;
		if (_fontRef == null) {
			if (other._fontRef != null)
				return false;
		} else if (!_fontRef.equals(other._fontRef))
			return false;
		return true;
	}
	
	public void copyAttributeFrom(Font src) {
		SFont sfont = ((FontImpl)src).getNative();
		SFont font = getNative();
		font.setBoldweight(sfont.getBoldweight());
		font.setColor(sfont.getColor());
		font.setHeightPoints(sfont.getHeightPoints());
		font.setName(sfont.getName());
		font.setItalic(sfont.isItalic());
		font.setStrikeout(sfont.isStrikeout());
		font.setTypeOffset(sfont.getTypeOffset());
		font.setUnderline(sfont.getUnderline());
	}
	public void setFontName(String fontName) {
		getNative().setName(fontName);
	}
	public void setBoldweight(Boldweight boldweight) {
		getNative().setBoldweight(EnumUtil.toFontBoldweight(boldweight));
	}
	public void setItalic(boolean italic) {
		getNative().setItalic(italic);		
	}
	
	public void setStrikeout(boolean strikeout) {
		getNative().setStrikeout(strikeout);	
	}
	public void setUnderline(Underline underline) {
		getNative().setUnderline(EnumUtil.toFontUnderline(underline));
	}
	public void setFontHeight(int twip){
		setFontHeightInPoint(UnitUtil.twipToPoint(twip));
	}
	public void setColor(Color color) {
		getNative().setColor(((ColorImpl)color).getNative());
	}
	public void setFontHeightInPoint(int point) {
		getNative().setHeightPoints(point);	
	}
}
