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
 * The exception is thrown when encountering an invalid formula.
 * @author dennis
 * @since 3.5.0
 */
public class InvalidFormulaException extends InvalidModelOpException {
	private static final long serialVersionUID = 1L;

	public InvalidFormulaException() {
		super();
	}

	public InvalidFormulaException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFormulaException(String message) {
		super(message);
	}

	public InvalidFormulaException(Throwable cause) {
		super(cause);
	}

}
