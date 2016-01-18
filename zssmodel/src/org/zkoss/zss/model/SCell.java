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
package org.zkoss.zss.model;

import java.util.Date;

import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.zss.model.SHyperlink.HyperlinkType;
/**
 * Represent a cell of a sheet in a Spreadsheet. A cell contains value and style ({@link CellStyle}), and its type is one of {@link CellType}.
 * @author dennis
 * @since 3.5.0
 */
public interface SCell extends CellStyleHolder,FormulaContent{

	/**
	 * @since 3.5.0
	 */
	public enum CellType {
		BLANK(3),
		STRING(1),
		FORMULA(2),
		NUMBER(0),
		BOOLEAN(4),		
		ERROR(5);
		
		private int value;
	    private CellType(int value) {
	        this.value = value;
	    }

	    public int value() {
	        return value;
	    }
	    
//	    public String toString(){
//	    	new Exception().printStackTrace();
//	    	return "XXX";
//	    }
	}
	
	public SSheet getSheet();
	
	public CellType getFormulaResultType();
	
	/**
	 * @return the cell type
	 */
	public CellType getType();
	/**
	 * @return cell value.
	 */
	public Object getValue();
	
	public void setValue(Object value);
//	public NCellValue getCellValue();
	
	/**
	 * Because you always get a not-null cell object, use this method to judge the cell is really null or not.
	 * @return TRUE if this cell is really null which means it have not been created.
	 */
	public boolean isNull();
	
	public int getRowIndex();
	
	public int getColumnIndex();
	
	/**
	 * @return cell reference like A1
	 */
	public String getReferenceString();
	
	public SHyperlink getHyperlink();
	
	/**
	 * Set or clear a hyperlink
	 * @param hyperlink hyperlink to set, or null to clear
	 */
	public void setHyperlink(SHyperlink hyperlink);
	
	/** setup a hyperlink*/
	public SHyperlink setupHyperlink(HyperlinkType type,String address,String label);

//	boolean isReadonly();
//	

	/**
	 * clear cell value , reset it to blank
	 */
	public void clearValue();//
	/**
	 * Set string value, if the value start with '=', then it sets as formula 
	 */
	public void setStringValue(String value);
	public String getStringValue();
	
	
	/** 
	 * Setup a rich text value(Create a new one if the old value is not a rich-text) and return the instance which to be edited.
	 */
	public SRichText setupRichTextValue();
	
	/**
	 * Return the rich text value. if this cell is a simple string value, it will return a read-only rich-text which wraps string-value and current font.
	 * @return
	 */
	public SRichText getRichTextValue();
	
	/**
	 *  Check if this cell contains a rich-text value 
	 */
	public boolean isRichTextValue();
	
	/**
	 * set formula with string without '=', e.g. SUM(A1:B2)
	 * @param fromula
	 */
	public void setFormulaValue(String formula);
	
	/**
	 * @return returns formula string without '=', e.g. SUM(A1:B2)
	 */
	public String getFormulaValue();
	
	public void setNumberValue(Double number);
	public Double getNumberValue();
	
	/**
	 * Sets the number value a date instance, it will transfer the date to double value 
	 */
	public void setDateValue(Date date);
	/**
	 * Gets the date value that is transfered by the double number value.
	 */
	public Date getDateValue();
	
	public void setBooleanValue(Boolean bool);
	/**
	 * Gets the boolean value
	 */
	public Boolean getBooleanValue();
	
	public ErrorValue getErrorValue();
	public void setErrorValue(ErrorValue errorValue);
	
	
	public void setComment(SComment comment);
	public SComment setupComment();
	public SComment getComment();
	
	/**
	 * Delete the comment associated with this cell.
	 * @since 3.7.0
	 */
	public void deleteComment();
}
