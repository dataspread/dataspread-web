/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.repository.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zss.api.Exporters;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.BookRepository;
import org.zkoss.zss.app.ui.UiUtil;
/**
 * 
 * @author dennis
 *
 */
public class CollaborativeRepository implements BookRepository{
	private static final long serialVersionUID = -4784289072382777993L;
	File root;
	Map<String, Book> books = new HashMap<String, Book>(list().size() + 5);

	public CollaborativeRepository(File root){
		this.root = root;
	}
	
	
	public List<BookInfo> list() {
		synchronized (this) {
			List<BookInfo> books = new ArrayList<BookInfo>();
			for(File f:root.listFiles(new FileFilter() {
				public boolean accept(File file) {
					if(file.isFile() && !file.isHidden()){
						String ext = FileUtil.getNameExtension(file.getName()).toLowerCase();
						if("xls".equals(ext) || "xlsx".equals(ext)){
							return true;
						}
					}
					return false;
				}
			})){
				books.add(new SimpleBookInfo(f,f.getName(),new Date(f.lastModified())));
			}
			return books;
		}
	}

	public Book load(BookInfo info) throws IOException {
		String name = info.getName();
		synchronized (this) {
			if(books.containsKey(name))
				return books.get(name);
			
			Book book = Importers.getImporter().imports(((SimpleBookInfo)info).getFile(), info.getName());
			books.put(name, book);
			return book;
		}
	}
	
	public BookInfo save(BookInfo info, Book book) throws IOException {
		return save(info, book, false);
	}

	public BookInfo save(BookInfo info, Book book, boolean isForce) throws IOException {
		if(UiUtil.isRepositoryReadonly()){
			return null;
		}
		
		synchronized (this) {
			FileOutputStream fos = null;
			try{
				File f = ((SimpleBookInfo)info).getFile();
				//write to temp file first to avoid write error damage original file 
				File temp = File.createTempFile("temp", f.getName());
				fos = new FileOutputStream(temp);
				//ZSS-680: ZSS app always save to xlsx format
				String type = "excel";
				switch(book.getType()) {
				case XLS:
					type = "xls";
					break;
				case XLSX:
					//fall down
				default:
					type = "xlsx";
					break;
				}
				Exporters.getExporter(type).export(book, fos);
				
				fos.close();
				fos = null;
				
				FileUtil.copy(temp,f);
				temp.delete();
				
			}finally{
				if(fos!=null)
					fos.close();
			}
			return info;
		}
	}
	
	public BookInfo saveAs(String bookname,Book book) throws IOException {
		if(UiUtil.isRepositoryReadonly()){
			return null;
		}
		
		synchronized (this) {
				
			String name = FileUtil.getName(bookname);
			String ext = "";
			switch(book.getType()){
			case XLS:
				ext = ".xls";
				break;
			case XLSX:
				ext = ".xlsx";
				break;
			default:
				throw new RuntimeException("unknow book type");
			}
			File f = new File(root,name+ext);
			int c = 0;
			if(f.exists()){
				f = new File(root,name+"("+(++c)+")"+ext);
			}
			SimpleBookInfo info = new SimpleBookInfo(f,f.getName(),new Date());
			
			books.put(name, book);
			return save(info,book);
		}
	}


	public boolean delete(BookInfo info) throws IOException {
		if(UiUtil.isRepositoryReadonly()){
			return false;
		}
		
		synchronized (this) {
			File f = ((SimpleBookInfo)info).getFile();
			if(!f.exists()){
				return false;
			}
			books.remove(info.getName());
			return f.delete();
		}
	}
}
