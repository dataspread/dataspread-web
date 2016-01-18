/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2015/03/24 , Created by henrichen
}}IS_NOTE

Copyright (C) 2015 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import org.zkoss.poi.ss.formula.ptg.TablePtg;
import org.zkoss.poi.ss.formula.ptg.TablePtg.Item;
import org.zkoss.zss.model.sys.dependency.ColumnRef;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;
/**
 * 
 * @author henrichen
 * @since 3.8.0
 */
public class ColumnRefImpl extends RefImpl implements ColumnRef{

	private static final long serialVersionUID = 1L;
	 
	private final boolean _withHeaders;
	private final String _tableName;
	private final String _columnName1;
	private final String _columnName2;
	private final Item _item1;
	private final Item _item2;
	
	public ColumnRefImpl(String bookName, String sheetName, 
			String tableName, Item item1, Item item2, 
			String columnName1, String columnName2, boolean withHeaders,
			int row, int column, int lastRow, int lastColumn) {
		super(RefType.TABLE, bookName, sheetName, null, row, column, lastRow, lastColumn);
		this._tableName = tableName;
		this._columnName1 = columnName1;
		this._columnName2 = columnName2;
		this._item1 = item1;
		this._item2 = item2;
		this._withHeaders = withHeaders;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((bookName == null) ? 0 : bookName.hashCode());
		result = prime * result
				+ ((_tableName == null) ? 0 : _tableName.hashCode());
		result = prime * result
				+ ((_columnName1 == null) ? 0 : _columnName1.hashCode());
		result = prime * result
				+ ((_columnName2 == null) ? 0 : _columnName2.hashCode());
		result = prime * result
				+ ((_item1 == null) ? 0 : _item1.hashCode());
		result = prime * result
				+ ((_item2 == null) ? 0 : _item2.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		ColumnRefImpl other = (ColumnRefImpl) obj;
		
		if (bookName == null) {
			if (other.bookName != null)
				return false;
		} else if (!bookName.equals(other.bookName))
			return false;
		
		if (_tableName == null) {
			if (other._tableName != null)
				return false;
		} else if (!_tableName.equals(other._tableName))
			return false;
		
		if (_columnName1 == null) {
			if (other._columnName1 != null)
				return false;
		} else if (!_columnName1.equals(other._columnName1))
			return false;
		
		if (_columnName2 == null) {
			if (other._columnName2 != null)
				return false;
		} else if (!_columnName2.equals(other._columnName2))
			return false;
		
		if (_item1 == null) {
			if (other._item1 != null)
				return false;
		} else if (!_item1.equals(other._item1))
			return false;
		
		if (_item2 == null) {
			if (other._item2 != null)
				return false;
		} else if (!_item2.equals(other._item2))
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		final int row1 = getRow();
		final int row2 = getLastRow();
		final int col1 = getColumn();
		final int col2 = getLastColumn();
		
		return bookName+":" + sheetName+"!" + TablePtg.formatAsFormulaString(_tableName, _item1, _item2, _columnName1, _columnName2, false)
		+ (row1 == row2 && col1 == col2 ? new org.zkoss.poi.ss.util.CellReference(row1,col1).formatAsString() :
				new org.zkoss.poi.ss.util.AreaReference(new org.zkoss.poi.ss.util.CellReference(row1, col1),
						new org.zkoss.poi.ss.util.CellReference(row2, col2)).formatAsString());
	}

	//ZSS-966
	@Override
	public String getTableName() {
		return _tableName;
	}

	//ZSS-966
	@Override
	public String getColumnName1() {
		return _columnName1;
	}

	//ZSS-966
	@Override
	public String getColumnName2() {
		return _columnName2;
	}
	
	//ZSS-967
	@Override
	public boolean isWithHeaders() {
		return _withHeaders;
	}
}
