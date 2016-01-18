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
import java.util.Collections;
import java.util.Iterator;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
class RowProxy extends AbstractRowAdv{
	private static final long serialVersionUID = 1L;
	
	private final WeakReference<AbstractSheetAdv> _sheetRef;
	private final int _index;
	AbstractRowAdv _proxy;
	
	public RowProxy(AbstractSheetAdv sheet, int index) {
		this._sheetRef = new WeakReference(sheet);
		this._index = index;
	}
	@Override
	public SSheet getSheet(){
		AbstractSheetAdv sheet = _sheetRef.get();
		if(sheet==null){
			throw new IllegalStateException("proxy target lost, you should't keep this instance");
		}
		return sheet;
	}
	
	protected void loadProxy(){
		if(_proxy==null){
			_proxy = (AbstractRowAdv)((AbstractSheetAdv)getSheet()).getRow(_index,false);
		}
	}
	
	public int getIndex() {
		loadProxy();
		return _proxy==null?_index:_proxy.getIndex();
	}


	public boolean isNull() {
		loadProxy();
		return _proxy==null?true:_proxy.isNull();
	}


	public int getStartCellIndex() {
		loadProxy();
		return _proxy==null?-1:_proxy.getStartCellIndex();
	}


	public int getEndCellIndex() {
		loadProxy();
		return _proxy==null?-1:_proxy.getEndCellIndex();
	}
	
	public SCellStyle getCellStyle() {
		return getCellStyle(false);
	}

	public SCellStyle getCellStyle(boolean local) {
		loadProxy();
		if(_proxy!=null){
			return _proxy.getCellStyle(local);
		}
		return local?null:getSheet().getBook().getDefaultCellStyle();
	}
	
	public void setCellStyle(SCellStyle cellStyle) {
		loadProxy();
		if(_proxy==null){
			_proxy = (AbstractRowAdv)((AbstractSheetAdv)getSheet()).getOrCreateRow(_index);
		}
		_proxy.setCellStyle(cellStyle);
	}
	
	@Override
	public AbstractCellAdv getCell(int columnIdx, boolean proxy) {
		throw new UnsupportedOperationException("readonly");
	}
	@Override
	AbstractCellAdv getOrCreateCell(int columnIdx) {
		throw new UnsupportedOperationException("readonly");
	}
	@Override
	void clearCell(int start, int end) {
		throw new UnsupportedOperationException("readonly");
	}
	@Override
	void insertCell(int start, int size) {
		throw new UnsupportedOperationException("readonly");
	}
	@Override
	void deleteCell(int start, int size) {
		throw new UnsupportedOperationException("readonly");
	}
	@Override
	public void destroy() {
		throw new IllegalStateException("never link proxy object and call it's release");
	}
	@Override
	public void checkOrphan() {}
	
//	@Override
//	void onModelInternalEvent(ModelInternalEvent event) {}
	
	@Override
	public int getHeight() {
		loadProxy();
		if (_proxy != null) {
			return _proxy.getHeight();
		}
		return getSheet().getDefaultRowHeight();
	}

	@Override
	public boolean isHidden() {
		loadProxy();
		if (_proxy != null) {
			return _proxy.isHidden();
		}
		return false;
	}

	@Override
	public void setHeight(int width) {
		loadProxy();
		if (_proxy == null) {
			_proxy = (AbstractRowAdv)((AbstractSheetAdv)getSheet()).getOrCreateRow(_index);
		}
		_proxy.setHeight(width);
	}

	@Override
	public void setHidden(boolean hidden) {
		loadProxy();
		if (_proxy == null) {
			_proxy = (AbstractRowAdv)((AbstractSheetAdv)getSheet()).getOrCreateRow(_index);
		}
		_proxy.setHidden(hidden);
	}
	
	@Override
	public boolean isCustomHeight() {
		loadProxy();
		if (_proxy != null) {
			return _proxy.isCustomHeight();
		}
		return false;
	}
	@Override
	public void setCustomHeight(boolean custom) {
		loadProxy();
		_proxy.setCustomHeight(custom);
	}
	@Override
	public Iterator<SCell> getCellIterator(boolean reverse) {
		loadProxy();
		if (_proxy != null) {
			return _proxy.getCellIterator(reverse);
		}
		return Collections.EMPTY_LIST.iterator();
	}
	@Override
	void setIndex(int newidx) {
		throw new UnsupportedOperationException("readonly");
	}
	@Override
	void moveCellTo(AbstractRowAdv target, int start, int end, int offset) {
		throw new UnsupportedOperationException("readonly");
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Row:").append(getIndex());
		return sb.toString();
	}
	
	//ZSS-688
	//@since 3.6.0
	/*package*/ AbstractRowAdv cloneRow(AbstractSheetAdv sheet) {
		if (_proxy == null) {
			return new 	RowProxy(sheet, this._index);
		} else {
			return _proxy.cloneRow(sheet);
		}
	}
	
	public Iterator<SCell> getCellIterator(boolean reverse, int start, int end) {
		loadProxy();
		if (_proxy != null) {
			return _proxy.getCellIterator(reverse, start, end);
		}
		return Collections.EMPTY_LIST.iterator();
	}
	@Override
	public Iterator<SCell> getCellIterator() {
		return getCellIterator(false);
	}
}
