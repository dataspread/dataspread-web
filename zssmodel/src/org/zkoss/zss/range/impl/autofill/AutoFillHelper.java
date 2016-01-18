/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl.autofill;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.lang.Strings;
import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.PasteOption;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.PasteOption.PasteType;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.format.FormatContext;
import org.zkoss.zss.model.sys.format.FormatEngine;
import org.zkoss.zss.model.sys.format.FormatResult;
import org.zkoss.zss.range.SRange.FillType;

/**
 * To help data filling.
 * @author Dennis
 * @since 3.5.0
 */
//the code was migrated from original BookHelper.fill series method.
public class AutoFillHelper {
	
	//inner fill direction for #fill
	private static final int FILL_INVALID = 0;
	private static final int FILL_NONE = 1; //no way to fill
	private static final int FILL_UP = 2;
	private static final int FILL_DOWN = 3;
	private static final int FILL_RIGHT = 4;
	private static final int FILL_LEFT = 5;
	
	/**
	 * fill the sheet, from srcRef to dstRef
	 * @param sheet
	 * @param srcRegion
	 * @param dstRegion
	 * @param fillType
	 */
	public void fill(SSheet sheet, CellRegion srcRegion, CellRegion dstRegion, FillType fillType) {
		final int fillDir = getFillDirection(srcRegion, dstRegion);
		if (fillDir == FILL_NONE) { //nothing to fill up, just return
			return;
		}
		switch(fillDir) {
		case FILL_UP:
			fillUp(sheet, srcRegion, dstRegion, fillType);
			return;
		case FILL_DOWN:
			fillDown(sheet, srcRegion, dstRegion, fillType);
			return;
		case FILL_RIGHT:
			fillRight(sheet, srcRegion, dstRegion, fillType);
			return;
		case FILL_LEFT:
			fillLeft(sheet, srcRegion, dstRegion, fillType);
			return;
		}
		//FILL_INVALID
		throw new InvalidModelOpException("Destination range must include source range and can be fill in one direction only"); 
	}
	private static int getShortWeekIndex(String x, Locale locale) { //ZSS-69
		return ShortWeekData.getInstance(CircularData.NORMAL, locale).getIndex(x);
	}
	private static int getFullWeekIndex(String x, Locale locale) { //ZSS-69
		return FullWeekData.getInstance(CircularData.NORMAL, locale).getIndex(x); 
	}
	private static int getShortMonthIndex(String x, Locale locale) { //ZSS-69
		return ShortMonthData.getInstance(CircularData.NORMAL, locale).getIndex(x);
	}
	private static int getFullMonthIndex(String x, Locale locale) { //ZSS-69
		return FullMonthData.getInstance(CircularData.NORMAL, locale).getIndex(x);
	}
	private static boolean isShortWeek(String x, Locale locale) { //ZSS-69
		return getShortWeekIndex(x, locale) >= 0;
	}
	private static boolean isFullWeek(String x, Locale locale) { //ZSS-69
		return getFullWeekIndex(x, locale) >= 0;
	}
	private static boolean isShortMonth(String x, Locale locale) { //ZSS-69
		return getShortMonthIndex(x, locale) >= 0;
	}
	private static boolean isFullMonth(String x, Locale locale) { //ZSS-69
		return getFullMonthIndex(x, locale) >= 0;
	}
	private static int nextWeekIndex(int current, int step) {
		return nextCircularIndex(current, step, 7);
	}
	private static int nextMonthIndex(int current, int step) {
		return nextCircularIndex(current, step, 12);
	}
	private static int nextCircularIndex(int current, int step, int modulo) {
		current += step;
		if (current < 0) {
			current += modulo;
		}
		return current % modulo;
	}
	//ZSS-69, locale then US when doing drag-fill
	private static int getWeekMonthSubType(String x, Locale locale) { 
		if (isShortWeek(x, locale)) {
			return Step.SHORT_WEEK; //a short week
		}
		if (isShortMonth(x, locale)) {
			return Step.SHORT_MONTH; //a  short month
		}
		if (isFullWeek(x, locale)) {
			return Step.FULL_WEEK; //a full week
		}
		if (isFullMonth(x, locale)) {
			return Step.FULL_MONTH; //a  full month
		}
		if (isShortWeek(x, Locale.US)) {
			return Step.US_SHORT_WEEK; //a US short week
		}
		if (isShortMonth(x, Locale.US)) {
			return Step.US_SHORT_MONTH; //a US short month
		}
		if (isFullWeek(x, Locale.US)) {
			return Step.US_FULL_WEEK; //a US full week
		}
		if (isFullMonth(x, Locale.US)) {
			return Step.US_FULL_MONTH; //a US full month
		}
		if (Strings.isBlank(x)){
			return Step.BLANK; //a blank string
		} else {
			return Step.STRING; //a pure string
		}
	}
    private static final Pattern datePattern = Pattern.compile("[yMwWDdFE]+");
    private static final Pattern timePattern = Pattern.compile("[HhKkmsS]+");
    private static boolean isDatePattern(String pattern) {
		Matcher dateM = datePattern.matcher(pattern);
		return dateM.find();
    }
    private static boolean isTimePattern(String pattern) {
		Matcher dateM = timePattern.matcher(pattern);
		return dateM.find();
    }
    FormatEngine formatEngine;
    
