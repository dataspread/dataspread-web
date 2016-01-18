/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 23, 2014, Created by henrichen
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl.sys;

import java.util.Locale;

import org.zkoss.poi.ss.format.Formatters;
import org.zkoss.poi.ss.util.ToExcelNumberConverter;
import org.zkoss.zss.model.SCell.CellType;

/**
 * @author henrichen
 * @since 3.5.0
 */
public class NumberInputMaskImpl implements org.zkoss.zss.model.sys.input.NumberInputMask {
	@Override
	public Object[] parseNumberInput(String txt, Locale locale) {
		//TODO prepare a NumberInputMask that will set number format if input with comma thousand separator.
		//		final Locale locale = ZssContext.getCurrent().getLocale(); //ZSS-67
		final char dot = Formatters.getDecimalSeparator(locale);
		final char comma = Formatters.getGroupingSeparator(locale);
		String txt0 = txt;
		if (dot != '.' || comma != ',') {
			final int dotPos = txt.lastIndexOf(dot);
			txt0 = txt.replace(comma, ',');
			if (dotPos >= 0) {
				txt0 = txt0.substring(0, dotPos)+'.'+txt0.substring(dotPos+1);
			}
		}

		try {
			// ZSS-691
			final Double val = ToExcelNumberConverter.toExcelNumber(Double.parseDouble(txt0), false);
			return new Object[] {CellType.NUMBER, val}; //double
		} catch (NumberFormatException ex) {
			return new Object[] {txt, null};
		}
	}
}
