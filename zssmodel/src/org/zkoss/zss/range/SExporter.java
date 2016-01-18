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
import java.io.OutputStream;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SheetRegion;

/**
 * An exporter can export a book model to a output stream or file.
 * @author dennis
 * @since 3.5.0
 */
public interface SExporter {
	/**
	 * Export a book
	 * @param book the book to export
	 * @param fos the output stream to store data
	 * @throws IOException
	 */
	public void export(SBook book, OutputStream fos) throws IOException;
	
	/**
	 * Export a book
	 * @param book the book to export
	 * @param fos the output file to store data
	 * @throws IOException
	 */
	public void export(SBook book, File file) throws IOException;

	/**
	 * Export sheet to output-stream, note : not all implementation support this operation
	 * @param sheet
	 * @param fos
	 * @deprecated 
	 */
	//create this for compatibility
	public void export(SSheet sheet, OutputStream fos) throws IOException;

	/**
	 * Export sheet to output-stream, note : not all implementation support this operation
	 * @param sheet
	 * @param fos
	 * @deprecated 
	 */
	//create this for compatibility
	public void export(SheetRegion sheetRegion, OutputStream fos) throws IOException;
}
