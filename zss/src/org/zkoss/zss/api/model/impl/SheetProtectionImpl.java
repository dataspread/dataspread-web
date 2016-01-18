/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 22, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.api.model.impl;

import java.io.Serializable;

import org.zkoss.zss.api.model.SheetProtection;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SSheetProtection;

/**
 * @author henri
 * @since 3.5.0
 */
public class SheetProtectionImpl implements SheetProtection, Serializable {
	private static final long serialVersionUID = 913006910889795824L;
	
	private ModelRef<SSheet> _sheetRef;
	private ModelRef<SSheetProtection> _sspRef;
	
	public SheetProtectionImpl(ModelRef<SSheet> sheetRef, ModelRef<SSheetProtection> sspRef) {
		this._sheetRef = sheetRef;
		this._sspRef = sspRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_sspRef == null) ? 0 : _sspRef.hashCode());
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
		SheetProtectionImpl other = (SheetProtectionImpl) obj;
		if (_sspRef == null) {
			if (other._sspRef != null)
				return false;
		} else if (!_sspRef.equals(other._sspRef))
			return false;
		return true;
	}
	
	public SSheetProtection getNative() {
		return _sspRef.get();
	}

	@Override
	public boolean isObjectsEditable() {
		return getNative().isObjects();
	}

	@Override
	public boolean isScenariosEditable() {
		return getNative().isScenarios();
	}
	
	@Override
	public boolean isFormatCellsAllowed() {
		return getNative().isFormatCells();
	}

	@Override
	public boolean isFormatColumnsAllowed() {
		return getNative().isFormatColumns();
	}

	@Override
	public boolean isFormatRowsAllowed() {
		return getNative().isFormatRows();
	}

	@Override
	public boolean isInsertColumnsAllowed() {
		return getNative().isInsertColumns();
	}

	@Override
	public boolean isInsertRowsAllowed() {
		return getNative().isInsertRows();
	}

	@Override
	public boolean isInsertHyperlinksAllowed() {
		return getNative().isInsertHyperlinks();
	}

	@Override
	public boolean isDeleteColumnsAllowed() {
		return getNative().isDeleteColumns();
	}

	@Override
	public boolean isDeleteRowsAllowed() {
		return getNative().isDeleteRows();
	}

	@Override
	public boolean isSelectLockedCellsAllowed() {
		return getNative().isSelectLockedCells();
	}

	@Override
	public boolean isSortAllowed() {
		return getNative().isSort();
	}

	@Override
	public boolean isAutoFilterAllowed() {
		return getNative().isAutoFilter();
	}
	
	@Override
	public boolean isPivotTablesAllowed() {
		return getNative().isPivotTables();
	}

	@Override
	public boolean isSelectUnlockedCellsAllowed() {
		return getNative().isSelectUnlockedCells();
	}
}
