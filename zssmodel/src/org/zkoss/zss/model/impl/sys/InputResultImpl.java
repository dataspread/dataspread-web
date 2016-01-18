/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl.sys;

import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.sys.input.InputResult;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class InputResultImpl implements InputResult{

	private String _editText = null;
	private Object _value = null;
	private CellType _type = CellType.BLANK;
	private String _format = null;
	public InputResultImpl(){}
	public InputResultImpl(String input) {
		this._editText = input;
	}

	public String getEditText() {
		return _editText;
	}

	public Object getValue() {
		return _value;
	}

	public CellType getType() {
		return _type;
	}

	void setValue(Object value) {
		this._value = value;
	}

	void setType(CellType type) {
		this._type = type;
	}
	public String getFormat() {
		return _format;
	}
	void setFormat(String format) {
		this._format = format;
	}
}
