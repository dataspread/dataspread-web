/* CellStyleImpl.java

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

import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.Color;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SColor;
//import org.zkoss.zss.model.sys.XBook;
//import org.zkoss.zss.model.sys.impl.BookHelper;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class CellStyleImpl implements CellStyle{
	
	protected ModelRef<SBook> _bookRef;
	protected ModelRef<SCellStyle> _styleRef;
	
	protected FontImpl _font;
	
	public CellStyleImpl(ModelRef<SBook> book,ModelRef<SCellStyle> style) {
		this._bookRef = book;
		this._styleRef = style;
	}
	
	public SCellStyle getNative(){
		return _styleRef.get();
	}
	public ModelRef<SCellStyle> getRef(){
		return _styleRef;
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
		CellStyleImpl other = (CellStyleImpl) obj;
		if (_styleRef == null) {
			if (other._styleRef != null)
				return false;
		} else if (!_styleRef.equals(other._styleRef))
			return false;
		return true;
	}

	public FontImpl getFont() {
		if(_font!=null){
			return _font;
		}
		return _font = new FontImpl(_bookRef,new SimpleRef(getNative().getFont()));
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(getNative()).append("]");
		sb.append("font:[").append(getFont()).append("]");
		return sb.toString();
	}

	public ColorImpl getBackgroundColor() {
		return getFillColor();
	}
	public ColorImpl getBackColor() {
		SColor srcColor = getNative().getBackColor();
		return new ColorImpl(_bookRef,new SimpleRef(srcColor));
	}
	
	public ColorImpl getFillColor() {
		SColor srcColor = getNative().getFillColor();
		return new ColorImpl(_bookRef,new SimpleRef(srcColor));
	}

	public FillPattern getFillPattern(){
		return EnumUtil.toStyleFillPattern(getNative().getFillPattern());
	}

	public Alignment getAlignment(){
		return EnumUtil.toStyleAlignemnt(getNative().getAlignment());
	}

	public VerticalAlignment getVerticalAlignment(){
		return EnumUtil.toStyleVerticalAlignemnt(getNative().getVerticalAlignment());
	}

	public boolean isWrapText() {
		return getNative().isWrapText();
	}
	
	public BorderType getBorderLeft(){
		return EnumUtil.toStyleBorderType(getNative().getBorderLeft());
	}

	public BorderType getBorderTop(){
		return EnumUtil.toStyleBorderType(getNative().getBorderTop());
	}

	public BorderType getBorderRight(){
		return EnumUtil.toStyleBorderType(getNative().getBorderRight());
	}

	public BorderType getBorderBottom(){
		return EnumUtil.toStyleBorderType(getNative().getBorderBottom());
	}

	public Color getBorderTopColor(){
		return new ColorImpl(_bookRef,new SimpleRef<SColor>(getNative().getBorderTopColor()));
	}

	public Color getBorderLeftColor(){
		return new ColorImpl(_bookRef,new SimpleRef<SColor>(getNative().getBorderLeftColor()));
	}

	public Color getBorderBottomColor(){
		return new ColorImpl(_bookRef,new SimpleRef<SColor>(getNative().getBorderBottomColor()));
	}

	public Color getBorderRightColor(){
		return new ColorImpl(_bookRef,new SimpleRef<SColor>(getNative().getBorderRightColor()));
	}
	
	public String getDataFormat(){
		return getNative().getDataFormat();
	}

	public boolean isLocked() {
		return getNative().isLocked();
	}

	public boolean isHidden() {
		return getNative().isHidden();
	}
}
