/* InsertDeleteUpdate.java

	Purpose:
		
	Description:
		
	History:
		Feb 21, 2014 Created by Pao Wang

Copyright (C) 2014 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.range.impl;

import org.zkoss.zss.model.SSheet;

/**
 * a range of row/column indicates insert/delete changes.
 * @author Pao
 * @since 3.5.0
 */
public class InsertDeleteUpdate {
	private SSheet _sheet;
	private boolean _inserted;
	private boolean _row;
	private int _index;
	private int _lastIndex;

	public InsertDeleteUpdate(SSheet sheet, boolean inserted, boolean row, int index, int lastIndex) {
		this._sheet = sheet;
		this._inserted = inserted;
		this._row = row;
		this._index = index;
		this._lastIndex = lastIndex;
	}

	public SSheet getSheet() {
		return _sheet;
	}

	public boolean isInserted() {
		return _inserted;
	}

	public boolean isRow() {
		return _row;
	}

	public int getIndex() {
		return _index;
	}

	public int getLastIndex() {
		return _lastIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _index;
		result = prime * result + (_inserted ? 1231 : 1237);
		result = prime * result + _lastIndex;
		result = prime * result + (_row ? 1231 : 1237);
		result = prime * result + ((_sheet == null) ? 0 : _sheet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		InsertDeleteUpdate other = (InsertDeleteUpdate)obj;
		if(_index != other._index)
			return false;
		if(_inserted != other._inserted)
			return false;
		if(_lastIndex != other._lastIndex)
			return false;
		if(_row != other._row)
			return false;
		if(_sheet == null) {
			if(other._sheet != null)
				return false;
		} else if(!_sheet.equals(other._sheet))
			return false;
		return true;
	}

}
