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

package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;

/**
 * zss-576: enhanced protection.
 * 
 * Very wired way to present "checked" in sheet protection dialog in xlsx file.
 * 
 * + sheet protected:
 *   + selectLockedCells: NA or false -> checked; true -> unchecked
 *   + selectUnlockedCells: NA or false -> checked; true -> unchecked
 *   + scenarios: NA or false -> checked; true -> unchecked
 *   + objects: NA or false -> checked; true -> unchecked
 *   + other Xxx: false -> checked; NA or true -> unchecked 
 *   
 * + sheet unprotected:
 *   + selectLockedCells: NA or false -> checked; true -> unchecked
 *   + selectUnlockedCells: NA or false -> checked; true -> unchecked
 *   + scenarios: NA, true, or false -> unchecked
 *   + objects: NA, true, or false -> unchecked
 *   + other Xxx: false -> checked; NA or true -> unchecked 
 * 
 * @author henri
 *
 */
public class XSSFSheetProtection implements org.zkoss.poi.ss.usermodel.SheetProtection {
	private CTSheetProtection _ctp;
	protected XSSFSheetProtection(CTSheetProtection protection) {
		_ctp = protection; 
	}
	
    /**
     * @return true when Autofilters are locked and the sheet is protected.
     */
    public boolean isAutoFilter() {
    	return _ctp.isSetAutoFilter() && !_ctp.getAutoFilter();
    }

    /**
     * @return true when Deleting columns is locked and the sheet is protected.
     */
    public boolean isDeleteColumns() {
    	return _ctp.isSetDeleteColumns() && !_ctp.getDeleteColumns();
    }

    /**
     * @return true when Deleting rows is locked and the sheet is protected.
     */
    public boolean isDeleteRows() {
    	return _ctp.isSetDeleteRows() && !_ctp.getDeleteRows();
    }

    /**
     * @return true when Formatting cells is locked and the sheet is protected.
     */
    public boolean isFormatCells() {
    	return _ctp.isSetFormatCells() && !_ctp.getFormatCells();
    }

    /**
     * @return true when Formatting columns is locked and the sheet is protected.
     */
    public boolean isFormatColumns() {
    	return _ctp.isSetFormatColumns() && !_ctp.getFormatColumns();
    }

    /**
     * @return true when Formatting rows is locked and the sheet is protected.
     */
    public boolean isFormatRows() {
    	return _ctp.isSetFormatRows() && !_ctp.getFormatRows();
    }

    /**
     * @return true when Inserting columns is locked and the sheet is protected.
     */
    public boolean isInsertColumns() {
    	return _ctp.isSetInsertColumns() && !_ctp.getInsertColumns();
    }

    /**
     * @return true when Inserting hyperlinks is locked and the sheet is protected.
     */
    public boolean isInsertHyperlinks() {
    	return _ctp.isSetInsertHyperlinks() && !_ctp.getInsertHyperlinks();
    }

    /**
     * @return true when Inserting rows is locked and the sheet is protected.
     */
    public boolean isInsertRows() {
    	return _ctp.isSetInsertRows() && !_ctp.getInsertRows();
    }

    /**
     * @return true when Pivot tables are locked and the sheet is protected.
     */
    public boolean isPivotTables() {
    	return _ctp.isSetPivotTables() && !_ctp.getPivotTables();
    }

    /**
     * @return true when Sorting is locked and the sheet is protected.
     */
    public boolean isSort() {
    	return _ctp.isSetSort() && !_ctp.getSort();
    }

    /**
     * @return true when Objects are locked and the sheet is protected.
     */
    public boolean isObjects() {
    	return _ctp.isSetSheet() && _ctp.getSheet() && (!_ctp.isSetObjects() || !_ctp.getObjects()); //ZSS-679
    }

    /**
     * @return true when Scenarios are locked and the sheet is protected.
     */
    public boolean isScenarios() {
    	return _ctp.isSetSheet() && _ctp.getSheet() && (!_ctp.isSetScenarios() || !_ctp.getScenarios()); //ZSS-679
    }