    private FormatEngine getFormatEngine(){
    	if(formatEngine==null){
    		formatEngine = EngineFactory.getInstance().createFormatEngine();
    	}
    	return formatEngine;
    }

	private int getDateTimeSubType(SCell cell) {
		FormatResult result = getFormatEngine().format(cell, new FormatContext(ZssContext.getCurrent().getLocale()));
		Format format = result.getFormater();
        if (result.isDateFormatted() && format instanceof SimpleDateFormat){
        	//check if a pure time format
        	final String pattern = ((SimpleDateFormat)format).toPattern();
            return isDatePattern(pattern) ? Step.DATE : Step.TIME; //a date or a time
        }
        return Step.NUMBER; //a nubmer
	}
	private class StepChunk {
		private Step[] _steps;
		protected StepChunk() {}
		
		public StepChunk(SCell[] srcCells, FillType fillType, boolean positive, int siblingCount) {
			_steps = new Step[srcCells.length];
			int b = 0, e = 0;
			CellType prevtype = null;
			int subType = -1;
			final Locale locale = ZssContext.getCurrent().getLocale(); //ZSS-69
			for (int j = 0; j < srcCells.length; ++j) {
				final SCell cell = srcCells[j];
				final CellType type = cell.isNull() ? CellType.BLANK : cell.getType();
				if (type != prevtype) {
					if (prevtype != null) {
						prepareSteps(srcCells, b, e, positive, fillType, subType, siblingCount); //per the chunk, get the proper Step
					}
					b = e = j;
					prevtype = type;
					if (type == CellType.STRING) { //could be Blank, String, short week/month, full week/month
						final String x = cell.getStringValue();
						subType = getWeekMonthSubType(x, locale); //ZSS-69
					} else if (type == CellType.NUMBER) { //could be number/date/time
						subType = getDateTimeSubType(cell);
					}
					continue;
				}
				//type == prevtype
				if (type == CellType.STRING) { //check if week/month
					final String x = cell.getStringValue();
					final int curSubType = getWeekMonthSubType(x, locale); //ZSS-69
					if (curSubType == subType) {
						e = j;
						continue;
					}
					//subType changed
					prepareSteps(srcCells, b, e, positive, fillType, subType, siblingCount); //prepare steps
					b = e = j;
					subType = curSubType;
				} else if (type == CellType.NUMBER) { //special case, date or number
					final int curSubType = getDateTimeSubType(cell);
					if (subType == curSubType) {
						e = j;
						continue;
					}
					//subType changed
					prepareSteps(srcCells, b, e, positive, fillType, subType, siblingCount); //prepare steps
					b = e = j;
					subType = curSubType;
				}
				e = j;
			}
			//last one
			prepareSteps(srcCells, b, e, positive, fillType, subType, siblingCount); //per the chunk, get the proper Step
		}
		public Step getStep(int srcIndex) {
			return _steps[srcIndex];
		}
		private void replaceWithCopyStep(int index) {
			_steps[index] = CopyStep.instance;
		}
		private void prepareSteps(SCell[] srcCells, int b, int e, boolean positive, FillType fillType, int subType, int siblingCount) {
			final SCell srcCell = srcCells[b];
			final CellType type = srcCell.isNull() ? CellType.BLANK : srcCell.getType();
			Step step;
			switch(type) {
			default:
			case FORMULA:
			case BOOLEAN:
			case ERROR:
				step = CopyStep.instance; //copy
				break;
			case BLANK:
				step = BlankStep.instance; //blank
				break;
			case NUMBER:
				switch(subType) {
				default:
				case Step.NUMBER:
					step = srcCells.length == 1 && siblingCount == 1 ? CopyStep.instance : 	//number, one source cell, copy only  
						fillType == FillType.GROWTH_TREND ? 
							getGrowthStep(srcCells, b, e, positive) : //a growth trend 
							getLinearStep(srcCells, b, e, positive) ; //a linear trend
					break;
				case Step.DATE: //date
					step = getDateStep(srcCells, b, e, positive, fillType, subType);
					break;
				case Step.TIME: //time
					step = getTimeStep(srcCells, b, e, positive, fillType, subType);
					break;
				}
				break;
			case STRING:
				final Locale locale = ZssContext.getCurrent().getLocale(); //ZSS-69 locale aware then US when drag-fill
				switch(subType) {
				default:
				case Step.BLANK:
					step = BlankStep.instance;
					break;
				case Step.STRING:
					step = CopyStep.instance;
					break;
				case Step.SHORT_WEEK: //short week
					step = getShortWeekStep(srcCells, b, e, positive, locale);
					break;
				case Step.SHORT_MONTH: //short month
					step = getShortMonthStep(srcCells, b, e, positive, locale);
					break;
				case Step.FULL_WEEK: //full week
					step = getFullWeekStep(srcCells, b, e, positive, locale);
					break;
				case Step.FULL_MONTH: //full month
					step = getFullMonthStep(srcCells, b, e, positive, locale);
					break;
				case Step.US_SHORT_WEEK: //US short week
					step = getShortWeekStep(srcCells, b, e, positive, Locale.US);
					break;
				case Step.US_SHORT_MONTH: //US short month
					step = getShortMonthStep(srcCells, b, e, positive, Locale.US);
					break;
				case Step.US_FULL_WEEK: //US full week
					step = getFullWeekStep(srcCells, b, e, positive, Locale.US);
					break;
				case Step.US_FULL_MONTH: //US full month
					step = getFullMonthStep(srcCells, b, e, positive, Locale.US);
					break;
				}
 				break;
			}
			for(int k = b; k <= e; ++k) { //associate step to the cells chunk
				_steps[k] = step; 
			}
		}
	}
	private class CopyStepChunk extends StepChunk {
		@Override
		public Step getStep(int index) {
			return CopyStep.instance;
		}
	}
	private final StepChunk CopyStepChunkInstance = new CopyStepChunk();
	
