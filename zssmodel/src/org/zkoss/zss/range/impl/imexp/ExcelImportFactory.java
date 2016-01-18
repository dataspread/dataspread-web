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

import org.zkoss.zss.range.SImporterFactory;
import org.zkoss.zss.range.SImporter;
/**
 * 
 * @author dennis
 * @author Hawk
 * @since 3.5.0
 */
public class ExcelImportFactory implements SImporterFactory{

	@Override
	public SImporter createImporter() {
		return new ExcelImportAdapter();
	}

}
