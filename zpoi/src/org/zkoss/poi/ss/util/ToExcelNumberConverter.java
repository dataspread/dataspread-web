/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 11, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.poi.ss.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author henri
 * @since 3.5.0
 */
public class ToExcelNumberConverter {
	public static double toExcelNumber(double value, boolean rounding) {
		final String v = Double.toString(value);
		if (v.length() < 16) return value;
		DecimalFormat formatter = new DecimalFormat("0.##############E0", new DecimalFormatSymbols(Locale.US));
		formatter.setRoundingMode(rounding ? RoundingMode.HALF_UP : RoundingMode.DOWN);
		final String vstr = formatter.format(value);
		return Double.parseDouble(vstr);
	}
}
