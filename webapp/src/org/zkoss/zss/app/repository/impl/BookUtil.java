/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.repository.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.zkoss.lang.Strings;
import org.zkoss.lang.SystemException;
import org.zkoss.zss.api.Exporter;
import org.zkoss.zss.api.Exporters;
import org.zkoss.zss.api.IllegalOpArgumentException;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Book.BookType;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.BookRepository;
/**
 * 
 * @author dennis
 *
 */
public class BookUtil {

	/**
	 * Attach extension for a file name if the extension dones't exist
	 */
	static public String appendExtension(String fileName, Book book) {
		String name = FileUtil.getName(fileName);
		String dotext = FileUtil.getNameExtension(fileName);
		
		if(!Strings.isBlank(dotext)){
			dotext = "."+dotext;
		}else{
			if(book != null) {
				switch(book.getType()){
					case XLS:
						dotext = ".xls";
						break;
					case XLSX:
						dotext = ".xlsx";
						break;
					default:
						dotext = ".xlsx";
				}
			} else 
				dotext = ".xlsx";
		}
		
		return name + dotext;
	}
	
	/**
	 * Gets suggested file name of a book
	 */
	static public String suggestFileName(String fileName, Book book, BookRepository rep){
		int i = 0;
		fileName = appendExtension(fileName, book);
		String name = FileUtil.getName(fileName);
		String dotext = "." + FileUtil.getNameExtension(fileName);
		
		Set<String> names = new HashSet<String>();

		for(BookInfo info:rep.list()){
			names.add(info.getName());
		}
		String sname = name+dotext;
		while(names.contains(sname)){
			sname = name+"("+ ++i +")"+dotext;
		}
		return sname;
	}
	
	/**
	 * Gets suggested file name of a book
	 */
	static public String suggestFileName(Book book) {
		String bn = book.getBookName();
		BookType type = book.getType();
		
		String ext = type==BookType.XLS?".xls":".xlsx";
		int i = bn.lastIndexOf('.');
		if(i==0){
			bn = "book";
		}else if(i>0){
			bn = bn.substring(0,i);
		}
		return bn+ext;
	}
	
	static public String suggestPdfName(Book book) {
		String bn = book.getBookName();
		String ext = ".pdf";
		int i = bn.lastIndexOf('.');
		if(i==0){
			bn = "book";
		}else if(i>0){
			bn = bn.substring(0,i);
		}
		return bn+ext;
	}
	
	static File workingFolder;
	
	static public File getWorkingFolder() {
		if (workingFolder == null) {
			synchronized (BookUtil.class) {
				if (workingFolder == null) {
					workingFolder = new File(
							System.getProperty("java.io.tmpdir"), "zssappwork");
					if (!workingFolder.exists()) {
						if (!workingFolder.mkdirs()) {
							throw new SystemException(
									"Can't get working folder:"
											+ workingFolder.getAbsolutePath());
						}
					}
				}
			}
		}
		return workingFolder;
	}
	static public File saveBookToWorkingFolder(Book book) throws IOException{
		return saveBookToWorkingFolder(book,null);
	}
	static public File saveBookToWorkingFolder(Book book,String exporterType) throws IOException{
		Exporter exporter= exporterType==null?Exporters.getExporter():Exporters.getExporter(exporterType);
		if(exporter==null){
			throw new IllegalOpArgumentException("can't find exporter "+exporterType);
		}
		String bn = suggestFileName(book);
		
		String name = FileUtil.getName(bn);
		String ext = FileUtil.getNameExtension(bn);
		
		if(Strings.isBlank(name)){
			name = "book";
		}
		
		//prefix has > 3
		File f = File.createTempFile("temp_"+name,"."+ext,getWorkingFolder());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			exporter.export(book, fos);
		}finally{
			if(fos!=null){
				fos.close();
			}
		}
		return f;
	}
	
}
