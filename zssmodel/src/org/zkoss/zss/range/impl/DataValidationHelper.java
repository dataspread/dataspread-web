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
package org.zkoss.zss.range.impl;

import java.util.Date;
import java.util.Locale;

import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.SDataValidation.ValidationType;
import org.zkoss.zss.model.impl.FormulaResultCellValue;
import org.zkoss.zss.model.sys.CalendarUtil;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;
import org.zkoss.zss.model.sys.input.InputEngine;
import org.zkoss.zss.model.sys.input.InputParseContext;
import org.zkoss.zss.model.sys.input.InputResult;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class DataValidationHelper {

	private final SDataValidation _validation;
	private final SSheet _sheet;
	
	public DataValidationHelper(SDataValidation validation) {
		this._validation = validation;
		this._sheet = validation.getSheet();
	}

	public boolean validate(int row, int col, String editText, String dataformat) {
		final InputResult result = normalizeInput(editText, dataformat, ZssContext.getCurrent().getLocale());
		return validate(row, col, result.getType(),result.getValue(), dataformat);
	}
	
	public boolean validate(int row, int col, CellType cellType, Object value, String dataformat) {
		//allow any value => no need to do validation
		ValidationType vtype = _validation.getValidationType();
		if (vtype == ValidationType.ANY) { //can be any value, meaning no validation
			return true;
		}
		//ignore empty and value is empty
		if (vtype!=ValidationType.TEXT_LENGTH 
				&& (value == null || (value instanceof String && ((String)value).length() == 0))) {
			if (_validation.isIgnoreBlank()) {
				return true;
			}
		}
		//get new evaluated formula value 
		if (cellType == CellType.FORMULA) {
			
			FormulaEngine engine = EngineFactory.getInstance().createFormulaEngine();
			
			FormulaExpression expr = engine.parse((String)value, new FormulaParseContext(_sheet, null));
			if(expr.hasError()){
				return false;
			}
			FormulaResultCellValue result = new FormulaResultCellValue(engine.evaluate(expr, new FormulaEvaluationContext(_sheet, null)));
			
			value = result.getValue();
			cellType = result.getCellType();
		}
		CalendarUtil cal = EngineFactory.getInstance().getCalendarUtil();
		//start validation
		boolean success = true;
		switch(vtype) {
			// Integer ('Whole number') type
			case INTEGER:
				if (!isInteger(value) || !validateOperation((Number)value)) {
					success = false;
				}
				break;
			// Decimal type
			case DECIMAL:
				if (!isDecimal(value) || !validateOperation((Number)value)) {
					success = false;
				}
				break;
			// ZSS-260: the input value is a Date object, must convert it to Excel date type (a double number) before validating
			// Date type
			case DATE:
			// Time type
			case TIME:
				success = (value instanceof Date) && validateOperation(cal.dateToDoubleValue((Date)value));
				break;
			// List type ( combo box type )
			case LIST:
				if (!validateListOperation(row, col, (value instanceof Date)?cal.dateToDoubleValue((Date)value):value, dataformat)) {;
					success = false;
				}
				break;
			// String length type
			case TEXT_LENGTH:
				if ((value!=null && !isString(value))
					|| !validateOperation(Integer.valueOf(value == null ? 0 : ((String)value).length()))) {
					success = false;
				}
				break;
			// Formula ( 'Custom' ) type
			case CUSTOM:
				//zss 3.5 log it
				success = false;
//				throw new UnsupportedOperationException("Custom Validation is not supported yet!");
		}
		return success;
	}
	
	private static boolean isInteger(Object value) {
		if (value instanceof Number) {
			return ((Number)value).intValue() ==  ((Number)value).doubleValue();
		}
		return false;
	}
	private static boolean isDecimal(Object value) {
		return value instanceof Number;
	}
	
	private static boolean isString(Object value) {
		return value instanceof String; 
	}
	
	private boolean validateOperation(Number value) {
		if (value == null) {
			return false;
		}
		
		Object value1 = _validation.getValue1(0);
		if (!(value1 instanceof Number)) { //type does not match
			return false;
		}
		Object value2 = _validation.getValue2(0);
		double v1 = ((Number)value1).doubleValue();
		double v = value.doubleValue();
		double v2;
		switch(_validation.getOperatorType()) {
		case BETWEEN:
			if (!(value2 instanceof Number)) { //type does not match
				return false;
			}
			v2 = ((Number)value2).doubleValue();
			return v >= v1 && v <= v2;
		case NOT_BETWEEN:
			if (!(value2 instanceof Number)) { //type does not match
				return false;
			}
			v2 = ((Number)value2).doubleValue();
			return v < v1 || v > v2;
		case EQUAL:
			return v == v1;
		case NOT_EQUAL:
			return v != v1;
		case GREATER_THAN:
				return v > v1;
		case LESS_THAN:
				return v < v1;
		case GREATER_OR_EQUAL:
				return v >= v1;
		case LESS_OR_EQUAL:
				return v <= v1;
		}
		return true;
	}
	
	private boolean validateListOperation(int row, int col, Object value, String dataformat) {
		if (value == null) {
			return false;
		}
		if(_validation.hasReferToCellList()){
			for(SCell cell:_validation.getReferToCellList(row, col)){
				Object val = cell.getValue();
				if(value.equals(val)){
					return true;
				}
			}
		}else{
			int size = _validation.getNumOfValue1();
			for(int i=0;i<size;i++){
				Object val = _validation.getValue1(i);
				if(value.equals(val)){
					return true;
				}
				if (val instanceof String 
						&& value.equals(normalizeValue((String)val, dataformat))) {
					return true;
				}
			}
		}
		return false;
	}

	private InputResult normalizeInput(String editText, String dataformat, Locale locale) {
		final InputEngine ie = EngineFactory.getInstance().createInputEngine();
		return ie.parseInput(editText == null ? ""
				: editText, dataformat, new InputParseContext(locale));
	}
	
	private Object normalizeValue(String editText, String dataformat) {
		return normalizeInput(editText, dataformat, Locale.US).getValue();
	}
}
