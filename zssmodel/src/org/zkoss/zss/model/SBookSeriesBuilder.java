/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/11/14 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.util.Set;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.lang.Strings;

/**
 * A utility class that build a book series that allow external book references can work.
 * @author dennis
 * @since 3.5.0
 */
public abstract class SBookSeriesBuilder {
	
	private static SBookSeriesBuilder _instance;
	
	/**
	 * @return a builder used to build a book series
	 */
	public static SBookSeriesBuilder getInstance(){
		
		if(_instance==null){
			synchronized(SBookSeriesBuilder.class){
				if(_instance==null){
					String clz = Library.getProperty("org.zkoss.zss.model.BookSeriesBuilder.class");
					if (!Strings.isEmpty(clz)) {
						try {
							_instance = (SBookSeriesBuilder) Classes.forNameByThread(clz).newInstance();
						} catch (Exception e) {
							throw new RuntimeException(e.getMessage(),e);
						}
					}else{
						_instance = new SBookSeriesBuilder() {
							@Override
							public void buildBookSeries(Set<SBook> books) {
								throw new UnsupportedOperationException("not supported in this edition");
							}
							@Override
							public void buildBookSeries(SBook[] books) {
								throw new UnsupportedOperationException("not supported in this edition");
							}
						};
					}
				}
			}
		}
		return _instance;
	}
	
	/**
	 * Build a book series that allow external book references can work among specified books
	 * @param books books that have external reference to other books
	 */
	abstract public void buildBookSeries(Set<SBook> books);
	
	/**
	 * @see #buildBookSeries(Set) 
	 */
	abstract public void buildBookSeries(SBook... books);
}
