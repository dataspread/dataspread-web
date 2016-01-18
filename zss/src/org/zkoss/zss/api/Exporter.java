/* Exporter.java

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
package org.zkoss.zss.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;

/**
 * Exporter for a book or a sheet.
 * @author dennis
 * @since 3.0.0
 */
public interface Exporter {
	
	/**
	 * Export book
	 * @param book the book to export
	 * @param fos the output stream to store data
	 * @throws IOException
	 */
	public void export(Book book, OutputStream fos) throws IOException;
	
	/**
	 * Export book
	 * @param book the book to export
	 * @param fos the output file to store data
	 * @throws IOException
	 */
	public void export(Book book, File file) throws IOException;
	
//	doesn't support to export sheet, selection until we find a good way in poi/html/pdf
	/**
	 * Export the sheet.
	 * 
	 * Note : Not all exporter supports this api.
	 * @param sheet the sheet to export
	 * @param fos the output stream to store data
	 * @throws IOException
	 * 
	 * @deprecated since 3.5.0
	 */
	@Deprecated
	public void export(Sheet sheet, OutputStream fos) throws IOException;
	/**
	 * Export selection of sheet
	 * 
	 * Note : Not all exporter supports this api.
	 * @param sheet the sheet to export
	 * @param selection the selection to export
	 * @param fos the output stream to store data
	 * @throws IOException
	 * 
	 * @deprecated since 3.5.0
	 */
	@Deprecated
	public void export(Sheet sheet,AreaRef selection,OutputStream fos) throws IOException;

//  even html exporter doesn't support to disable heading yet
//	hide this before there has any implementation
//	/**
//	 * @return true if this exporter support heading configuration
//	 */
//	public boolean isSupportHeadings();
//	
//	/**
//	 * Sets heading configuration,
//	 * @param enable true to enable heading
//	 */
//	public void enableHeadings(boolean enable);
}
