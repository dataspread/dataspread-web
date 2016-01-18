/* ColumnPrecedentRefImpl.java

	Purpose:
		
	Description:
		
	History:
		Mar 25, 2015 10:39:31 AM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import org.zkoss.poi.ss.formula.ptg.TablePtg;
import org.zkoss.zss.model.sys.dependency.ColumnPrecedentRef;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;

//ZSS-967
/**
 * Implementation of precedent reference to {@link ColumnRef}s.
 * @author henri
 * @since 3.8.0
 */
public class ColumnPrecedentRefImpl extends RefImpl implements ColumnPrecedentRef {
	private static final long serialVersionUID = -2180648336808339667L;
	private final String _tableName;
	private final String _columnName;
	public ColumnPrecedentRefImpl(String bookName, String tableName, String columnName) {
		super(RefType.NAME, bookName, null, null, -1, -1, -1, -1);
		_tableName = tableName.toUpperCase();
		_columnName = columnName.toUpperCase();
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = bookName.hashCode();
		result = prime * result + _tableName.hashCode();
		result = prime * result + _columnName.hashCode();
		return result;
	}
	
	public boolean equals(Object other) {
		if (this == other)
			return true;
		
		if (!(other instanceof ColumnPrecedentRef)) 
			return false;
		
		final ColumnPrecedentRef o = (ColumnPrecedentRef) other;
		return this.getBookName().equals(o.getBookName())
				&& this.getTableName().equals(o.getTableName())
				&& this.getColumnName().equals(o.getColumnName());
	}

	@Override
	public String getTableName() {
		return _tableName;
	}

	@Override
	public String getColumnName() {
		return _columnName;
	}

	@Override
	public String toString() {
		return "ColumnPrecedentRef()"+bookName+":" + TablePtg.formatAsFormulaString(_tableName, null, null, _columnName, null, false);
	}
}
