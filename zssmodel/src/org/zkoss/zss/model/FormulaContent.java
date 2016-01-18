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
package org.zkoss.zss.model;
/**
 * Indicate that the object has formula content
 * @author dennis
 * @since 3.5.0
 */
public interface FormulaContent {

	/**
	 * Clear the formula result cache if there is evaluation result
	 */
	public void clearFormulaResultCache();
	
	/**
	 * @return returns TRUE if it has parsing error, FALSE if no error found or not a formula content
	 */
	public boolean isFormulaParsingError();
	
}