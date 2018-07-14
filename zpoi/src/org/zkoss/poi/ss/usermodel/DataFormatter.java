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

   2012 - Alfresco Software, Ltd.
   Alfresco Software has modified source of this file
   The details of changes as svn diff can be found in svn at location root/projects/3rd-party/src 
==================================================================== */
package org.zkoss.poi.ss.usermodel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.poi.ss.formula.eval.NotImplementedException;

/**
 * DataFormatter contains methods for formatting the value stored in an
 * Cell. This can be useful for reports and GUI presentations when you
 * need to display data exactly as it appears in Excel. Supported formats
 * include currency, SSN, percentages, decimals, dates, phone numbers, zip
 * codes, etc.
 * <p>
 * Internally, formats will be implemented using subclasses of {@link Format}
 * such as {@link DecimalFormat} and {@link SimpleDateFormat}. Therefore the
 * formats used by this class must obey the same pattern rules as these Format
 * subclasses. This means that only legal number pattern characters ("0", "#",
 * ".", "," etc.) may appear in number formats. Other characters can be
 * inserted <em>before</em> or <em> after</em> the number pattern to form a
 * prefix or suffix.
 * </p>
 * <p>
 * For example the Excel pattern <code>"$#,##0.00 "USD"_);($#,##0.00 "USD")"
 * </code> will be correctly formatted as "$1,000.00 USD" or "($1,000.00 USD)".
 * However the pattern <code>"00-00-00"</code> is incorrectly formatted by
 * DecimalFormat as "000000--". For Excel formats that are not compatible with
 * DecimalFormat, you can provide your own custom {@link Format} implementation
 * via <code>DataFormatter.addFormat(String,Format)</code>. The following
 * custom formats are already provided by this class:
 * </p>
 * <pre>
 * <ul><li>SSN "000-00-0000"</li>
 *     <li>Phone Number "(###) ###-####"</li>
 *     <li>Zip plus 4 "00000-0000"</li>
 * </ul>
 * </pre>
 * <p>
 * If the Excel format pattern cannot be parsed successfully, then a default
 * format will be used. The default number format will mimic the Excel General
 * format: "#" for whole numbers and "#.##########" for decimal numbers. You
 * can override the default format pattern with <code>
 * DataFormatter.setDefaultNumberFormat(Format)</code>. <b>Note:</b> the
 * default format will only be used when a Format cannot be created from the
 * cell's data format string.
 *
 * <p>
 * Note that by default formatted numeric values are trimmed.
 * Excel formats can contain spacers and padding and the default behavior is to strip them off.
 * </p>
 * <p>Example:</p>
 * <p>
 * Consider a numeric cell with a value <code>12.343</code> and format <code>"##.##_ "</code>.
 *  The trailing underscore and space ("_ ") in the format adds a space to the end and Excel formats this cell as <code>"12.34 "</code>,
 *  but <code>DataFormatter</code> trims the formatted value and returns <code>"12.34"</code>.
 * </p>
 * You can enable spaces by passing the <code>emulateCsv=true</code> flag in the <code>DateFormatter</code> cosntructor.
 * If set to true, then the output tries to conform to what you get when you take an xls or xlsx in Excel and Save As CSV file:
 * <ul>
 *  <li>returned values are not trimmed</li>
 *  <li>Invalid dates are formatted as  255 pound signs ("#")</li>
 *  <li>simulate Excel's handling of a format string of all # when the value is 0.
 *   Excel will output "", <code>DataFormatter</code> will output "0".
 * </ul>
 * @author James May (james dot may at fmr dot com)
 * @author Robert Kish
  *
 */
public class DataFormatter {

    /** Pattern to find a number format: "0" or  "#" */
    private static final Pattern numPattern = Pattern.compile("[0#]+");

    /** Pattern to find days of week as text "ddd...." */
    private static final Pattern daysAsText = Pattern.compile("([d]{3,})", Pattern.CASE_INSENSITIVE);

    /** Pattern to find "AM/PM" marker */
    private static final Pattern amPmPattern = Pattern.compile("((A|P)[M/P]*)", Pattern.CASE_INSENSITIVE);

    /** 
     * A regex to find locale patterns like [$$-1009] and [$?-452].
     * Note that we don't currently process these into locales 
     */
    private static final Pattern localePatternGroup = Pattern.compile("(\\[\\$[^-\\]]*-[0-9A-Z]+\\])");

    /**
     * A regex to match the colour formattings rules.
     * Allowed colours are: Black, Blue, Cyan, Green,
     *  Magenta, Red, White, Yellow, "Color n" (1<=n<=56)
     */
    private static final Pattern colorPattern = 
       Pattern.compile("(\\[BLACK\\])|(\\[BLUE\\])|(\\[CYAN\\])|(\\[GREEN\\])|" +
       		"(\\[MAGENTA\\])|(\\[RED\\])|(\\[WHITE\\])|(\\[YELLOW\\])|" +
       		"(\\[COLOR\\s*\\d\\])|(\\[COLOR\\s*[0-5]\\d\\])", Pattern.CASE_INSENSITIVE);

