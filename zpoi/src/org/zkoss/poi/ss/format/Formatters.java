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
package org.zkoss.poi.ss.format;

import java.text.DecimalFormatSymbols;
import java.util.Formatter;
import java.util.Locale;

import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.util.Locales;

/**
 * Utility class for CellFormat.
 * @author henrichen
 *
 */
public class Formatters {
	public static void format(Locale locale, Formatter formatter, String format, Object... args) { //ZSS-68
		formatter.format(locale, format, args);
	}
	
	/**
	 * Returns the GroupingSeparator character of the current locale.
	 * @return the GroupingSeparator character of the current locale.
	 */
	public static char getGroupingSeparator(Locale locale) { //ZSS-68
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		return symbols.getGroupingSeparator();
	}
	
	/**
	 * Returns the getDecimalSeparator character of the current locale.
	 * @return the getDecimalSeparator character of the current locale.
	 */
	public static char getDecimalSeparator(Locale locale) { //ZSS-68
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		return symbols.getDecimalSeparator();
	}
	
	/**
	 * Returns the getCurrencySymbol character of the current locale.
	 * @return the getCurrencySymbol character of the current locale.
	 */
	public static String getCurrencySymbol(Locale locale) { //ZSS-629
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		return symbols.getCurrencySymbol();
	}
}
