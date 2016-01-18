/* SBookSeries.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/11/14 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * It contains multiple {@link SBook} objects so that formulas in them can reference to cells of another book.
 * @author dennis
 * @since 3.5.0
 */
public interface SBookSeries {
	
	/**
	 * Get the book by name;
	 * @param name the book name
	 * @return the book or null if not found.
	 */
	public SBook getBook(String name);
	
	/**
	 * Gets the books list
	 * @return the readonly books list
	 */
	public List<SBook> getBooks();
	
	/**
	 * Get the ReadWriteLock for synchronized when read-write model for current accessing.
	 * @return
	 */
	public ReadWriteLock getLock();
	
	/**
	 * Get the runtime custom attribute that stored in this book
	 * @param name the attribute name
	 * @return the value, or null if not found
	 */
	public Object getAttribute(String name);
	
	/**
	 * Set the runtime custom attribute to stored in this book, the attribute is only use for developer to stored runtime data in the book,
	 * values will not stored to excel when exporting.
	 * @param name name the attribute name
	 * @param value the attribute value
	 */
	public Object setAttribute(String name,Object value);
	
	/**
	 * Get the unmodifiable runtime attributes map
	 * @return
	 */
	public Map<String,Object> getAttributes();
	
	/**
	 * @return return the flag that indicate formula cache will be cleared automatically or not, default is FALSE.
	 */
	public boolean isAutoFormulaCacheClean();
	
	/**
	 * @see #isAutoFormulaCacheClean()
	 */
	public void setAutoFormulaCacheClean(boolean enable);
}
