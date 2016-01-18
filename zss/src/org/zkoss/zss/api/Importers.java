/* Importers.java

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

import org.zkoss.zss.api.impl.ImporterImpl;
import org.zkoss.zss.range.SImporter;
import org.zkoss.zss.range.SImporters;

/**
 * The main class to get an importer.
 * @author dennis
 *
 */
public class Importers {

	/**
	 * Gets importer
	 * @param type the importer type (e.x "excel")
	 * @return importer instance for the type, null if not found
	 */
	public static Importer getImporter(String type) {
		SImporter imp = SImporters.getImporter(type);
		return imp == null ? null : new ImporterImpl(imp);
	}
	
	/**
	 * Gets default excel importer
	 * @return importer instance for excel, null if not found
	 */
	public static Importer getImporter() {
		return getImporter("excel");
	}
}
