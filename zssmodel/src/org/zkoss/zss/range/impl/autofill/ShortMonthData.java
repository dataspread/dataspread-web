/* FullWeekData.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 29, 2011 5:21:07 PM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.zss.range.impl.autofill;

import java.text.DateFormatSymbols;
import java.util.Locale;

import org.zkoss.util.CacheMap;
import org.zkoss.util.Pair;

/**
 * @author henrichen
 *
 */
public class ShortMonthData extends CircularData { //ZSS-69
	private ShortMonthData(String[] data, int type, Locale locale) {
		super(data, type, locale);
	}
	private static final CacheMap _monthData;
	static {
		_monthData = new CacheMap(4);
		_monthData.setLifetime(24*60*60*1000);
	}
	public static ShortMonthData getInstance(int type, Locale locale) {
		final Pair key = new Pair(locale, Integer.valueOf(type));
		ShortMonthData value = (ShortMonthData) _monthData.get(key);
		if (value == null) { //create and cache
			DateFormatSymbols symbols = new DateFormatSymbols(locale);
			if (symbols == null) {
				symbols = new DateFormatSymbols(Locale.US);
			}
			String[] month13 = symbols.getShortMonths();
			String[] month12 = new String[12];
			System.arraycopy(month13, 0, month12, 0, 12);
			value = new ShortMonthData(month12, type, locale);
			_monthData.put(key, value);
		}
		return value;
	}
}
