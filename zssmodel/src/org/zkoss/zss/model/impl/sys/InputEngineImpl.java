/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl.sys;

import java.util.Locale;

import org.zkoss.poi.ss.usermodel.ErrorConstants;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.sys.input.InputEngine;
import org.zkoss.zss.model.sys.input.InputParseContext;
import org.zkoss.zss.model.sys.input.InputResult;
import org.zkoss.zss.model.sys.input.NumberInputMask;
/**
 * 
 * @author Hawk
 * @since 3.5.0
 */
public class InputEngineImpl implements InputEngine{

	public static DateInputMask _dateInputMask = new DateInputMask();
	public static NumberInputMask _numberInputMask = new NumberInputMaskImpl();

	@Override
	public InputResult parseInput(String editText, String formatPattern, InputParseContext context) {
		InputResultImpl result = null;
		if (editText != null) {
			result = new InputResultImpl();
			if("".equals(editText)){ //to compatible with 3.0, a blank string should become a blank input, not empty string
				result.setType(CellType.BLANK);
				result.setValue(null);
			}else{
				Object[] convertedResult = editTextToValue(editText, formatPattern, context.getLocale());
				result.setType((CellType)convertedResult[0]);
				result.setValue(convertedResult[1]);
				if(convertedResult.length>2){//with format
					result.setFormat((String)convertedResult[2]);
				}
			}
		}
		return result;

	}

	/**
	 * 
	 * @param txt the text to be input
	 * @param formatPattern the cell text format 
	 * @return object array with the value type in 0(an Integer), 
	 * 		the value in 1(an Object), and the date format in 2(a String if parse as a date)   
	 */
	private Object[] editTextToValue(String txt, String formatPattern, Locale locale) {
		if (txt != null) {
			//bug #300:	Numbers in Text-cells are not treated as text (leading zero is removed)
			if (formatPattern != null) {
				if (isStringFormat(formatPattern)) { 
					return new Object[] {CellType.STRING, txt}; //string
				}
			}
			if (txt.startsWith("=")) {
				if (txt.trim().length() > 1) {
					return new Object[] {CellType.FORMULA, txt.substring(1)}; //formula 
				} else {
					return new Object[] {CellType.STRING, txt}; //string
				}
			} else if ("true".equalsIgnoreCase(txt) || "false".equalsIgnoreCase(txt)) {
				return new Object[] {CellType.BOOLEAN, Boolean.valueOf(txt)}; //boolean
			} else if (txt.startsWith("#")) { //might be an error
				final byte err = getErrorCode(txt);
				return err < 0 ? 
						new Object[] {CellType.STRING, txt}: //string
							new Object[] {CellType.ERROR, new Byte(err)}; //error
			} else {
				return parseEditTextToDoubleDateOrString(txt, locale); //ZSS-67
			}
		}
		return null;
	}

	private Object[] parseEditTextToDoubleDateOrString(String txt, Locale locale) {
		//ZSS-665
		final Object[] results = _numberInputMask.parseNumberInput(txt, locale);
		if (results[0] instanceof String) { 
			return parseEditTextToDateOrString(txt, locale);
		} else { //result[0] is number
			return results;
		}
	}

	private Object[] parseEditTextToDateOrString(String txt, Locale locale) {
		final Object[] results = _dateInputMask.parseDateInput(txt, locale); 
		if (results[0] instanceof String) { 
			return new Object[] {CellType.STRING, results[0]}; 
		} else { // result[0] is Date
			return new Object[] {CellType.NUMBER, results[0], results[1]}; //date with format
		}
	}

	private byte getErrorCode(String errString) {
		if ("#NULL!".equals(errString))
			return ErrorConstants.ERROR_NULL;
		if ("#DIV/0!".equals(errString))
			return ErrorConstants.ERROR_DIV_0;
		if ("#VALUE!".equals(errString))
			return ErrorConstants.ERROR_VALUE;
		if ("#REF!".equals(errString))
			return ErrorConstants.ERROR_REF;
		if ("#NAME?".equals(errString))
			return ErrorConstants.ERROR_NAME;
		if ("#NUM!".equals(errString))
			return ErrorConstants.ERROR_NUM; 
		if ("#N/A".equals(errString))
			return ErrorConstants.ERROR_NA;
		return -1;
	}

	private boolean isStringFormat(String formatStr) {
		return "@".equals(formatStr); //TODO, shall prepare a regular expression match check!
	}

}
