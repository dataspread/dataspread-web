/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 17, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SSheetProtection;

/**
 * @author henri
 *
 */
public class SheetProtectionImpl implements SSheetProtection, Serializable {
	private static final long serialVersionUID = -4821001553145539929L;
	// bit position match xls native record so don't change them.
	private static final int OBJECTS        = 0x01;
	private static final int SCENARIOS 		= 0x02;
	private static final int FORMAT_CELLS 	= 0x04;
	private static final int FORMAT_COLUMNS = 0x08;
	
	private static final int FORMAT_ROWS 	= 0x10;
	private static final int INSERT_COLUMNS	= 0x20;
	private static final int INSERT_ROWS	= 0x40;
	private static final int INSERT_HYPERLINKS	= 0x80;
	 
	private static final int DELETE_COLUMNS	= 0x100;
	private static final int DELETE_ROWS	= 0x200;
	private static final int SEL_LOCKED_CELLS	= 0x400;
	private static final int SORT			= 0x800;
		
	private static final int AUTO_FILTER	= 0x1000;
	private static final int PIVOT_TABLES 	= 0x2000;
	private static final int SEL_UNLOCKED_CELLS = 0x4000;
	
	int _bits;
	
	protected SheetProtectionImpl() {
		_bits = SEL_UNLOCKED_CELLS | SEL_LOCKED_CELLS;
	}
	
	/**
	 * get whether linked objects or embedded objects can be edited.
	 * @return whether linked objects or embedded objects can be edited.
	 */
	public boolean isObjects() {
	    return (OBJECTS & _bits) != 0;
	}
	
	/**
	 * get whether scenarios can be edited.
	 */
	public boolean isScenarios() {
		return (SCENARIOS & _bits) != 0;
	}
	
	/**
	 * get whether cells can be formatted.
	 */
	public boolean isFormatCells() {
		return (FORMAT_CELLS & _bits) != 0;
	}
	
	/**
	 * get whether columns can be formatted.
	 */
	public boolean isFormatColumns() {
		return (FORMAT_COLUMNS & _bits) != 0;
	}
	
	/**
	 * get whether rows can be formatted.
	 */
	public boolean isFormatRows() {
		return (FORMAT_ROWS & _bits) != 0;
	}
	
	/**
	 * get whether columns can be inserted.
	 */
	public boolean isInsertColumns() {
		return (INSERT_COLUMNS & _bits) != 0;
	}
	
	/**
	 * get whether rows can be inserted.
	 */
	public boolean isInsertRows() {
		return (INSERT_ROWS & _bits) != 0;
	}
	
	/**
	 * get whether hyperlinks can be inserted. 
	 */
	public boolean isInsertHyperlinks() {
		return (INSERT_HYPERLINKS & _bits) != 0;
	}
	
	/**
	 * get whether columns can be deleted.
	 */
	public boolean isDeleteColumns() {
		return (DELETE_COLUMNS & _bits) != 0;
	}
	
	/**
	 * get whether rows can be deleted. 
	 */
	public boolean isDeleteRows() {
		return (DELETE_ROWS & _bits) != 0;
	}
	
	/**
	 * get whether locked cells can be selected.
	 */
	public boolean isSelectLockedCells() {
		return (SEL_LOCKED_CELLS & _bits) != 0;
	}
	
	/**
	 * get whether cells can be sorted.
	 */
	public boolean isSort() {
		return (SORT & _bits) != 0;
	}
	
	/**
	 * get whether cells can be filtered.
	 */
	public boolean isAutoFilter() {
		return (AUTO_FILTER & _bits) != 0;
	}
	
	/**
	 * get whether PivotTable reports ccan be created or modified. 
	 */
	public boolean isPivotTables() {
		return (PIVOT_TABLES & _bits) != 0;
	}
	
	/**
	 * get whether unlocked cells can be selected.
	 */
	public boolean isSelectUnlockedCells() {
		return (SEL_UNLOCKED_CELLS & _bits) != 0;
	}
	
	/**
	 * set whether linked objects or embedded objects can be edited.
	 */
	public void setObjects(boolean flag) {
	    _bits = setBoolean(OBJECTS, _bits, flag);
	}
	
	/**
	 * set whether scenarios can be edited.
	 */
	public void setScenarios(boolean flag) {
		_bits = setBoolean(SCENARIOS, _bits, flag);
	}
	
	/**
	 * set whether cells can be formatted.
	 */
	public void setFormatCells(boolean flag) {
		_bits = setBoolean(FORMAT_CELLS, _bits, flag);
	}
	
	/**
	 * set whether columns can be formatted.
	 */
	public void setFormatColumns(boolean flag) {
		_bits = setBoolean(FORMAT_COLUMNS, _bits, flag);
	}
	
	/**
	 * set whether rows can be formatted.
	 */
	public void setFormatRows(boolean flag) {
		_bits = setBoolean(FORMAT_ROWS, _bits, flag);
	}
	
	/**
	 * set whether columns can be inserted.
	 */
	public void setInsertColumns(boolean flag) {
		_bits = setBoolean(INSERT_COLUMNS, _bits, flag);
	}
	
	/**
	 * set whether rows can be inserted.
	 */
	public void setInsertRows(boolean flag) {
		_bits = setBoolean(INSERT_ROWS, _bits, flag);
	}
	
	/**
	 * set whether hyperlinks can be inserted. 
	 */
	public void setInsertHyperlinks(boolean flag) {
		_bits = setBoolean(INSERT_HYPERLINKS, _bits, flag);
	}
	
	/**
	 * set whether columns can be deleted.
	 */
	public void setDeleteColumns(boolean flag) {
		_bits = setBoolean(DELETE_COLUMNS, _bits, flag);
	}
	
	/**
	 * set whether rows can be deleted. 
	 */
	public void setDeleteRows(boolean flag) {
		_bits = setBoolean(DELETE_ROWS, _bits, flag);
	}
	
	/**
	 * set whether locked cells can be selected.
	 */
	public void setSelectLockedCells(boolean flag) {
		_bits = setBoolean(SEL_LOCKED_CELLS, _bits, flag);
	}
	
	/**
	 * set whether cells can be sorted.
	 */
	public void setSort(boolean flag) {
		_bits = setBoolean(SORT, _bits, flag);
	}
	
	/**
	 * set whether cells can be filtered.
	 */
	public void setAutoFilter(boolean flag) {
		_bits = setBoolean(AUTO_FILTER, _bits, flag);
	}
	
	/**
	 * set whether PivotTable reports ccan be created or modified. 
	 */
	public void setPivotTables(boolean flag) {
		_bits = setBoolean(PIVOT_TABLES, _bits, flag);
	}
	
	/**
	 * set whether unlocked cells can be selected.
	 */
	public void setSelectUnlockedCells(boolean flag) {
		_bits =  setBoolean(SEL_UNLOCKED_CELLS, _bits, flag);
	}
	
	//utility to set/reset a bit
	final private int setBoolean(int bit, int bits, boolean flag) {
		return flag ? (bits | bit) : (bits & ~bit);
	}

	//ZSS-688
	//@Since 3.6.0
	/*package*/ SheetProtectionImpl cloneSheetProtectionImpl() {
		final SheetProtectionImpl tgt = new SheetProtectionImpl();
		tgt._bits = this._bits;
		return tgt;
	}
	
}
