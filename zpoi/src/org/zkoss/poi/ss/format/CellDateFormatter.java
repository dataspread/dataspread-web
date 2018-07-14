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

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;

import org.zkoss.poi.ss.format.CellNumberFormatter.FormatterType;
import org.zkoss.util.Pair;

/**
 * Formats a date value.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class CellDateFormatter extends CellFormatter {
    private boolean amPmUpper;
    private boolean showM;
    private boolean showAmPm;
    private final DateFormat dateFmt;
    private String sFmt;

    private static final long EXCEL_EPOCH_TIME;
    private static final Date EXCEL_EPOCH_DATE;

    //20111229, henrichen@zkoss.org: ZSS-68
	/*package*/ enum FormatterType {
		SIMPLE_DATE;
	}
    //20111229, henrichen@zkoss.org: ZSS-68
	static CellFormatter getFormatter(FormatterType ft, final Locale locale) {
		final Pair<FormatterType, Locale> key = new Pair<FormatterType, Locale>(ft, locale);
		CellFormatter formatter = (CellFormatter) _formatters.get(key);
		if (formatter != null) { //in cache, use it
			return formatter;
		}
		switch(ft) {
		case SIMPLE_DATE:
			formatter = new CellDateFormatter("mm/d/y", locale);
			break;
		}
		_formatters.put(key, formatter); //cache
		return formatter;	
	}
	
    static {
        Calendar c = Calendar.getInstance();
        c.set(1904, 0, 1, 0, 0, 0);
        EXCEL_EPOCH_DATE = c.getTime();
        EXCEL_EPOCH_TIME = c.getTimeInMillis();
    }

    private class DatePartHandler implements CellFormatPart.PartHandler {
        private int mStart = -1;
        private int mLen;
        private int hStart = -1;
        private int hLen;
        private int sStart = -1;
        private boolean everMinute = false;

        public String handlePart(Matcher m, String part, CellFormatType type,
                StringBuffer desc) {

            int pos = desc.length();
            char firstCh = part.charAt(0);
            switch (firstCh) {
            case 's':
            case 'S':
                if (!everMinute && mStart >= 0 && mLen <= 2) {
                    for (int i = 0; i < mLen; i++)
                        desc.setCharAt(mStart + i, 'm');
                    mStart = -1;
                    everMinute = true;
                } else {
                	sStart = pos;
                }
                return part.toLowerCase();

            case 'h':
            case 'H':
                mStart = -1;
                hStart = pos;
                hLen = part.length();
                return part.toLowerCase();

            case 'd':
            case 'D':
                mStart = -1;
                if (part.length() <= 2)
                    return part.toLowerCase();
                else
                    return part.toLowerCase().replace('d', 'E');

            case 'm':
            case 'M':
            	if (!everMinute && (hStart >= 0 || sStart >=0) && part.length() <= 2) { //20101201, henrichen@zkoss.org: m after h shall be a minute if length <= 2
            		everMinute = true;
            		return part.toLowerCase();
            	} else {
	                mStart = pos;
	                mLen = part.length();
	                return part.toUpperCase();
            	}

            case 'y':
            case 'Y':
                mStart = -1;
                if (part.length() == 3)
                    part = "yyyy";
                return part.toLowerCase();

            case '0':
                mStart = -1;
                int sLen = part.length();
                sFmt = "%0" + (sLen + 2) + "." + sLen + "f";
                return part.replace('0', 'S');

            case 'a':
            case 'A':
            case 'p':
            case 'P':
                if (part.length() > 1) {
                    // am/pm marker
                    mStart = -1;
                    showAmPm = true;
                    showM = Character.toLowerCase(part.charAt(1)) == 'm';
                    // For some reason "am/pm" becomes AM or PM, but "a/p" becomes a or p
                    amPmUpper = showM || Character.isUpperCase(part.charAt(0));

                    return "a";
                }
                //noinspection fallthrough

            default:
                return null;
            }
        }

        public void finish(StringBuffer toAppendTo) {
            if (hStart >= 0 && !showAmPm) {
                for (int i = 0; i < hLen; i++) {
                    toAppendTo.setCharAt(hStart + i, 'H');
                }
            }
        }
    }

    /**
     * Creates a new date formatter with the given specification.
     *
     * @param format The format.
     */
    public CellDateFormatter(String format, Locale locale) { //20111229, henrichen@zkoss.org: ZSS-68
        super(format, locale);
        DatePartHandler partHandler = new DatePartHandler();
        StringBuffer descBuf = CellFormatPart.parseFormat(format,
                CellFormatType.DATE, partHandler);
        partHandler.finish(descBuf);
        // tweak the format pattern to pass tests on JDK 1.7,
        // See https://issues.apache.org/bugzilla/show_bug.cgi?id=53369
        String ptrn = descBuf.toString().replaceAll("((y)(?!y))(?<!yy)", "yy");
        dateFmt = new SimpleDateFormat(ptrn, locale);
    }

    /** {@inheritDoc} */
    public void formatValue(StringBuffer toAppendTo, Object value) {
        if (value == null)
            value = 0.0;
        if (value instanceof Number) {
            Number num = (Number) value;
            double v = num.doubleValue();
            if (v == 0.0)
                value = EXCEL_EPOCH_DATE;
            else
                value = new Date((long) (EXCEL_EPOCH_TIME + v));
        }

        AttributedCharacterIterator it = dateFmt.formatToCharacterIterator(
                value);
        boolean doneAm = false;
        boolean doneMillis = false;

        it.first();
        for (char ch = it.first();
             ch != CharacterIterator.DONE;
             ch = it.next()) {
            if (it.getAttribute(DateFormat.Field.MILLISECOND) != null) {
                if (!doneMillis) {
                    Date dateObj = (Date) value;
                    int pos = toAppendTo.length();
                    Formatter formatter = new Formatter(toAppendTo, locale); //ZSS-68
                    long msecs = dateObj.getTime() % 1000;
                    formatter.format(locale, sFmt, msecs / 1000.0); //ZSS-68
                    toAppendTo.delete(pos, pos + 2);
                    doneMillis = true;
                }
            } else if (it.getAttribute(DateFormat.Field.AM_PM) != null) {
            	// 20131016, kuroridoplayer@gmail.com: handle whether AM, PM should showM or not 
            	// and also take care situation under other language(that don't use AM/PM).
                if (!doneAm) {
                	if(showAmPm) {
	                	if (Character.toUpperCase(ch) =='A' || Character.toUpperCase(ch) =='P') { // if A/a or P/p, handle showM 
	                	    if (amPmUpper) {
	                	    	toAppendTo.append(Character.toUpperCase(ch));
	                	        if (showM)
	                	            toAppendTo.append('M');
	                	    } else {
	                	    	toAppendTo.append(Character.toLowerCase(ch));
	                	        if (showM)
	                	            toAppendTo.append('m');
	                	    }
	                	    doneAm = true;
	                	} else {
	                		toAppendTo.append(ch); // append AM_PM field directly.
	                	}
                	}
                }
            } else {
                toAppendTo.append(ch);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For a date, this is <tt>"mm/d/y"</tt>.
     */
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        getFormatter(FormatterType.SIMPLE_DATE, locale).formatValue(toAppendTo, value); //ZSS-68
    }
}
