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

package org.zkoss.poi.ss.usermodel;

/**
 * Enhanced sheet protection.
 * @author henri
 *
 */
public interface SheetProtection {
    /**
     * @return true when Autofilters are locked and the sheet is protected.
     */
    public boolean isAutoFilter();

    /**
     * @return true when Deleting columns is locked and the sheet is protected.
     */
    public boolean isDeleteColumns();

    /**
     * @return true when Deleting rows is locked and the sheet is protected.
     */
    public boolean isDeleteRows();

    /**
     * @return true when Formatting cells is locked and the sheet is protected.
     */
    public boolean isFormatCells();

    /**
     * @return true when Formatting columns is locked and the sheet is protected.
     */
    public boolean isFormatColumns();

    /**
     * @return true when Formatting rows is locked and the sheet is protected.
     */
    public boolean isFormatRows();

    /**
     * @return true when Inserting columns is locked and the sheet is protected.
     */
    public boolean isInsertColumns();

    /**
     * @return true when Inserting hyperlinks is locked and the sheet is protected.
     */
    public boolean isInsertHyperlinks();

    /**
     * @return true when Inserting rows is locked and the sheet is protected.
     */
    public boolean isInsertRows();

    /**
     * @return true when Pivot tables are locked and the sheet is protected.
     */
    public boolean isPivotTables();

    /**
     * @return true when Sorting is locked and the sheet is protected.
     */
    public boolean isSort();

    /**
     * @return true when Objects are locked and the sheet is protected.
     */
    public boolean isObjects();

    /**
     * @return true when Scenarios are locked and the sheet is protected.
     */
    public boolean isScenarios();

    /**
     * @return true when Selection of locked cells is locked and the sheet is protected.
     */
    public boolean isSelectLockedCells();

    /**
     * @return true when Selection of unlocked cells is locked and the sheet is protected.
     */
    public boolean isSelectUnlockedCells();

    /**
     * Enable Autofilters locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setAutoFilter(boolean flag);

    /**
     * Enable Deleting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setDeleteColumns(boolean flag);

    /**
     * Enable Deleting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setDeleteRows(boolean flag);
    
    /**
     * Enable Formatting cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatCells(boolean flag);

    /**
     * Enable Formatting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatColumns(boolean flag);

    /**
     * Enable Formatting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setFormatRows(boolean flag);

    /**
     * Enable Inserting columns locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertColumns(boolean flag);

    /**
     * Enable Inserting hyperlinks locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertHyperlinks(boolean flag);

    /**
     * Enable Inserting rows locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setInsertRows(boolean flag);

    /**
     * Enable Pivot Tables locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setPivotTables(boolean flag);

    /**
     * Enable Sort locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSort(boolean flag);

    /**
     * Enable Objects locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setObjects(boolean flag);

    /**
     * Enable Scenarios locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setScenarios(boolean flag);
    
    /**
     * Enable Selection of locked cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSelectLockedCells(boolean flag);

    /**
     * Enable Selection of unlocked cells locking.
     * This does not modify sheet protection status.
     * To enforce this locking, call {@link #enableLocking()}
     */
    public void setSelectUnlockedCells(boolean flag);
}
