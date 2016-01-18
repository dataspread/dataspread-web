/* Exporters.java

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

import org.zkoss.zss.api.impl.ExporterImpl;
import org.zkoss.zss.range.SExporter;
import org.zkoss.zss.range.SExporters;

/**
 * The main class to get an exporter.
 * @author dennis
 * @since 3.0.0
 */
public class Exporters {
	/**
	 * Gets exporter
	 * @param type the exporter type (e.x "excel")
	 * @return exporter instance for the type, null if not found
	 */
	public static Exporter getExporter(String type) {
		SExporter exp = SExporters.getExporter(type);
		return exp==null?null:new ExporterImpl(exp);
	}
	
	/**
	 * Gets default excel exporter
	 * @return excel exporter instance, null if not found
	 */
	public static Exporter getExporter() {
		return getExporter("excel");
	}

}
