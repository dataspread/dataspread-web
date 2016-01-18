/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 23, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.api.model;

import org.zkoss.zss.model.SDataValidation;

/**
 * Represents data validation for a worksheet range.
 * 
 * @author henri
 *
 */
public interface Validation {
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

		public SDataValidation.AlertStyle getNative() {
			switch(this) {
			case INFO:
				return SDataValidation.AlertStyle.INFO;
			case STOP:
				return SDataValidation.AlertStyle.STOP;
			case WARNING:
				return SDataValidation.AlertStyle.WARNING;
			}
			return null;
		}
}
	
	/**
	 * @since 3.5.0
	 */
	public enum ValidationType {
		ANY, INTEGER, DECIMAL, LIST, 
		DATE, TIME, TEXT_LENGTH, CUSTOM;

		public SDataValidation.ValidationType getNative() {
			switch(this) {
			case ANY:
				return SDataValidation.ValidationType.ANY;
			case CUSTOM:
				return SDataValidation.ValidationType.CUSTOM;
			case DATE:
				return SDataValidation.ValidationType.DATE;
			case DECIMAL:
				return SDataValidation.ValidationType.DECIMAL;
			case INTEGER:
				return SDataValidation.ValidationType.INTEGER;
			case LIST:
				return SDataValidation.ValidationType.LIST;
			case TEXT_LENGTH:
				return SDataValidation.ValidationType.TEXT_LENGTH;
			case TIME:
				return SDataValidation.ValidationType.TIME;
			}
			return null;
		}
	}
	
	/**
	 * @since 3.5.0
	 */
	public enum OperatorType{
		BETWEEN, NOT_BETWEEN, EQUAL, NOT_EQUAL, 
		GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL;
		
		public SDataValidation.OperatorType getNative() {
			switch(this) {
			case BETWEEN:
				return SDataValidation.OperatorType.BETWEEN;
			case EQUAL:
				return SDataValidation.OperatorType.EQUAL;
			case GREATER_OR_EQUAL:
				return SDataValidation.OperatorType.GREATER_OR_EQUAL;
			case GREATER_THAN:
				return SDataValidation.OperatorType.GREATER_THAN;
			case LESS_OR_EQUAL:
				return SDataValidation.OperatorType.LESS_OR_EQUAL;
			case LESS_THAN:
				return SDataValidation.OperatorType.LESS_THAN;
			case NOT_BETWEEN:
				return SDataValidation.OperatorType.NOT_BETWEEN;
			case NOT_EQUAL:
				return SDataValidation.OperatorType.NOT_EQUAL;
			}
			return null;
		}
	}
	
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
	
	public ValidationType getValidationType();
	public void setValidationType(ValidationType type);
	
	public OperatorType getOperatorType();
	public void setOperatorType(OperatorType type);

	public String getFormula1();
	public String getFormula2();
	public void setFormula1(String formula);
	public void setFormula2(String formula2);
}
