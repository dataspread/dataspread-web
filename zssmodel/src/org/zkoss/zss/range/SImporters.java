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

import org.zkoss.zss.range.impl.imexp.*;
/**
 * This class contains utility methods to register the importer factory and get a importer by registered name.
 * @author dennis
 * @since 3.5.0
 */
public class SImporters {
	static private HashMap<String,SImporterFactory> factories = new LinkedHashMap<String, SImporterFactory>();
	
	static{
		//default registration
		register("excel",new ExcelImportFactory());
	}
	
	/**
	 * Get the default importer which is excel format, and it is smart enough to recognize the format(xls or xlsx)
	 * @return
	 */
	static final synchronized public SImporter getImporter(){
		return getImporter("excel");
	}
	
	/**
	 * Get the registered importer
	 * @param name
	 * @return
	 */
	static final synchronized public SImporter getImporter(String name){
		SImporterFactory factory = factories.get(name);
		if(factory!=null){
			return factory.createImporter();
		}
		throw new IllegalStateException("can find any importer named "+name);
	}
	
	/**
	 * Register a importer factory by its name which is also used to get it back.
	 * @param name name of the importer factory which is used to get it
	 * @param factory the importer factory you want to register
	 */
	static final synchronized public void register(String name,SImporterFactory factory){
		factories.put(name, factory);
	}
}
