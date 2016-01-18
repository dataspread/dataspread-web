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

import org.zkoss.poi.hssf.util.HSSFColor;
import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.util.Locales;

import javax.swing.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zkoss.poi.ss.format.CellFormatter.logger;
import static org.zkoss.poi.ss.format.CellFormatter.quote;

/**
 * Objects of this class represent a single part of a cell format expression.
 * Each cell can have up to four of these for positive, zero, negative, and text
 * values.
 * <p/>
 * Each format part can contain a color, a condition, and will always contain a
 * format specification.  For example <tt>"[Red][>=10]#"</tt> has a color
 * (<tt>[Red]</tt>), a condition (<tt>>=10</tt>) and a format specification
 * (<tt>#</tt>).
 * <p/>
 * This class also contains patterns for matching the subparts of format
 * specification.  These are used internally, but are made public in case other
 * code has use for them.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class CellFormatPart {
	private final Locale locale;
    private final Color color;
    private CellFormatCondition condition;
    private final CellFormatter format;
    private final CellFormatType type;
    private final String prefix; //ZSS-881

    private static final Map<String, Color> NAMED_COLORS;

    static {
        NAMED_COLORS = new TreeMap<String, Color>(
                String.CASE_INSENSITIVE_ORDER);

        Map<Integer,HSSFColor> colors = HSSFColor.getIndexHash();
        for (HSSFColor color : colors.values()) {
            Class<? extends HSSFColor> type = color.getClass();
            String name = type.getSimpleName();
            if (name.equals(name.toUpperCase())) {
                short[] rgb = color.getTriplet();
                Color c = new Color(rgb[0], rgb[1], rgb[2]);
                NAMED_COLORS.put(name, c);
                if (name.indexOf('_') > 0)
                    NAMED_COLORS.put(name.replace('_', ' '), c);
                if (name.indexOf("_PERCENT") > 0)
                    NAMED_COLORS.put(name.replace("_PERCENT", "%").replace('_',
                            ' '), c);
            }
        }
    }

    /** Pattern for the color part of a cell format part. */
    public static final Pattern COLOR_PAT;
    /** Pattern for the condition part of a cell format part. */
    public static final Pattern CONDITION_PAT;
    /** Pattern for the format specification part of a cell format part. */
    public static final Pattern SPECIFICATION_PAT;
    /** Pattern for an entire cell single part. */
    public static final Pattern FORMAT_PAT;
    /** Pattern for an i18n part of a cell format. */
    public static final Pattern LOCALE_PAT; //20100615, Henri Chen

    /** Within {@link #FORMAT_PAT}, the group number for the matched locale code. */
    public static final int LOCALE_GROUP;
    
    /** Within {@link #FORMAT_PAT}, the group number for the matched color. */
    public static final int COLOR_GROUP;
    /**
     * Within {@link #FORMAT_PAT}, the group number for the operator in the
     * condition.
     */
    public static final int CONDITION_OPERATOR_GROUP;
    /**
     * Within {@link #FORMAT_PAT}, the group number for the value in the
     * condition.
     */
    public static final int CONDITION_VALUE_GROUP;
    /**
     * Within {@link #FORMAT_PAT}, the group number for the format
     * specification.
     */
    public static final int SPECIFICATION_GROUP;

    static {
    	// A locale specification
    	String locale = "\\[\\$.*\\-([0-9A-F]{1,8})\\]";
        // A condition specification
        String condition = "([<>=]=?|!=|<>)    # The operator\n" +
                "  \\s*([0-9]+(?:\\.[0-9]*)?)\\s*  # The constant to test against\n";

        String color =
                "\\[(black|blue|cyan|green|magenta|red|white|yellow|color [0-9]+)\\]";

        // A number specification
        // Note: careful that in something like ##, that the trailing comma is not caught up in the integer part

        // A part of a specification
        String part = "\\\\.                 # Quoted single character\n" +
                "|\"([^\\\\\"]|\\\\.)*\"         # Quoted string of characters (handles escaped quotes like \\\") \n" +
                "|_.                             # Space as wide as a given character\n" +
                "|\\*.                           # Repeating fill character\n" +
                "|@                              # Text: cell text\n" +
                "|([0?\\#](?:[0?\\#,]*))         # Number: digit + other digits and commas\n" +
                "|e[-+]                          # Number: Scientific: Exponent\n" +
                "|m{1,5}                         # Date: month or minute spec\n" +
                "|d{1,4}                         # Date: day/date spec\n" +
                "|y{2,4}                         # Date: year spec\n" +
                "|h{1,2}                         # Date: hour spec\n" +
                "|s{1,2}                         # Date: second spec\n" +
                "|am?/pm?                        # Date: am/pm spec\n" +
                "|\\[h{1,2}\\]                   # Elapsed time: hour spec\n" +
                "|\\[m{1,2}\\]                   # Elapsed time: minute spec\n" +
                "|\\[s{1,2}\\]                   # Elapsed time: second spec\n" +
                "|[^;]                           # A character\n" + "";

        String format = "(?:" + locale + ")?     # locale code\n" + 
        		"(?:" + color + ")?              # Text color\n" +
                "(?:\\[" + condition + "\\])?    # Condition\n" +
                "((?:" + part + ")+)             # Format spec\n";

        int flags = Pattern.COMMENTS | Pattern.CASE_INSENSITIVE;
        LOCALE_PAT = Pattern.compile(locale, flags); //20100615, Henri Chen
        COLOR_PAT = Pattern.compile(color, flags);
        CONDITION_PAT = Pattern.compile(condition, flags);
        SPECIFICATION_PAT = Pattern.compile(part, flags);
        FORMAT_PAT = Pattern.compile(format, flags);

        // Calculate the group numbers of important groups.  (They shift around
        // when the pattern is changed; this way we figure out the numbers by
        // experimentation.)

        LOCALE_GROUP = findGroup(FORMAT_PAT, "[$-409]@", "409"); //20100615, Henri Chen
        COLOR_GROUP = findGroup(FORMAT_PAT, "[Blue]@", "Blue");
        CONDITION_OPERATOR_GROUP = findGroup(FORMAT_PAT, "[>=1]@", ">=");
        CONDITION_VALUE_GROUP = findGroup(FORMAT_PAT, "[>=1]@", "1");
        SPECIFICATION_GROUP = findGroup(FORMAT_PAT, "[Blue][>1]\\a ?", "\\a ?");
    }

    interface PartHandler {
        String handlePart(Matcher m, String part, CellFormatType type,
                StringBuffer desc);
    }

    /**
     * Create an object to represent a format part.
     *
     * @param desc The string to parse.
     */
    public CellFormatPart(String desc) {
    	//ZSS-881
    	//preprocess [DBNum1][$-404]General
    	int j = desc.indexOf("][$");
    	if (j >= 0 && desc.startsWith("[")) {
    		prefix = desc.substring(1, j);
    		desc = desc.substring(j+1); //[$...
    	} else {
    		prefix = null;
    	}
    	
        Matcher m = FORMAT_PAT.matcher(desc);
        if (!m.matches()) {
            throw new IllegalArgumentException("Unrecognized format: " + quote(
                    desc));
        }
        Locale locale0 = getLocale(m); //20100616, Henri Chen
        locale = locale0;
        color = getColor(m);
        condition = getCondition(m);
        type = getCellFormatType(m);
        format = getFormatter(desc, m);
    }

    /**
     * Returns <tt>true</tt> if this format part applies to the given value. If
     * the value is a number and this is part has a condition, returns
     * <tt>true</tt> only if the number passes the condition.  Otherwise, this
     * always return <tt>true</tt>.
     *
     * @param valueObject The value to evaluate.
     *
     * @return <tt>true</tt> if this format part applies to the given value.
     */
    public boolean applies(Object valueObject) {
        if (condition == null || !(valueObject instanceof Number)) {
            if (valueObject == null)
                throw new NullPointerException("valueObject");
            return true;
        } else {
            Number num = (Number) valueObject;
            return condition.pass(num.doubleValue());
        }
    }

    /**
     * Returns the number of the first group that is the same as the marker
     * string.  The search starts with group 1.
     *
     * @param pat    The pattern to use.
     * @param str    The string to match against the pattern.
     * @param marker The marker value to find the group of.
     *
     * @return The matching group number.
     *
     * @throws IllegalArgumentException No group matches the marker.
     */
    private static int findGroup(Pattern pat, String str, String marker) {
        Matcher m = pat.matcher(str);
        if (!m.find())
            throw new IllegalArgumentException(
                    "Pattern \"" + pat.pattern() + "\" doesn't match \"" + str +
                            "\"");
        for (int i = 1; i <= m.groupCount(); i++) {
            String grp = m.group(i);
            if (grp != null && grp.equals(marker))
                return i;
        }
        throw new IllegalArgumentException(
                "\"" + marker + "\" not found in \"" + pat.pattern() + "\"");
    }

    //20100616, Henri Chen
    /**
     * Returns the locale specification from the matcher, or <tt>null</tt> if
     * there is none.
     * @param m The matcher for the format part.
     * @return The locale specification of <tt>null</tt>.
     */
    private static Locale getLocale(Matcher m) {
    	String ldesc = m.group(LOCALE_GROUP);
   		logger.finer("format locale: "+ldesc);
    	Locale l = getLocale(ldesc);
    	if (l == null)
    		logger.warning("Unknown locale: " + quote(ldesc));
    	return l;
    }
    
    private static Locale getLocale(String ldesc) {
    	Locale locale = null;
    	//ZSS-68
    	if (ldesc != null && !ldesc.isEmpty()) {
    		try {
	    		Integer code = Integer.parseInt(ldesc, 16);
	    		locale = LOCALES.get(code);
    		} catch (NumberFormatException ex) { 
    			// ignore
    		}
    	}
        return locale == null ? ZssContext.getCurrent().getLocale() : locale;
    }
    /**
     * Returns the color specification from the matcher, or <tt>null</tt> if
     * there is none.
     *
     * @param m The matcher for the format part.
     *
     * @return The color specification or <tt>null</tt>.
     */
    private static Color getColor(Matcher m) {
        String cdesc = m.group(COLOR_GROUP);
        if (cdesc == null || cdesc.length() == 0)
            return null;
        Color c = NAMED_COLORS.get(cdesc);
        if (c == null)
            logger.warning("Unknown color: " + quote(cdesc));
        return c;
    }

    /**
     * Returns the condition specification from the matcher, or <tt>null</tt> if
     * there is none.
     *
     * @param m The matcher for the format part.
     *
     * @return The condition specification or <tt>null</tt>.
     */
    private CellFormatCondition getCondition(Matcher m) {
        String mdesc = m.group(CONDITION_OPERATOR_GROUP);
        if (mdesc == null || mdesc.length() == 0)
            return null;
        return CellFormatCondition.getInstance(m.group(
                CONDITION_OPERATOR_GROUP), m.group(CONDITION_VALUE_GROUP));
    }

    /**
     * Returns the CellFormatType object implied by the format specification for
     * the format part.
     *
     * @param matcher The matcher for the format part.
     *
     * @return The CellFormatType.
     */
    private CellFormatType getCellFormatType(Matcher matcher) {
        String fdesc = matcher.group(SPECIFICATION_GROUP);
        return formatType(fdesc);
    }

    /**
     * Returns the formatter object implied by the format specification for the
     * format part.
     *
     * @param matcher The matcher for the format part.
     *
     * @return The formatter.
     */
    private CellFormatter getFormatter(String desc, Matcher matcher) {
        // ZSS-777
        // check currency
    	StringBuilder currency = new StringBuilder();
        if (type == CellFormatType.NUMBER && desc.startsWith("[$")) {
        	int j = desc.indexOf('-');
        	if (j < 0 ) {
        		j = desc.indexOf(']');
        	}
        	if (j >= 0) {
        		currency.append('"').append(desc.substring(2, j)).append('"');
        	}
        }

        String fdesc = matcher.group(SPECIFICATION_GROUP);
        //20141105, henrichen: handle [$HKD] special case
        if (type == CellFormatType.NUMBER && fdesc.startsWith("[$")) {
        	int j = fdesc.indexOf(']');
        	if (j >= 0) {
        		fdesc = fdesc.substring(j+1);
        	}
        }
        return type.formatter(currency+fdesc, locale); //ZSS-68
    }
 
    /**
     * Returns the type of format.
     *
     * @param fdesc The format specification
     *
     * @return The type of format.
     */
    private CellFormatType formatType(String fdesc) {
        fdesc = fdesc.trim();
        if (fdesc.equals("") || fdesc.equalsIgnoreCase("General"))
            return CellFormatType.GENERAL;

        //ZSS-777
        if (fdesc.startsWith("[$")) { //might be currency, filter it out!
        	int j = fdesc.indexOf(']');
        	if (j >= 0) {
        		fdesc = fdesc.substring(j+1);
        	}
        }
        
        Matcher m = SPECIFICATION_PAT.matcher(fdesc);
        boolean couldBeDate = false;
        boolean seenZero = false;
        while (m.find()) {
            String repl = m.group(0);
            if (repl.length() > 0) {
                switch (repl.charAt(0)) {
                case '@':
                    return CellFormatType.TEXT;
                case 'd':
                case 'D':
                case 'y':
                case 'Y':
                    return CellFormatType.DATE;
                case 'h':
                case 'H':
                case 'm':
                case 'M':
                case 's':
                case 'S':
                    // These can be part of date, or elapsed
                    couldBeDate = true;
                    break;
                case '0':
                    // This can be part of date, elapsed, or number
                    seenZero = true;
                    break;
                case '[':
                	// 20130620, paowang@potix.com: if token starts with "[", it could be "international number formats", "conditions" or any other
                	if(repl.length() >= 2) {
                		switch(repl.charAt(1)) { //20130620, paowang@potix.com: if and only if token's 2nd char. is 'h', 'm' or 's', this is a elapsed time format.
                			case 'h':
                			case 'm':
                			case 's':
                				return CellFormatType.ELAPSED;
                		}
                	}
                    break; // 20130620, paowang@potix.com: just break. it still could be any type.
                case '#':
                case '?':
                    return CellFormatType.NUMBER;
                }
            }
        }

        // Nothing definitive was found, so we figure out it deductively
        if (couldBeDate)
            return CellFormatType.DATE;
        if (seenZero)
            return CellFormatType.NUMBER;
        return CellFormatType.TEXT;
    }

    /**
     * Returns a version of the original string that has any special characters
     * quoted (or escaped) as appropriate for the cell format type.  The format
     * type object is queried to see what is special.
     *
     * @param repl The original string.
     * @param type The format type representation object.
     *
     * @return A version of the string with any special characters replaced.
     *
     * @see CellFormatType#isSpecial(char)
     */
    static String quoteSpecial(String repl, CellFormatType type) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < repl.length(); i++) {
            char ch = repl.charAt(i);
            if (ch == '\'' && type.isSpecial('\'')) {
                sb.append('\u0000');
                continue;
            }

            boolean special = type.isSpecial(ch);
            if (special)
                sb.append("'");
            sb.append(ch);
            if (special)
                sb.append("'");
        }
        return sb.toString();
    }

    /**
     * Apply this format part to the given value.  This returns a {@link
     * CellFormatResult} object with the results.
     *
     * @param value The value to apply this format part to.
     *
     * @return A {@link CellFormatResult} object containing the results of
     *         applying the format to the value.
     */
    public CellFormatResult apply(Object value) {
        boolean applies = applies(value);
        String text;
        Color textColor;
        if (applies) {
            text = format.format(value);
            textColor = color;
        } else {
            text = format.simpleFormat(value);
            textColor = null;
        }
        return new CellFormatResult(applies, text, textColor);
    }

    /**
     * Apply this format part to the given value, applying the result to the
     * given label.
     *
     * @param label The label
     * @param value The value to apply this format part to.
     *
     * @return <tt>true</tt> if the
     */
    public CellFormatResult apply(JLabel label, Object value) {
        CellFormatResult result = apply(value);
        label.setText(result.text);
        if (result.textColor != null) {
            label.setForeground(result.textColor);
        }
        return result;
    }

    /**
     * Returns the CellFormatType object implied by the format specification for
     * the format part.
     *
     * @return The CellFormatType.
     */
    public CellFormatType getCellFormatType() { //20120402, henrichen@zkoss.org: make public
        return type;
    }

    /**
     * Returns <tt>true</tt> if this format part has a condition.
     *
     * @return <tt>true</tt> if this format part has a condition.
     */
    boolean hasCondition() {
        return condition != null;
    }

    public static StringBuffer parseFormat(String fdesc, CellFormatType type,
            PartHandler partHandler) {

        // Quoting is very awkward.  In the Java classes, quoting is done
        // between ' chars, with '' meaning a single ' char. The problem is that
        // in Excel, it is legal to have two adjacent escaped strings.  For
        // example, consider the Excel format "\a\b#".  The naive (and easy)
        // translation into Java DecimalFormat is "'a''b'#".  For the number 17,
        // in Excel you would get "ab17", but in Java it would be "a'b17" -- the
        // '' is in the middle of the quoted string in Java.  So the trick we
        // use is this: When we encounter a ' char in the Excel format, we
        // output a \u0000 char into the string.  Now we know that any '' in the
        // output is the result of two adjacent escaped strings.  So after the
        // main loop, we have to do two passes: One to eliminate any ''
        // sequences, to make "'a''b'" become "'ab'", and another to replace any
        // \u0000 with '' to mean a quote char.  Oy.
        //
        // For formats that don't use "'" we don't do any of this
        Matcher m = SPECIFICATION_PAT.matcher(fdesc);
        StringBuffer fmt = new StringBuffer();
        while (m.find()) {
            String part = group(m, 0);
            if (part.length() > 0) {
                String repl = partHandler.handlePart(m, part, type, fmt);
                if (repl == null) {
                    switch (part.charAt(0)) {
                    case '\"':
                        repl = quoteSpecial(part.substring(1,
                                part.length() - 1), type);
                        break;
                    case '\\':
                        repl = quoteSpecial(part.substring(1), type);
                        break;
                    case '_':
                        repl = " ";
                        break;
                    case '*': //!! We don't do this for real, we just put in 3 of them
                        repl = ""; //expandChar(part); //20100924, henrichen@zkoss.org: DON'T expand, just clear it out!
                        break;
                    default:
                        repl = part;
                        break;
                    }
                }
                m.appendReplacement(fmt, Matcher.quoteReplacement(repl));
            }
        }
        m.appendTail(fmt);

        if (type.isSpecial('\'')) {
            // Now the next pass for quoted characters: Remove '' chars, making "'a''b'" into "'ab'"
            int pos = 0;
            while ((pos = fmt.indexOf("''", pos)) >= 0) {
                fmt.delete(pos, pos + 2);
            }

            // Now the final pass for quoted chars: Replace any \u0000 with ''
            pos = 0;
            while ((pos = fmt.indexOf("\u0000", pos)) >= 0) {
                fmt.replace(pos, pos + 1, "''");
            }
        }

        return fmt;
    }

    /**
     * Expands a character. This is only partly done, because we don't have the
     * correct info.  In Excel, this would be expanded to fill the rest of the
     * cell, but we don't know, in general, what the "rest of the cell" is.
     *
     * @param part The character to be repeated is the second character in this
     *             string.
     *
     * @return The character repeated three times.
     */
    static String expandChar(String part) {
        String repl;
        char ch = part.charAt(1);
        repl = "" + ch + ch + ch;
        return repl;
    }

    /**
     * Returns the string from the group, or <tt>""</tt> if the group is
     * <tt>null</tt>.
     *
     * @param m The matcher.
     * @param g The group number.
     *
     * @return The group or <tt>""</tt>.
     */
    public static String group(Matcher m, int g) {
        String str = m.group(g);
        return (str == null ? "" : str);
    }
    
    //ZSS-68 country code -> Java Locale 
    private static final Map<Integer, Locale> LOCALES = 
    		new HashMap<Integer, Locale>();
    
    static {
		LOCALES.put( 0x0436, new Locale("af"));
		LOCALES.put( 0x044E, new Locale("am"));
		LOCALES.put( 0x3801, new Locale("ar", "AE"));
		LOCALES.put( 0x3C01, new Locale("ar", "BH"));
		LOCALES.put( 0x1401, new Locale("ar", "DZ"));
		LOCALES.put( 0x0C01, new Locale("ar", "EG"));
		LOCALES.put( 0x0801, new Locale("ar", "IQ"));
		LOCALES.put( 0x2C01, new Locale("ar", "JO"));
		LOCALES.put( 0x3401, new Locale("ar", "KW"));
		LOCALES.put( 0x3001, new Locale("ar", "LB"));
		LOCALES.put( 0x1001, new Locale("ar", "LY"));
		LOCALES.put( 0x1801, new Locale("ar", "MA"));
		LOCALES.put( 0x2001, new Locale("ar", "OM"));
		LOCALES.put( 0x4001, new Locale("ar", "QA"));
		LOCALES.put( 0x0401, new Locale("ar", "SA"));
		LOCALES.put( 0x2801, new Locale("ar", "SY"));
		LOCALES.put( 0x1C01, new Locale("ar", "TN"));
		LOCALES.put( 0x2401, new Locale("ar", "YE"));
		LOCALES.put( 0x044D, new Locale("as"));
		LOCALES.put( 0x082C, new Locale("az", "AZ", "Cyrl"));
		LOCALES.put( 0x042C, new Locale("az", "AX", "Latn"));
		LOCALES.put( 0x0423, new Locale("be"));
		LOCALES.put( 0x0402, new Locale("bg"));
		LOCALES.put( 0x0845, new Locale("bn", "BD"));
		LOCALES.put( 0x0445, new Locale("bn", "IN"));
		LOCALES.put( 0x0451, new Locale("bo"));
		LOCALES.put( 0x141A, new Locale("bs"));
		LOCALES.put( 0x0403, new Locale("ca"));
		LOCALES.put( 0x0405, new Locale("cs"));
		LOCALES.put( 0x0452, new Locale("cy"));
		LOCALES.put( 0x0406, new Locale("da"));
		LOCALES.put( 0x0C07, new Locale("de", "AT"));
		LOCALES.put( 0x0807, new Locale("de", "CH"));
		LOCALES.put( 0x0407, new Locale("de", "DE"));
		LOCALES.put( 0x1407, new Locale("de", "LI"));
		LOCALES.put( 0x1007, new Locale("de", "LU"));
		LOCALES.put( 0x0465, new Locale("dv"));
		LOCALES.put( 0x0408, new Locale("el"));
		LOCALES.put( 0x0C09, new Locale("en", "AU"));
		LOCALES.put( 0x2809, new Locale("en", "BZ"));
		LOCALES.put( 0x1009, new Locale("en", "CA"));
		LOCALES.put( 0x2409, new Locale("en", "CB"));
		LOCALES.put( 0x0809, new Locale("en", "GB"));
		LOCALES.put( 0x1809, new Locale("en", "IE"));
		LOCALES.put( 0x4009, new Locale("en", "IN"));
		LOCALES.put( 0x2009, new Locale("en", "JM"));
		LOCALES.put( 0x1409, new Locale("en", "NZ"));
		LOCALES.put( 0x3409, new Locale("en", "PH"));
		LOCALES.put( 0x2C09, new Locale("en", "TT"));
		LOCALES.put( 0x0409, new Locale("en", "US"));
		LOCALES.put( 0x1C09, new Locale("en", "ZA"));
		LOCALES.put( 0x2C0A, new Locale("es", "AR"));
		LOCALES.put( 0x400A, new Locale("es", "BO"));
		LOCALES.put( 0x340A, new Locale("es", "CL"));
		LOCALES.put( 0x240A, new Locale("es", "CO"));
		LOCALES.put( 0x140A, new Locale("es", "CR"));
		LOCALES.put( 0x1C0A, new Locale("es", "DO"));
		LOCALES.put( 0x300A, new Locale("es", "EC"));
		LOCALES.put( 0x040A, new Locale("es", "ES"));
		LOCALES.put( 0x100A, new Locale("es", "GT"));
		LOCALES.put( 0x480A, new Locale("es", "HN"));
		LOCALES.put( 0x080A, new Locale("es", "MX"));
		LOCALES.put( 0x4C0A, new Locale("es", "NI"));
		LOCALES.put( 0x180A, new Locale("es", "PA"));
		LOCALES.put( 0x280A, new Locale("es", "PE"));
		LOCALES.put( 0x500A, new Locale("es", "PR"));
		LOCALES.put( 0x3C0A, new Locale("es", "PY"));
		LOCALES.put( 0x440A, new Locale("es", "SV"));
		LOCALES.put( 0x380A, new Locale("es", "UY"));
		LOCALES.put( 0x200A, new Locale("es", "VE"));
		LOCALES.put( 0x0425, new Locale("et"));
		LOCALES.put( 0x042D, new Locale("eu"));
		LOCALES.put( 0x0429, new Locale("fa"));
		LOCALES.put( 0x040B, new Locale("fi"));
		LOCALES.put( 0x0438, new Locale("fo"));
		LOCALES.put( 0x080C, new Locale("fr", "BE"));
		LOCALES.put( 0x0C0C, new Locale("fr", "CA"));
		LOCALES.put( 0x100C, new Locale("fr", "CH"));
		LOCALES.put( 0x040C, new Locale("fr", "FR"));
		LOCALES.put( 0x140C, new Locale("fr", "LU"));
		LOCALES.put( 0x043C, new Locale("gd"));
		LOCALES.put( 0x083C, new Locale("gd", "IE"));
		LOCALES.put( 0x0474, new Locale("gn"));
		LOCALES.put( 0x0447, new Locale("gu"));
		LOCALES.put( 0x040D, new Locale("he"));
		LOCALES.put( 0x0439, new Locale("hi"));
		LOCALES.put( 0x041A, new Locale("hr"));
		LOCALES.put( 0x040E, new Locale("hu"));
		LOCALES.put( 0x042B, new Locale("hy"));
		LOCALES.put( 0x0421, new Locale("id"));
		LOCALES.put( 0x040F, new Locale("is"));
		LOCALES.put( 0x0810, new Locale("it", "CH"));
		LOCALES.put( 0x0410, new Locale("it", "IT"));
		LOCALES.put( 0x0411, new Locale("ja"));
		LOCALES.put( 0x043F, new Locale("kk"));
		LOCALES.put( 0x0453, new Locale("km"));
		LOCALES.put( 0x044B, new Locale("kn"));
		LOCALES.put( 0x0412, new Locale("ko"));
		LOCALES.put( 0x0460, new Locale("ks"));
		LOCALES.put( 0x0476, new Locale("la"));
		LOCALES.put( 0x0454, new Locale("lo"));
		LOCALES.put( 0x0427, new Locale("lt"));
		LOCALES.put( 0x0426, new Locale("lv"));
		LOCALES.put( 0x0481, new Locale("mi"));
		LOCALES.put( 0x042F, new Locale("mk"));
		LOCALES.put( 0x044C, new Locale("ml"));
		LOCALES.put( 0x0850, new Locale("mn"));
		LOCALES.put( 0x0450, new Locale("mn"));
		LOCALES.put( 0x044E, new Locale("mr"));
		LOCALES.put( 0x083E, new Locale("ms", "BN"));
		LOCALES.put( 0x043E, new Locale("ms", "MY"));
		LOCALES.put( 0x043A, new Locale("mt"));
		LOCALES.put( 0x0455, new Locale("my"));
		LOCALES.put( 0x0461, new Locale("ne"));
		LOCALES.put( 0x0813, new Locale("nl", "BE"));
		LOCALES.put( 0x0413, new Locale("nl", "NL"));
		LOCALES.put( 0x0814, new Locale("no", "NO"));
		LOCALES.put( 0x0414, new Locale("no"));
		LOCALES.put( 0x0448, new Locale("or"));
		LOCALES.put( 0x0446, new Locale("pa"));
		LOCALES.put( 0x0415, new Locale("pl"));
		LOCALES.put( 0x0416, new Locale("pt", "BR"));
		LOCALES.put( 0x0816, new Locale("pt", "PT"));
		LOCALES.put( 0x0417, new Locale("rm"));
		LOCALES.put( 0x0418, new Locale("ro"));
		LOCALES.put( 0x0818, new Locale("ro", "MO"));
		LOCALES.put( 0x0419, new Locale("ru"));
		LOCALES.put( 0x0819, new Locale("ru", "MO"));
		LOCALES.put( 0x044F, new Locale("sa"));
		LOCALES.put( 0x042E, new Locale("sb"));
		LOCALES.put( 0x0459, new Locale("sd"));
		LOCALES.put( 0x045B, new Locale("si"));
		LOCALES.put( 0x041B, new Locale("sk"));
		LOCALES.put( 0x0424, new Locale("sl"));
		LOCALES.put( 0x0477, new Locale("so"));
		LOCALES.put( 0x041C, new Locale("sq"));
		LOCALES.put( 0x0C1A, new Locale("sr", "SP", "Cyrl"));
		LOCALES.put( 0x081A, new Locale("sr", "SP", "Latn"));
		LOCALES.put( 0x081D, new Locale("sv", "FI"));
		LOCALES.put( 0x041D, new Locale("sv", "SE"));
		LOCALES.put( 0x0441, new Locale("sw"));
		LOCALES.put( 0x0449, new Locale("ta"));
		LOCALES.put( 0x044A, new Locale("te"));
		LOCALES.put( 0x0428, new Locale("tg"));
		LOCALES.put( 0x041E, new Locale("th"));
		LOCALES.put( 0x0442, new Locale("tk"));
		LOCALES.put( 0x0432, new Locale("tn"));
		LOCALES.put( 0x041F, new Locale("tr"));
		LOCALES.put( 0x0431, new Locale("ts"));
		LOCALES.put( 0x0444, new Locale("tt"));
		LOCALES.put( 0x0422, new Locale("uk"));
		LOCALES.put( 0x0420, new Locale("ur"));
		LOCALES.put( 0x0000, new Locale("UTF_8"));
		LOCALES.put( 0x0843, new Locale("uz", "UZ", "Cyrl"));
		LOCALES.put( 0x0443, new Locale("uz", "UZ", "Latn"));
		LOCALES.put( 0x042A, new Locale("vi"));
		LOCALES.put( 0x0434, new Locale("xh"));
		LOCALES.put( 0x043D, new Locale("yi"));
		LOCALES.put( 0x0804, new Locale("zh", "CN"));
		LOCALES.put( 0x0C04, new Locale("zh", "HK"));
		LOCALES.put( 0x1404, new Locale("zh", "MO"));
		LOCALES.put( 0x1004, new Locale("zh", "SG"));
		LOCALES.put( 0x0404, new Locale("zh", "TW"));
		LOCALES.put( 0x0435, new Locale("zu"));
    };
}