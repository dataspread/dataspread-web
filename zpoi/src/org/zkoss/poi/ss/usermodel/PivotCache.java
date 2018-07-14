/* PivotCache.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 19, 2012 2:43:15 PM , Created by sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.poi.ss.usermodel;

import java.util.List;

/**
 * @author sam
 *
 */
public interface PivotCache {
	
    /**
     * Return the parent workbook
     *
     * @return the parent workbook
     */
    Workbook getWorkbook();
	
    /**
     * Returns the cache id
     * 
     * @return
     */
	public long getCacheId();
	
	/**
	 * Returns {@link SheetSource}
	 * 
	 * @return
	 */
	public SheetSource getSheetSource();
	
	/**
	 * Returns all cache fields
	 * 
	 * @return
	 */
	public List<CacheField> getFields();
	
	/**
	 * Returns all cache records
	 * 
	 * @return
	 */
	public List<CacheRecord> getRecords();

	short getRefreshedVersion();
	
	short getMinRefreshableVersion();
	
	short getCreatedVersion();
	
	public interface SheetSource {
		
		/**
		 * Returns the source of sheet name
		 * @return string sheet name
		 */
		public String getName();
		
		/**
		 * Returns the source reference 
		 * @return string
		 */
		public String getRef();
	}
	
	public interface CacheField {
		
		public long getNumberFormatId();
		
		public void setName(String name);
		
		public String getName();
		
		public List<Object> getSharedItems();
		
		public void setDatabaseField(boolean databaseField);
		
		public boolean getDatabaseField();
		
		public int getFieldGroup();
		
		public int getGroupBase();
		
		public List<Integer> getGroupDiscrete();
	}
	
	public interface CacheRecord {
		
		public List<Object> getData();
	}
}