/* SBooks.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.BookBindings;

/**
 * Contains factory methods to create a {@link SBook}.
 * @author dennis
 * @since 3.5.0
 */
public class SBooks {

	/**
	 * Create a book with the bookName
	 * @param bookName
	 * @return the book instance
	 */
	public static SBook createOrGetBook(String bookName){
		SBook book = BookBindings.getBookByName(bookName, true);
		if (book==null){
			book=new BookImpl(bookName);
			((AbstractBookAdv) book).initDefaultCellStyles();
			BookBindings.put(book.getBookName(),book);
		}
		return book;
	}
}