	private int[] getTimeParts(SCell srcCell) {
		int[] parts = new int[7]; //year, month, day, hour, mintue, second, millsecond
		int j = 0;
		Date date = srcCell.getDateValue();
		final Calendar cal = Calendar.getInstance(); //TODO Timezone?
		cal.setTimeInMillis(date.getTime());
		parts[j++]= cal.get(Calendar.YEAR);
		parts[j++]= cal.get(Calendar.MONTH);
		parts[j++]= cal.get(Calendar.DAY_OF_MONTH);
		
		FormatResult result = getFormatEngine().format(srcCell, new FormatContext(ZssContext.getCurrent().getLocale()));
		Format format = result.getFormater();
        if (result.isDateFormatted() && format instanceof SimpleDateFormat){
        	final String pattern1 = ((SimpleDateFormat)format).toPattern();
        	final boolean withtime = isTimePattern(pattern1);
        	if (withtime) {
        		parts[j++]= cal.get(Calendar.HOUR_OF_DAY);
        		parts[j++]= cal.get(Calendar.MINUTE);
        		parts[j++]= cal.get(Calendar.SECOND);
        		parts[j++]= cal.get(Calendar.MILLISECOND);
        		return parts;
        	}
        }
        parts[j++]= -1;
		return parts;
	}
	private static Step getTimeStep(SCell[] srcCells, int b, int e, boolean positive, FillType fillType, int subType) {
		final SCell srcCell1 = srcCells[b];
		if (b == e) { //only one srcCell
			return new DateTimeStep(srcCell1.getDateValue(),0, 0, 0, positive ? 60*60*1000 : -60*60*1000, subType);
		}
		
		//more than one srcCell
		Date date1 = srcCell1.getDateValue();
		SCell srcCell2 = srcCells[b+1];
		Date date2 = srcCell2.getDateValue();
		final int step = (int)(date2.getTime() - date1.getTime());
    	for(int k = b+2; k <= e; ++k) {
    		srcCell2 = srcCells[k];
    		date1 = date2;
    		date2 = srcCell2.getDateValue();
    		if (step != (date2.getTime() - date1.getTime())) {
    			return CopyStep.instance;
    		}
    	}
    	return new DateTimeStep(date2, 0, 0, 0, step, subType);
	}
	private Step getDateStep(SCell[] srcCells, int b, int e, boolean positive, FillType fillType, int subType) {
		if (fillType == FillType.DEFAULT) {
			fillType = FillType.DAYS;
		}
		return myGetDateStep(srcCells, b, e, positive, fillType, subType); 
	}
	private Step myGetDateStep(SCell[] srcCells, int b, int e, boolean positive, FillType fillType, int subType) {
		final SCell srcCell1 = srcCells[b]; 
		final int[] time1 = getTimeParts(srcCell1);
    	int j = 0;
    	int y1 = time1[j]; ++j; 
    	int m1 = time1[j]; ++j; 
    	int d1 = time1[j]; ++j; 
    	int h1 = time1[j]; ++j;
    	int min1 = time1[j]; ++j;
    	int s1 = time1[j]; ++j;
    	int ms1 = time1[j];
    	int t1 = (h1 < 0 ? 0 : h1 * 60 * 60 * 1000) + min1 * 60 * 1000 + s1 * 1000 + ms1; //time field in millisecond of a day
		if (b == e) { //only one srcCell
			Date date = srcCells[b].getDateValue();
			switch(fillType) {
			case DAYS:
				return new DateTimeStep(date, 0, 0, positive ? 1 : -1, 0, subType);
			case HOURS:
				return new DateTimeStep(date, 0, 0, 0, positive ? 60*60*1000 : -60*60*1000, subType);
			case MONTHS:
				return new DateTimeStep(date, 0, positive ? 1 : -1, 0, 0, subType);
			case YEARS:
				return new DateTimeStep(date, positive ? 1 : -1, 0, 0, 0, subType);
			case WEEKDAYS:
				return new DateTimeStep(date, 0, 0, positive ? 7 : -7, 0, subType);
			default:
				return CopyStep.instance;
			}
		}
		
		//more than one srcCell
    	final SCell srcCell2  = srcCells[b+1];
    	final int[] time2 = getTimeParts(srcCell2);
    	j = 0;
    	int y2 = time2[j]; ++j; 
    	int m2 = time2[j]; ++j; 
    	int d2 = time2[j]; ++j; 
    	int h2 = time2[j]; ++j;
    	int min2 = time2[j]; ++j;
    	int s2 = time2[j]; ++j;
    	int ms2 = time2[j];
    	//month different of years
    	int diffM = m2 - m1 + (y2 - y1) * 12;
    	//day different in a month
    	int diffD = d2 - d1;
    	if (h1 < 0 && h2 >= 0) {
    		h1 = h2;
    		min1 = min2;
    		s1 = s2;
    		ms1 = ms2;
    	} else if (h1 >= 0 && h2 < 0) {
    		h2 = h1;
    		min2 = min1;
    		s2 = s1;
    		ms2 = ms1;
    	}
    	//time field in millisecond of a single day day
    	int t2 = (h2 < 0 ? 0 : h2 * 60 * 60 * 1000) + min2 * 60 * 1000 + s2 * 1000 + ms2;
    	//time different of a single day
    	int diffT = t2 - t1;
    	for(int k = b+2; k <= e; ++k) {
        	y1 = y2;
        	m1 = m2;
        	d1 = d2;
        	h1 = h2;
        	min1 = min2;
        	s1 = s2;
        	ms1 = ms2;
        	t1 = t2;
        	
        	final SCell srcCell = srcCells[k];
        	final int[] time = getTimeParts(srcCell);
        	j = 0;
        	y2 = time[j]; ++j; 
        	m2 = time[j]; ++j; 
        	d2 = time[j]; ++j; 
        	h2 = time[j]; ++j;
        	min2 = time[j]; ++j;
        	s2 = time[j]; ++j;
        	ms2 = time[j];
        	if ((diffM != m2 - m1 + (y2 - y1) * 12) || (diffD != d2 - d1)) {
        		//if the month and day difference doesn't equals to cell1 and 2, then use the simple copy step
        		return CopyStep.instance;
        	}
        	if (h1 < 0 && h2 >= 0) {
        		h1 = h2;
        		min1 = min2;
        		s1 = s2;
        		ms1 = ms2;
        	} else if (h1 >= 0 && h2 < 0) {
        		h2 = h1;
        		min2 = min1;
        		s2 = s1;
        		ms2 = ms1;
        	}
        	t2 = (h2 < 0 ? 0 : h2 * 60 * 60 * 1000) + min2 * 60 * 1000 + s2 * 1000 + ms2;
        	
        	if (diffT != t2 - t1) {
        		//if the time difference doesn't equals to cell1 and 2, then reset the time different
        		diffT  = 0;
        	}
    	}
    	
		//sync -1 value back to 0 (see getTimeParts) 
		if (h1 < 0) {
			h1 = 0;
		}
		if (h2 < 0) {
			h2 = 0;
		}
		
    	final Calendar cal2 = Calendar.getInstance(); //TODO Timezone?
		cal2.set(y2, m2, d2, h2, min2, s2);
		cal2.set(Calendar.MILLISECOND, ms2);
		if(diffD != 0){//special cause that care month step or day step
			final Calendar cal1 = Calendar.getInstance(); //TODO Timezone?
			cal1.set(y1, m1, d1, h1, min1, s1);
			cal1.set(Calendar.MILLISECOND, ms1);
			
			int doy1 = cal1.get(Calendar.DAY_OF_YEAR);
			int doy2 = cal2.get(Calendar.DAY_OF_YEAR);
			
			diffD = (y2-y1)*365+doy2-doy1; //real day different
			return new DateTimeStep(cal2.getTime(), 0, 0, diffD, diffT, -1);
		}else{
			return new DateTimeStep(cal2.getTime(), 0, diffM, 0, diffT, -1);
		}
	}
	private static Step getGrowthStep(SCell[] srcCells, int b, int e, boolean positive) {
		if (b == e) { //only one source cell
			return CopyStep.instance;
		}
		//calc first ratio
		double prev = srcCells[b].getNumberValue();
		double curv = srcCells[b+1].getNumberValue();
		double ratio = curv / prev;
		prev = curv;
		for (int k = b+2; k <=e; ++k) {
			final SCell srcCell = srcCells[k];
			curv = srcCell.getNumberValue();
			if (ratio != (curv / prev)) {
				return CopyStep.instance;
			}
			prev = curv;
		}
		return new GrowthStep(curv, ratio, -1);
	}
	private static Step getLinearStep(SCell[] srcCells, int b, int e, boolean positive) {
		int count = e-b+1;
		final double[] values = new double[count];
		for (int j = 0, k = b; k <=e; ++k) {
			final SCell srcCell = srcCells[k];
			values[j++] = srcCell.getNumberValue();
		}
		if (count == 1) {
			final double step = positive ? 1 : -1;
			return new LinearStep(values[count-1], step, step, Step.NUMBER);
		} else if (count == 2) { //standard linear series
			final double step = values[1] - values[0];
			return new LinearStep(values[count-1], step, step, -1);
		} else if (count == 3) { //3 source case (by experiment)
			double step = values[2] - values[0];
			double initStep	= (step + values[1] - values[0]) / 3;
			step /= 2;
			return new LinearStep(values[count-1], initStep, step, -1);
		} else if (count == 4) { //4 source case (by experiment)
			double initStep = (values[2] - values[0]) / 2;
			double step = (values[3]-values[0]) * 0.3 + (values[2]-values[1]) * 0.1;
			return new LinearStep(values[count-1], initStep, step, -1);
		}
		//TODO, for values equals to 5 or above 5, we apply the 5 values rule, though it is not the same to the Excel!
		//else if (j >= 5) { //5 source case (by experiment) 
			double initStep = -0.4 * values[0] - 0.1 * values[1] + 0.2 * values[2] + 0.5 * values[3] - 0.2 * values[4];
			double step = -0.2 * values[0] - 0.1 * values[1] + 0.1 * values[3] + 0.2 * values[4];
			return new LinearStep(values[count-1], initStep, step, -1);
		//}
	}
	private static int getCaseType(String x) {
		if (Character.isLowerCase(x.charAt(0))) {
			return 1; //lowercase
		} else if (Character.isUpperCase(x.charAt(1))) {
			return 2; //uppercase
		}
		return 0; //normal case
	}
	private static Step getShortWeekStep(SCell[] srcCells, int b, int e, boolean positive, Locale locale) { //ZSS-69
		final int count = e-b+1;
		String bWeek = null;
		int preIndex = -1;
		int step = 0;
		for (int j = b; j <= e; ++j) {
			final SCell srcCell = srcCells[j];
			final String x = srcCell.getStringValue(); 	
			final int weekIndex = getShortWeekIndex(x, locale); //ZSS-69
			if (step == 0) {
				if (preIndex >= 0) {
					step = weekIndex - preIndex;
					preIndex = weekIndex;
				} else {
					bWeek = x;
					preIndex = weekIndex;
					if (count == 1) { //no more, step default to 1
						step = positive ? 1 : -1;
					}
				}
			} else {
				preIndex = nextWeekIndex(preIndex, step);
				if (preIndex != weekIndex) { //not a week sequence
					return CopyStep.instance; //a copy step
				}
			}
		}
		return new ShortWeekStep(preIndex, step, getCaseType(bWeek), b == e ? Step.SHORT_WEEK : -1, locale); //ZSS-69
	}
	private static Step getFullWeekStep(SCell[] srcCells, int b, int e, boolean positive, Locale locale) { //ZSS-69
		final int count = e-b+1;
		String bWeek = null;
		int preIndex = -1;
		int step = 0;
		for (int j = b; j <= e; ++j) {
			final SCell srcCell = srcCells[j];
			final String x = srcCell.getStringValue(); 	
			final int weekIndex = getFullWeekIndex(x, locale); //ZSS-69
			if (step == 0) {
				if (preIndex >= 0) {
					step = weekIndex - preIndex;
					preIndex = weekIndex;
				} else {
					bWeek = x;
					preIndex = weekIndex;
					if (count == 1) { //no more, step default to 1
						step = positive ? 1 : -1;
					}
				}
			} else {
				preIndex = nextWeekIndex(preIndex, step);
				if (preIndex != weekIndex) { //not a week sequence
					return CopyStep.instance; //a copy step
				}
			}
		}
		return new FullWeekStep(preIndex, step, getCaseType(bWeek), b == e ? Step.FULL_WEEK : -1, locale); //ZSS-69
	}
	private static Step getShortMonthStep(SCell[] srcCells, int b, int e, boolean positive, Locale locale) {
		final int count = e-b+1;
		String bMonth = null;
		int preIndex = -1;
		int step = 0;
		for (int j = b; j <= e; ++j) {
			final SCell srcCell = srcCells[j];
			final String x = srcCell.getStringValue(); 	
			final int monthIndex = getShortMonthIndex(x, locale); //ZSS-69
			if (step == 0) {
				if (preIndex >= 0) {
					step = monthIndex - preIndex;
					preIndex = monthIndex;
				} else {
					bMonth = x;
					preIndex = monthIndex;
					if (count == 1) { //no more, step default to 1
						step = positive ? 1 : -1;
					}
				}
			} else {
				preIndex = nextMonthIndex(preIndex, step);
				if (preIndex != monthIndex) { //not a month sequence
					return CopyStep.instance; //a copy step
				}
			}
		}
		return new ShortMonthStep(preIndex, step, getCaseType(bMonth), b == e ? Step.SHORT_MONTH : -1, locale); //ZSS-69
	}
	private static Step getFullMonthStep(SCell[] srcCells, int b, int e, boolean positive, Locale locale) { //ZSS-69
		final int count = e-b+1;
		String bMonth = null;
		int preIndex = -1;
		int step = 0;
		for (int j = b; j <= e; ++j) {
			final SCell srcCell = srcCells[j];
			final String x = srcCell.getStringValue(); 	
			final int monthIndex = getFullMonthIndex(x, locale); //ZSS-69
			if (step == 0) {
				if (preIndex >= 0) {
					step = monthIndex - preIndex;
					preIndex = monthIndex;
				} else {
					bMonth = x;
					preIndex = monthIndex;
					if (count == 1) { //no more, step default to 1
						step = positive ? 1 : -1;
					}
				}
			} else {
				preIndex = nextMonthIndex(preIndex, step);
				if (preIndex != monthIndex) { //not a month sequence
					return CopyStep.instance; //a copy step
				}
			}
		}
		return new FullMonthStep(preIndex, step, getCaseType(bMonth), b == e ? Step.FULL_MONTH : -1, locale); //ZSS-69
	}
	
