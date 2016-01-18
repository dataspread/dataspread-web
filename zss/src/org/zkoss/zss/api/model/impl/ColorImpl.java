/* ColorImpl.java

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

import org.zkoss.zss.api.model.Color;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SColor;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class ColorImpl implements Color{

	private ModelRef<SBook> _bookRef;
	private ModelRef<SColor> _colorRef;

	public ColorImpl(ModelRef<SBook> book, ModelRef<SColor> color) {
		this._bookRef = book;
		this._colorRef = color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_colorRef == null) ? 0 : _colorRef.hashCode());
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
		ColorImpl other = (ColorImpl) obj;
		if (_colorRef == null) {
			if (other._colorRef != null)
				return false;
		} else if (!_colorRef.equals(other._colorRef))
			return false;
		return true;
	}

	public SColor getNative() {
		return _colorRef.get();
	}
	public ModelRef<SColor> getRef(){
		return _colorRef;
	}
	public ModelRef<SBook> getBookRef(){
		return _bookRef;
	}

	public String getHtmlColor() {
		return getNative().getHtmlColor();
	}

}
