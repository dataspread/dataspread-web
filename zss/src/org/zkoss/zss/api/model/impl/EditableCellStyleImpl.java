/* EditableCellStyleImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/6/4 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model.impl;

import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.Color;
import org.zkoss.zss.api.model.EditableCellStyle;
import org.zkoss.zss.api.model.Font;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCellStyle;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class EditableCellStyleImpl extends CellStyleImpl implements EditableCellStyle{
	
	public EditableCellStyleImpl(ModelRef<SBook> book,ModelRef<SCellStyle> style) {
		super(book,style);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_styleRef == null) ? 0 : _styleRef.hashCode());
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
		EditableCellStyleImpl other = (EditableCellStyleImpl) obj;
		if (_styleRef == null) {
			if (other._styleRef != null)
				return false;
		} else if (!_styleRef.equals(other._styleRef))
			return false;
		return true;
	}
	public void setFont(Font font) {
		this._font = (FontImpl)font; 
		getNative().setFont(font==null?null:this._font.getNative());
	}

	public void copyAttributeFrom(CellStyle src) {
		getNative().copyFrom(((CellStyleImpl)src).getNative());
	}

	@Deprecated
	public void setBackgroundColor(Color color) {
		setBackColor(color); //ZSS-1068
	}
	public void setFillColor(Color color) {
		getNative().setFillColor(((ColorImpl)color).getNative());
	}

	public void setFillPattern(FillPattern pattern) {
		getNative().setFillPattern(EnumUtil.toStyleFillPattern(pattern));	
	}

	public void setAlignment(Alignment alignment){
		getNative().setAlignment(EnumUtil.toStyleAlignemnt(alignment));
	}

	public void setVerticalAlignment(VerticalAlignment alignment){
		getNative().setVerticalAlignment(EnumUtil.toStyleVerticalAlignemnt(alignment));
	}
	
	public void setWrapText(boolean wraptext) {
		getNative().setWrapText(wraptext);
	}

	public void setBorderLeft(BorderType borderType){
		getNative().setBorderLeft(EnumUtil.toStyleBorderType(borderType));
	}

	public void setBorderTop(BorderType borderType){
		getNative().setBorderTop(EnumUtil.toStyleBorderType(borderType));
	}

	public void setBorderRight(BorderType borderType){
		getNative().setBorderRight(EnumUtil.toStyleBorderType(borderType));
	}
	
	public void setBorderBottom(BorderType borderType){
		getNative().setBorderBottom(EnumUtil.toStyleBorderType(borderType));
	}
	
	public void setBorderTopColor(Color color){
		getNative().setBorderTopColor(((ColorImpl)color).getNative());
	}
	
	public void setBorderLeftColor(Color color){
		getNative().setBorderLeftColor(((ColorImpl)color).getNative());
	}

	public void setBorderBottomColor(Color color){
		getNative().setBorderBottomColor(((ColorImpl)color).getNative());
	}
	
	public void setBorderRightColor(Color color){
		getNative().setBorderRightColor(((ColorImpl)color).getNative());
	}
	
	public void setDataFormat(String format){
		if(getDataFormat().equals(format)){
			return;
		}
		// ZSS-510, when format is null or empty, it should be assigned as "General" format.
		format = format.equals("") ? "General" : format;
		getNative().setDataFormat(format);
	}

	public void setLocked(boolean locked) {
		getNative().setLocked(locked);
	}

	public void setHidden(boolean hidden) {
		getNative().setHidden(hidden);;
	}
	
	//ZSS-918
	public void setRotation(int rotation) {
		getNative().setRotation(rotation);
	}
	
	//ZSS-915
	public void setIndention(int indention) {
		getNative().setIndention(indention);
	}

	//ZSS-1068
	@Override
	public void setBackColor(Color color) {
		getNative().setBackColor(((ColorImpl)color).getNative());
	}
}
