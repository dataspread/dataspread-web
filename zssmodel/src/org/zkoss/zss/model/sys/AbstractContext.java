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
package org.zkoss.zss.model.sys;

import java.util.Locale;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public abstract class AbstractContext {

	Locale _locale;

	public AbstractContext() {
		this._locale = Locale.getDefault();
	}

	public AbstractContext(Locale locale) {
		this._locale = locale;
	}

	public Locale getLocale() {
		return _locale;
	}

	public void setLocale(Locale locale) {
		this._locale = locale;
	}
}
