/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.lang.ref.WeakReference;

import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
class ColumnProxy implements SColumn {
	private static final long serialVersionUID = 1L;
	private final WeakReference<AbstractSheetAdv> _sheetRef;
	private final int _index;
	private AbstractColumnArrayAdv _proxy;

	public ColumnProxy(AbstractSheetAdv sheet, int index) {
		this._sheetRef = new WeakReference(sheet);
		this._index = index;
	}

	protected void loadProxy(boolean split) {
		if(split){
			_proxy = (AbstractColumnArrayAdv) ((AbstractSheetAdv)getSheet()).getOrSplitColumnArray(_index);
		}else if (_proxy == null) {
			_proxy = (AbstractColumnArrayAdv) ((AbstractSheetAdv)getSheet()).getColumnArray(_index);
		}
	}

	@Override
	public SSheet getSheet() {
		AbstractSheetAdv sheet = _sheetRef.get();
		if (sheet == null) {
			throw new IllegalStateException(
					"proxy target lost, you should't keep this instance");
		}
		return sheet;
	}

	@Override
	public int getIndex() {
		return _index;
	}

	@Override
	public boolean isNull() {
		loadProxy(false);
		return _proxy == null ? true : false;
	}

	@Override
	public SCellStyle getCellStyle() {
		loadProxy(false);
		if (_proxy != null) {
			return _proxy.getCellStyle();
		}
		return getSheet().getBook().getDefaultCellStyle();
	}
	
	@Override
	public SCellStyle getCellStyle(boolean local) {
		loadProxy(false);
		if (_proxy != null) {
			return _proxy.getCellStyle(local);
		}
		return local?null:getSheet().getBook().getDefaultCellStyle();
	}

	@Override
	public void setCellStyle(SCellStyle cellStyle) {
		loadProxy(true);
		_proxy.setCellStyle(cellStyle);
	}

	@Override
	public int getWidth() {
		loadProxy(false);
		if (_proxy != null) {
			return _proxy.getWidth();
		}
		return getSheet().getDefaultColumnWidth();
	}

	@Override
	public boolean isHidden() {
		loadProxy(false);
		if (_proxy != null) {
			return _proxy.isHidden();
		}
		return false;
	}

	@Override
	public void setWidth(int width) {
		loadProxy(true);
		_proxy.setWidth(width);
	}

	@Override
	public void setHidden(boolean hidden) {
		loadProxy(true);
		_proxy.setHidden(hidden);
	}
	
	@Override
	public boolean isCustomWidth() {
		loadProxy(false);
		if (_proxy != null) {
			return _proxy.isCustomWidth();
		}
		return false;
	}
	@Override
	public void setCustomWidth(boolean custom) {
		loadProxy(true);
		_proxy.setCustomWidth(custom);
	}
}
