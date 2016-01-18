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
 * the default and one-book book-series
 * @author dennis
 * @since 3.5.0
 */
public class SimpleBookSeriesImpl extends AbstractBookSeriesAdv {
	private static final long serialVersionUID = 1L;
	
	final private AbstractBookAdv _book;
	
	final private DependencyTable _dependencyTable;
	
	final private ReadWriteLock _lock = new ReentrantReadWriteLock();
	
	private Map<String, Object> _attributes;
	
	public SimpleBookSeriesImpl(AbstractBookAdv book){
		this._book = book;
		_dependencyTable = EngineFactory.getInstance().createDependencyTable();
		((DependencyTableAdv)_dependencyTable).setBookSeries(this);
	}
	@Override
	public SBook getBook(String name) {
		return _book.getBookName().equals(name)?_book:null;
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
		List<SBook> books = new ArrayList<SBook>(1);
		books.add(_book);
		return Collections.unmodifiableList(books);
	}

	@Override
	public Object getAttribute(String name) {
		return name != null ? getAttributeMap().get(name) : null;
	}

	@Override
	public Object setAttribute(String name, Object value) {
		if(name != null) {
			Map<String, Object> map = getAttributeMap();
			return value != null ? map.put(name, value) : map.remove(name);
		} else {
			return null;
		}
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap(getAttributeMap());
	}
	
	private Map<String, Object> getAttributeMap() {
		if(_attributes == null) {
			_attributes = new LinkedHashMap<String, Object>();
		}
		return _attributes;
	}
}