	private StepChunk getRowStepChunk(SSheet sheet, FillType fillType, int col, int row1, int row2, boolean pos, int colCount) {
		switch(fillType) {
		case DEFAULT:
			final int diff = row2 - row1;
			final SCell[] cells = new SCell[(pos ? diff : -diff) + 1];
			if (pos) {
				for (int row = row1, j = 0; row <= row2; ++row) {
					final SCell srcCell = sheet.getCell(row, col);
					cells[j++] = srcCell;
				}
			} else {
				for (int row = row1, j = 0; row >= row2; --row) {
					final SCell srcCell = sheet.getCell(row, col);
					cells[j++] = srcCell;
				}
			}
			return new StepChunk(cells, fillType, pos, colCount);
		case COPY:
		case FORMATS:
		case VALUES:
		default:
			return CopyStepChunkInstance; //pure copy
		}
	}
	private StepChunk getColStepChunk(SSheet sheet, FillType fillType, int row, int col1, int col2, boolean pos, int rowCount) {
		switch(fillType) {
		case DEFAULT:
			final int diff = col2 - col1;
			final SCell[] cells = new SCell[(pos ? diff : -diff) + 1];
			if (pos) {
				for (int col = col1, j = 0; col <= col2; ++col) {
					final SCell srcCell = sheet.getCell(row, col);
					cells[j++] = srcCell;
				}
			} else {
				for (int col = col1, j = 0; col >= col2; --col) {
					final SCell srcCell = sheet.getCell(row, col);
					cells[j++] = srcCell;
				}
			}
			return new StepChunk(cells, fillType, pos, rowCount);
		case COPY:
		case FORMATS:
		case VALUES:
		default:
			return CopyStepChunkInstance; //pure copy
		}
	}
	private static void replaceWithCopyStep(StepChunk[] stepChunks, int index, int b, int e) {
		for(int j = b; j < e; ++j) {
			stepChunks[j].replaceWithCopyStep(index);
		}
	}
	private static void handleSpecialCopyStep(StepChunk[] stepChunks, int srcCount, int siblingCount) {
		//handle special copy only case (two consecutive same type of row)
		for(int index = 0; index < srcCount; ++index) {
			int b = 0, preType = -1, j = 0, count = 0;
			for(; j < siblingCount; ++j) {
				final StepChunk stepChunk = stepChunks[j];
				final Step step = stepChunk.getStep(index);
				final int stepType = step.getDataType();
				if (preType != stepType) { //something different
					if (stepType != Step.BLANK) {
						if (preType >= 0 && count > 0) {
							replaceWithCopyStep(stepChunks, index, b, j);
						}
						count = 0;
						b = j;
						preType = stepType;
					}
				} else {
					if (stepType != Step.BLANK) { //count of equal type
						++count;
					}
				}
			}
			if (preType >= 0 && count > 0) { //last segment
				replaceWithCopyStep(stepChunks, index, b, j);
			}
		}
	}
	
	
	private PasteType toSupportedFillPasteType(FillType fillType){
		//TODO FILL_DEFAULT, FILL_DAYS, FILL_WEEKDAYS, FILL_MONTHS, FILL_YEARS, FILL_GROWTH_TREND
		switch(fillType) {
		case DEFAULT:
		case COPY:
			return PasteType.ALL;
		case FORMATS:
			return PasteType.FORMATS;
		case VALUES:
			return PasteType.VALUES;
		default:
			return null;
		}
	}
	