    /**
      * Cells formatted with a date or time format and which contain invalid date or time values
     *  show 255 pound signs ("#").
      */
     private static final String invalidDateTimeString;
     static {
         StringBuilder buf = new StringBuilder();
         for(int i = 0; i < 255; i++) buf.append('#');
         invalidDateTimeString = buf.toString();
     }

    /**
     * The decimal symbols of the locale used for formatting values.
     */
    private final DecimalFormatSymbols decimalSymbols;

    /**
     * The date symbols of the locale used for formatting values.
     */
    private final DateFormatSymbols dateSymbols;

    /** <em>General</em> format for whole numbers. */
    private final Format generalWholeNumFormat;

    /** <em>General</em> format for decimal numbers. */
    private final Format generalDecimalNumFormat;

    /** A default format to use when a number pattern cannot be parsed. */
    private Format defaultNumFormat;

    /**
     * A map to cache formats.
     *  Map<String,Format> formats
     */
    private final Map<String,Format> formats;

    private boolean emulateCsv = false;
    private Locale locale = Locale.getDefault();

    /**
     * Creates a formatter using the {@link Locale#getDefault() default locale}.
     */
    public DataFormatter() {
        this(false);
    }

    /**
     * Creates a formatter using the {@link Locale#getDefault() default locale}.
     *
     * @param  emulateCsv whether to emulate CSV output.
     */
    public DataFormatter(boolean emulateCsv) {
        this(Locale.getDefault());
        this.emulateCsv = emulateCsv;
    }

    /**
     * Creates a formatter using the given locale.
     *
     * @param  emulateCsv whether to emulate CSV output.
     */
    public DataFormatter(Locale locale, boolean emulateCsv) {
        this(locale);
        this.emulateCsv = emulateCsv;
    }

    /**
     * Creates a formatter using the given locale.
     */
    public DataFormatter(Locale locale) {
    	this.locale= locale; //20111229, henrichen@zkoss.org: ZSS-68
        dateSymbols = new DateFormatSymbols(locale);
        decimalSymbols = new DecimalFormatSymbols(locale);
        generalWholeNumFormat = new DecimalFormat("#", decimalSymbols);
        generalDecimalNumFormat = new DecimalFormat("#.##########", decimalSymbols);

        formats = new HashMap<String,Format>();

        // init built-in formats

        Format zipFormat = ZipPlusFourFormat.instance;
        addFormat("00000\\-0000", zipFormat);
        addFormat("00000-0000", zipFormat);

        Format phoneFormat = PhoneFormat.instance;
        // allow for format string variations
        addFormat("[<=9999999]###\\-####;\\(###\\)\\ ###\\-####", phoneFormat);
        addFormat("[<=9999999]###-####;(###) ###-####", phoneFormat);
        addFormat("###\\-####;\\(###\\)\\ ###\\-####", phoneFormat);
        addFormat("###-####;(###) ###-####", phoneFormat);

        Format ssnFormat = SSNFormat.instance;
        addFormat("000\\-00\\-0000", ssnFormat);
        addFormat("000-00-0000", ssnFormat);
    }

    /**
     * Return a Format for the given cell if one exists, otherwise try to
     * create one. This method will return <code>null</code> if the any of the
     * following is true:
     * <ul>
     * <li>the cell's style is null</li>
     * <li>the style's data format string is null or empty</li>
     * <li>the format string cannot be recognized as either a number or date</li>
     * </ul>
     *
     * @param cell The cell to retrieve a Format for
     * @return A Format for the format String
     */
    private Format getFormat(Cell cell) {
        if ( cell.getCellStyle() == null) {
            return null;
        }

        int formatIndex = cell.getCellStyle().getDataFormat();
        String formatStr = cell.getCellStyle().getDataFormatString();
        if(formatStr == null || formatStr.trim().length() == 0) {
            return null;
        }
        return getFormat(cell.getNumericCellValue(), formatIndex, formatStr);
    }

