/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.repository.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.zkoss.zss.api.Exporters;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.BookRepository;
import org.zkoss.zss.app.ui.UiUtil;
import org.zkoss.zss.model.ModelEvents;
import org.zkoss.zss.range.SRanges;
/**
 * 
 * @author dennis
 *
 */
public class SimpleRepository implements BookRepository{
	private static final long serialVersionUID = 934417121819181741L;
	File root;
	public SimpleRepository(File root){
		this.root = root;
	}
	
	public synchronized List<BookInfo> list() {
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

	public synchronized Book load(BookInfo info) throws IOException {
		Book book = Importers.getImporter().imports(((SimpleBookInfo)info).getFile(), info.getName());
		return book;
	}
	
	public BookInfo save(BookInfo info, Book book) throws IOException {
		return save(info, book, false);
	}

	// TODO: remove synchronized keyword, and use each book name as synchronized key to let different file be access concurrently.
	public synchronized BookInfo save(BookInfo info, Book book, boolean isForce) throws IOException {
		if(UiUtil.isRepositoryReadonly()){
			return null;
		}
			
		ByteArrayOutputStream cacheOutputStream = null;
		FileOutputStream fileOutputStream = null;
		ReadWriteLock lock = book.getInternalBook().getBookSeries().getLock();
		try{
			// 1. write to memory cache
			boolean skip = false;
			lock.writeLock().lock();
			try {
				if(!book.getInternalBook().isDirty() && !isForce) {
					skip = true;
					return info;
				}
				// blank excel file needs 41xx bytes
				cacheOutputStream = new ByteArrayOutputStream(5000);
				exportBook(book, cacheOutputStream);
				book.getInternalBook().setDirty(false);
			} finally {
				lock.writeLock().unlock();
				
				// save notification when user file saved or user force to save 
				if (!skip)
					SRanges.range(book.getSheetAt(0).getInternalSheet()).notifyCustomEvent(ModelEvents.ON_MODEL_DIRTY_CHANGE, false, false);
			}
			
			// 2. write to temporary file
			//write to temp file first to avoid write error damage original file 
			File f = ((SimpleBookInfo)info).getFile();
			File temp = File.createTempFile("temp", f.getName());
			temp.deleteOnExit();
			fileOutputStream = new FileOutputStream(temp);
			cacheOutputStream.writeTo(fileOutputStream);
			
			// 3. file copy
			FileUtil.copy(temp,f);
			temp.delete();
		}finally{
			if(cacheOutputStream != null)
				cacheOutputStream.close();
			
			if(fileOutputStream != null)
				fileOutputStream.close();
		}
		
		return info;
	}
	
	private void exportBook(Book book, OutputStream fos) throws IOException {
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
	}
	
	public synchronized BookInfo saveAs(String bookname, Book book) throws IOException {
		if(UiUtil.isRepositoryReadonly()){
			return null;
		}
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
		SimpleBookInfo info = new SimpleBookInfo(f,f.getName(),new Date());
		return save(info, book, true);
	}

	public boolean delete(BookInfo info) throws IOException {
		if(UiUtil.isRepositoryReadonly()){
			return false;
		}
		File f = ((SimpleBookInfo)info).getFile();
		if(!f.exists()){
			return false;
		}
		return f.delete();
	}
}