	public void fillDown(SSheet sheet, CellRegion srcRef, CellRegion dstRef, FillType fillType) {
		//TODO FILL_DEFAULT, FILL_DAYS, FILL_WEEKDAYS, FILL_MONTHS, FILL_YEARS, FILL_GROWTH_TREND
		PasteType pasteType = toSupportedFillPasteType(fillType);
		if(pasteType==null){
			return;
		}

		PasteOption pasteOption = new PasteOption();
		pasteOption.setPasteType(pasteType);
		
		final int rowCount = srcRef.getRowCount();
		final int colCount = srcRef.getColumnCount();
		final int srctRow = srcRef.getRow();
		final int srcbRow = srcRef.getLastRow();
		final int srclCol = srcRef.getColumn();
		final int srcrCol = srcRef.getLastColumn();
		
		final int dstbRow = dstRef.getLastRow();
		final StepChunk[] stepChunks = new StepChunk[colCount];
		//prepare StepChunks
		for(int c = srclCol, j = 0; c <= srcrCol; ++c) {
			final StepChunk stepChunk = getRowStepChunk(sheet, fillType, c, srctRow, srcbRow, true, colCount);
			stepChunks[j++] = stepChunk;
		}
		//handle special copy only case (two consecutive same type of row)
		handleSpecialCopyStep(stepChunks, rowCount, colCount);
		
		//ZSS-631 copy entire row at once to handle merge issue
		int dstRowDiffCount = dstRef.getRowCount()-rowCount;
		int pastebRow =  dstbRow - (dstRowDiffCount<=rowCount?0:dstRowDiffCount%rowCount);
		if(pastebRow >= srcbRow + rowCount){
			sheet.pasteCell(new SheetRegion(sheet,srctRow,srclCol,srcbRow,srcrCol), 
				new CellRegion(srcbRow + 1,srclCol,pastebRow,srcrCol), pasteOption);
		}

		if (!isPasteValue(pasteType)) return; //ZSS-722
		
		for(int c = srclCol, j = 0; c <= srcrCol; ++c) {
			final StepChunk stepChunk = stepChunks[j++];
			for(int srcIndex = 0, r = srcbRow + 1; r <= dstbRow; ++r, ++srcIndex) {
				final int index = srcIndex % rowCount;
				final int srcrow = srctRow + index;
				final SCell srcCell = sheet.getCell(srcrow, c);
				if (srcCell.isNull()) {
					sheet.clearCell(new CellRegion(r,c));
				} else {
					Object value = stepChunk.getStep(index).next(srcCell);
					applyStepValue(srcCell,sheet.getCell(r,c),value);
				}
			}
		}
	}
	
