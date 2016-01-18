/* EnhancedProtection.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/04/09, Created by Henri Chen
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.poi.hssf.record;

import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.LittleEndianOutput;

//20140409, henrichen: EnhancedProtection
// A kind of {@link FeatHdrRecord}. 
// [MS-XLS].pdf. p678
final public class EnhancedProtection {
	
	// bitfields for SHAREDFEATURES_ISFPROTECTION
	private static final BitField protObject         	= BitFieldFactory.getInstance(0x01);
	private static final BitField protScenarios 		= BitFieldFactory.getInstance(0x02);
	private static final BitField protFormatCells 		= BitFieldFactory.getInstance(0x04);
	private static final BitField protFormatColumns 	= BitFieldFactory.getInstance(0x08);
	
	private static final BitField protFormatRows 		= BitFieldFactory.getInstance(0x10);
	private static final BitField protInsertColumns	= BitFieldFactory.getInstance(0x20);
	private static final BitField protInsertRows		= BitFieldFactory.getInstance(0x40);
	private static final BitField protInsertHyperlinks	= BitFieldFactory.getInstance(0x80);
	 
	private static final BitField protDeleteColumns	= BitFieldFactory.getInstance(0x100);
	private static final BitField protDeleteRows		= BitFieldFactory.getInstance(0x200);
	private static final BitField protSelLockedCells	= BitFieldFactory.getInstance(0x400);
	private static final BitField protSort				= BitFieldFactory.getInstance(0x800);
		
	private static final BitField protAutoFilter		= BitFieldFactory.getInstance(0x1000);
	private static final BitField protPivotTables 		= BitFieldFactory.getInstance(0x2000);
	private static final BitField protSelUnlockedCells	= BitFieldFactory.getInstance(0x4000);
	// bit 15 ~ bit 32 reserved

	int _bits;
	 
	EnhancedProtection() {
		setSelectLockedCells(true);
		setSelectUnlockedCells(true);
		
		//20140806, henrichen@zkoss.org: Experiment with Excel 2007.
		//A new blank sheet will always set these 4 bits to true.
		setObjects(true);
		setScenarios(true);
	}
 
	EnhancedProtection(RecordInputStream in) {
		_bits = in.readInt();
	}

	/**
	 * get whether linked objects or embedded objects can be edited.
	 * @return whether linked objects or embedded objects can be edited.
	 */
	public boolean isObjects() {
	    return protObject.isSet(_bits);
	}
	
	/**
	 * get whether scenarios can be edited.
	 */
	public boolean isScenarios() {
		return protScenarios.isSet(_bits);
	}
	
	/**
	 * get whether cells can be formatted.
	 */
	public boolean isFormatCells() {
		return protFormatCells.isSet(_bits);
	}
	
	/**
	 * get whether columns can be formatted.
	 */
	public boolean isFormatColumns() {
		return protFormatColumns.isSet(_bits);
	}
	
	/**
	 * get whether rows can be formatted.
	 */
	public boolean isFormatRows() {
		return protFormatRows.isSet(_bits);
	}
	
	/**
	 * get whether columns can be inserted.
	 */
	public boolean isInsertColumns() {
		return protInsertColumns.isSet(_bits);
	}
	
	/**
	 * get whether rows can be inserted.
	 */
	public boolean isInsertRows() {
		return protInsertRows.isSet(_bits);
	}
	
	/**
	 * get whether hyperlinks can be inserted. 
	 */
	public boolean isInsertHyperlinks() {
		return protInsertHyperlinks.isSet(_bits);
	}
	
	/**
	 * get whether columns can be deleted.
	 */
	public boolean isDeleteColumns() {
		return protDeleteColumns.isSet(_bits);
	}
	
	/**
	 * get whether rows can be deleted. 
	 */
	public boolean isDeleteRows() {
		return protDeleteRows.isSet(_bits);
	}
	
	/**
	 * get whether locked cells can be selected.
	 */
	public boolean isSelectLockedCells() {
		return protSelLockedCells.isSet(_bits);
	}
	
	/**
	 * get whether cells can be sorted.
	 */
	public boolean isSort() {
		return protSort.isSet(_bits);
	}
	
	/**
	 * get whether cells can be filtered.
	 */
	public boolean isAutoFilter() {
		return protAutoFilter.isSet(_bits);
	}
	
	/**
	 * get whether PivotTable reports ccan be created or modified. 
	 */
	public boolean isPivotTables() {
		return protPivotTables.isSet(_bits);
	}
	
	/**
	 * get whether unlocked cells can be selected.
	 */
	public boolean isSelectUnlockedCells() {
		return  protSelUnlockedCells.isSet(_bits);
	}
	
	/**
	 * set whether linked objects or embedded objects can be edited.
	 */
	public void setObjects(boolean flag) {
	    _bits = protObject.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether scenarios can be edited.
	 */
	public void setScenarios(boolean flag) {
		_bits = protScenarios.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether cells can be formatted.
	 */
	public void setFormatCells(boolean flag) {
		_bits = protFormatCells.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether columns can be formatted.
	 */
	public void setFormatColumns(boolean flag) {
		_bits = protFormatColumns.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether rows can be formatted.
	 */
	public void setFormatRows(boolean flag) {
		_bits = protFormatRows.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether columns can be inserted.
	 */
	public void setInsertColumns(boolean flag) {
		_bits = protInsertColumns.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether rows can be inserted.
	 */
	public void setInsertRows(boolean flag) {
		_bits = protInsertRows.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether hyperlinks can be inserted. 
	 */
	public void setInsertHyperlinks(boolean flag) {
		_bits = protInsertHyperlinks.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether columns can be deleted.
	 */
	public void setDeleteColumns(boolean flag) {
		_bits = protDeleteColumns.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether rows can be deleted. 
	 */
	public void setDeleteRows(boolean flag) {
		_bits = protDeleteRows.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether locked cells can be selected.
	 */
	public void setSelectLockedCells(boolean flag) {
		_bits = protSelLockedCells.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether cells can be sorted.
	 */
	public void setSort(boolean flag) {
		_bits = protSort.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether cells can be filtered.
	 */
	public void setAutoFilter(boolean flag) {
		_bits = protAutoFilter.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether PivotTable reports ccan be created or modified. 
	 */
	public void setPivotTables(boolean flag) {
		_bits = protPivotTables.setBoolean(_bits, flag);
	}
	
	/**
	 * set whether unlocked cells can be selected.
	 */
	public void setSelectUnlockedCells(boolean flag) {
		_bits =  protSelUnlockedCells.setBoolean(_bits, flag);
	}
	
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("    .bits       = ")
	    	  .append(Integer.toHexString(_bits)).append("\n");
	    buffer.append("       .protObjects         = ").append(isObjects()).append("\n");
	    buffer.append("       .protScenarios       = ").append(isScenarios()).append("\n");
	    buffer.append("       .protFormatCells     = ").append(isFormatCells()).append("\n");
	    buffer.append("       .protFormatColumns   = ").append(isFormatColumns()).append("\n");
	    buffer.append("       .protFormatRows      = ").append(isFormatRows()).append("\n");
	    buffer.append("       .protInsertColumns   = ").append(isInsertColumns()).append("\n");
	    buffer.append("       .protInsertRows      = ").append(isInsertRows()).append("\n");
	    buffer.append("       .protInsertHyperlinks= ").append(isInsertHyperlinks()).append("\n");
	    buffer.append("       .protDeleteColumns   = ").append(isDeleteColumns()).append("\n");
	    buffer.append("       .protDeleteRows      = ").append(isDeleteRows()).append("\n");
	    buffer.append("       .protSelLockedCells  = ").append(isSelectLockedCells()).append("\n");
	    buffer.append("       .protSort            = ").append(isSort()).append("\n");
	    buffer.append("       .protAutoFilter      = ").append(isAutoFilter()).append("\n");
	    buffer.append("       .protPivotTables     = ").append(isPivotTables()).append("\n");
	    buffer.append("       .protSelUnlockedCells= ").append(isSelectUnlockedCells()).append("\n");
	    
	    return buffer.toString();
	}
 
	public void serialize(LittleEndianOutput out) {
		out.writeInt(_bits);
	}
}
