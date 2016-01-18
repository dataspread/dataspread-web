/* ImporterImpl.java

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
package org.zkoss.zss.api.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.zkoss.zss.api.Importer;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.impl.BookImpl;
import org.zkoss.zss.api.model.impl.SimpleRef;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.range.SImporter;

/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class ImporterImpl implements Importer{
	private SImporter _importer;
	public ImporterImpl(SImporter importer) {
		this._importer = importer;
	}

	
	public Book imports(InputStream is, String bookName)throws IOException{
		if(is == null){
			throw new IllegalArgumentException("null inputstream");
		}
		if(bookName == null){
			throw new IllegalArgumentException("null book name");
		}
		return new BookImpl(new SimpleRef<SBook>(_importer.imports(is, bookName)));
	}


	public SImporter getNative() {
		return _importer;
	}


	@Override
	public Book imports(File file, String bookName) throws IOException {
		if(file == null){
			throw new IllegalArgumentException("null file");
		}
		if(bookName == null){
			throw new IllegalArgumentException("null book name");
		}
		FileInputStream is = null;
		try{
			is = new FileInputStream(file);
			return imports(is,bookName);
		}finally{
			if(is!=null){
				try{
					is.close();
				}catch(Exception x){}//eat
			}
		}
	}


	@Override
	public Book imports(URL url, String bookName) throws IOException {
		if(url == null){
			throw new IllegalArgumentException("null url");
		}
		if(bookName == null){
			throw new IllegalArgumentException("null book name");
		}
		InputStream is = null;
		try{
			is = url.openStream();
			return imports(is,bookName);
		}finally{
			if(is!=null){
				try{
					is.close();
				}catch(Exception x){}//eat
			}
		}
	}
}
