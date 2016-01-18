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
package org.zkoss.zss.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.sys.DependencyTableAdv;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class BookSeriesImpl extends AbstractBookSeriesAdv {
	private static final long serialVersionUID = 1L;
	
	final private HashMap<String,AbstractBookAdv> _books;
	
	final private DependencyTable _dependencyTable;
	
	final private ReadWriteLock _lock = new ReentrantReadWriteLock();
	
	private Map<String, Object> _attributes;

	public BookSeriesImpl(AbstractBookAdv... books){
		this._books = new LinkedHashMap<String, AbstractBookAdv>(1);
		_dependencyTable = EngineFactory.getInstance().createDependencyTable();
		((DependencyTableAdv)_dependencyTable).setBookSeries(this);
		for(AbstractBookAdv book:books){
			this._books.put(book.getBookName(), book);
			
			if(book.getBookSeries().isAutoFormulaCacheClean()){//if any book auto
				this.setAutoFormulaCacheClean(true);
			}
			
			((DependencyTableAdv) _dependencyTable)
					.merge((DependencyTableAdv) ((AbstractBookSeriesAdv) book
							.getBookSeries()).getDependencyTable());
			book.setBookSeries(this);
		}
	}
	@Override
	public SBook getBook(String name) {
		return _books.get(name);
	}

	@Override
	public DependencyTable getDependencyTable() {
		return _dependencyTable;
	}
	@Override
	public ReadWriteLock getLock() {
		return _lock;
	}
	@Override
	public List<SBook> getBooks() {
		return Collections.unmodifiableList(new ArrayList<SBook>(_books.values()));
	}
	
	@Override
	public Object getAttribute(String name) {
		return _attributes==null?null:_attributes.get(name);
	}

	@Override
	public Object setAttribute(String name, Object value) {
		if(_attributes==null){
			_attributes = new HashMap<String, Object>();
		}
		return _attributes.put(name, value);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return _attributes==null?Collections.EMPTY_MAP:Collections.unmodifiableMap(_attributes);
	}
}
