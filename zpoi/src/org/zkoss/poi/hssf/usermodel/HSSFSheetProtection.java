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

package org.zkoss.poi.hssf.usermodel;

import org.zkoss.poi.hssf.record.EnhancedProtection;

/**
 * zss-576: enhanced protection
 * 
 * @author henri
 *
 */
public class HSSFSheetProtection implements org.zkoss.poi.ss.usermodel.SheetProtection {
	EnhancedProtection _enhancedProtection;
	
	HSSFSheetProtection(EnhancedProtection protection) {
		_enhancedProtection = protection;
	}
	
    /**
     * @return true when Autofilters are locked and the sheet is protected.
     */
    public boolean isAutoFilter() {
    	return _enhancedProtection.isAutoFilter();
    }

    /**
     * @return true when Deleting columns is locked and the sheet is protected.
     */
    public boolean isDeleteColumns() {
    	return _enhancedProtection.isDeleteColumns();
    }

    /**
     * @return true when Deleting rows is locked and the sheet is protected.
     */
    public boolean isDeleteRows() {
    	return _enhancedProtection.isDeleteRows();
    }

    /**
     * @return true when Formatting cells is locked and the sheet is protected.
     */
    public boolean isFormatCells() {
    	return _enhancedProtection.isFormatCells();
    }

    /**
     * @return true when Formatting columns is locked and the sheet is protected.
     */
    public boolean isFormatColumns() {
    	return _enhancedProtection.isFormatColumns();
    }

    /**
     * @return true when Formatting rows is locked and the sheet is protected.
     */
    public boolean isFormatRows() {
    	return _enhancedProtection.isFormatRows();
    }

    /**
     * @return true when Inserting columns is locked and the sheet is protected.
     */
    public boolean isInsertColumns() {
    	return _enhancedProtection.isInsertColumns();
    }

    /**
     * @return true when Inserting hyperlinks is locked and the sheet is protected.
     */
    public boolean isInsertHyperlinks() {
    	return _enhancedProtection.isInsertHyperlinks();
    }

    /**
     * @return true when Inserting rows is locked and the sheet is protected.
     */
    public boolean isInsertRows() {
    	return _enhancedProtection.isInsertRows();
    }

    /**
     * @return true when Pivot tables are locked and the sheet is protected.
     */
    public boolean isPivotTables() {
    	return _enhancedProtection.isPivotTables();
    }

    /**
     * @return true when Sorting is locked and the sheet is protected.
     */
    public boolean isSort() {
    	return _enhancedProtection.isSort();
    }

    /**
     * @return true when Objects are locked and the sheet is protected.
     */
    public boolean isObjects() {
    	return _enhancedProtection.isObjects();
    }

    /**
     * @return true when Scenarios are locked and the sheet is protected.
     */
    public boolean isScenarios() {
    	return _enhancedProtection.isScenarios();
    }

    /**
     * @return true when Selection of locked cells is locked and the sheet is protected.
     */
    public boolean isSelectLockedCells() {
    	return _enhancedProtection.isSelectLockedCells();
    }

    /**
     * @return true when Selection of unlocked cells is locked and the sheet is protected.
     */
    public boolean isSelectUnlockedCells() {
    	return _enhancedProtection.isSelectUnlockedCells();
    }
    
    /**
     * Enable Autofilters locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setAutoFilter(boolean flag) {
    	_enhancedProtection.setAutoFilter(flag);
    }

    /**
     * Enable Deleting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setDeleteColumns(boolean flag) {
    	_enhancedProtection.setDeleteColumns(flag);
    }

    /**
     * Enable Deleting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setDeleteRows(boolean flag) {
    	_enhancedProtection.setDeleteRows(flag);
    }
    
    /**
     * Enable Formatting cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatCells(boolean flag) {
    	_enhancedProtection.setFormatCells(flag);
    }

    /**
     * Enable Formatting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatColumns(boolean flag) {
    	_enhancedProtection.setFormatColumns(flag);
    }

    /**
     * Enable Formatting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatRows(boolean flag) {
    	_enhancedProtection.setFormatRows(flag);
    }

    /**
     * Enable Inserting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertColumns(boolean flag) {
    	_enhancedProtection.setInsertColumns(flag);
    }

    /**
     * Enable Inserting hyperlinks locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertHyperlinks(boolean flag) {
    	_enhancedProtection.setInsertHyperlinks(flag);
    }

    /**
     * Enable Inserting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertRows(boolean flag) {
    	_enhancedProtection.setInsertRows(flag);
    }

    /**
     * Enable Pivot Tables locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setPivotTables(boolean flag) {
    	_enhancedProtection.setPivotTables(flag);
    }

    /**
     * Enable Sort locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSort(boolean flag) {
    	_enhancedProtection.setSort(flag);
    }

    /**
     * Enable Objects locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setObjects(boolean flag) {
    	_enhancedProtection.setObjects(flag);
    }

    /**
     * Enable Scenarios locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setScenarios(boolean flag) {
    	_enhancedProtection.setScenarios(flag);
    }
    
    /**
     * Enable Selection of locked cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSelectLockedCells(boolean flag) {
    	_enhancedProtection.setSelectLockedCells(flag);
    }

    /**
     * Enable Selection of unlocked cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSelectUnlockedCells(boolean flag) {
    	_enhancedProtection.setSelectUnlockedCells(flag);
    }
}
