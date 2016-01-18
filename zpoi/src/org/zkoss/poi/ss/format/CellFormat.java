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

import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.util.CacheMap;
import org.zkoss.util.Pair;
import org.zkoss.poi.ss.usermodel.DateUtil;
import org.zkoss.poi.ss.usermodel.DataFormatter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Format a value according to the standard Excel behavior.  This "standard" is
 * not explicitly documented by Microsoft, so the behavior is determined by
 * experimentation; see the tests.
 * <p/>
 * An Excel format has up to four parts, separated by semicolons.  Each part
 * specifies what to do with particular kinds of values, depending on the number
 * of parts given: <dl> <dt>One part (example: <tt>[Green]#.##</tt>) <dd>If the
 * value is a number, display according to this one part (example: green text,
 * with up to two decimal points). If the value is text, display it as is.
 * <dt>Two parts (example: <tt>[Green]#.##;[Red]#.##</tt>) <dd>If the value is a
 * positive number or zero, display according to the first part (example: green
 * text, with up to two decimal points); if it is a negative number, display
 * according to the second part (example: red text, with up to two decimal
 * points). If the value is text, display it as is. <dt>Three parts (example:
 * <tt>[Green]#.##;[Red]#.##;[Black]#.##</tt>) <dd>If the value is a positive
 * number, display according to the first part (example: green text, with up to
 * two decimal points); if it is a negative
 * number, display according to the second part (example: red text, with up to
 * two decimal points); if it is zero, display according to the third part
 * (example: black text, with up to two decimal points). If the value is text, display it as is. <dt>Four parts
 * (example: <tt>[Green]#.##;[Red]#.##;[Black]#.##;[@]</tt>) <dd>If the value is
 * a positive number, display according to the first part (example: green text,
 * with up to two decimal points); if it is a
 * negative number, display according to the second part (example: red text, with
 * up to two decimal points); if it is zero, display according to the
 * third part (example: black text, with up to two decimal points). If the value is text, display according to the
 * fourth part (example: text in the cell's usual color, with the text value
 * surround by brackets). </dl>
 * <p/>
 * In addition to these, there is a general format that is used when no format
 * is specified.  This formatting is presented by the {@link #GENERAL_FORMAT}
 * object.
 * 
 * TODO Merge this with {@link DataFormatter} so we only have one set of
 * code for formatting numbers.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
@SuppressWarnings({"Singleton"})
public class CellFormat {
    private final String format;
    private final CellFormatPart posNumFmt;
    private final CellFormatPart zeroNumFmt;
    private final CellFormatPart negNumFmt;
    private final CellFormatPart textFmt;
    private final int formatPartCount;

    private static final Pattern ONE_PART = Pattern.compile(
            CellFormatPart.FORMAT_PAT.pattern() + "(;|$)",
            Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);

    private static final CellFormatPart DEFAULT_TEXT_FORMAT =
            new CellFormatPart("@");

    /*
     * Cells that cannot be formatted, e.g. cells that have a date or time
     * format and have an invalid date or time value, are displayed as 255
     * pound signs ("#").
     */
    private static final String INVALID_VALUE_FOR_FORMAT =
            "###################################################" +
            "###################################################" +
            "###################################################" +
            "###################################################" +
            "###################################################";

    private static String QUOTE = "\"";

    //20111229, henrichen@zkoss.org: ZSS-68
	public static final CellFormat getGeneralFormat(final Locale locale) {
		return new CellFormat("General") {
	        @Override
	        public CellFormatResult apply(Object value, int cellWidth) { //ZSS-666
	            String text;
	            if (value == null) {
	                text = "";
	            } else if (value instanceof Byte){ //20100616, Henri Chen
	            	text = ErrorEval.getText(((Byte)value).intValue());
	            } else if (value instanceof Number) {
	                text = CellNumberFormatter.getFormatter(CellNumberFormatter.FormatterType.SIMPLE_NUMBER, locale).format(value);
	            } else if (value instanceof Boolean) { //20100616, Henri Chen
	            	text = ((Boolean)value).booleanValue() ? "TRUE" : "FALSE";
	            } else {
	                text = value.toString();
	            }
	            return new CellFormatResult(true, text, null);
	        }
	    };
	}
    /**
     * Format a value as it would be were no format specified.  This is also
     * used when the format specified is <tt>General</tt>.
     */
/*    public static final CellFormat GENERAL_FORMAT = new CellFormat("General") {
        @Override
        public CellFormatResult apply(Object value) {
            String text = (new CellGeneralFormatter()).format(value);
//            String text;
//            if (value == null) {
//                text = "";
//            } else if (value instanceof Byte){ //20100616, Henri Chen
//            	text = ErrorEval.getText(((Byte)value).intValue());
//            } else if (value instanceof Number) {
//                text = CellNumberFormatter.SIMPLE_NUMBER.format(value);
//            } else if (value instanceof Boolean) { //20100616, Henri Chen
//            	text = ((Boolean)value).booleanValue() ? "TRUE" : "FALSE";
//            } else {
//                text = value.toString();
//            }
            return new CellFormatResult(true, text, null);
        }
    };
*/
    /** Maps a format string to its parsed version for efficiencies sake. */
    private static final Map<Object, CellFormat> formatCache = //ZSS-68
            Collections.synchronizedMap(new WeakHashMap<Object, CellFormat>());

    /**
     * Returns a {@link CellFormat} that applies the given format.  Two calls
     * with the same format may or may not return the same object.
     *
     * @param format The format.
     *
     * @return A {@link CellFormat} that applies the given format.
     */
    public static CellFormat getInstance(String format, Locale locale) { //20111229, henrichen@zkoss.org: ZSS-68
    	final Pair<String, Locale> key = new Pair<String, Locale>(format, locale);
        CellFormat fmt = formatCache.get(key);
        if (fmt == null) {
            if (format.equals("General") || format.equals("@"))
                fmt = getGeneralFormat(locale);
            else
                fmt = new CellFormat(format);
            formatCache.put(key, fmt);
        }
        return fmt;
    }

    /**
     * Creates a new object.
     *
     * @param format The format.
     */
    public CellFormat(String format) { //ZSS-666
        this.format = format;
        Matcher m = ONE_PART.matcher(format);
        List<CellFormatPart> parts = new ArrayList<CellFormatPart>();

        while (m.find()) {
            try {
                String valueDesc = m.group();

                // Strip out the semicolon if it's there
                if (valueDesc.endsWith(";"))
                    valueDesc = valueDesc.substring(0, valueDesc.length() - 1);

                parts.add(new CellFormatPart(valueDesc));
            } catch (RuntimeException e) {
                CellFormatter.logger.log(Level.WARNING,
                        "Invalid format: " + CellFormatter.quote(m.group()), e);
                parts.add(null);
            }
        }
        
        formatPartCount = parts.size();
        
        switch (formatPartCount) {
        case 1:
            posNumFmt = parts.get(0);
            negNumFmt = null;
            zeroNumFmt = null;
            textFmt = posNumFmt != null ?
            		posNumFmt.getCellFormatType() == CellFormatType.TEXT ? posNumFmt : DEFAULT_TEXT_FORMAT : DEFAULT_TEXT_FORMAT;
            _implicit = true; //implicit negative
            break;
        case 2:
            posNumFmt = parts.get(0);
            negNumFmt = parts.get(1);
            zeroNumFmt = null;
            textFmt = DEFAULT_TEXT_FORMAT;
            break;
        case 3:
            posNumFmt = parts.get(0);
            negNumFmt = parts.get(1);
            zeroNumFmt = parts.get(2);
            textFmt = DEFAULT_TEXT_FORMAT;
            break;
        case 4:
        default:
            posNumFmt = parts.get(0);
            negNumFmt = parts.get(1);
            zeroNumFmt = parts.get(2);
            textFmt = parts.get(3);
            break;
        }
    }

    /**
     * Returns the result of applying the format to the given value.  If the
     * value is a number (a type of {@link Number} object), the correct number
     * format type is chosen; otherwise it is considered a text object.
     *
     * @param value The value
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(Object value, int cellWidth) { //ZSS-666
    	if (value instanceof Byte){ //20100616, Henri Chen
    		return new CellFormatResult(false, ErrorEval.getText(((Byte)value).intValue()), null);
    	} else if (value instanceof Number) {
            Number num = (Number) value;
            double val = num.doubleValue();
            if (val < 0 &&
                    ((formatPartCount == 2
                            && !posNumFmt.hasCondition() && !negNumFmt.hasCondition())
                    || (formatPartCount == 3 && !negNumFmt.hasCondition())
                    || (formatPartCount == 4 && !negNumFmt.hasCondition()))) {
                // The negative number format has the negative formatting required,
                // e.g. minus sign or brackets, so pass a positive value so that
                // the default leading minus sign is not also output
            	// TODO: apply cellWidth to show #######?
                return _implicit ? negNumFmt.apply(value) : negNumFmt.apply(-val); //20100615, Henri Chen
            } else {
                return getApplicableFormatPart(val).apply(val);
            }
        } else if (value instanceof java.util.Date) {
            // Don't know (and can't get) the workbook date windowing (1900 or 1904)
            // so assume 1900 date windowing
            Double numericValue = DateUtil.getExcelDate((Date) value);
            if (DateUtil.isValidExcelDate(numericValue)) {
                return getApplicableFormatPart(numericValue).apply(value);
            } else {
                throw new IllegalArgumentException("value not a valid Excel date");
            }
//marked out after upgrade to POI 3.8-Final            
//        } else if (value instanceof Date) { //20100615, Henri Chen.
//        	return posNumFmt.apply(value);
        } else {
            return textFmt.apply(value);
        }
    }

    /**
     * Returns the result of applying the format to the given date.
     *
     * @param date         The date.
     * @param numericValue The numeric value for the date.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    private CellFormatResult apply(Date date, double numericValue, int cellWidth) {
        return getApplicableFormatPart(numericValue).apply(date);
    }

    /**
     * Fetches the appropriate value from the cell, and returns the result of
     * applying it to the appropriate format.  For formula cells, the computed
     * value is what is used.
     *
     * @param c The cell.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(Cell c) {
    	final int cellWidth = c.getSheet().getColumnWidth(c.getColumnIndex()) >> 8; //ZSS-666
        switch (ultimateType(c)) {
        case Cell.CELL_TYPE_BLANK:
            return EMPTY_CELL_FORMAT_RESULT;
        case Cell.CELL_TYPE_BOOLEAN:
            return apply(c.getBooleanCellValue(), cellWidth);
        case Cell.CELL_TYPE_NUMERIC:
            Double value = c.getNumericCellValue();
            if (getApplicableFormatPart(value).getCellFormatType() == CellFormatType.DATE) {
                if (DateUtil.isValidExcelDate(value)) {
                    return apply(c.getDateCellValue(), value, cellWidth);
                } else {
                    return apply(INVALID_VALUE_FOR_FORMAT, cellWidth);
                }
            } else {
                return apply(value, cellWidth);
            }
//            return apply(posNumFmt.getCellFormatType() == CellFormatType.DATE ? c.getDateCellValue() : c.getNumericCellValue()); //20100615, Henri Chen
        case Cell.CELL_TYPE_STRING:
        	final String str = c.getStringCellValue();
            return str == null ? EMPTY_CELL_FORMAT_RESULT : apply(str, cellWidth);
        case Cell.CELL_TYPE_ERROR:
        	return apply(c.getErrorCellValue(), cellWidth); //20100616, Henri Chen
        default:
            return apply("?", cellWidth);
        }
    }
    
    //20131209, dennischen@zkoss.org, api to know to convert double to date 
	public boolean isApplicableDateFormat(Double value) {
		if (getApplicableFormatPart(value).getCellFormatType() == CellFormatType.DATE) {
			if (DateUtil.isValidExcelDate(value)) {
				return true;
			}
		}
		return false;
	}
    
    private static final CellFormatResult EMPTY_CELL_FORMAT_RESULT = new CellFormatResult(false, "", null); 

    /**
     * Uses the result of applying this format to the value, setting the text
     * and color of a label before returning the result.
     *
     * @param label The label to apply to.
     * @param value The value to process.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(JLabel label, Object value, int cellWidth) {
        CellFormatResult result = apply(value, cellWidth);
        label.setText(result.text);
        if (result.textColor != null) {
            label.setForeground(result.textColor);
        }
        return result;
    }

    /**
     * Uses the result of applying this format to the given date, setting the text
     * and color of a label before returning the result.
     *
     * @param label        The label to apply to.
     * @param date         The date.
     * @param numericValue The numeric value for the date.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    private CellFormatResult apply(JLabel label, Date date, double numericValue, int cellWidth) {
        CellFormatResult result = apply(date, numericValue, cellWidth);
        label.setText(result.text);
        if (result.textColor != null) {
            label.setForeground(result.textColor);
        }
        return result;
    }

    /**
     * Fetches the appropriate value from the cell, and uses the result, setting
     * the text and color of a label before returning the result.
     *
     * @param label The label to apply to.
     * @param c     The cell.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(JLabel label, Cell c) {
    	final int cellWidth = c.getSheet().getColumnWidth(c.getColumnIndex()) >> 8;  //ZSS-666
        switch (ultimateType(c)) {
        case Cell.CELL_TYPE_BLANK:
            return apply(label, "", cellWidth);
        case Cell.CELL_TYPE_BOOLEAN:
            return apply(label, c.getBooleanCellValue(), cellWidth);
        case Cell.CELL_TYPE_NUMERIC:
            Double value = c.getNumericCellValue();
            if (getApplicableFormatPart(value).getCellFormatType() == CellFormatType.DATE) {
                if (DateUtil.isValidExcelDate(value)) {
                    return apply(label, c.getDateCellValue(), value, cellWidth);
                } else {
                    return apply(label, INVALID_VALUE_FOR_FORMAT, cellWidth);
                }
            } else {
                return apply(label, value, cellWidth);
            }
        case Cell.CELL_TYPE_STRING:
            return apply(label, c.getStringCellValue(), cellWidth);
        default:
            return apply(label, "?", cellWidth);
        }
    }

    /**
     * Returns the {@link CellFormatPart} that applies to the value.  Result
     * depends on how many parts the cell format has, the cell value and any
     * conditions.  The value must be a {@link Number}.
     * 
     * @param value The value.
     * @return The {@link CellFormatPart} that applies to the value.
     */
    private CellFormatPart getApplicableFormatPart(Object value) {
        
        if (value instanceof Number) {
            
            double val = ((Number) value).doubleValue();
            
            if (formatPartCount == 1) {
            	//20120725 samchuang@zkoss.org: ZSS-142
                if (posNumFmt != null && (!posNumFmt.hasCondition()
                        || (posNumFmt.hasCondition() && posNumFmt.applies(val)))) {
                    return posNumFmt;
                } else {
                    return new CellFormatPart("General");
                }
            } else if (formatPartCount == 2) {
            	//20120608 samchuang@zkoss.org: ZSS-134
                if (posNumFmt != null && ((!posNumFmt.hasCondition() && val >= 0)
                        || (posNumFmt.hasCondition() && posNumFmt.applies(val)))) {
                    return posNumFmt;
                } else if (negNumFmt != null && (!negNumFmt.hasCondition()
                        || (negNumFmt.hasCondition() && negNumFmt.applies(val)))) {
                    return negNumFmt;
                } else {
                	// Return ###...### (255 #s) to match Excel 2007 behaviour
                    return new CellFormatPart(QUOTE + INVALID_VALUE_FOR_FORMAT + QUOTE);
                }
            } else {
                if ((!posNumFmt.hasCondition() && val > 0)
                        || (posNumFmt.hasCondition() && posNumFmt.applies(val))) {
                    return posNumFmt;
                } else if ((!negNumFmt.hasCondition() && val < 0)
                        || (negNumFmt.hasCondition() && negNumFmt.applies(val))) {
                    return negNumFmt;
                // Only the first two format parts can have conditions
                } else {
                    return zeroNumFmt;
                }
            }
        } else {
            throw new IllegalArgumentException("value must be a Number");
        }
        
    }

    /**
     * Returns the ultimate cell type, following the results of formulas.  If
     * the cell is a {@link Cell#CELL_TYPE_FORMULA}, this returns the result of
     * {@link Cell#getCachedFormulaResultType()}.  Otherwise this returns the
     * result of {@link Cell#getCellType()}.
     *
     * @param cell The cell.
     *
     * @return The ultimate type of this cell.
     */
    public static int ultimateType(Cell cell) {
        int type = cell.getCellType();
        if (type == Cell.CELL_TYPE_FORMULA)
            return cell.getCachedFormulaResultType();
        else
            return type;
    }

    /**
     * Returns <tt>true</tt> if the other object is a {@link CellFormat} object
     * with the same format.
     *
     * @param obj The other object.
     *
     * @return <tt>true</tt> if the two objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof CellFormat) {
            CellFormat that = (CellFormat) obj;
            return format.equals(that.format);
        }
        return false;
    }

    /**
     * Returns a hash code for the format.
     *
     * @return A hash code for the format.
     */
    @Override
    public int hashCode() {
        return format.hashCode();
    }
    
    //20100615, Henri Chen: patch to distinguish implicit negative number format
    private boolean _implicit;
}