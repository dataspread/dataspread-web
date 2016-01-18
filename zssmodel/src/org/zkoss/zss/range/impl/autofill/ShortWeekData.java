/* ShortWeekData.java

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
public class ShortWeekData extends CircularData { //ZSS-69
	private ShortWeekData(String[] data, int type, Locale locale) {
		super(data, type, locale);
	}
	private static final CacheMap _weekData;
	static {
		_weekData = new CacheMap(4);
		_weekData.setLifetime(24*60*60*1000);
	}
	public static ShortWeekData getInstance(int type, Locale locale) {
		final Pair key = new Pair(locale, Integer.valueOf(type));
		ShortWeekData value = (ShortWeekData) _weekData.get(key);
		if (value == null) { //create and cache
			DateFormatSymbols symbols = new DateFormatSymbols(locale);
			if (symbols == null) {
				symbols = new DateFormatSymbols(Locale.US);
			}
			String[] week8 = symbols.getShortWeekdays();
			String[] week7 = new String[7];
			System.arraycopy(week8, 1, week7, 0, 7);
			value = new ShortWeekData(week7, type, locale);
			_weekData.put(key, value);
		}
		return value;
	}
}
