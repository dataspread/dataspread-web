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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class RowImpl extends AbstractRowAdv {
	private static final long serialVersionUID = 1L;

	private AbstractSheetAdv _sheet;
	private int _index;
	
	private final IndexPool<SCell> cells = new IndexPool<SCell>(){
		private static final long serialVersionUID = 1L;

		@Override
		void resetIndex(int newidx, SCell obj) {
			((AbstractCellAdv)obj).setIndex(newidx);
		}};

	private AbstractCellStyleAdv cellStyle;
	
	
	private Integer height;
	private boolean hidden = false;
	private boolean customHeight = false;

	public RowImpl(AbstractSheetAdv sheet, int index) {
		this._sheet = sheet;
		this._index = index;
	}

	@Override
	public SSheet getSheet() {
		checkOrphan();
		return _sheet;
	}

	@Override
	public int getIndex() {
		checkOrphan();
		return _index;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public AbstractCellAdv getCell(int columnIdx, boolean proxy) {
		AbstractCellAdv cellObj = (AbstractCellAdv) cells.get(columnIdx);
		if (cellObj != null) {
			return cellObj;
		}
		checkOrphan();
		return proxy ? new CellProxy(_sheet, getIndex(), columnIdx) : null;
	}

	@Override
	AbstractCellAdv getOrCreateCell(int columnIdx) {
		AbstractCellAdv cellObj = (AbstractCellAdv) cells.get(columnIdx);
		if (cellObj == null) {
			checkOrphan();
			if(columnIdx > getSheet().getBook().getMaxColumnIndex()){
				throw new IllegalStateException("can't create the cell that exceeds max column size "+getSheet().getBook().getMaxColumnIndex());
			}
			cellObj = new CellImpl(this, columnIdx);
			cells.put(columnIdx, cellObj);
		}
		return cellObj;
	}

	@Override
	public int getStartCellIndex() {
		return cells.firstKey();
	}

	@Override
	public int getEndCellIndex() {
		return cells.lastKey();
	}

	@Override
	public void clearCell(int start, int end) {
		// clear before move relation
		for (SCell cell : cells.subValues(start, end)) {
			((AbstractCellAdv) cell).destroy();
		}
		cells.clear(start, end);
	}

	@Override
	public void insertCell(int cellIdx, int size) {
		if (size <= 0)
			return;
		
		cells.insert(cellIdx, size);
		
		//destroy the cell that exceeds the max size
		int maxSize = getSheet().getBook().getMaxColumnSize();
		Collection<SCell> exceeds = new ArrayList<SCell>(cells.subValues(maxSize, Integer.MAX_VALUE));
		if(exceeds.size()>0){
			cells.trim(maxSize);
		}
		for(SCell cell:exceeds){
			((AbstractCellAdv) cell).destroy();
		}
	}

	@Override
	public void deleteCell(int cellIdx, int size) {
		if (size <= 0)
			return;
		
		// clear before move relation
		for (SCell cell : cells.subValues(cellIdx, cellIdx + size - 1)) {
			((AbstractCellAdv) cell).destroy();
		}

		cells.delete(cellIdx, size);
	}

	@Override
	public void checkOrphan() {
		if (_sheet == null) {
			throw new IllegalStateException("doesn't connect to parent");
		}
	}

	@Override
	public void destroy() {
		checkOrphan();
		for (SCell cell : cells.values()) {
			((AbstractCellAdv) cell).destroy();
		}
		_sheet = null;
	}

	@Override
	public SCellStyle getCellStyle() {
		return getCellStyle(false);
	}

	@Override
	public SCellStyle getCellStyle(boolean local) {
		if (local || cellStyle != null) {
			return cellStyle;
		}
		checkOrphan();
		return _sheet.getBook().getDefaultCellStyle();
	}

	@Override
	public void setCellStyle(SCellStyle cellStyle) {
		Validations.argInstance(cellStyle, CellStyleImpl.class);
		this.cellStyle = (CellStyleImpl) cellStyle;
	}

	@Override
	public int getHeight() {
		if(height!=null){
			return height.intValue();
		}
		checkOrphan();
		return getSheet().getDefaultRowHeight();
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public void setHeight(int height) {
		this.height = Integer.valueOf(height);
	}

	@Override
	public void setHidden(boolean hidden) {
		if (this.hidden == hidden) return;
		
		this.hidden = hidden;
		
		//ZSS-988: Subtotal(1xx, range) depends on hidden/unhidden rows/columns
		final SBook book = _sheet.getBook();
		//ZSS-1007
		final int col1 = getStartCellIndex();
		final int col2 = getEndCellIndex();
		if (col1 >= 0 && col2 >= 0) { //handle dependency if with data
			//ZSS-1047: (side-effect of ZSS-988 and ZSS-1007 which consider setHidden() of SUBTOTAL() function)
			final boolean includePrecedent = false;
			ModelUpdateUtil.handlePrecedentUpdate(book.getBookSeries(),
					new RefImpl(book.getBookName(), _sheet.getSheetName(), getIndex(),
							col1, getIndex(), col2), includePrecedent);
		}
	}
	
	@Override
	public boolean isCustomHeight() {
		return customHeight;
	}
	@Override
	public void setCustomHeight(boolean custom) {
		customHeight = custom;
	}

	@Override
	public Iterator<SCell> getCellIterator(boolean reverse) {
		return Collections.unmodifiableCollection(reverse?cells.descendingValues():cells.values()).iterator();
	}

	@Override
	public Iterator<SCell> getCellIterator(boolean reverse, int start, int end) {
		return Collections.unmodifiableCollection(reverse?cells.descendingSubValues(start, end):cells.subValues(start, end)).iterator();
	}


	@Override
	void setIndex(int newidx) {
		int oldIdx = _index;
		this._index = newidx;
		for(SCell cell:cells.values()){
			((AbstractCellAdv) cell).setRow(oldIdx,this);//set this row again to trigger rebuildFormulaDependency
		}
	}

	@Override
	void moveCellTo(AbstractRowAdv target, int start, int end, int offset) {
		if(!(target instanceof RowImpl)){
			throw new IllegalStateException("not RowImpl, is "+target);
		}
		if(getSheet()!=target.getSheet()){
			throw new IllegalStateException("not in the same sheet");
		}
		
		if(target!=this){
			//clear the cell in different target range first
			Collection<SCell> toReplace = ((RowImpl)target).cells.clear(start+offset, end+offset);
			for(SCell cell:toReplace){
				((AbstractCellAdv) cell).destroy();
			}
		}
		
		Collection<SCell> toMove = cells.clear(start, end);
		int oldRowIdx = getIndex();
		for(SCell cell:toMove){
			int newidx = cell.getColumnIndex()+offset;
			SCell old = ((RowImpl)target).cells.put(newidx, cell);
			((AbstractCellAdv) cell).setIndex(newidx);
			((AbstractCellAdv) cell).setRow(oldRowIdx,target);
			if(old!=null){
				((AbstractCellAdv) old).destroy();
			}
		}
		
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Row:").append(getIndex()).append(cells.keySet());
		return sb.toString();
	}
	
	//ZSS-688
	/*package*/ AbstractRowAdv cloneRow(AbstractSheetAdv sheet) {
		final RowImpl tgt = new RowImpl(sheet, this._index);
		
		for (SCell cell : this.cells.values()) {
			tgt.cells.put(cell.getColumnIndex(), ((CellImpl)cell).cloneCell(tgt));
		}

		tgt.cellStyle = this.cellStyle;
		tgt.height = this.height;
		tgt.hidden = this.hidden;
		tgt.customHeight = this.customHeight;
		
		return tgt;
	}
	
	//ZSS-816
	@Override
	public Iterator<SCell> getCellIterator() {
		return Collections.unmodifiableCollection(cells.values()).iterator();
	}
}
