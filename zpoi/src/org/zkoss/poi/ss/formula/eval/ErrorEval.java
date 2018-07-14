/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.zkoss.poi.ss.formula.eval;

import org.zkoss.poi.ss.usermodel.ErrorConstants;
import org.zkoss.poi.ss.usermodel.Hyperlink;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author henrichen@zkoss.org: HYPERLINK function
 */
public final class ErrorEval implements ValueEval, HyperlinkEval {

    // convenient access to namespace
    private static final ErrorConstants EC = null;

    /** <b>#NULL!</b>  - Intersection of two cell ranges is empty */
    public static final ErrorEval NULL_INTERSECTION = new ErrorEval(EC.ERROR_NULL);
    /** <b>#DIV/0!</b> - Division by zero */
    public static final ErrorEval DIV_ZERO = new ErrorEval(EC.ERROR_DIV_0);
    /** <b>#VALUE!</b> - Wrong type of operand */
    public static final ErrorEval VALUE_INVALID = new ErrorEval(EC.ERROR_VALUE);
    /** <b>#REF!</b> - Illegal or deleted cell reference */
    public static final ErrorEval REF_INVALID = new ErrorEval(EC.ERROR_REF);
    /** <b>#NAME?</b> - Wrong function or range name */
    public static final ErrorEval NAME_INVALID = new ErrorEval(EC.ERROR_NAME);
    /** <b>#NUM!</b> - Value range overflow */
    public static final ErrorEval NUM_ERROR = new ErrorEval(EC.ERROR_NUM);
    /** <b>#N/A</b> - Argument or function not available */
    public static final ErrorEval NA = new ErrorEval(EC.ERROR_NA);

    //ZSS-810
    public static final ErrorEval FORMULA_INVALID = new ErrorEval(EC.ERROR_FORMULA);

    // POI internal error codes
    private static final int CIRCULAR_REF_ERROR_CODE = 0xFFFFFFC4;
    private static final int FUNCTION_NOT_IMPLEMENTED_CODE = 0xFFFFFFE2;

    // Note - Excel does not seem to represent this condition with an error code
    public static final ErrorEval CIRCULAR_REF_ERROR = new ErrorEval(CIRCULAR_REF_ERROR_CODE);


    /**
     * Translates an Excel internal error code into the corresponding POI ErrorEval instance
     * @param errorCode
     */
    public static ErrorEval valueOf(int errorCode) {
        switch(errorCode) {
            case ErrorConstants.ERROR_NULL:  return NULL_INTERSECTION;
            case ErrorConstants.ERROR_DIV_0: return DIV_ZERO;
            case ErrorConstants.ERROR_VALUE: return VALUE_INVALID;
            case ErrorConstants.ERROR_REF:   return REF_INVALID;
            case ErrorConstants.ERROR_NAME:  return NAME_INVALID;
            case ErrorConstants.ERROR_NUM:   return NUM_ERROR;
            case ErrorConstants.ERROR_NA:    return NA;
            // non-std errors (conditions modeled as errors by POI)
            case CIRCULAR_REF_ERROR_CODE:        return CIRCULAR_REF_ERROR;
            case ErrorConstants.ERROR_FORMULA: return FORMULA_INVALID;
        }
        throw new RuntimeException("Unexpected error code (" + errorCode + ")");
    }

    /**
     * Converts error codes to text.  Handles non-standard error codes OK.  
     * For debug/test purposes (and for formatting error messages).
     * @return the String representation of the specified Excel error code.
     */
    public static String getText(int errorCode) {
        if(ErrorConstants.isValidCode(errorCode)) {
            return ErrorConstants.getText(errorCode);
        }
        // It is desirable to make these (arbitrary) strings look clearly different from any other
        // value expression that might appear in a formula.  In addition these error strings should
        // look unlike the standard Excel errors.  Hence tilde ('~') was used.
        switch(errorCode) {
            case CIRCULAR_REF_ERROR_CODE: return "~CIRCULAR~REF~";
            case FUNCTION_NOT_IMPLEMENTED_CODE: return "~FUNCTION~NOT~IMPLEMENTED~";
        }
        return "~non~std~err(" + errorCode + ")~";
    }

    private int _errorCode;
    /**
     * @param errorCode an 8-bit value
     */
    private ErrorEval(int errorCode) {
        _errorCode = errorCode;
    }

    public int getErrorCode() {
        return _errorCode;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(getText(_errorCode));
        sb.append("]");
        return sb.toString();
    }
    
	//20100720, henrichen@zkoss.org: handle HYPERLINK function
	private Hyperlink _hyperlink;
	public void setHyperlink(Hyperlink hyperlink) {
		_hyperlink = hyperlink;
	}
	
	public Hyperlink getHyperlink() {
		return _hyperlink;
	}
	//20101221, henrichen@zkoss.org: error text tip!
    public static String getTooltip(int errorCode) {
        if(!ErrorConstants.isValidCode(errorCode)) {
	        switch(errorCode) {
	            case CIRCULAR_REF_ERROR_CODE: return "Error: Circular dependency detected";
	            case FUNCTION_NOT_IMPLEMENTED_CODE: return "~FUNCTION~NOT~IMPLEMENTED~";
	        }
	        return "Error: Unknown error code ("+errorCode+")";
        }
        return null;
    }
}
