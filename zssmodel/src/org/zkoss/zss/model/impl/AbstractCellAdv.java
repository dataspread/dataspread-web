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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.usermodel.Hyperlink;
import org.zkoss.zss.model.ErrorValue;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SComment;
import org.zkoss.zss.model.SHyperlink;
import org.zkoss.zss.model.SRichText;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SHyperlink.HyperlinkType;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public abstract class AbstractCellAdv implements SCell,LinkedModelObject,Serializable{
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
	
	/*package*/ abstract void evalFormula();
	/*package*/ abstract Object getValue(boolean evaluatedVal);
	
	@Override
	public Object getValue(){
		return getValue(true);
	}
	
	@Override
	public void setStringValue(String value) {
		setValue(value, true); //ZSS-853
	}

	@Override
	public String getStringValue() {
		if(getType() == CellType.FORMULA){
			evalFormula();
			checkFormulaResultType(CellType.STRING,CellType.BLANK);
		}else{
			checkType(CellType.STRING,CellType.BLANK);
		}
		Object val = getValue();
		return val==null?"":val instanceof SRichText?((SRichText)val).getText():(String)val;
	}

	@Override
	public void setNumberValue(Double number) {
		setValue(number);
	}

	@Override
	public Double getNumberValue() {
		if(getType() == CellType.FORMULA){
			evalFormula();
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
	public void setDateValue(Date date) {
		double num = EngineFactory.getInstance().getCalendarUtil().dateToDoubleValue(date);
		setNumberValue(num);
	}

	@Override
	public Date getDateValue() {
		//compatible with 3.0(poi)
		if (CellType.BLANK.equals(getType())) {
            return null;
        }
		Number num = getNumberValue();
		return EngineFactory.getInstance().getCalendarUtil().doubleValueToDate(num.doubleValue());
	}
	
	@Override
	public void setBooleanValue(Boolean date) {
		setValue(date);
	}

	@Override
	public Boolean getBooleanValue() {
		CellType type = getType();
		if(getType() == CellType.FORMULA){
			evalFormula();
			checkFormulaResultType(CellType.BOOLEAN,CellType.BLANK);
		}else{
			checkType(CellType.BOOLEAN,CellType.BLANK);
		}
		
		return Boolean.TRUE.equals(getValue());
	}

	@Override
	public ErrorValue getErrorValue() {
		if(getType() == CellType.FORMULA){
			evalFormula();
			checkFormulaResultType(CellType.ERROR);
		}else{
			checkType(CellType.ERROR);
		}
		return (ErrorValue)getValue();
	}

	@Override
	public void setErrorValue(ErrorValue errorValue) {
		setValue(errorValue);
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
		setValue(text);
		return text;
	}

//	@Override
//	public void setRichTextValue(NRichText text) {
//		Validations.argInstance(text, AbstractRichTextAdv.class);
//		setValue(text);
//	}

	@Override
	public SRichText getRichTextValue() {
		if(getType() == CellType.FORMULA){
			evalFormula();
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
	
	/*package*/ abstract void setIndex(int newidx);
	/*package*/ abstract void setRow(int oldRowIdx, AbstractRowAdv row);
	/*package*/ abstract Ref getRef();

	//ZSS-565: Support input with Swedish locale into formula 
	public abstract void setFormulaValue(String formula, Locale locale);
	
	//ZSS-688
	//@since 3.6.0
	/*package*/ abstract AbstractCellAdv cloneCell(AbstractRowAdv row);
	
	//ZSS-818
	//@since 3.7.0
	public abstract void setFormulaResultValue(ValueEval value);
	
	//ZSS-853
	//@since 3.7.0
	protected abstract void setValue(Object value, boolean aString);
	
	//ZSS-873
	//@since 3.7.0
	public abstract FormulaExpression getFormulaExpression(); 
}
