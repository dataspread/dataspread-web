/* XSSFPivotTableHelpers.java

	Purpose:
		
	Description:
		
	History:
		May 17, 2012 12:57:18 PM, Created by henri

Copyright (C) 2012 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.poi.xssf.usermodel.helpers;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.poi.ss.usermodel.PivotTableHelper;

/**
 * @author henri
 *
 */
public class XSSFPivotTableHelpers {
	public static final XSSFPivotTableHelpers instance = new XSSFPivotTableHelpers(); 
	private PivotTableHelper _helper;
	public void setHelper(PivotTableHelper helper) { //ZSS-703
		_helper = helper;
	}
	public PivotTableHelper getHelper() {
		if (_helper != null)
			return _helper;
		final String clsStr = Library.getProperty(PivotTableHelper.CLASS);
		if (clsStr != null) {
			try {
				final Class<?> cls = Classes.forNameByThread(clsStr);
				_helper = (PivotTableHelper) cls.newInstance();
				return _helper;
			} catch (ClassNotFoundException e) {
				//ignore
			} catch (IllegalAccessException e) {
				//ignore
			} catch (InstantiationException e) {
				//ignore
			}
		}
		_helper = new XSSFPivotTableHelper();
		return _helper;
	}
}