	public void applyStepValue(SCell srcCell,SCell dstCell,Object value){
		CellType type = srcCell.getType();
		if(type==CellType.FORMULA){
			//it is formula, it should shift by copy/paste already, just ignore it.
			return;
		}
		dstCell.setValue(value);
	}
	
	public void fillUp(SSheet sheet, CellRegion srcRef, CellRegion dstRef, FillType fillType) {
		//TODO FILL_DEFAULT, FILL_DAYS, FILL_WEEKDAYS, FILL_MONTHS, FILL_YEARS, FILL_GROWTH_TREND
		PasteType pasteType = toSupportedFillPasteType(fillType);
		if(pasteType==null){
			return;
		}

		PasteOption pasteOption = new PasteOption();
		pasteOption.setPasteType(pasteType);
		
		final int rowCount = srcRef.getRowCount();
		final int colCount = srcRef.getColumnCount();
		final int srctRow = srcRef.getRow();
		final int srcbRow = srcRef.getLastRow();
		final int srclCol = srcRef.getColumn();
		final int srcrCol = srcRef.getLastColumn();
		
		final int dsttRow = dstRef.getRow();
		final StepChunk[] stepChunks = new StepChunk[colCount];
		for(int c = srclCol, j = 0; c <= srcrCol; ++c) {
			final StepChunk stepChunk = getRowStepChunk(sheet, fillType, c, srcbRow, srctRow, false, colCount);
			stepChunks[j++] = stepChunk;
		}
		//handle special copy only case (two consecutive same type of row)
		handleSpecialCopyStep(stepChunks, rowCount, colCount);
		//ZSS-631 copy entire row at once to handle merge issue
		int dstRowDiffCount = dstRef.getRowCount()-rowCount;
		int pastetRow =  dsttRow + (dstRowDiffCount<=rowCount?0:dstRowDiffCount%rowCount);
		if(pastetRow <= srctRow - rowCount){
			//only past when the range is not overlapped
			sheet.pasteCell(new SheetRegion(sheet,srctRow,srclCol,srcbRow,srcrCol), 
					new CellRegion(pastetRow,srclCol,srctRow - 1,srcrCol), pasteOption);
		}
		
		if (!isPasteValue(pasteType)) return; //ZSS-722
		
		for(int c = srclCol, j = 0; c <= srcrCol; ++c) {
			final StepChunk stepChunk = stepChunks[j++];
			for(int srcIndex = 0, r = srctRow - 1; r >= dsttRow; --r, ++srcIndex) {
				final int index = srcIndex % rowCount;
				final int srcrow = srcbRow - index;
				final SCell srcCell = sheet.getCell(srcrow, c);
				if (srcCell.isNull()) {
					sheet.clearCell(new CellRegion(r,c));
				} else {
					Object value = stepChunk.getStep(index).next(srcCell);
					applyStepValue(srcCell, sheet.getCell(r,c),value);
				}
			}
		}
	}
	