    private Format getFormat(double cellValue, int formatIndex, String formatStrIn) {
//      // Might be better to separate out the n p and z formats, falling back to p when n and z are not set.
//      // That however would require other code to be re factored.
//      String[] formatBits = formatStrIn.split(";");
//      int i = cellValue > 0.0 ? 0 : cellValue < 0.0 ? 1 : 2; 
//      String formatStr = (i < formatBits.length) ? formatBits[i] : formatBits[0];

        String formatStr = formatStrIn;
        // Excel supports positive/negative/zero, but java
        // doesn't, so we need to do it specially
        final int firstAt = formatStr.indexOf(';');
        final int lastAt = formatStr.lastIndexOf(';');
        // p and p;n are ok by default. p;n;z and p;n;z;s need to be fixed.
        if (firstAt != -1 && firstAt != lastAt) {
            final int secondAt = formatStr.indexOf(';', firstAt + 1);
            if (secondAt == lastAt) { // p;n;z
                if (cellValue == 0.0) {
                    formatStr = formatStr.substring(lastAt + 1);
                } else {
                    formatStr = formatStr.substring(0, lastAt);
                }
            } else {
                if (cellValue == 0.0) { // p;n;z;s
                    formatStr = formatStr.substring(secondAt + 1, lastAt);
                } else {
                    formatStr = formatStr.substring(0, secondAt);
                }
            }
        }

       // Excel's # with value 0 will output empty where Java will output 0. This hack removes the # from the format.
       if (emulateCsv && cellValue == 0.0 && formatStr.contains("#") && !formatStr.contains("0")) {
           formatStr = formatStr.replaceAll("#", "");
       }
       
        // See if we already have it cached
        Format format = formats.get(formatStr);
        if (format != null) {
            return format;
        }
        
        // Is it one of the special built in types, General or @?
        if ("General".equalsIgnoreCase(formatStr) || "@".equals(formatStr)) {
            if (isWholeNumber(cellValue)) {
                return generalWholeNumFormat;
            }
            return generalDecimalNumFormat;
        }
        
        // Build a formatter, and cache it
        format = createFormat(cellValue, formatIndex, formatStr);
        formats.put(formatStr, format);
        return format;
    }

    /**
     * Create and return a Format based on the format string from a  cell's
     * style. If the pattern cannot be parsed, return a default pattern.
     *
     * @param cell The Excel cell
     * @return A Format representing the excel format. May return null.
     */
    public Format createFormat(Cell cell) {

        int formatIndex = cell.getCellStyle().getDataFormat();
        String formatStr = cell.getCellStyle().getDataFormatString();
        return createFormat(cell.getNumericCellValue(), formatIndex, formatStr);
    }

    private Format createFormat(double cellValue, int formatIndex, String sFormat) {
        String formatStr = sFormat;
        
        // Remove colour formatting if present
        Matcher colourM = colorPattern.matcher(formatStr);
        while(colourM.find()) {
           String colour = colourM.group();
           
           // Paranoid replacement...
           int at = formatStr.indexOf(colour);
           if(at == -1) break;
           String nFormatStr = formatStr.substring(0,at) +
              formatStr.substring(at+colour.length());
           if(nFormatStr.equals(formatStr)) break;

           // Try again in case there's multiple
           formatStr = nFormatStr;
           colourM = colorPattern.matcher(formatStr);
        }
        
        //ZSS-881
        // preprocess [DBNum1][$-404]General
        // TODO:Strip off the prefix information, we have not known how to deal with the prefix
    	int j = formatStr.indexOf("][$");
    	if (j >= 0 && formatStr.startsWith("[")) {
    		formatStr = formatStr.substring(j+1); //[$...
    	}        
    	
    	// Strip off the locale information, we use an instance-wide locale for everything
        Matcher m = localePatternGroup.matcher(formatStr);
        while(m.find()) {
            String match = m.group();
            String symbol = match.substring(match.indexOf('$') + 1, match.indexOf('-'));
            if (symbol.indexOf('$') > -1) {
                StringBuffer sb = new StringBuffer();
                sb.append(symbol.substring(0, symbol.indexOf('$')));
                sb.append('\\');
                sb.append(symbol.substring(symbol.indexOf('$'), symbol.length()));
                symbol = sb.toString();
            }
            formatStr = m.replaceAll(symbol);
            m = localePatternGroup.matcher(formatStr);
        }

        // Check for special cases
        if(formatStr == null || formatStr.trim().length() == 0) {
            return getDefaultFormat(cellValue);
        }
        
        if ("General".equalsIgnoreCase(formatStr) || "@".equals(formatStr)) {
           if (isWholeNumber(cellValue)) {
               return generalWholeNumFormat;
           }
           return generalDecimalNumFormat;
        }

        if(DateUtil.isADateFormat(formatIndex,formatStr) &&
                DateUtil.isValidExcelDate(cellValue)) {
            return createDateFormat(formatStr, cellValue);
        }
        
        // Excel supports fractions in format strings, which Java doesn't
        if (formatStr.indexOf("#/#") >= 0 || formatStr.indexOf("?/?") >= 0) {
            // Strip custom text in quotes and escaped characters for now as it can cause performance problems in fractions.
        	String strippedFormatStr = formatStr.replaceAll("\\\\ ", " ").replaceAll("\\\\.", "").replaceAll("\"[^\"]*\"", " ");

        	boolean ok = true;
        	for (String part: strippedFormatStr.split(";")) {
        		int indexOfFraction = indexOfFraction(part);
        		if (indexOfFraction == -1 || indexOfFraction != lastIndexOfFraction(part)) {
        			ok = false;
        			break;
        		}
        	}
            if (ok) {
                return new FractionFormat(strippedFormatStr);
            }
        }
        
        if (numPattern.matcher(formatStr).find()) {
            return createNumberFormat(formatStr, cellValue);
        }

        if (emulateCsv) {
            return new ConstantStringFormat(cleanFormatForNumber(formatStr));
        }
        // TODO - when does this occur?
        return null;
    }
    
