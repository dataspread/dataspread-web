/* ColumnPrecedentRefImpl.java

	Purpose:
		
	Description:
		
	History:
		Mar 25, 2015 10:39:31 AM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.sys.dependency.TablePrecedentRef;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;

//ZSS-966
/**
 * Implementation of precedent reference to {@link ColumnRef}s.
 * @author henri
 * @since 3.8.0
 */
public class TablePrecedentRefImpl extends RefImpl implements TablePrecedentRef {
	private static final long serialVersionUID = -2180648336808339667L;
	private final String _tableName;
	
	public TablePrecedentRefImpl(String bookName, String tableName) {
		super(RefType.NAME, bookName, null, null, -1, -1, -1, -1);
		_tableName = tableName.toUpperCase();
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = bookName.hashCode();
		result = prime * result + _tableName.hashCode();
		return result;
	}
	
	public boolean equals(Object other) {
		if (this == other)
			return true;
		
		if (!(other instanceof TablePrecedentRefImpl)) 
			return false;
		
		final TablePrecedentRefImpl o = (TablePrecedentRefImpl) other;
		return this.bookName.equals(o.bookName)
				&& this._tableName.equals(o._tableName);
	}

	@Override
	public String getTableName() {
		return _tableName;
	}
	
	public String toString() {
		return "TablePrecedentRef()"+ getBookName() + ":" + _tableName;
	}
}
