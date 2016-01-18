/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.zkoss.poi.ss.usermodel;

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.zkoss.util.CacheMap;
import org.zkoss.util.Pair;

/**
 * Utility to identify built-in formats.  The following is a list of the formats as
 * returned by this class.<p/>
 *<p/>
 *       0, "General"<br/>
 *       1, "0"<br/>
 *       2, "0.00"<br/>
 *       3, "#,##0"<br/>
 *       4, "#,##0.00"<br/>
 *       5, "$#,##0_);($#,##0)"<br/>
 *       6, "$#,##0_);[Red]($#,##0)"<br/>
 *       7, "$#,##0.00);($#,##0.00)"<br/>
 *       8, "$#,##0.00_);[Red]($#,##0.00)"<br/>
 *       9, "0%"<br/>
 *       0xa, "0.00%"<br/>
 *       0xb, "0.00E+00"<br/>
 *       0xc, "# ?/?"<br/>
 *       0xd, "# ??/??"<br/>
 *       0xe, "m/d/yy"<br/>
 *       0xf, "d-mmm-yy"<br/>
 *       0x10, "d-mmm"<br/>
 *       0x11, "mmm-yy"<br/>
 *       0x12, "h:mm AM/PM"<br/>
 *       0x13, "h:mm:ss AM/PM"<br/>
 *       0x14, "h:mm"<br/>
 *       0x15, "h:mm:ss"<br/>
 *       0x16, "m/d/yy h:mm"<br/>
 *<p/>
 *       // 0x17 - 0x24 reserved for international and undocumented
 *       0x25, "#,##0_);(#,##0)"<br/>
 *       0x26, "#,##0_);[Red](#,##0)"<br/>
 *       0x27, "#,##0.00_);(#,##0.00)"<br/>
 *       0x28, "#,##0.00_);[Red](#,##0.00)"<br/>
 *       0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)"<br/>
 *       0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)"<br/>
 *       0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)"<br/>
 *       0x2c, "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)"<br/>
 *       0x2d, "mm:ss"<br/>
 *       0x2e, "[h]:mm:ss"<br/>
 *       0x2f, "mm:ss.0"<br/>
 *       0x30, "##0.0E+0"<br/>
 *       0x31, "@" - This is text format.<br/>
 *       0x31  "text" - Alias for "@"<br/>
 * <p/>
 *
 * @author Yegor Kozlov
 *
 * Modified 6/17/09 by Stanislav Shor - positive formats don't need starting '('
 *
 */
public final class BuiltinFormats {
	/**
	 * The first user-defined format starts at 164.
	 */
	public static final int FIRST_USER_DEFINED_FORMAT_INDEX = 164;

