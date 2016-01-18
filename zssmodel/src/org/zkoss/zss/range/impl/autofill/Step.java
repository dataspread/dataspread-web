/* Step.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 29, 2011 2:24:14 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.zss.range.impl.autofill;

import org.zkoss.zss.model.SCell;

/**
 * Step for handling incremental auto fill
 * @author henrichen
 * @since 2.1.0
 */
public interface Step {
	public static final int STRING = 0;
	public static final int SHORT_WEEK = 1;
	public static final int SHORT_MONTH = 2;
	public static final int FULL_WEEK = 3;
	public static final int FULL_MONTH = 4;
	public static final int NUMBER = 5;
	public static final int DATE = 6;
	public static final int TIME = 7;
	public static final int BLANK = 8;
	public static final int US_SHORT_WEEK = 0x11; //ZSS-69
	public static final int US_SHORT_MONTH = 0x12; //ZSS-69
	public static final int US_FULL_WEEK = 0x13; //ZSS-69
	public static final int US_FULL_MONTH = 0x14; //ZSS-69
	
	/** Return next value of this step sequence per the source cell.
	 * 
	 * @param srcCell the source cell for filling
	 * @return next value of this step per the source cell
	 */
	public Object next(SCell srcCell); //return next value of the incremental sequence
	/**
	 * Returns the data type of this Step.
	 * @return the data type of this Step.
	 */
	public int getDataType();
}
