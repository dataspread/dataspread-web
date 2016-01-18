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
package org.zkoss.zss.model.sys.formula;

import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public interface FormulaExpression {

	/**
	 * indicated the expression has parsing error
	 * @return
	 */
	boolean hasError();
	
	/**
	 * Get the expression parsing error message if any
	 * @return
	 */
	String getErrorMessage();
	
	String getFormulaString();
	
//	String reformSheetNameChanged(String oldName,String newName);
	
	//parsing result for Name
	
	boolean isAreaRefs();
	
	Ref[] getAreaRefs();
	
	//ZSS-747
	/**
	 * Returns the cached parsing things.
	 * @since 3.6.0
	 */
	Ptg[] getPtgs();

	//ZSS-747
	/**
	 * Returns whether this is a multiple-area formula used in char data; 
	 * e.g. (A1, B1, Sheet2!A1:B2).
	 * @since 3.6.0
	 */
	boolean isMultipleAreaFormula();
}
