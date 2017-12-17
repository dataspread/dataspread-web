/* BookImpl.java

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
package org.zkoss.zss.api.model.impl;

import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.range.impl.imexp.AbstractExcelImporter;

import java.util.concurrent.locks.ReadWriteLock;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class BookImpl implements Book{

	private ModelRef<SBook> _bookRef;
	private BookType _type;
	private SBook _book;

	public BookImpl(ModelRef<SBook> ref){
		this._bookRef = ref;
		SBook book = ref.get();
		_book = book;

		//ZSS-610, get the information that stored in book's attibute when importing
		String bookType = AbstractExcelImporter.getBookType(book);
		_type = "xls".equalsIgnoreCase(bookType)?BookType.XLS:BookType.XLSX;
	}

	public BookImpl(String bookName){
		_book = BookBindings.getBookByName(bookName);
	}

	public SBook getNative() {
		return _book;
	}
	
	@Override
	public SBook getInternalBook(){
		return _book;
	}
	
	public ModelRef<SBook> getRef(){
		return _bookRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_bookRef == null) ? 0 : _bookRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookImpl other = (BookImpl) obj;
		if (_bookRef == null) {
            return other._bookRef == null;
		} else return _bookRef.equals(other._bookRef);
    }

	public String getBookName() {
		return getNative().getBookName();
	}
	
	public BookType getType(){
		return _type; 
	}

	public int getSheetIndex(Sheet sheet) {
		if(sheet==null) return -1;
		return getNative().getSheetIndex(((SheetImpl)sheet).getNative());
	}

	public int getNumberOfSheets() {
		return getNative().getNumOfSheet();
	}
	
	public SheetImpl getSheetAt(int index){
		SSheet sheet = getNative().getSheet(index);
		return new SheetImpl(_bookRef,new SimpleRef<SSheet>(sheet));
	}
	
	public SheetImpl getSheet(String name){
		SSheet sheet = getNative().getSheetByName(name);
		
		return sheet==null?null:new SheetImpl(_bookRef,new SimpleRef<SSheet>(sheet));
	}

	@Override
	public void setShareScope(String scope) {
		getNative().setShareScope(scope);
	}

	@Override
	public String getShareScope() {
		return getNative().getShareScope();
	}

	@Override
	@Deprecated
	public Object getSync() {
		return getNative();
	}

	@Override
	public boolean hasNameRange(String name) {
		return getNative().getNameByName(name)!=null;
	}

	@Override
	public int getMaxRows() {
		return getNative().getMaxRowSize();
	}

	@Override
	public int getMaxColumns() {
		return getNative().getMaxColumnSize();
	}

	@Override
	public ReadWriteLock getLock() {
		return getNative().getBookSeries().getLock();
	}
	
}
