/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import org.model.AutoRollbackConnection;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SHyperlink.HyperlinkType;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.format.FormatContext;
import org.zkoss.zss.model.sys.format.FormatEngine;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.input.InputEngine;
import org.zkoss.zss.model.sys.input.InputParseContext;
import org.zkoss.zss.model.sys.input.InputResult;

import java.io.Serializable;
import java.util.*;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public abstract class AbstractCellAdv implements SCell,Serializable{
	private static final long serialVersionUID = 1L;
	
	protected void checkType(CellType... types){
		CellType type = getType();
		for(CellType t:types){
			if(t.equals(type)){
				return;
			}
		}
		throw new IllegalStateException("is "+getType()+", not the one of "+Arrays.asList(types));
	}
	protected void checkFormulaResultType(CellType... types){
		if(!getType().equals(CellType.FORMULA)){
			throw new IllegalStateException("is "+getType()+", not the one of "+types);
		}
		
		Set<CellType> set = new LinkedHashSet<CellType>();
		for(CellType t:types){
			set.add(t);
		}
		if(!set.contains(getFormulaResultType())){
			throw new IllegalStateException("is "+getFormulaResultType()+", not the one of "+Arrays.asList(types));
		}
	}
	
	/*package*/ abstract void evalFormula(boolean sync);

    /*package*/
    public abstract Object getValue(boolean evaluatedVal);
	public abstract Object getValue(boolean evaluatedVal, boolean sync);

    @Override
	public Object getValue(){
		return getValue(true);
	}

	@Override
	public Object getValueSync(){
		return getValue(true, true);
	}


	@Override
	public void setStringValue(String value, AutoRollbackConnection connection, boolean updateToDB) {
		setValue(value, true, connection, updateToDB); //ZSS-853
	}

	@Override
	public String getStringValue() {
		return 	getStringValue(false);
	}

	@Override
	public String getStringValue(boolean sync) {
		if(getType() == CellType.FORMULA){
			evalFormula(sync);
			checkFormulaResultType(CellType.STRING,CellType.BLANK);
		}else{
			checkType(CellType.STRING,CellType.BLANK);
		}
		Object val = getValue();
		return val==null?"":val instanceof SRichText?((SRichText)val).getText():(String)val;
	}

	@Override
	public void setNumberValue(Double number, AutoRollbackConnection connection, boolean updateToDB) {
		setValue(number, connection, updateToDB);
	}

	@Override
	public Double getNumberValue()
	{
		return getNumberValue(false);
	}

	@Override
	public Double getNumberValue(boolean sync) {
		if(getType() == CellType.FORMULA){
			evalFormula(sync);
			checkFormulaResultType(CellType.NUMBER,CellType.BLANK);
		}else{
			checkType(CellType.NUMBER,CellType.BLANK);
		}
		Object val = getValue();
		if(val instanceof Double){
			return (Double)val;
		}else if(val instanceof Number){
			return ((Number)val).doubleValue();
		}else{
			return Double.valueOf(0D);
		}
	}

	@Override
	public void setDateValue(Date date, AutoRollbackConnection connection, boolean updateToDB) {
		double num = EngineFactory.getInstance().getCalendarUtil().dateToDoubleValue(date);
		setNumberValue(num, connection, updateToDB);
	}

	@Override
	public Date getDateValue() {
			return getDateValue(false);
	}

	@Override
	public Date getDateValue(boolean sync) {
		//compatible with 3.0(poi)
		if (CellType.BLANK.equals(getType())) {
            return null;
        }
		Number num = getNumberValue(sync);
		return EngineFactory.getInstance().getCalendarUtil().doubleValueToDate(num.doubleValue());
	}
	
	@Override
	public void setBooleanValue(Boolean boolVal, AutoRollbackConnection connection, boolean updateToDB) {
		setValue(boolVal, connection, updateToDB);
	}

	@Override
	public Boolean getBooleanValue() {
		return 	getBooleanValue(false);
	}

	@Override
	public Boolean getBooleanValue(boolean sync) {
		CellType type = getType();
		if(getType() == CellType.FORMULA){
			evalFormula(sync);
			checkFormulaResultType(CellType.BOOLEAN,CellType.BLANK);
		}else{
			checkType(CellType.BOOLEAN,CellType.BLANK);
		}
		
		return Boolean.TRUE.equals(getValue());
	}

	@Override
	public ErrorValue getErrorValue() {
		return getErrorValue(false);
	}

	@Override
	public ErrorValue getErrorValue(boolean sync) {
		if(getType() == CellType.FORMULA){
			evalFormula(sync);
			checkFormulaResultType(CellType.ERROR);
		}else{
			checkType(CellType.ERROR);
		}
		return (ErrorValue)getValue();
	}

	@Override
	public void setErrorValue(ErrorValue errorValue, AutoRollbackConnection connection, boolean updateToDB) {
		setValue(errorValue, connection, updateToDB);
	}

	@Override
	public String getFormulaValue() {
		checkType(CellType.FORMULA);
		FormulaExpression expr = (FormulaExpression)getValue(false);
		return expr.getFormulaString();
	}
	

	@Override
	public SRichText setupRichTextValue() {
		Object val = getValue();
		if(val instanceof SRichText){
			return (SRichText)val;
		}
		SRichText text = new RichTextImpl();
		// Mangesh - Value needs to be updated in import
		setValue(text, null, false);
		return text;
	}

//	@Override
//	public void setRichTextValue(NRichText text) {
//		Validations.argInstance(text, AbstractRichTextAdv.class);
//		setValue(text);
//	}

	@Override
	public SRichText getRichTextValue() {
		return getRichTextValue(false);
	}

	@Override
	public SRichText getRichTextValue(boolean sync) {
		if(getType() == CellType.FORMULA){
			evalFormula(sync);
			checkFormulaResultType(CellType.STRING,CellType.BLANK);
		}else{
			checkType(CellType.STRING,CellType.BLANK);
		}
		Object val = getValue();
		if(val instanceof SRichText){
			return (SRichText)val;
		}
		return new ReadOnlyRichTextImpl(val==null?"":(String)val,getCellStyle().getFont());
	}	
	
	@Override 
	public boolean isRichTextValue(){
		Object val = getValue(false);
		return val instanceof SRichText;
	}
	
	@Override
	public SHyperlink setupHyperlink(HyperlinkType type,String address,String label){
		SHyperlink hyperlink = getHyperlink();
		if(hyperlink!=null){
			hyperlink.setType(type);
			hyperlink.setAddress(address);
			hyperlink.setLabel(label);
			return hyperlink;
		}
		setHyperlink(hyperlink = new HyperlinkImpl(type,address,label));
		return hyperlink;
	}
	@Override
	public SComment setupComment(){
		SComment comment = getComment();
		if(comment!=null){
			return comment;
		}
		setComment(comment = new CommentImpl());
		return comment;
	}
	
	/*package*/ public abstract Ref getRef();

	//ZSS-565: Support input with Swedish locale into formula 
	public abstract void setFormulaValue(String formula, Locale locale, AutoRollbackConnection connection, boolean updateToDB);
	
	//ZSS-818
	//@since 3.7.0
	public abstract void setFormulaResultValue(ValueEval value);
	
	//ZSS-853
	//@since 3.7.0
	protected abstract void setValue(Object value, boolean aString, AutoRollbackConnection connection, boolean updateToDB);
	
	//ZSS-873
	//@since 3.7.0
	public abstract FormulaExpression getFormulaExpression();

	private boolean equalObjects(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		if (obj1 != null) {
			return obj1.equals(obj2);
		}
		return false;
	}

	public void setValueParse(String valueParse, AutoRollbackConnection connection, int trxId, boolean updateToDB) {
		final InputEngine ie = EngineFactory.getInstance().createInputEngine();
		Locale locale = ZssContext.getCurrent().getLocale();
		InputResult result;
		result = ie.parseInput(valueParse == null ? ""
				: valueParse, getCellStyle().getDataFormat(), new InputParseContext(locale));
		Object resultVal = result.getValue();
		if (getType() == result.getType()) {
			// 20140828, henrichen: getValue() will cause evalFormula(); costly.
			if (result.getType() == CellType.FORMULA) {
				FormatEngine fe = EngineFactory.getInstance().createFormatEngine();
				String oldEditText = fe.getEditText(this, new FormatContext(locale));
				if (valueParse.equals(oldEditText)) {
					return;
				}
			} else {
				Object cellval = getValue();
				if (equalObjects(cellval, resultVal)) {
					return;
				}
			}
		}
		String format = result.getFormat();

		switch (result.getType()) {
			case BLANK:
				clearValue(connection, updateToDB);
				break;
			case BOOLEAN:
				setBooleanValue((Boolean) resultVal, connection, updateToDB);
				break;
			case FORMULA:
				setFormulaValue((String) resultVal, locale, connection, updateToDB); //ZSS-565
				break;
			case NUMBER:
				if (resultVal instanceof Date) {
					setDateValue((Date) resultVal, connection, updateToDB);
				} else {
					setNumberValue((Double) resultVal, connection, updateToDB);
				}
				break;
			case STRING:
				setStringValue((String) resultVal, connection, updateToDB);
				break;
			case ERROR:
				setErrorValue(ErrorValue.valueOf(((Byte) resultVal).byteValue()), connection, updateToDB); //ZSS-672
				break;
			default:
				setValue(resultVal, connection, updateToDB);
		}
	}

	protected abstract byte[] toBytes();

	public abstract void shift(int rowShift, int colShift);

    public abstract void setSheet(AbstractSheetAdv sheet);
}
