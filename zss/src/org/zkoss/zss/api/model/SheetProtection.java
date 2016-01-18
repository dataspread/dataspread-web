/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 22, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.api.model;

/**
 * This interface provides access to what are allowed operations for a 
 * protected sheet.
 * @author henri
 *
 */
public interface SheetProtection {
	/**
	 * get whether linked objects or embedded objects can be edited.
	 * @return whether linked objects or embedded objects can be edited.
	 */
	public boolean isObjectsEditable();
	
	/**
	 * get whether scenarios can be edited.
	 */
	public boolean isScenariosEditable();
	
	/**
	 * get whether cells can be formatted.
	 */
	public boolean isFormatCellsAllowed();
	
	/**
	 * get whether columns can be formatted.
	 */
	public boolean isFormatColumnsAllowed();
	
	/**
	 * get whether rows can be formatted.
	 */
	public boolean isFormatRowsAllowed();
	
	/**
	 * get whether columns can be inserted.
	 */
	public boolean isInsertColumnsAllowed();
	
	/**
	 * get whether rows can be inserted.
	 */
	public boolean isInsertRowsAllowed();
	
	/**
	 * get whether hyperlinks can be inserted. 
	 */
	public boolean isInsertHyperlinksAllowed();
	
	/**
	 * get whether columns can be deleted.
	 */
	public boolean isDeleteColumnsAllowed();
	
	/**
	 * get whether rows can be deleted. 
	 */
	public boolean isDeleteRowsAllowed();
	
	/**
	 * get whether locked cells can be selected.
	 */
	public boolean isSelectLockedCellsAllowed();
	
	/**
	 * get whether cells can be sorted.
	 */
	public boolean isSortAllowed();
	
	/**
	 * get whether cells can be filtered.
	 */
	public boolean isAutoFilterAllowed();
	
	/**
	 * get whether PivotTable reports ccan be created or modified. 
	 */
	public boolean isPivotTablesAllowed();
	
	/**
	 * get whether unlocked cells can be selected.
	 */
	public boolean isSelectUnlockedCellsAllowed();
}
