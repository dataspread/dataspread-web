/* Book.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model;

import java.util.concurrent.locks.ReadWriteLock;

import org.zkoss.zss.model.SBook;

/**
 * This interface provides entry to access Spreadsheet's data model.  
 * @author dennis
 * @since 3.0.0
 */
public interface Book {
	public enum BookType {
		XLS, XLSX
	}
	
	/** 
	 * get the internal model object to do advanced operation <br/>
	 * Note : operate on internal object will not automatically update Spreadsheet   
	 * @return
	 */
	public SBook getInternalBook();
	
	/**
	 * Gets the object for synchronized a book.
	 * Note: you shouldn't synchronize a book directly, you have to get the sync object to synchronize it
	 * @return
	 * @deprecated since 3.5.0, use {@link #getLock()} 
	 */
	public Object getSync();
	
	/**
	 * Get the read-write lock of this book
	 * @return
	 * @since 3.5.0
	 */
	public ReadWriteLock getLock();
	
	/**
	 * Gets the book name
	 * @return tge book name
	 */
	public String getBookName();

	/**
	 * Gets the book type
	 * @return the book type
	 */
	public BookType getType();

	/**
	 * Gets the index of sheet
	 * @param sheet 
	 * @return the index of sheet or -1 if not found
	 */
	public int getSheetIndex(Sheet sheet);

	/**
	 * Gets the number of sheet
	 * @return the number of sheet
	 */
	public int getNumberOfSheets();

	/**
	 * Gets sheet by index
	 * @param index index of sheet
	 * @return
	 */
	public Sheet getSheetAt(int index);

	/**
	 * Gets sheet by sheet name
	 * @param name name of sheet
	 * @return the sheet or null if not found
	 */
	public Sheet getSheet(String name);

	/**
	 * Sets share scope of this book, the possible value is "desktop","session","application"
	 * @param scope  
	 */
	public void setShareScope(String scope);
	
	/**
	 * Gets share scope of this book
	 * @return
	 */
	public String getShareScope();
	
	
	/**
	 * check if this book has named range
	 * @param name the name to check
	 * @return true if it has a range of the name
	 */
	public boolean hasNameRange(String name);
	
	/**
	 * @return the maximum number of usable rows in each sheet
	 */
	public int getMaxRows();
	
	/**
	 * @return the maximum number of usable column in each sheet
	 */
	public int getMaxColumns();

}
