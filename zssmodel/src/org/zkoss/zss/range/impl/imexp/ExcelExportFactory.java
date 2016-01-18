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
package org.zkoss.zss.range.impl.imexp;

import org.zkoss.lang.Library;
import org.zkoss.zss.range.SExporterFactory;
import org.zkoss.zss.range.SExporter;
/**
 * 
 * @author dennis
 * @author Hawk
 * @since 3.5.0
 */
public class ExcelExportFactory implements SExporterFactory{
	/**
	 * @since 3.5.0
	 */
	public enum Type{
		XLS,XLSX;
	}
	
	private Type _type;
	
	public ExcelExportFactory(Type type){
		this._type = type;
	}
	
	@Override
	public SExporter createExporter() {
		AbstractExcelExporter exporter = _type == Type.XLSX ?
			new ExcelXlsxExporter() : new ExcelXlsExporter();
		exporter.setExportCache(isExportCache()); //ZSS-873
		return exporter;
	}

	//ZSS-873
	private boolean isExportCache() {
		String importCache = Library.getProperty("org.zkoss.zss.export.cache", "false");
		return "true".equalsIgnoreCase(importCache.trim());
	}
}
