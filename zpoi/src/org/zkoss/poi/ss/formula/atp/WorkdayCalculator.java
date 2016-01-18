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

package org.zkoss.poi.ss.formula.atp;

import java.util.Calendar;
import java.util.Date;

import org.zkoss.poi.ss.usermodel.DateUtil;

/**
 * A calculator for workdays, considering dates as excel representations.
 * 
 * @author jfaenomoto@gmail.com
 */
public class WorkdayCalculator {

    public static final WorkdayCalculator instance = new WorkdayCalculator();

    /**
     * Constructor.
     */
    private WorkdayCalculator() {
        // enforcing singleton
    }

    /**
     * Calculate how many workdays are there between a start and an end date, as excel representations, considering a range of holidays.
     * 
     * @param start start date.
     * @param end end date.
     * @param holidays an array of holidays.
     * @return number of workdays between start and end dates, including both dates.
     */
    public int calculateWorkdays(double start, double end, double[] holidays) {
        int saturdaysPast = this.pastDaysOfWeek(start, end, Calendar.SATURDAY);
        int sundaysPast = this.pastDaysOfWeek(start, end, Calendar.SUNDAY);
        int nonWeekendHolidays = this.calculateNonWeekendHolidays(start, end, holidays);
        return (int) (end - start + 1) - saturdaysPast - sundaysPast - nonWeekendHolidays;
    }

    /**
     * Calculate the workday past x workdays from a starting date, considering a range of holidays.
     * 
     * @param start start date.
     * @param workdays number of workdays to be past from starting date.
     * @param holidays an array of holidays.
     * @return date past x workdays.
     */
    public Date calculateWorkdays(double start, int workdays, double[] holidays) {
    	if (workdays == 0){
    		return DateUtil.getJavaDate(start);
    	}
        double targetDay = start + workdays; //final result
        double durationStart = getDurationNextStart(start, workdays); //the start day of the duration to count holidays 
        
        while(true) {
        	double durationEnd = targetDay;   //the end day of the duration to count holidays
            int holidays2Skip = countHolidaysInDuration(durationStart, durationEnd, holidays);
            if (holidays2Skip == 0){
            	break;
            }else{
            	targetDay = calculateTargetDay(targetDay, holidays2Skip, workdays);
            	//count holidays in next duration
            	durationStart = getDurationNextStart(durationEnd, workdays);
            }
        }
        
        return DateUtil.getJavaDate(targetDay);
    }
    
    /*
     * calculate next start day of the duration to count holidays
     */
    private double getDurationNextStart(double end, double workdays){
    	return workdays >= 0 ? end+1 : end-1;
    }
    
    /*
     * move target day by bypassing holidays. 
     */
    private double calculateTargetDay(double targetDay, int holidays2Skip, double workdays){
    	if (workdays >= 0){
    		return targetDay + holidays2Skip;
    	}else{
    		return targetDay - holidays2Skip;
    	}
    }

    /*
     * count holidays including Saturday, Monday, and specified non-weekend holidays between 
     * start day and end day.
     * */
	private int countHolidaysInDuration(double start, double end, double[] holidays) {
		int saturdaysPast = this.pastDaysOfWeek(start, end, Calendar.SATURDAY);
		int sundaysPast = this.pastDaysOfWeek(start, end, Calendar.SUNDAY);
		int nonWeekendHolidays = this.calculateNonWeekendHolidays(start, end, holidays);

		return saturdaysPast + sundaysPast + nonWeekendHolidays;
	}

    /**
     * Calculates how many days of week past between a start and an end date.
     * 
     * @param start start date.
     * @param end end date.
     * @param dayOfWeek a day of week as represented by {@link Calendar} constants.
     * @return how many days of week past in this interval.
     */
    protected int pastDaysOfWeek(double start, double end, int dayOfWeek) {
        int pastDaysOfWeek = 0;
        int startDay = (int) Math.floor(start < end ? start : end);
        int endDay = (int) Math.floor(end > start ? end : start);
        for (; startDay <= endDay; startDay++) {
            Calendar today = Calendar.getInstance();
            today.setTime(DateUtil.getJavaDate(startDay));
            if (today.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                pastDaysOfWeek++;
            }
        }
        return pastDaysOfWeek ;
    }

    /**
     * Calculates how many holidays in a list are workdays, considering an interval of dates.
     * 
     * @param start start date.
     * @param end end date.
     * @param holidays an array of holidays.
     * @return number of holidays that occur in workdays, between start and end dates.
     */
    protected int calculateNonWeekendHolidays(double start, double end, double[] holidays) {
        int nonWeekendHolidays = 0;
        double startDay = start < end ? start : end;
        double endDay = end > start ? end : start;
        for (int i = 0; i < holidays.length; i++) {
            if (isInARange(startDay, endDay, holidays[i])) {
                if (!isWeekend(holidays[i])) {
                    nonWeekendHolidays++;
                }
            }
        }
        return nonWeekendHolidays;
    }

    /**
     * @param aDate a given date.
     * @return <code>true</code> if date is weekend, <code>false</code> otherwise.
     */
    protected boolean isWeekend(double aDate) {
        Calendar date = Calendar.getInstance();
        date.setTime(DateUtil.getJavaDate(aDate));
        return date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    /**
     * @param aDate a given date.
     * @param holidays an array of holidays.
     * @return <code>true</code> if date is a holiday, <code>false</code> otherwise.
     */
    protected boolean isHoliday(double aDate, double[] holidays) {
        for (int i = 0; i < holidays.length; i++) {
            if (Math.round(holidays[i]) == Math.round(aDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param aDate a given date.
     * @param holidays an array of holidays.
     * @return <code>1</code> is not a workday, <code>0</code> otherwise.
     */
    protected int isNonWorkday(double aDate, double[] holidays) {
        return isWeekend(aDate) || isHoliday(aDate, holidays) ? 1 : 0;
    }

    /**
     * @param start start date.
     * @param end end date.
     * @param aDate a date to be analyzed.
     * @return <code>true</code> if aDate is between start and end dates, <code>false</code> otherwise.
     */
    protected boolean isInARange(double start, double end, double aDate) {
        return aDate >= start && aDate <= end;
    }
}
