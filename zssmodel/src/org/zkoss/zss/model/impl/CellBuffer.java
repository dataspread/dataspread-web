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
package org.zkoss.zss.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.poi.ss.formula.FormulaRenderer;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SComment;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SHyperlink;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.impl.sys.formula.ParsingBook;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.range.impl.StyleUtil;

/**
 * a help class to hold cell data and apply to another
 * @author Dennis
 * @since 3.5.0
 */
public class CellBuffer {
	
	private boolean _null = true;;
	
	private CellType _type;
	private Object _value;
	private String _formula;
	private SCellStyle _style;
	
	private SComment _comment;
	private SDataValidation _validation;
	private SHyperlink _hyperlink;
	
	public CellBuffer(){
	}
	
	public boolean isNull(){
		return _null;
	}
	
	public void setNull(boolean isNull){
		this._null = isNull;
	}
	
	public CellType getType() {
		return _type;
	}
	public void setType(CellType type) {
		this._type = type;
	}
	public Object getValue() {
		return _value;
	}
	public void setValue(Object value) {
		this._value = value;
	}
	public String getFormula() {
		return _formula;
	}
	public void setFormula(String formula) {
		this._formula = formula;
	}
	public SCellStyle getStyle() {
		return _style;
	}
	public void setStyle(SCellStyle style) {
		this._style = style;
	}
	public SComment getComment() {
		return _comment;
	}
	public void setComment(SComment comment) {
		this._comment = comment;
	}
	public SDataValidation getValidation() {
		return _validation;
	}
	public void setValidation(SDataValidation validation) {
		this._validation = validation;
	}
	public SHyperlink getHyperlink(){
		return _hyperlink;
	}
	public void setHyperlink(SHyperlink hyperlink){
		this._hyperlink = hyperlink;
	}
	
	public static CellBuffer bufferAll(SCell cell){
		CellBuffer buffer = new CellBuffer();
		if(!cell.isNull()){
			buffer.setNull(false);
			buffer.setType(cell.getType());
			
			if(cell.getType() == CellType.FORMULA){
				//ZSS-1002
				final String formula = cell.getFormulaValue();
				final ParsingBook parsingBook = new ParsingBook(cell.getSheet().getBook());
				final Ptg[] tokens = ((AbstractCellAdv)cell).getFormulaExpression().getPtgs();
				final String result = FormulaRenderer.toFormulaCopyText(parsingBook, tokens, formula);
				buffer.setFormula(result);
			}else{
				buffer.setValue(cell.getValue());
			}
			
			buffer.setStyle(StyleUtil.prepareStyle(cell));
			buffer.setHyperlink(cell.getHyperlink());
			buffer.setComment(cell.getComment());
			buffer.setValidation(cell.getSheet().getDataValidation(cell.getRowIndex(), cell.getColumnIndex()));
		}
		return buffer;
	}
	
	public void applyStyle(SCell destCell) {
		//style are shared between sheets, could use it directly
		destCell.setCellStyle(getStyle());
	}
	
	public void applyValue(SCell destCell) {
		if(getType()==CellType.FORMULA){
			destCell.setFormulaValue(getFormula());
		}else{
			destCell.setValue(getValue());
		}
	}
	public void applyComment(SCell destCell) {
		SComment comment = getComment();
		destCell.setComment(comment==null?null:((AbstractCommentAdv)comment).clone());
	}
	
	public void applyHyperlink(SCell destCell) {
		SHyperlink link = getHyperlink();
		destCell.setHyperlink(link==null?null:((AbstractHyperlinkAdv)link).clone());
	}
}
