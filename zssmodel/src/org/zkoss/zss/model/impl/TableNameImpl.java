/* TableNameImpl.java

	Purpose:
		
	Description:
		
	History:
		Mar 18, 2015 1:02:28 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.STable;

/**
 * Mark interface for SName that is associated with Table.
 * @author henri
 * @since 3.8.0
 */
public class TableNameImpl extends NameImpl {
	private static final long serialVersionUID = -6269415794828338660L;
	STable _table;
	public TableNameImpl(AbstractBookAdv book, STable table, String id, String name) {
		super(book, id, name, null);
		this._table = table;
	}

	@Override
	void setName(String newname, String applyToSheetName) {
		super.setName(newname, applyToSheetName);
		this._table.setName(newname);
		this._table.setDisplayName(newname);
	}
}
