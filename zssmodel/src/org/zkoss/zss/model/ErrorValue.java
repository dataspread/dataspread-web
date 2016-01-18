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

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * An error result of a evaluated formula.
 * @author dennis
 * @since 3.5.0
 */
public class ErrorValue implements Serializable{
	private static final long serialVersionUID = 1L;
	/** <b>#NULL!</b>  - Intersection of two cell ranges is empty */
    public static final byte ERROR_NULL = 0x00;
    /** <b>#DIV/0!</b> - Division by zero */
    public static final byte ERROR_DIV_0 = 0x07;
    /** <b>#VALUE!</b> - Wrong type of operand */
    public static final byte INVALID_VALUE = 0x0F; 
    /** <b>#REF!</b> - Illegal or deleted cell reference */
    public static final byte ERROR_REF = 0x17;  
    /** <b>#NAME?</b> - Wrong function or range name */
    public static final byte INVALID_NAME = 0x1D; 
    /** <b>#NUM!</b> - Value range overflow */
    public static final byte ERROR_NUM = 0x24; 
    /** <b>#N/A</b> - Argument or function not available */
    public static final byte ERROR_NA = 0x2A;
    
    //TODO zss 3.5 this value is not in zpoi
    public static final byte INVALID_FORMULA = 0x7f;
	
    //ZSS-672
	public static final ErrorValue NULL  = new ErrorValue(ERROR_NULL, "#NULL!");
	public static final ErrorValue DIV0 = new ErrorValue(ERROR_DIV_0, "#DIV/0!");
	public static final ErrorValue VALUE = new ErrorValue(INVALID_VALUE, "#VALUE!");
	public static final ErrorValue REF = new ErrorValue(ERROR_REF, "#REF!");
	public static final ErrorValue NAME = new ErrorValue(INVALID_NAME, "#NAME?");
	public static final ErrorValue NUM = new ErrorValue(ERROR_NUM, "#NUM!");
	public static final ErrorValue NA = new ErrorValue(ERROR_NA, "#N/A");
	public static final ErrorValue FORMULA = new ErrorValue(INVALID_FORMULA, "#ERROR!");
	 
	private static final Map<Byte, ErrorValue> ERR_MAP = new HashMap<Byte, ErrorValue>(16);
	static {
		ERR_MAP.put(NULL._code, NULL);
		ERR_MAP.put(DIV0._code, DIV0);
		ERR_MAP.put(VALUE._code, VALUE);
		ERR_MAP.put(REF._code, REF);
		ERR_MAP.put(NAME._code, NAME);
		ERR_MAP.put(NUM._code, NUM);
		ERR_MAP.put(NA._code, NA);
		ERR_MAP.put(FORMULA._code, FORMULA);
	}
	public static ErrorValue valueOf(byte code) {
		final ErrorValue v = ERR_MAP.get(code);
		return v == null ? ErrorValue.NA : v; //unknown code are interpreted into #N/A
	}
	
	private byte _code;
	private String _message;

	public ErrorValue(byte code) {
		this(code, null);
	}

	public ErrorValue(byte code, String message) {
		this._code = code;
		this._message = message;
	}

	public byte getCode() {
		return _code;
	}

	/**
	 * Set error code.
	 * @param code should be one of public byte constant in this class
	 */
	public void setCode(byte code) {
		this._code = code;
	}

	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		this._message = message;
	}
	
	/**
	 * @return might be #NULL!, #NAME?, or #NUM! etc...
	 */
	public String getErrorString(){
		return getErrorString(_code);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("/").append(getErrorString());
		return sb.toString();
	}
	
    public static final String getErrorString(int errorCode) {
        switch(errorCode) {
            case ERROR_NULL:  return "#NULL!";
            case ERROR_DIV_0: return "#DIV/0!";
            case INVALID_VALUE: return "#VALUE!";
            case ERROR_REF:   return "#REF!";
            case INVALID_NAME:  return "#NAME?";
            case ERROR_NUM:   return "#NUM!";
            case ERROR_NA:    return "#N/A";
            case INVALID_FORMULA:    return "#ERROR!"; // formula has error
        }
        return "#N/A";
//        throw new IllegalArgumentException("Bad error code (" + errorCode + ")");
    }

}
