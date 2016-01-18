/*

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
package org.zkoss.zss.range;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.zkoss.zss.model.SBook;

/**
 * An importer to import a input stream, file..etc to a new book
 * @author dennis
 * @since 3.5.0
 */
public interface SImporter {
	/**
	 * Import book from a input stream
	 * @param is the input stream
	 * @param bookName the book name for imported book
	 * @return the book instance
	 * @throws IOException
	 */
	public SBook imports(InputStream is, String bookName) throws IOException;
	
	/**
	 * Import book from a file
	 * @param file the file
	 * @param bookName the book name for imported book
	 * @return the book instance
	 * @throws IOException
	 */
	public SBook imports(File file, String bookName) throws IOException;
	
	/**
	 * Import book from a URL
	 * @param url the url
	 * @param bookName the book name for imported book
	 * @return the book instance
	 * @throws IOException
	 */
	public SBook imports(URL url, String bookName) throws IOException;
}
