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

import java.util.List;
import java.util.Set;

/**
 * This class stores the restrictions on what data can or should be entered in a cell.
 * @author dennis
 * @since 3.5.0
 */
public interface SDataValidation extends FormulaContent{
	/**
	 * @since 3.5.0
	 */
	public enum AlertStyle {
		STOP((byte)0x00), WARNING((byte)0x01), INFO((byte)0x02);
		
		private byte value;
		AlertStyle(byte value){
			this.value = value;
		}
		
		public byte getValue(){
			return value;
		}
	}
	
	/**
	 * @since 3.5.0
	 */
	public enum ValidationType {
		ANY, INTEGER, DECIMAL, LIST, 
		DATE, TIME, TEXT_LENGTH, CUSTOM;

	}
	
	/**
	 * @since 3.5.0
	 */
	public enum OperatorType{
		BETWEEN, NOT_BETWEEN, EQUAL, NOT_EQUAL, 
		GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL;
	}
	
	public SSheet getSheet();
	
	public AlertStyle getAlertStyle();
	public void setAlertStyle(AlertStyle alertStyle);
	
	public void setIgnoreBlank(boolean ignore);
	public boolean isIgnoreBlank();
	
	public void setInCellDropdown(boolean show);
	public boolean isInCellDropdown();
	
	public void setShowInput(boolean show);
	public boolean isShowInput();

	public void setShowError(boolean show);
	public boolean isShowError();

	public void setInputTitle(String title);
	public void setInputMessage(String message);
	public String getInputTitle();
	public String getInputMessage();

	public void setErrorTitle(String title);
	public void setErrorMessage(String message);
	public String getErrorTitle();
	public String getErrorMessage();

	public Set<CellRegion> getRegions();
	public void setRegions(Set<CellRegion> regions);
	public void addRegion(CellRegion region);
	public void removeRegion(CellRegion region);
	
	public ValidationType getValidationType();
	public void setValidationType(ValidationType type);
	
	public OperatorType getOperatorType();
	public void setOperatorType(OperatorType type);
	
	/**
	 * Return formula parsing state.
	 * @return true if has error, false if no error or no formula
	 */
	public boolean isFormulaParsingError();
	
	public boolean hasReferToCellList();
	public List<SCell> getReferToCellList();
	
	public int getNumOfValue();
	public Object getValue(int i);
	public int getNumOfValue1();
	public Object getValue1(int i);
	public int getNumOfValue2();
	public Object getValue2(int i);
	
	public String getFormula1();
	public String getFormula2();
	public void setFormula1(String formula);
	public void setFormula2(String formula);

	public Object getId();

	/**
	 * Returns whether the referred list formula contains a relative column. 
	 * @return
	 * @since 3.7.0
	 */
	public boolean hasReferToRelativeCol();
	/**
	 * Returns whether the referred list formula contains a relative row.
	 * @return
	 * @since 3.7.0
	 */
	public boolean hasReferToRelativeRow();
	/**
	 * Return the referred list relative to the specified row and column.
	 * @param row
	 * @param col
	 * @return
	 * @since 3.7.0
	 */
	public List<SCell> getReferToCellList(int row, int col);
}
