/* CellData.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model;

import java.util.Date;

import org.zkoss.zss.api.IllegalFormulaException;

/**
 * This interface provides access to "data" part of a cell including type, text, and value.
 * @author dennis
 * @since 3.0.0
 */
public interface CellData {
	
	/**
	 * The cell type
	 * @author dennis
	 */
	public enum CellType{
		NUMERIC,
		STRING,
		FORMULA,
		BLANK,
		BOOLEAN,
		ERROR;
	}
	
	/**
	 * @return the row index
	 */
	public int getRow();
	/**
	 * @return column index
	 */
	public int getColumn();
	
	/**
	 * Gets the cell type.<br/>
	 * If the cell is a formula then you are possible getting the {@link CellType#FORMULA} or {@link CellType#ERROR}.<br/>
	 * You could use {@link #getResultType()} to get result type for a cell or a formula-cell. 
	 * @return cell type
	 */
	public CellType getType();
	
	/**
	 * Gets the cell result type. <br/>
	 * 
	 * @return cell result type
	 */
	public CellType getResultType();
	
	/**
	 * @return the data object, it is String, Number, Boolean
	 */
	public Object getValue();
	
	/**
	 * 
	 * @return the double data object of this cell
	 */
	public Double getDoubleValue();
	
	/**
	 * 
	 * @return the date data object of this cell
	 */
	public Date getDateValue();
	
	/**
	 * 
	 * @return the string object of this cell
	 */
	public String getStringValue();
	
	/**
	 * 
	 * @return the string object that represent the formula of this cell
	 */
	public String getFormulaValue();
	
	/**
	 * 
	 * @return the boolean object of this cell
	 */
	public Boolean getBooleanValue();
	
	/**
	 * @return the formatted text string
	 */
	public String getFormatText();
	
	/**
	 * @return the edit text
	 */
	public String getEditText();
	
	/**
	 * @return true if it is a blank cell
	 */
	public boolean isBlank();
	
	/**
	 * @return true if it is a formula cell
	 */
	public boolean isFormula();
	
	/**
	 * Sets the data object, it should be a String, Number, Date or Boolean
	 * @param value the data object
	 */
	public void setValue(Object value);
	
	/**
	 * Sets the edit text, for example, "123" is number, "2012/1/1" is date, "=SUM(A1:B1)" is formula
	 * @param editText
	 * @throws IllegalFormulaException
	 */
	public void setEditText(String editText);
	
	/**
	 * Validates the edit text if this cell has validation constraint
	 * @param editText the edit text
	 * @return false if the editText can't pass the validation
	 */
	public boolean validateEditText(String editText);
	
	//ZSS-725
	/**
	 * Sets the rich text in html format.
	 * @since 3.6.0 
	 */
	public void setRichText(String html);
	
	
	//ZSS-725
	/**
	 * @return rich text in html format; null if not a rich text.
	 * @since 3.6.0
	 */
	public String getRichText();
}