	private final static String[] _formats;

/*
0 General General 18 Time h:mm AM/PM
1 Decimal 0 19 Time h:mm:ss AM/PM
2 Decimal 0.00 20 Time h:mm
3 Decimal #,##0 21 Time h:mm:ss
4 Decimal #,##0.00 2232 Date/Time M/D/YY h:mm
531 Currency "$"#,##0_);("$"#,##0) 37 Account. _(#,##0_);(#,##0)
631 Currency "$"#,##0_);[Red]("$"#,##0) 38 Account. _(#,##0_);[Red](#,##0)
731 Currency "$"#,##0.00_);("$"#,##0.00) 39 Account. _(#,##0.00_);(#,##0.00)
831 Currency "$"#,##0.00_);[Red]("$"#,##0.00) 40 Account. _(#,##0.00_);[Red](#,##0.00)
9 Percent 0% 4131 Currency _("$"* #,##0_);_("$"* (#,##0);_("$"* "-"_);_(@_)
10 Percent 0.00% 4231 33 Currency _(* #,##0_);_(* (#,##0);_(* "-"_);_(@_)
11 Scientific 0.00E+00 4331 Currency _("$"* #,##0.00_);_("$"* (#,##0.00);_("$"* "-"??_);_(@_)
12 Fraction # ?/? 4431 33 Currency _(* #,##0.00_);_(* (#,##0.00);_(* "-"??_);_(@_)
13 Fraction # ??/?? 45 Time mm:ss
1432 Date M/D/YY 46 Time [h]:mm:ss
15 Date D-MMM-YY 47 Time mm:ss.0
16 Date D-MMM 48 Scientific ##0.0E+0
17 Date MMM-YY 49 Text @
* */
	static {
		List<String> m = new ArrayList<String>();
		putFormat(m, 0, "General");
		putFormat(m, 1, "0");
		putFormat(m, 2, "0.00");
		putFormat(m, 3, "#,##0");
		putFormat(m, 4, "#,##0.00");
		
		putFormat(m, 5, "\"$\"#,##0_);\\(\"$\"#,##0\\)");
		putFormat(m, 6, "\"$\"#,##0_);[Red]\\(\"$\"#,##0\\)");
		putFormat(m, 7, "\"$\"#,##0.00_);\\(\"$\"#,##0.00\\)");
		putFormat(m, 8, "\"$\"#,##0.00_);[Red]\\(\"$\"#,##0.00\\)");
		putFormat(m, 9, "0%");
		putFormat(m, 0xa, "0.00%");
		putFormat(m, 0xb, "0.00E+00");
		putFormat(m, 0xc, "# ?/?");
		putFormat(m, 0xd, "# ??/??");
		putFormat(m, 0xe, "m/d/yyyy"); //20111229, henrichen@zkoss.org: ZSS-68
		putFormat(m, 0xf, "d-mmm-yy");
		putFormat(m, 0x10, "d-mmm");
		putFormat(m, 0x11, "mmm-yy");
		putFormat(m, 0x12, "h:mm AM/PM");
		putFormat(m, 0x13, "h:mm:ss AM/PM");
		putFormat(m, 0x14, "h:mm");
		putFormat(m, 0x15, "h:mm:ss");
		putFormat(m, 0x16, "m/d/yyyy h:mm"); //20111229, henrichen@zkoss.org: ZSS-68
		
//		putFormat(m, 5, "\"$\"#,##0_);(\"$\"#,##0)");
//		putFormat(m, 6, "\"$\"#,##0_);[Red](\"$\"#,##0)");
//		putFormat(m, 7, "\"$\"#,##0.00_);(\"$\"#,##0.00)");
//		putFormat(m, 8, "\"$\"#,##0.00_);[Red](\"$\"#,##0.00)");
//		putFormat(m, 9, "0%");
//		putFormat(m, 0xa, "0.00%");
//		putFormat(m, 0xb, "0.00E+00");
//		putFormat(m, 0xc, "# ?/?");
//		putFormat(m, 0xd, "# ??/??");
//		putFormat(m, 0xe, "m/d/yyyy"); //20111229, henrichen@zkoss.org: ZSS-68
//		putFormat(m, 0xf, "d-mmm-yy");
//		putFormat(m, 0x10, "d-mmm");
//		putFormat(m, 0x11, "mmm-yy");
//		putFormat(m, 0x12, "h:mm AM/PM");
//		putFormat(m, 0x13, "h:mm:ss AM/PM");
//		putFormat(m, 0x14, "h:mm");
//		putFormat(m, 0x15, "h:mm:ss");
//		putFormat(m, 0x16, "m/d/yyyy h:mm"); //20111229, henrichen@zkoss.org: ZSS-68

		// 0x17 - 0x24 reserved for international and undocumented
		for (int i=0x17; i<=0x24; i++) {
			// TODO - one junit relies on these values which seems incorrect
			putFormat(m, i, "reserved-0x" + Integer.toHexString(i));
		}

		putFormat(m, 0x25, "#,##0_);\\(#,##0\\)");
		putFormat(m, 0x26, "#,##0_);[Red]\\(#,##0\\)");
		putFormat(m, 0x27, "#,##0.00_);\\(#,##0.00\\)");
		putFormat(m, 0x28, "#,##0.00_);[Red]\\(#,##0.00\\)");
		putFormat(m, 0x29, "_(* #,##0_);_(* \\(#,##0\\);_(* \"-\"_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
		putFormat(m, 0x2a, "_(\"$\"* #,##0_);_(\"$\"* \\(#,##0\\);_(\"$\"* \"-\"_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
		putFormat(m, 0x2b, "_(* #,##0.00_);_(* \\(#,##0.00\\);_(* \"-\"??_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
		putFormat(m, 0x2c, "_(\"$\"* #,##0.00_);_(\"$\"* \\(#,##0.00\\);_(\"$\"* \"-\"??_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
		
//		putFormat(m, 0x25, "#,##0_);(#,##0)");
//		putFormat(m, 0x26, "#,##0_);[Red](#,##0)");
//		putFormat(m, 0x27, "#,##0.00_);(#,##0.00)");
//		putFormat(m, 0x28, "#,##0.00_);[Red](#,##0.00)");
//		putFormat(m, 0x29, "_(* #,##0_);_(* (#,##0);_(* \"-\"_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
//		putFormat(m, 0x2a, "_($* #,##0_);_($* (#,##0);_($* \"-\"_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
//		putFormat(m, 0x2b, "_(* #,##0.00_);_(* (#,##0.00);_(* \"-\"??_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
//		putFormat(m, 0x2c, "_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)"); //20100924, henrichen@zkoss.org: space after *(star)
		putFormat(m, 0x2d, "mm:ss");
		putFormat(m, 0x2e, "[h]:mm:ss");
		putFormat(m, 0x2f, "mm:ss.0");
		putFormat(m, 0x30, "##0.0E+0");
		putFormat(m, 0x31, "@");
		String[] ss = new String[m.size()];
		m.toArray(ss);
		_formats = ss;
	}
	private static void putFormat(List<String> m, int index, String value) {
		if (m.size() != index) {
			throw new IllegalStateException("index " + index  + " is wrong");
		}
		m.add(value);
	}


	/**
	 * @deprecated (May 2009) use {@link #getAll()}
	 */
	public static Map<Integer, String> getBuiltinFormats() {
		Map<Integer, String> result = new LinkedHashMap<Integer, String>();
		for (int i=0; i<_formats.length; i++) {
			result.put(Integer.valueOf(i), _formats[i]);
		}
		return result;
	}

	/**
	 * @return array of built-in data formats
	 */
	public static String[] getAll() {
		return _formats.clone();
	}

	/**
	 * Get the format string that matches the given format index
	 *
	 * @param index of a built in format
	 * @return string represented at index of format or <code>null</code> if there is not a built-in format at that index
	 */
	public static String getBuiltinFormat(int index) {
		if (index < 0 || index >=_formats.length) {
			return null;
		}
		return _formats[index];
	}

	/**
	 * Get the format index that matches the given format string
	 * <p/>
	 * Automatically converts "text" to excel's format string to represent text.
	 * </p>
	 * @param pFmt string matching a built-in format
	 * @return index of format or -1 if undefined.
	 */
	public static int getBuiltinFormat(String pFmt) {
		String fmt;
		if (pFmt.equalsIgnoreCase("TEXT")) {
			fmt = "@";
		} else {
			fmt = pFmt;
		}

		for(int i =0; i< _formats.length; i++) {
			if(fmt.equals(_formats[i])) {
				return i;
			}
		}
		return -1;
	}
	
	//20111229, henrichen@zkoss.org: ZSS-68
	public static String getBuiltinFormat(int index, Locale locale) {
		String fmt = getBuiltinFormat(index);
		if (fmt == null && index != 0x100) {
			return null;
		}
		if (Locale.US.equals(locale)) { //US locale, let go
			return fmt;
		}
		//per locale @see DateInputMask
		switch(index) {
		case 0xf: //d-mmm-yy
		case 0x10: //d-mmm
		case 0x11: //mmm-yy
			fmt = getNameMonthFormat(index, locale);
			break;
		case 0xe: //m/d/yyyy
		case 0x13: //h:mm:ss AM/PM
		case 0x16: //m/d/yyyy hh:mm
		case 0x100: //special date time for input edit text
			fmt = getNumMonthFormat(index, locale);
			break;
		}
		return fmt;
	}
	
	//20111229, henrichen@zkoss.org: ZSS-68
	private static final CacheMap _dateFormat;
	static {
		_dateFormat = new CacheMap(8);
		_dateFormat.setLifetime(24*60*60*1000);
	}

	//20111229, henrichen@zkoss.org: support locale
	private static String getNameMonthFormat(int formatType, Locale locale) {
		final Pair key = new Pair(locale, Integer.valueOf(formatType));
		final String result = (String) _dateFormat.get(key);
		if (result != null) { //already cached
			return result;
		}
		
		boolean noyear = false;
		boolean noday = false;
		switch(formatType) {
		case 0xf: //d-mmm-yy
			//with date and year 
			break;
		case 0x10: //d-mmm
			noyear = true;
			break;
		case 0x11: //mmm-yy
			noday = true;
			break;
		}
		DateFormat format = DateFormat.getDateInstance(DateFormat.LONG, locale);
		final TimeZone gmt = TimeZone.getTimeZone("GMT");
		format.setTimeZone(gmt);
		final Calendar cal = Calendar.getInstance(gmt);
		cal.set(65, 6, 18);
		final AttributedCharacterIterator iter 
			= format.formatToCharacterIterator(cal.getTime());
		final StringBuilder sb = new StringBuilder();
		int dcount = 0;
		int mcount = 0;
		int ycount = 0;
		int cindex = -2;
		for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			final Map attrs = iter.getAttributes();
			if (attrs.isEmpty()) {
				if (mcount > 0 || dcount > 0 || ycount > 0) {
					if (cindex < 0)
						cindex = sb.length();
					sb.append(c);
				}
			} else {
				for(DateFormat.Field field : (Set<DateFormat.Field>) attrs.keySet()) {
					switch(field.getCalendarField()) {
					case Calendar.DAY_OF_MONTH:
						if (!noday) {
							sb.append('d');
							++dcount;
						} else if (cindex >= 0){
							sb.delete(cindex, sb.length());
						}
						cindex = -2;
						break;
					case Calendar.YEAR:
						if (!noyear) {
							if (ycount < 2) {
								sb.append('y');
								++ycount;
							}
						} else if (cindex >= 0) {
							sb.delete(cindex, sb.length());
						}
						cindex = -2;
						break;
					case Calendar.MONTH:
						if (mcount < 3) {
							sb.append('m');
							++mcount;
						}
						cindex = -2;
						break;
					}
				}
			}
		}
		
		if ((noday || noyear) && cindex > 0) {
			sb.delete(cindex, sb.length());
		}
		
		//cache it
		final String formatString = sb.toString(); 
		_dateFormat.put(key, formatString);
		return formatString;
	}
	
	//20111229, henrichen@zkoss.org: support locale
	private static String getNumMonthFormat(int formatType, Locale locale) {
		final Pair key = new Pair(locale, Integer.valueOf(formatType));
		final String result = (String) _dateFormat.get(key);
		if (result != null) { //already cached
			return result;
		}
		
		final TimeZone gmt = TimeZone.getTimeZone("GMT");
		DateFormat format = null;
		switch(formatType) {
		case 0x13: //TIME
			//return "hh:mm:ss AM/PM" in US locale
			//ZSS-379: dirty trick; in Excel TW time format == US
			if ("zh".equals(locale.getLanguage())) {
				final String formatString = "hh:mm:ss AM/PM"; 
				_dateFormat.put(key, formatString);
				return formatString;
			}
			format = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
			break;
		case 0xe: //DATE
			//return "m/d/yyyy" in US locale;
			format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			break;
		case 0x16: //DATE_TIME (for text)
			//return "m/d/yyyy hh:mm" in US locale
			format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
			break;
		case 0x100: //DATE_TIME (for edit text)
			//return "m/d/yyyy hh:mm:ss AM/PM" in US locale
			//ZSS-379: dirty trick; in Excel TW time format == US
			if ("zh".equals(locale.getLanguage())) {
				final String formatString = "yyyy/m/d hh:mm:ss AM/PM"; 
				_dateFormat.put(key, formatString);
				return formatString;
			} else {
				format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
			}
			break;
		}
		format.setTimeZone(gmt);
		final Calendar cal = Calendar.getInstance(gmt);
		cal.set(1234, 5, 6, 7, 8, 9);
		final AttributedCharacterIterator iter 
			= format.formatToCharacterIterator(cal.getTime());
		final StringBuilder sb = new StringBuilder();
		int ycount = 0;
		int yindex = -1;
		int mcount = 0;
		boolean alreadyAMPM = false;
		for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			final Map attrs = iter.getAttributes();
			if (attrs.isEmpty()) {
				sb.append(c);
			} else {
				for(DateFormat.Field field : (Set<DateFormat.Field>) attrs.keySet()) {
					switch(field.getCalendarField()) {
					case Calendar.DAY_OF_MONTH:
						sb.append('d');
						break;
					case Calendar.YEAR:
						yindex = sb.length();
						sb.append('y');
						++ycount;
						break;
					case Calendar.HOUR_OF_DAY:
					case Calendar.HOUR:
						sb.append('h');
						break;
					case Calendar.MONTH: //
						if (mcount < 2) {
							sb.append('m');
							++mcount;
						}
						break;
					case Calendar.MINUTE:
						sb.append('m');
						break;
					case Calendar.SECOND:
						sb.append('s');
						break;
					case Calendar.AM:
					case Calendar.AM_PM:
						if (!alreadyAMPM) {
							sb.append("AM/PM");
							alreadyAMPM = true;
						}
						break;
					default: //neither cases
						if (DateFormat.Field.HOUR1.equals(field)
							|| DateFormat.Field.HOUR_OF_DAY1.equals(field)
							|| DateFormat.Field.HOUR0.equals(field)
							|| DateFormat.Field.HOUR_OF_DAY0.equals(field)) {
							sb.append('h');
						}
						break;
					}
				}
			}
		}
		if (yindex >= 0) { //add year to 4
			for(int k = ycount; k < 4; ++k) {
				sb.insert(yindex, 'y');
			}
		}
		
		//cache it
		final String formatString = sb.toString(); 
		_dateFormat.put(key, formatString);
		return formatString;
	}
}