	public void fillRight(SSheet sheet, CellRegion srcRef, CellRegion dstRef, FillType fillType) {
		//TODO FILL_DEFAULT, FILL_DAYS, FILL_WEEKDAYS, FILL_MONTHS, FILL_YEARS, FILL_GROWTH_TREND
		PasteType pasteType = toSupportedFillPasteType(fillType);
		if(pasteType==null){
			return;
		}

		PasteOption pasteOption = new PasteOption();
		pasteOption.setPasteType(pasteType);
		
		final int rowCount = srcRef.getRowCount();
		final int colCount = srcRef.getColumnCount();
		final int srclCol = srcRef.getColumn();
		final int srcrCol = srcRef.getLastColumn();
		final int srctRow = srcRef.getRow();
		final int srcbRow = srcRef.getLastRow();
		
		final int dstrCol = dstRef.getLastColumn();
		final StepChunk[] stepChunks = new StepChunk[rowCount];
		for(int r = srctRow, j = 0; r <= srcbRow; ++r) {
			final StepChunk stepChunk = getColStepChunk(sheet, fillType, r, srclCol, srcrCol, true, rowCount);
			stepChunks[j++] = stepChunk;
		}
		//handle special copy only case (two consecutive same type of row)
		handleSpecialCopyStep(stepChunks, colCount, rowCount);
		
		//ZSS-631 copy entire row at once to handle merge issue
		int dstColDiffCount = dstRef.getColumnCount()-colCount;
		int pasterCol =  dstrCol - (dstColDiffCount<=colCount?0:dstColDiffCount%colCount);
		if(pasterCol >= srcrCol + colCount){
			sheet.pasteCell(new SheetRegion(sheet,srctRow,srclCol,srcbRow,srcrCol), 
				new CellRegion(srctRow, srcrCol+1, srcbRow, pasterCol), pasteOption);
		}
		
		if (!isPasteValue(pasteType)) return; //ZSS-722
		
		for(int r = srctRow, j = 0; r <= srcbRow; ++r) {
			final StepChunk stepChunk = stepChunks[j++];
			for(int srcIndex = 0, c = srcrCol + 1; c <= dstrCol; ++c, ++srcIndex) {
				final int index = srcIndex % colCount;
				final int srccol = srclCol + index;
				final SCell srcCell = sheet.getCell(r, srccol);
				if (srcCell.isNull()) {
					sheet.clearCell(new CellRegion(r, c));
				} else {
					
					Object value = stepChunk.getStep(index).next(srcCell);
					applyStepValue(srcCell,sheet.getCell(r,c),value);
				}
			}
		}
	}
	
