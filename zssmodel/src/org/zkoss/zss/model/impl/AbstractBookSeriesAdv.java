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

import java.io.Serializable;

import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public abstract class AbstractBookSeriesAdv implements SBookSeries,Serializable{
	private static final long serialVersionUID = 1L;

	private boolean _autoFormulaCacheClean = false;//default off
	
	public abstract DependencyTable getDependencyTable();

	@Override
	public boolean isAutoFormulaCacheClean() {
		return _autoFormulaCacheClean;
	}
	@Override
	public void setAutoFormulaCacheClean(boolean autoFormulaCacheClean) {
		this._autoFormulaCacheClean = autoFormulaCacheClean;
	}
	
	
	
}
