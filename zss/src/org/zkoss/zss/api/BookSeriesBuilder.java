/* BookSeriesBuilder.java

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

import java.util.Set;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.lang.Strings;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.impl.BookImpl;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SBookSeriesBuilder;

/**
 * The book series builder which accepts multiple {@link Book} objects makes each of them can reference cells from other books.
 * @author dennis
 * @since 3.0.0
 */
public abstract class BookSeriesBuilder {
	
	private static BookSeriesBuilder _instance;
	
	public static BookSeriesBuilder getInstance(){
		
		if(_instance==null){
			synchronized(BookSeriesBuilder.class){
				if(_instance==null){
					_instance = new BookSeriesBuilderWrap();
				}
			}
		}
		return _instance;
	}
	
	static class BookSeriesBuilderWrap extends BookSeriesBuilder{

		public void buildBookSeries(Set<Book> books){
			buildBookSeries(books.toArray(new Book[books.size()]));
		}
		
		public void buildBookSeries(Book... books){
			if(books == null){
				throw new IllegalArgumentException("books is null");
			}
			SBook[] xbooks = new SBook[books.length];
			
			for(int i=0;i<xbooks.length;i++){
				xbooks[i] = ((BookImpl)books[i]).getNative();
			}
			SBookSeriesBuilder.getInstance().buildBookSeries(xbooks);
		}

	}
	
	abstract public void buildBookSeries(Set<Book> books);
	abstract public void buildBookSeries(Book... books);
}