	public void fillLeft(SSheet sheet, CellRegion srcRef, CellRegion dstRef, FillType fillType) {
		//TODO FILL_DEFAULT, FILL_DAYS, FILL_WEEKDAYS, FILL_MONTHS, FILL_YEARS, FILL_GROWTH_TREND
		PasteType pasteType = toSupportedFillPasteType(fillType);
		if(pasteType==null){
			return;
		}

		PasteOption pasteOption = new PasteOption();
		pasteOption.setPasteType(pasteType);
		
		final int rowCount = srcRef.getRowCount();
		final int colCount = srcRef.getColumnCount();
		final int srclCol = srcRef.getColumn();
		final int srcrCol = srcRef.getLastColumn();
		final int srctRow = srcRef.getRow();
		final int srcbRow = srcRef.getLastRow();
		
		final int dstlCol = dstRef.getColumn();
		final StepChunk[] stepChunks = new StepChunk[rowCount];
		for(int r = srctRow, j = 0; r <= srcbRow; ++r) {
			final StepChunk stepChunk = getColStepChunk(sheet, fillType, r, srcrCol, srclCol, false, rowCount);
			stepChunks[j++] = stepChunk;
		}
		//handle special copy only case (two consecutive same type of row)
		handleSpecialCopyStep(stepChunks, colCount, rowCount);
		
		//ZSS-631 copy entire row at once to handle merge issue
		int dstColDiffCount = dstRef.getColumnCount()-colCount;
		int pastelCol =  dstlCol + (dstColDiffCount<=colCount?0:dstColDiffCount%colCount);
		if(pastelCol <= srclCol - colCount){
			sheet.pasteCell(new SheetRegion(sheet,srctRow,srclCol,srcbRow,srcrCol), 
				new CellRegion(srctRow, pastelCol, srcbRow, srclCol-1), pasteOption);
		}
		
		if (!isPasteValue(pasteType)) return; //ZSS-722
		
		for(int r = srctRow, j = 0; r <= srcbRow; ++r) {
			final StepChunk stepChunk = stepChunks[j++];
			for(int srcIndex = 0, c = srclCol - 1; c >= dstlCol; --c, ++srcIndex) {
				final int index = srcIndex % colCount;
				final int srccol = srcrCol - index;
				final SCell srcCell = sheet.getCell(r, srccol);
				if (srcCell.isNull()) {
					sheet.clearCell(new CellRegion(r, c));
				} else {
					Object value = stepChunk.getStep(index).next(srcCell);
					applyStepValue(srcCell,sheet.getCell(r,c),value);
				}
			}
		}
	}
	
	//ZSS-722: autoFill() with non-VALUE option should not copy value over.
	private boolean isPasteValue(PasteType pasteType) {
		switch(pasteType) {
		case ALL:
		case ALL_EXCEPT_BORDERS:
		case VALUES:
		case VALUES_AND_NUMBER_FORMATS:
			return true;
		default:
			return false;
		}
	}
	
	private static int getFillDirection(CellRegion srcRef, CellRegion dstRef) {
			final int dsttRow = dstRef.getRow();
			final int dstbRow = dstRef.getLastRow();
			final int dstlCol = dstRef.getColumn();
			final int dstrCol = dstRef.getLastColumn();
			
			final int srctRow = srcRef.getRow();
			final int srcbRow = srcRef.getLastRow();
			final int srclCol = srcRef.getColumn();
			final int srcrCol = srcRef.getLastColumn();
			
			//check fill direction
			if (srclCol == dstlCol && srcrCol == dstrCol) {
				if (dsttRow == srctRow) {
					if (dstbRow > srcbRow) { //fill down
						return FILL_DOWN;
					} else if (dstbRow == srcbRow) { //nothing to fill
						return FILL_NONE;
					}
				}
				if (dstbRow == srcbRow && dsttRow < srctRow) { //fill up
					return FILL_UP;
				}
			} else if (srctRow == dsttRow && srcbRow == dstbRow) {
				if (dstlCol == srclCol && dstrCol > srcrCol) { //fill right
					return FILL_RIGHT;
				}
				if (dstrCol == srcrCol && dstlCol < srclCol) { //fill left
					return FILL_LEFT;
				}
			}
		return FILL_INVALID;
	}
		
}
