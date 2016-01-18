/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2015/4/2 , Created by JerryChen
}}IS_NOTE

Copyright (C) 2015 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;
/**
 * The exception is thrown when encountering invalid parameters.
 * @author JerryChen
 * @since 3.8.0
 */
public class InvalidDataValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidDataValidationException() {
		super();
	}

	public InvalidDataValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDataValidationException(String message) {
		super(message);
	}

	public InvalidDataValidationException(Throwable cause) {
		super(cause);
	}
}
