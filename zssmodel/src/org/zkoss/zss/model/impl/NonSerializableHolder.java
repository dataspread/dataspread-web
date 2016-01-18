/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/04/07 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.io.Serializable;

/**
 * To help hold a non-serializable object as a serializable (usually for cache)
 * @author Dennis
 */
public class NonSerializableHolder<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	//20140731, henrichen: make final so works as a FinalWrapper on double check locking
	//@see org.zkoss.zss.model.impl.sys.formula.FormulaEngineImpl#getEvalCtxMap()
	//@see http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
	private final transient T _noSer; 
	
	public NonSerializableHolder(T noSer){
		_noSer = noSer;
	}
	
	public T getObject(){
		return _noSer;
	}
}