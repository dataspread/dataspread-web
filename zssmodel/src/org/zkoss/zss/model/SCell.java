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

import org.model.AutoRollbackConnection;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.zss.model.SHyperlink.HyperlinkType;
import org.zkoss.zss.model.SSemantics.Semantics;

import java.sql.Connection;
import java.util.Date;
/**
 * Represent a cell of a sheet in a Spreadsheet. A cell contains value and style ({@link CellStyle}), and its type is one of {@link CellType}.
 * @author dennis
 * @since 3.5.0
 */
public interface SCell extends CellStyleHolder,FormulaContent{

	SSheet getSheet();

	CellType getFormulaResultType();
	
	/**
	 * @return the cell type
	 */
	CellType getType();

	Semantics getSemantics();

	void setSemantics(Semantics semantics);
	
	/**
	 * @return cell value.
	 */
	Object getValue();

	void setValue(Object value, AutoRollbackConnection connection, boolean updateToDB);
	
	/**
	 * Because you always get a not-null cell object, use this method to judge the cell is really null or not.
	 * @return TRUE if this cell is really null which means it have not been created.
	 */
	boolean isNull();
//	public NCellValue getCellValue();

	int getRowIndex();

	int getColumnIndex();
	
	/**
	 * @return cell reference like A1
	 */
	String getReferenceString();

	SHyperlink getHyperlink();
	
	/**
	 * Set or clear a hyperlink
	 * @param hyperlink hyperlink to set, or null to clear
	 */
	void setHyperlink(SHyperlink hyperlink);
	
	/** setup a hyperlink*/
	SHyperlink setupHyperlink(HyperlinkType type, String address, String label);

	/**
	 * clear cell value , reset it to blank
	 */
	void clearValue(AutoRollbackConnection connection, boolean updateToDB);

//	boolean isReadonly();
//	

	/**
	 * Set string value, if the value start with '=', then it sets as formula
	 */
	void setStringValue(String value, AutoRollbackConnection connection, boolean updateToDB);

	String getStringValue();

	/**
	 * Setup a rich text value(Create a new one if the old value is not a rich-text) and return the instance which to be edited.
	 */
	SRichText setupRichTextValue();
	
	/**
	 * Return the rich text value. if this cell is a simple string value, it will return a read-only rich-text which wraps string-value and current font.
	 * @return
	 */
	SRichText getRichTextValue();

	/**
	 *  Check if this cell contains a rich-text value
	 */
	boolean isRichTextValue();
	
	/**
	 * set formula with string without '=', e.g. SUM(A1:B2)
	 * @param formula
	 */
	void setFormulaValue(String formula, AutoRollbackConnection connection, boolean updateToDB);
	
	/**
	 * @return returns formula string without '=', e.g. SUM(A1:B2)
	 */
	String getFormulaValue();

	void setNumberValue(Double number, AutoRollbackConnection connection, boolean updateToDB);

	Double getNumberValue();

	/**
	 * Sets the number value a date instance, it will transfer the date to double value
	 */
	void setDateValue(Date date, AutoRollbackConnection connection, boolean updateToDB);
	
	/**
	 * Gets the date value that is transfered by the double number value.
	 */
	Date getDateValue();

	void setBooleanValue(Boolean bool, AutoRollbackConnection connection, boolean updateToDB);
	
	/**
	 * Gets the boolean value
	 */
	Boolean getBooleanValue();

	ErrorValue getErrorValue();

	void setErrorValue(ErrorValue errorValue, AutoRollbackConnection connection, boolean updateToDB);

	SComment setupComment();

	SComment getComment();

	void setComment(SComment comment);

	/**
	 * Parse Input string and update value
	 */
	void setValueParse(String valueParse, AutoRollbackConnection connection, boolean updateToDB);

	/**
	 * Delete the comment associated with this cell.
	 * @since 3.7.0
	 */
	void deleteComment();

	/**
	 * @since 3.5.0
	 */
	enum CellType {
		BLANK(3),
		STRING(1),
		FORMULA(2),
		NUMBER(0),
		BOOLEAN(4),
		ERROR(5),
		COLLECTION(6);

		private int value;

		CellType(int value) {
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


}