    private int indexOfFraction(String format) {
    	int i = format.indexOf("#/#");
    	int j = format.indexOf("?/?");
    	return i == -1 ? j : j == -1 ? i : Math.min(i,  j);
    }

    private int lastIndexOfFraction(String format) {
    	int i = format.lastIndexOf("#/#");
    	int j = format.lastIndexOf("?/?");
    	return i == -1 ? j : j == -1 ? i : Math.max(i,  j);
    }

    private Format createDateFormat(String pFormatStr, double cellValue) {
        String formatStr = pFormatStr;
        formatStr = formatStr.replaceAll("\\\\-","-");
        formatStr = formatStr.replaceAll("\\\\,",",");
        formatStr = formatStr.replaceAll("\\\\\\.","."); // . is a special regexp char
        formatStr = formatStr.replaceAll("\\\\ "," ");
        formatStr = formatStr.replaceAll("\\\\/","/"); // weird: m\\/d\\/yyyy 
        formatStr = formatStr.replaceAll(";@", "");
        formatStr = formatStr.replaceAll("\"/\"", "/"); // "/" is escaped for no reason in: mm"/"dd"/"yyyy

        boolean hasAmPm = false;
        Matcher amPmMatcher = amPmPattern.matcher(formatStr);
        while (amPmMatcher.find()) {
            formatStr = amPmMatcher.replaceAll("@");
            hasAmPm = true;
            amPmMatcher = amPmPattern.matcher(formatStr);
        }
        formatStr = formatStr.replaceAll("@", "a");


        Matcher dateMatcher = daysAsText.matcher(formatStr);
        if (dateMatcher.find()) {
            String match = dateMatcher.group(0);
            formatStr = dateMatcher.replaceAll(match.toUpperCase().replaceAll("D", "E"));
        }

        // Convert excel date format to SimpleDateFormat.
        // Excel uses lower and upper case 'm' for both minutes and months.
        // From Excel help:
        /*
            The "m" or "mm" code must appear immediately after the "h" or"hh"
            code or immediately before the "ss" code; otherwise, Microsoft
            Excel displays the month instead of minutes."
          */

        StringBuffer sb = new StringBuffer();
        char[] chars = formatStr.toCharArray();
        boolean mIsMonth = true;
        List<Integer> ms = new ArrayList<Integer>();
        boolean isElapsed = false;
        for(int j=0; j<chars.length; j++) {
            char c = chars[j];
            if (c == '[' && !isElapsed) {
                isElapsed = true;
                mIsMonth = false;
                sb.append(c);
            }
            else if (c == ']' && isElapsed) {
                isElapsed = false;
                sb.append(c);
            }
            else if (isElapsed) {
            if (c == 'h' || c == 'H') {
                    sb.append('H');
                }
                else if (c == 'm' || c == 'M') {
                    sb.append('m');
                }
                else if (c == 's' || c == 'S') {
                    sb.append('s');
                }
                else {
                    sb.append(c);
                }
            }
            else if (c == 'h' || c == 'H') {
                mIsMonth = false;
                if (hasAmPm) {
                    sb.append('h');
                } else {
                    sb.append('H');
                }
            }
            else if (c == 'm' || c == 'M') {
                if(mIsMonth) {
                    sb.append('M');
                    ms.add(
                            Integer.valueOf(sb.length() -1)
                    );
                } else {
                    sb.append('m');
                }
            }
            else if (c == 's' || c == 'S') {
                sb.append('s');
                // if 'M' precedes 's' it should be minutes ('m')
                for (int i = 0; i < ms.size(); i++) {
                    int index = ms.get(i).intValue();
                    if (sb.charAt(index) == 'M') {
                        sb.replace(index, index+1, "m");
                    }
                }
                mIsMonth = true;
                ms.clear();
            }
            else if (Character.isLetter(c)) {
                mIsMonth = true;
                ms.clear();
                if (c == 'y' || c == 'Y') {
                    sb.append('y');
                }
                else if (c == 'd' || c == 'D') {
                    sb.append('d');
                }
                else {
                    sb.append(c);
                }
            }
            else {
                sb.append(c);
            }
        }
        formatStr = sb.toString();

        try {
        	//20111229, henrichen@zkoss.org: ZSS-68
            //return new ExcelStyleDateFormatter(formatStr, dateSymbols);
        	ExcelStyleDateFormatter dateFormat = new ExcelStyleDateFormatter(formatStr, locale);
        	dateFormat.setDateFormatSymbols(dateSymbols);
        	return dateFormat;
        } catch(IllegalArgumentException iae) {

            // the pattern could not be parsed correctly,
            // so fall back to the default number format
            return getDefaultFormat(cellValue);
        }

    }

