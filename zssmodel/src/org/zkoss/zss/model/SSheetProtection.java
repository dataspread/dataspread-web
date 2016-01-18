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

package org.zkoss.zss.model;

/**
 * @author henri
 *
 */
public interface SSheetProtection {
	/**
	 * get whether linked objects or embedded objects can be edited.
	 * @return whether linked objects or embedded objects can be edited.
	 */
	public boolean isObjects();
	
	/**
	 * get whether scenarios can be edited.
	 */
	public boolean isScenarios();
	
	/**
	 * get whether cells can be formatted.
	 */
	public boolean isFormatCells();
	
	/**
	 * get whether columns can be formatted.
	 */
	public boolean isFormatColumns();
	
	/**
	 * get whether rows can be formatted.
	 */
	public boolean isFormatRows();
	
	/**
	 * get whether columns can be inserted.
	 */
	public boolean isInsertColumns();
	
	/**
	 * get whether rows can be inserted.
	 */
	public boolean isInsertRows();
	
	/**
	 * get whether hyperlinks can be inserted. 
	 */
	public boolean isInsertHyperlinks();
	
	/**
	 * get whether columns can be deleted.
	 */
	public boolean isDeleteColumns();
	
	/**
	 * get whether rows can be deleted. 
	 */
	public boolean isDeleteRows();
	
	/**
	 * get whether locked cells can be selected.
	 */
	public boolean isSelectLockedCells();
	
	/**
	 * get whether cells can be sorted.
	 */
	public boolean isSort();
	
	/**
	 * get whether cells can be filtered.
	 */
	public boolean isAutoFilter();
	
	/**
	 * get whether PivotTable reports ccan be created or modified. 
	 */
	public boolean isPivotTables();
	
	/**
	 * get whether unlocked cells can be selected.
	 */
	public boolean isSelectUnlockedCells();
	
	/**
	 * set whether linked objects or embedded objects can be edited.
	 */
	public void setObjects(boolean flag);
	
	/**
	 * set whether scenarios can be edited.
	 */
	public void setScenarios(boolean flag);
	
	/**
	 * set whether cells can be formatted.
	 */
	public void setFormatCells(boolean flag);
	
	/**
	 * set whether columns can be formatted.
	 */
	public void setFormatColumns(boolean flag);
	
	/**
	 * set whether rows can be formatted.
	 */
	public void setFormatRows(boolean flag);
	
	/**
	 * set whether columns can be inserted.
	 */
	public void setInsertColumns(boolean flag);
	
	/**
	 * set whether rows can be inserted.
	 */
	public void setInsertRows(boolean flag);
	
	/**
	 * set whether hyperlinks can be inserted. 
	 */
	public void setInsertHyperlinks(boolean flag);
	
	/**
	 * set whether columns can be deleted.
	 */
	public void setDeleteColumns(boolean flag);
	
	/**
	 * set whether rows can be deleted. 
	 */
	public void setDeleteRows(boolean flag);
	
	/**
	 * set whether locked cells can be selected.
	 */
	public void setSelectLockedCells(boolean flag);
	
	/**
	 * set whether cells can be sorted.
	 */
	public void setSort(boolean flag);
	
	/**
	 * set whether cells can be filtered.
	 */
	public void setAutoFilter(boolean flag);
	
	/**
	 * set whether PivotTable reports ccan be created or modified. 
	 */
	public void setPivotTables(boolean flag);
	
	/**
	 * set whether unlocked cells can be selected.
	 */
	public void setSelectUnlockedCells(boolean flag);
}
