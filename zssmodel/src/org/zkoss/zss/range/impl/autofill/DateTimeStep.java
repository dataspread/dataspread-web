/* DateTimeStep.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2/12/2013 9:31:44 AM, Created by dennischen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/


package org.zkoss.zss.range.impl.autofill;

import java.util.Calendar;
import java.util.Date;


//import org.zkoss.poi.ss.usermodel.DateUtil;



import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.sys.EngineFactory;

/**
 * Step value by given steps by Calendar
 * @author dennischen
 * @since 2.6.0
 */
//ZSS-541 date autofill timestamp is incorrect. 
//use calendar to calculate date step to fix 
public class DateTimeStep implements Step {
	private final Calendar _cal = Calendar.getInstance(); //TODO timezone?
	private long _current;
	private final int _yearStep;
	private final int _monthStep;
	private final int _dayStep;
	private final int _millisecondStep;
	private final int _type;
	private final long _zero;
	
	public DateTimeStep(Date date, int yearStep, int monthStep, int dayStep, int millisecondStep, int type) {
		_current = date.getTime();
		_yearStep = yearStep;
		_monthStep = monthStep;
		_dayStep = dayStep;
		_millisecondStep = millisecondStep;
		_type = type;
		_zero = EngineFactory.getInstance().getCalendarUtil().doubleValueToDate(0.0).getTime();
	}
	@Override
	public Object next(SCell cell) {
		_cal.clear();
		_cal.setTimeInMillis(_current);
		
		if(_yearStep!=0){
			_cal.add(Calendar.YEAR,_yearStep);
		}
		if(_monthStep!=0){
			_cal.add(Calendar.MONTH,_monthStep);
		}
		if(_dayStep!=0){
			_cal.add(Calendar.DATE,_dayStep);
		}
		if(_millisecondStep!=0){
			_cal.add(Calendar.MILLISECOND, _millisecondStep);
		}
		
		_current = _cal.getTimeInMillis();
		if (_current < _zero) { //refer to original MsecondStep and DateStep, but why?
			_current += 24 * 60 * 60 * 1000L;
			_cal.setTimeInMillis(_current);
		}
		
		return _cal.getTime();
	}
	@Override
	public int getDataType() {
		return _type;
	}
}
