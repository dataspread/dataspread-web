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
package org.zkoss.zss.range;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.zkoss.lang.Library;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.range.impl.imexp.ExcelExportFactory;
import org.zkoss.zss.range.impl.imexp.ExcelExportFactory.Type;

/**
 * A class that you can get an exporter by registered name and register an exporter.
 * @author dennis
 * @sicne 3.5.0
 */
public class SExporters {
	
	private static final Log _logger = Log.lookup(SExporters.class.getName());
	static private HashMap<String,SExporterFactory> _factories = new LinkedHashMap<String, SExporterFactory>();
	
	static{
		//default registration
		register("excel",new ExcelExportFactory(Type.XLSX));
		register("xlsx",new ExcelExportFactory(Type.XLSX));
		register("xls",new ExcelExportFactory(Type.XLS));
		
		// ex exporter registration
		String clzs = Library.getProperty("org.zkoss.zssex.model.default.ExporterFactory.class");
		if(clzs!=null){
			try {
				String[] exporters = clzs.split(",");
				for(String exporter : exporters) {
					String[] keyValue = exporter.split("=");
					try{
						register(keyValue[0], (SExporterFactory)Class.forName(keyValue[1]).newInstance());
					}catch(ClassNotFoundException e){
						_logger.warning("Can't find class for "+keyValue[0]+":"+e.getMessage());
					}
				}
			} catch(Exception e) {
				_logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Gets the default exporter, which is excel xlsx format
	 * @return
	 */
	static final synchronized public SExporter getExporter(){
		return getExporter("excel");
	}
	
	/**
	 * Gets the registered exporter by name
	 * @param name the exporter name
	 */
	static final synchronized public SExporter getExporter(String name){
		SExporterFactory factory = _factories.get(name);
		if(factory!=null){
			return factory.createExporter();
		}
		throw new IllegalStateException("can find any exporter named "+name);
		
	}
	
	/**
	 * Register a exporter factory
	 * @param name the name
	 * @param factory the exporter factory
	 */
	static final synchronized public void register(String name,SExporterFactory factory){
		_factories.put(name, factory);
	}
}