    private String cleanFormatForNumber(String formatStr) {
        StringBuffer sb = new StringBuffer(formatStr);

        if (emulateCsv) {
            // Requested spacers with "_" are replaced by a single space.
            // Full-column-width padding "*" are removed.
            // Not processing fractions at this time. Replace ? with space.
            // This matches CSV output.
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '_' || c == '*' || c == '?') {
                    if (i > 0 && sb.charAt((i - 1)) == '\\') {
                        // It's escaped, don't worry
                        continue;
                    }
                    if (c == '?') {
                        sb.setCharAt(i, ' ');
                    } else if (i < sb.length() - 1) {
                        // Remove the character we're supposed
                        //  to match the space of / pad to the
                        //  column width with
                        if (c == '_') {
                            sb.setCharAt(i + 1, ' ');
                        } else {
                            sb.deleteCharAt(i + 1);
                        }
                        // Remove the character too
                        sb.deleteCharAt(i);
                    }
                }
            }
        } else {
            // If they requested spacers, with "_",
            //  remove those as we don't do spacing
            // If they requested full-column-width
            //  padding, with "*", remove those too
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '_' || c == '*') {
                    if (i > 0 && sb.charAt((i - 1)) == '\\') {
                        // It's escaped, don't worry
                        continue;
                    }
                    if (i < sb.length() - 1) {
                        // Remove the character we're supposed
                        //  to match the space of / pad to the
                        //  column width with
                        sb.deleteCharAt(i + 1);
                    }
                    // Remove the _ too
                    sb.deleteCharAt(i);
                }
            }
        }

        // Now, handle the other aspects like 
        //  quoting and scientific notation
        for(int i = 0; i < sb.length(); i++) {
           char c = sb.charAt(i);
            // remove quotes and back slashes
            if (c == '\\' || c == '"') {
                sb.deleteCharAt(i);
                i--;

            // for scientific/engineering notation
            } else if (c == '+' && i > 0 && sb.charAt(i - 1) == 'E') {
                sb.deleteCharAt(i);
                i--;
            }
        }

        return sb.toString();
    }

    private Format createNumberFormat(String formatStr, double cellValue) {
        final String format = cleanFormatForNumber(formatStr);
        
        try {
            DecimalFormat df = new DecimalFormat(format, decimalSymbols);
            setExcelStyleRoundingMode(df);
            return df;
        } catch(IllegalArgumentException iae) {

            // the pattern could not be parsed correctly,
            // so fall back to the default number format
            return getDefaultFormat(cellValue);
        }
    }

    /**
     * Return true if the double value represents a whole number
     * @param d the double value to check
     * @return <code>true</code> if d is a whole number
     */
    private static boolean isWholeNumber(double d) {
        return d == Math.floor(d);
    }

    /**
     * Returns a default format for a cell.
     * @param cell The cell
     * @return a default format
     */
    public Format getDefaultFormat(Cell cell) {
        return getDefaultFormat(cell.getNumericCellValue());
    }
    private Format getDefaultFormat(double cellValue) {
        // for numeric cells try user supplied default
        if (defaultNumFormat != null) {
            return defaultNumFormat;

          // otherwise use general format
        }
        if (isWholeNumber(cellValue)){
            return generalWholeNumFormat;
        }
        return generalDecimalNumFormat;
    }
    
    /**
     * Performs Excel-style date formatting, using the
     *  supplied Date and format
     */
    private String performDateFormatting(Date d, Format dateFormat) {
       if(dateFormat != null) {
          return dateFormat.format(d);
      }
      return d.toString();
    }

    /**
     * Returns the formatted value of an Excel date as a <tt>String</tt> based
     * on the cell's <code>DataFormat</code>. i.e. "Thursday, January 02, 2003"
     * , "01/02/2003" , "02-Jan" , etc.
     *
     * @param cell The cell
     * @return a formatted date string
     */
    private String getFormattedDateString(Cell cell) {
        Format dateFormat = getFormat(cell);
        if(dateFormat instanceof ExcelStyleDateFormatter) {
           // Hint about the raw excel value
           ((ExcelStyleDateFormatter)dateFormat).setDateToBeFormatted(
                 cell.getNumericCellValue()
           );
        }
        Date d = cell.getDateCellValue();
        return performDateFormatting(d, dateFormat);
    }

    /**
     * Returns the formatted value of an Excel number as a <tt>String</tt>
     * based on the cell's <code>DataFormat</code>. Supported formats include
     * currency, percents, decimals, phone number, SSN, etc.:
     * "61.54%", "$100.00", "(800) 555-1234".
     *
     * @param cell The cell
     * @return a formatted number string
     */
    private String getFormattedNumberString(Cell cell) {

        Format numberFormat = getFormat(cell);
        double d = cell.getNumericCellValue();
        if (numberFormat == null) {
            return String.valueOf(d);
        }
        return numberFormat.format(new Double(d));
    }

    /**
     * Formats the given raw cell value, based on the supplied
     *  format index and string, according to excel style rules.
     * @see #formatCellValue(Cell)
     */
    public String formatRawCellContents(double value, int formatIndex, String formatString) {
       return formatRawCellContents(value, formatIndex, formatString, false);
    }
    /**
     * Formats the given raw cell value, based on the supplied
     *  format index and string, according to excel style rules.
     * @see #formatCellValue(Cell)
     */
    public String formatRawCellContents(double value, int formatIndex, String formatString, boolean use1904Windowing) {
        // Is it a date?
        if(DateUtil.isADateFormat(formatIndex,formatString)) {
            if(DateUtil.isValidExcelDate(value)) {
                Format dateFormat = getFormat(value, formatIndex, formatString);
                if(dateFormat instanceof ExcelStyleDateFormatter) {
                   // Hint about the raw excel value
                   ((ExcelStyleDateFormatter)dateFormat).setDateToBeFormatted(value);
                }
                Date d = DateUtil.getJavaDate(value, use1904Windowing);
                return performDateFormatting(d, dateFormat);
            }
             // RK: Invalid dates are 255 #s.
             if (emulateCsv) {
                 return invalidDateTimeString;
             }
        }
        // else Number
            Format numberFormat = getFormat(value, formatIndex, formatString);
            if (numberFormat == null) {
                return String.valueOf(value);
            }
            // RK: This hack handles scientific notation by adding the missing + back.
            String result = numberFormat.format(new Double(value));
            if (result.contains("E") && !result.contains("E-")) {
                result = result.replaceFirst("E", "E+");
            }
            return result;
    }

    /**
     * <p>
     * Returns the formatted value of a cell as a <tt>String</tt> regardless
     * of the cell type. If the Excel format pattern cannot be parsed then the
     * cell value will be formatted using a default format.
     * </p>
     * <p>When passed a null or blank cell, this method will return an empty
     * String (""). Formulas in formula type cells will not be evaluated.
     * </p>
     *
     * @param cell The cell
     * @return the formatted cell value as a String
     */
    public String formatCellValue(Cell cell) {
        return formatCellValue(cell, null);
    }

    /**
     * <p>
     * Returns the formatted value of a cell as a <tt>String</tt> regardless
     * of the cell type. If the Excel format pattern cannot be parsed then the
     * cell value will be formatted using a default format.
     * </p>
     * <p>When passed a null or blank cell, this method will return an empty
     * String (""). Formula cells will be evaluated using the given
     * {@link FormulaEvaluator} if the evaluator is non-null. If the
     * evaluator is null, then the formula String will be returned. The caller
     * is responsible for setting the currentRow on the evaluator
     *</p>
     *
     * @param cell The cell (can be null)
     * @param evaluator The FormulaEvaluator (can be null)
     * @return a string value of the cell
     */
    public String formatCellValue(Cell cell, FormulaEvaluator evaluator) {

        if (cell == null) {
            return "";
        }

        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            if (evaluator == null) {
                return cell.getCellFormula();
            }
            cellType = evaluator.evaluateFormulaCell(cell);
        }
        switch (cellType) {
            case Cell.CELL_TYPE_NUMERIC :

                if (DateUtil.isCellDateFormatted(cell)) {
                    return getFormattedDateString(cell);
                }
                return getFormattedNumberString(cell);

            case Cell.CELL_TYPE_STRING :
                return cell.getRichStringCellValue().getString();

            case Cell.CELL_TYPE_BOOLEAN :
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_BLANK :
                return "";
        }
        throw new RuntimeException("Unexpected celltype (" + cellType + ")");
    }

    //Henri
    public String formatCellValue(Cell cell, int cellType) {
        switch (cellType) {
        case Cell.CELL_TYPE_NUMERIC :
            if (DateUtil.isCellDateFormatted(cell)) {
                return getFormattedDateString(cell);
            }
            return getFormattedNumberString(cell);
		case Cell.CELL_TYPE_ERROR:
			return ErrorConstants.getText(cell.getErrorCellValue());
        case Cell.CELL_TYPE_STRING :
            return cell.getRichStringCellValue().getString();
        case Cell.CELL_TYPE_BOOLEAN :
            return String.valueOf(cell.getBooleanCellValue());
        case Cell.CELL_TYPE_BLANK :
            return "";
	    }
	    throw new RuntimeException("Unexpected celltype (" + cellType + ")");
    }
    
    /**
     * <p>
     * Sets a default number format to be used when the Excel format cannot be
     * parsed successfully. <b>Note:</b> This is a fall back for when an error
     * occurs while parsing an Excel number format pattern. This will not
     * affect cells with the <em>General</em> format.
     * </p>
     * <p>
     * The value that will be passed to the Format's format method (specified
     * by <code>java.text.Format#format</code>) will be a double value from a
     * numeric cell. Therefore the code in the format method should expect a
     * <code>Number</code> value.
     * </p>
     *
     * @param format A Format instance to be used as a default
     * @see java.text.Format#format
     */
    public void setDefaultNumberFormat(Format format) {
        Iterator<Map.Entry<String,Format>> itr = formats.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<String,Format> entry = itr.next();
            if (entry.getValue() == generalDecimalNumFormat
                    || entry.getValue() == generalWholeNumFormat) {
                entry.setValue(format);
            }
        }
        defaultNumFormat = format;
    }

    /**
     * Adds a new format to the available formats.
     * <p>
     * The value that will be passed to the Format's format method (specified
     * by <code>java.text.Format#format</code>) will be a double value from a
     * numeric cell. Therefore the code in the format method should expect a
     * <code>Number</code> value.
     * </p>
     * @param excelFormatStr The data format string
     * @param format A Format instance
     */
    public void addFormat(String excelFormatStr, Format format) {
        formats.put(excelFormatStr, format);
    }

    // Some custom formats

    /**
     * @return a <tt>DecimalFormat</tt> with parseIntegerOnly set <code>true</code>
     */
    /* package */ static DecimalFormat createIntegerOnlyFormat(String fmt) {
        DecimalFormat result = new DecimalFormat(fmt);
        result.setParseIntegerOnly(true);
        return result;
    }
    
    /**
     * Enables excel style rounding mode (round half up)
     *  on the Decimal Format if possible.
     * This will work for Java 1.6, but isn't possible
     *  on Java 1.5. 
     */
    public static void setExcelStyleRoundingMode(DecimalFormat format) {
        setExcelStyleRoundingMode(format, RoundingMode.HALF_UP);
    }

    /**
     * Enables custom rounding mode
     *  on the Decimal Format if possible.
     * This will work for Java 1.6, but isn't possible
     *  on Java 1.5.
     * @param format DecimalFormat
     * @param roundingMode RoundingMode
     */
    public static void setExcelStyleRoundingMode(DecimalFormat format, RoundingMode roundingMode) {
       try {
          Method srm = format.getClass().getMethod("setRoundingMode", RoundingMode.class);
          srm.invoke(format, roundingMode);
       } catch(NoSuchMethodException e) {
          // Java 1.5
       } catch(IllegalAccessException iae) {
          // Shouldn't happen
          throw new RuntimeException("Unable to set rounding mode", iae);
       } catch(InvocationTargetException ite) {
          // Shouldn't happen
          throw new RuntimeException("Unable to set rounding mode", ite);
       } catch(SecurityException se) {
          // Not much we can do here
       }
    }

    /**
     * Format class for Excel's SSN format. This class mimics Excel's built-in
     * SSN formatting.
     *
     * @author James May
     */
    @SuppressWarnings("serial")
   private static final class SSNFormat extends Format {
        public static final Format instance = new SSNFormat();
        private static final DecimalFormat df = createIntegerOnlyFormat("000000000");
        private SSNFormat() {
            // enforce singleton
        }

        /** Format a number as an SSN */
        public static String format(Number num) {
            String result = df.format(num);
            StringBuffer sb = new StringBuffer();
            sb.append(result.substring(0, 3)).append('-');
            sb.append(result.substring(3, 5)).append('-');
            sb.append(result.substring(5, 9));
            return sb.toString();
        }

        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(format((Number)obj));
        }

        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }

    /**
     * Format class for Excel Zip + 4 format. This class mimics Excel's
     * built-in formatting for Zip + 4.
     * @author James May
     */
    @SuppressWarnings("serial")
   private static final class ZipPlusFourFormat extends Format {
        public static final Format instance = new ZipPlusFourFormat();
        private static final DecimalFormat df = createIntegerOnlyFormat("000000000");
        private ZipPlusFourFormat() {
            // enforce singleton
        }

        /** Format a number as Zip + 4 */
        public static String format(Number num) {
            String result = df.format(num);
            StringBuffer sb = new StringBuffer();
            sb.append(result.substring(0, 5)).append('-');
            sb.append(result.substring(5, 9));
            return sb.toString();
        }

        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(format((Number)obj));
        }

        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }

    /**
     * Format class for Excel phone number format. This class mimics Excel's
     * built-in phone number formatting.
     * @author James May
     */
    @SuppressWarnings("serial")
   private static final class PhoneFormat extends Format {
        public static final Format instance = new PhoneFormat();
        private static final DecimalFormat df = createIntegerOnlyFormat("##########");
        private PhoneFormat() {
            // enforce singleton
        }

        /** Format a number as a phone number */
        public static String format(Number num) {
            String result = df.format(num);
            StringBuffer sb = new StringBuffer();
            String seg1, seg2, seg3;
            int len = result.length();
            if (len <= 4) {
                return result;
            }

            seg3 = result.substring(len - 4, len);
            seg2 = result.substring(Math.max(0, len - 7), len - 4);
            seg1 = result.substring(Math.max(0, len - 10), Math.max(0, len - 7));

            if(seg1 != null && seg1.trim().length() > 0) {
                sb.append('(').append(seg1).append(") ");
            }
            if(seg2 != null && seg2.trim().length() > 0) {
                sb.append(seg2).append('-');
            }
            sb.append(seg3);
            return sb.toString();
        }

        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(format((Number)obj));
        }

        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }
    
    /**
     * Format class that handles Excel style fractions, such as "# #/#" and "#/###"
     */
    @SuppressWarnings("serial")
    private static final class FractionFormat extends Format {
       private final String str;
       public FractionFormat(String s) {
          str = s;
       }
       
       public String format(Number num) {
    	   
    	  double doubleValue = num.doubleValue();
          
          // Format may be p or p;n or p;n;z (okay we never get a z).
    	  // Fall back to p when n or z is not specified.
          String[] formatBits = str.split(";");
          int f = doubleValue > 0.0 ? 0 : doubleValue < 0.0 ? 1 : 2; 
          String str = (f < formatBits.length) ? formatBits[f] : formatBits[0];
          
          double wholePart = Math.floor(Math.abs(doubleValue));
          double decPart = Math.abs(doubleValue) - wholePart;
          if (wholePart + decPart == 0) {
             return "0";
          }
          if (doubleValue < 0.0) {
        	  wholePart *= -1.0;
          }

          // Split the format string into decimal and fraction parts
          String[] parts = str.replaceAll("  *", " ").split(" ");
          String[] fractParts;
          if (parts.length == 2) {
             fractParts = parts[1].split("/");
          } else {
             fractParts = str.split("/");
          }
          
          // Excel supports both #/# and ?/?, but Java only the former
          for (int i=0; i<fractParts.length; i++) {
             fractParts[i] = fractParts[i].replace('?', '#');
          }

          if (fractParts.length == 2) {
         	 int fractPart1Length = Math.min(countHashes(fractParts[1]), 4); // Any more than 3 and we go around the loops for ever
             double minVal = 1.0;
             double currDenom = Math.pow(10 ,  fractPart1Length) - 1d;
             double currNeum = 0;
             for (int i = (int)(Math.pow(10,  fractPart1Length)- 1d); i > 0; i--) {
                for(int i2 = (int)(Math.pow(10,  fractPart1Length)- 1d); i2 > 0; i2--){
                   if (minVal >=  Math.abs((double)i2/(double)i - decPart)) {
                      currDenom = i;
                      currNeum = i2;
                      minVal = Math.abs((double)i2/(double)i  - decPart);
                   }
                }
             }
             NumberFormat neumFormatter = new DecimalFormat(fractParts[0]);
             NumberFormat denomFormatter = new DecimalFormat(fractParts[1]);
             if (parts.length == 2) {
                NumberFormat wholeFormatter = new DecimalFormat(parts[0]);
                String result = wholeFormatter.format(wholePart) + " " + neumFormatter.format(currNeum) + "/" + denomFormatter.format(currDenom);
                return result;
             } else {
                String result = neumFormatter.format(currNeum + (currDenom * wholePart)) + "/" + denomFormatter.format(currDenom);
                return result;
             }
          } else {
             throw new IllegalArgumentException("Fraction must have 2 parts, found " + fractParts.length + " for fraction format " + this.str);
          }
       }
       
       private int countHashes(String format) {
    	   int count = 0;
    	   for (int i=format.length()-1; i >= 0; i--) {
    		   if (format.charAt(i) == '#') {
    			   count++;
    		   }
    	   }
    	   return count;
       }

       public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
          return toAppendTo.append(format((Number)obj));
       }

       public Object parseObject(String source, ParsePosition pos) {
          throw new NotImplementedException("Reverse parsing not supported");
       }
    }

    /**
     * Format class that does nothing and always returns a constant string.
     *
     * This format is used to simulate Excel's handling of a format string
     * of all # when the value is 0. Excel will output "", Java will output "0".
     *
     * @see DataFormatter#createFormat(double, int, String)
     */
    @SuppressWarnings("serial")
   private static final class ConstantStringFormat extends Format {
        private static final DecimalFormat df = createIntegerOnlyFormat("##########");
        private final String str;
        public ConstantStringFormat(String s) {
            str = s;
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(str);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }
    
    //20110330, henrichen@zkoss.org:
    public static final Format getJavaFormat(Cell cell, Locale locale) { //ZSS-68
    	return new DataFormatter(locale, false).getFormat(cell);
    }
    
    //201400113, dennischen@zkoss.org:
    public static final Format getJavaFormat(double cellValue, String format,Locale locale) {
    	return new DataFormatter(locale, false).getFormat(cellValue,-1,format);//the index -1 in getFormat is for check buildin format, we set -1 to just ignore it.
    }
}
