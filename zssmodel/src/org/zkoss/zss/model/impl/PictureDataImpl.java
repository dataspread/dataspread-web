/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/07/25, Created by henrichen
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SPicture.Format;
import org.zkoss.zss.model.SPictureData;
/**
 * 
 * @author henrichen
 * @since 3.6.0
 */
public class PictureDataImpl implements SPictureData, Serializable {
	private static final long serialVersionUID = -8176040020483451498L;

	final private int _index;
	final private Format _format;
	final private byte[] _data;

	/*package*/ PictureDataImpl(int index, Format format, byte[] data) {
		this._index = index;
		this._format = format;
		this._data = data;
	}
	
	@Override
	public int getIndex() {
		return _index;
	}
	@Override
	public Format getFormat() {
		return _format;
	}
	@Override
	public byte[] getData() {
		return _data;
	}
}
