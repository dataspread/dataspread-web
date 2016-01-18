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

/**
 * The interface defines factory methods for exporters.
 * @author dennis
 * @since 3.5.0
 */
public interface SExporterFactory {

	/**
	 * Create an exporter
	 */
	public SExporter createExporter();
}
