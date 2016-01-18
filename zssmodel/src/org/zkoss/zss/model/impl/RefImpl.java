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

import java.io.Serializable;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class RefImpl implements Ref, Serializable {

	private static final long serialVersionUID = 1L;
	final private RefType _type;
	final protected String bookName;
	final protected String sheetName;
	final protected String lastSheetName;
	final private int _row;
	final private int _column;
	final private int _lastRow;
	final private int _lastColumn;
	private int _sheetIdx = -1; // tricky! used in IntervalTree only!

	public RefImpl(String bookName, String sheetName, int row, int column,
			int lastRow, int lastColumn) {
		this((row==lastRow&&column==lastColumn)?RefType.CELL:RefType.AREA, bookName, sheetName, null, row, column, lastRow,lastColumn);
	}

	public RefImpl(String bookName, String sheetName, int row, int column) {
		this(RefType.CELL, bookName, sheetName, null, row, column, row, column);
	}

	public RefImpl(String bookName, String sheetName, String lastSheetName, int row, int column,
			int lastRow, int lastColumn) {
		this((row==lastRow&&column==lastColumn)?RefType.CELL:RefType.AREA, bookName, sheetName, lastSheetName, row, column, lastRow,lastColumn);
	}

	public RefImpl(String bookName, String sheetName, String lastSheetName, int row, int column) {
		this(RefType.CELL, bookName, sheetName, lastSheetName, row, column, row, column);
	}

	public RefImpl(String bookName, String sheetName, int sheetIdx) {
		this(RefType.SHEET, bookName, sheetName, null, -1, -1, -1, -1);
		this._sheetIdx = sheetIdx;
	}

	public RefImpl(String bookName) {
		this(RefType.BOOK, bookName, null, null, -1, -1, -1, -1);
	}

	public RefImpl(AbstractCellAdv cell) {
		this(RefType.CELL, cell.getSheet().getBook().getBookName(), cell.getSheet().getSheetName(), null, cell.getRowIndex(),
		cell.getColumnIndex(), cell.getRowIndex(), cell.getColumnIndex());
	}

	public RefImpl(AbstractSheetAdv sheet, int sheetIdx) {
		this(RefType.SHEET, ((AbstractBookAdv) sheet.getBook()).getBookName(), sheet.getSheetName(), null, -1, -1, -1, -1);
		this._sheetIdx = sheetIdx;
	}

	public RefImpl(AbstractBookAdv book) {
		this(RefType.BOOK, book.getBookName(), null, null, -1, -1, -1, -1);
	}

	protected RefImpl(RefType type, String bookName, String sheetName, String lastSheetName,
			int row, int column, int lastRow, int lastColumn) {
		this._type = type;
		this.bookName = bookName;
		this.sheetName = sheetName;
		this.lastSheetName = lastSheetName;
		this._row = row;
		this._column = column;
		this._lastRow = lastRow;
		this._lastColumn = lastColumn;
	}

	@Override
	public RefType getType() {
		return _type;
	}

	@Override
	public String getBookName() {
		return bookName;
	}

	@Override
	public String getSheetName() {
		return sheetName;
	}
	
	@Override
	public String getLastSheetName() {
		return lastSheetName;
	}

	@Override
	public int getRow() {
		return _row;
	}

	@Override
	public int getColumn() {
		return _column;
	}

	@Override
	public int getLastRow() {
		return _lastRow;
	}

	@Override
	public int getLastColumn() {
		return _lastColumn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bookName == null) ? 0 : bookName.hashCode());
		result = prime * result + _column;
		result = prime * result + _lastColumn;
		result = prime * result + _lastRow;
		result = prime * result + _row;
		result = prime * result
				+ ((sheetName == null) ? 0 : sheetName.hashCode());
		result = prime * result
				+ ((lastSheetName == null) ? 0 : lastSheetName.hashCode());
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Ref))
			return false;
		Ref other = (Ref) obj;
		
		if (bookName == null) {
			if (other.getBookName() != null)
				return false;
		} else if (!bookName.equals(other.getBookName()))
			return false;
		if (_column != other.getColumn())
			return false;
		if (_lastColumn != other.getLastColumn())
			return false;
		if (_lastRow != other.getLastRow())
			return false;
		if (_row != other.getRow())
			return false;
		if (sheetName == null) {
			if (other.getSheetName() != null)
				return false;
		} else if (!sheetName.equals(other.getSheetName()))
			return false;
		if (lastSheetName == null) {
			if (other.getLastSheetName() != null)
				return false;
		} else if (!lastSheetName.equals(other.getLastSheetName()))
			return false;
		if (_type != other.getType())
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		switch (_type) {
		case AREA:
			sb.insert(0,":"+ new CellRegion(_lastRow, _lastColumn).getReferenceString());
		case CELL:
			sb.insert(0, new CellRegion(_row, _column).getReferenceString());
		case SHEET:
			if(lastSheetName != null) {
				sb.insert(0, sheetName + ":" + lastSheetName + "!");
			} else {
				sb.insert(0, sheetName + "!");
			}
			break;
		case OBJECT://will be override
			if(lastSheetName!=null){
				sb.insert(0, sheetName + ":" + lastSheetName + "!");
			}else if(sheetName!=null){
				sb.insert(0, sheetName + "!");
			}
		case NAME://will be override
		case BOOK:
		case INDIRECT:
		case TABLE: //ZSS-960
		}

		sb.insert(0, bookName + ":");
		return sb.toString();
	}

	//Tricky! This is for IntervalTree only
	@Override
	public int getSheetIndex() {
		return _sheetIdx;
	}
	
	//Tricky! This is for IntervalTree only
	@Override
	public int getLastSheetIndex() {
		return -1;
	}
}
