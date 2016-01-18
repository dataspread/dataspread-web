/* BookRepository.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/7/4 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.app;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.zkoss.zss.api.model.Book;

/**
 * @author dennis
 *
 */
public interface BookRepository extends Serializable {

	/**
	 * Get the BookInfo list
	 * @return the BookInfo list
	 */
	List<BookInfo> list();
	
	/**
	 * Loads a book
	 * @param info the BookInfo
	 * @return book or null if no such book.
	 * @throws IOException 
	 */
	Book load(BookInfo info) throws IOException;
	
	/**
	 * Saves a book and replace book of BookInfo. This method will do nothing when book's dirty flag is false.
	 * @param info the BookInfo
	 * @param book the book to be saved
	 * @return the updated BookInfo, or null if not saved.
	 * @throws IOException
	 */
	BookInfo save(BookInfo info, Book book) throws IOException;
	
	/**
	 * Saves a book and replace book of BookInfo
	 * @param info the BookInfo
	 * @param book the book to be saved
	 * @param isForce save without dirty check if true
	 * @return the updated BookInfo, or null if not saved.
	 * @throws IOException
	 */
	BookInfo save(BookInfo info, Book book, boolean isForce) throws IOException;
	
	/**
	 * Saves a book with a new name, 
	 * @param name the new name
	 * @param book the book to be saved
	 * @return the new BookInfo, or null if not saved.
	 * @throws IOException
	 */
	BookInfo saveAs(String name,Book book) throws IOException;
	
	/**
	 * Deletes a book
	 * @param info the BookInfo
	 * @return true if deleted, or false if not
	 * @throws IOException
	 */
	boolean delete(BookInfo info) throws IOException;
}
