/* SimpleRef.java

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

import org.zkoss.zss.model.SColor;

/**
 *
 * it simple use hard reference to a instance
 * @author dennis
 * @since 3.0.0
 */
public class SimpleRef<T> implements ModelRef<T>{

	T _instance;
	
	public SimpleRef(T instance){
		this._instance = instance;
	}
	
	public T get() {
		return _instance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_instance == null) ? 0 : _instance.hashCode());
		return result;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleRef other = (SimpleRef) obj;
		if (_instance == null) {
			if (other._instance != null)
				return false;
		} else if (!_instance.equals(other._instance))
			return false;
		return true;
	}
}