    /**
     * @return true when Selection of locked cells is locked and the sheet is protected.
     */
    public boolean isSelectLockedCells() {
    	return !_ctp.isSetSelectLockedCells() || !_ctp.getSelectLockedCells();
    }

    /**
     * @return true when Selection of unlocked cells is locked and the sheet is protected.
     */
    public boolean isSelectUnlockedCells() {
    	return !_ctp.isSetSelectUnlockedCells() || !_ctp.getSelectUnlockedCells();
    }
    
    /**
     * Enable Autofilters locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setAutoFilter(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetAutoFilter()) _ctp.unsetAutoFilter();
    	} else {
    		_ctp.setAutoFilter(false);
    	}
    }

    /**
     * Enable Deleting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setDeleteColumns(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetDeleteColumns()) _ctp.unsetDeleteColumns();
    	} else {
    		_ctp.setDeleteColumns(false);
    	}
    }

    /**
     * Enable Deleting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setDeleteRows(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetDeleteRows()) _ctp.unsetDeleteRows();
    	} else {
    		_ctp.setDeleteRows(false);
    	}
    }
    
    /**
     * Enable Formatting cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatCells(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetFormatCells()) _ctp.unsetFormatCells();
    	} else { 
    		_ctp.setFormatCells(false);
    	}
    }

    /**
     * Enable Formatting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatColumns(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetFormatColumns()) _ctp.unsetFormatColumns();
    	} else {
    		_ctp.setFormatColumns(false);
    	}
    }

    /**
     * Enable Formatting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatRows(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetFormatRows()) _ctp.unsetFormatRows();
    	} else {
    		_ctp.setFormatRows(false);
    	}
    }

    /**
     * Enable Inserting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertColumns(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetInsertColumns()) _ctp.unsetInsertColumns();
    	} else {
    		_ctp.setInsertColumns(false);
    	}
    }

    /**
     * Enable Inserting hyperlinks locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertHyperlinks(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetInsertHyperlinks()) _ctp.unsetInsertHyperlinks();
    	} else {
    		_ctp.setInsertHyperlinks(false);
    	}
    }

    /**
     * Enable Inserting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertRows(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetInsertRows()) _ctp.unsetInsertRows();
    	} else {
    		_ctp.setInsertRows(false);
    	}
    }

    /**
     * Enable Pivot Tables locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setPivotTables(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetPivotTables()) _ctp.unsetPivotTables();
    	} else {
    		_ctp.setPivotTables(false);
    	}
    }

    /**
     * Enable Sort locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSort(boolean flag) {
    	if (!flag) {
    		if (_ctp.isSetSort()) _ctp.unsetSort();
    	} else {
    		_ctp.setSort(false);
    	}
    }

    /**
     * Enable Objects locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setObjects(boolean flag) {
    	if (!flag) {
    		_ctp.setObjects(true);
    	} else {
    		if (_ctp.isSetObjects()) _ctp.unsetObjects();
    	}
    }

    /**
     * Enable Scenarios locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setScenarios(boolean flag) {
    	if (!flag) {
    		_ctp.setScenarios(true);
    	} else {
    		if (_ctp.isSetScenarios()) _ctp.unsetScenarios();
    	}
    }
    
    /**
     * Enable Selection of locked cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSelectLockedCells(boolean flag) {
    	if (!flag) {
    		_ctp.setSelectLockedCells(true);
    	} else {
    		if (_ctp.isSetSelectLockedCells()) _ctp.unsetSelectLockedCells();
    	}
    }

    /**
     * Enable Selection of unlocked cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSelectUnlockedCells(boolean flag) {
    	if (!flag) {
    		_ctp.setSelectUnlockedCells(true);
    	} else {
    		if (_ctp.isSetSelectUnlockedCells()) _ctp.unsetSelectUnlockedCells();
    	}
    }
}
