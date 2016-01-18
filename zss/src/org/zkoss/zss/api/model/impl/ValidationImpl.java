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

package org.zkoss.zss.api.model.impl;

import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Validation;
import org.zkoss.zss.model.SChart;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SSheet;

/**
 * @author henri
 *
 */
public class ValidationImpl implements Validation {
	private ModelRef<SDataValidation> _validationRef;
	
	public ValidationImpl(ModelRef<SDataValidation> validationRef) {
		this._validationRef = validationRef;
	}

	public SDataValidation getNative() {
		return _validationRef.get();
	}
		
	public Object getId(){
		return getNative().getId();
	}
		
	@Override
	public AlertStyle getAlertStyle() {
		switch(getNative().getAlertStyle()) {
		case INFO: 
			return AlertStyle.INFO;
		case STOP:
			return AlertStyle.STOP;
		case WARNING:
			return AlertStyle.WARNING;
		}
		return null;
	}

	@Override
	public void setAlertStyle(AlertStyle alertStyle) {
		SDataValidation.AlertStyle as = alertStyle.getNative();
		getNative().setAlertStyle(as);
	}

	@Override
	public void setIgnoreBlank(boolean ignore) {
		getNative().setIgnoreBlank(ignore);
	}

	@Override
	public boolean isIgnoreBlank() {
		return getNative().isIgnoreBlank();
	}

	@Override
	public void setInCellDropdown(boolean show) {
		getNative().setInCellDropdown(show);
	}

	@Override
	public boolean isInCellDropdown() {
		return getNative().isInCellDropdown();
	}

	@Override
	public void setShowInput(boolean show) {
		getNative().setShowInput(show);
	}

	@Override
	public boolean isShowInput() {
		return getNative().isShowInput();
	}

	@Override
	public void setShowError(boolean show) {
		getNative().setShowError(show);
	}

	@Override
	public boolean isShowError() {
		return getNative().isShowError();
	}

	@Override
	public void setInputTitle(String title) {
		getNative().setInputTitle(title);
	}

	@Override
	public void setInputMessage(String message) {
		getNative().setInputMessage(message);
	}

	@Override
	public String getInputTitle() {
		return getNative().getInputTitle();
	}

	@Override
	public String getInputMessage() {
		return getNative().getInputMessage();
	}

	@Override
	public void setErrorTitle(String title) {
		getNative().setErrorTitle(title);
	}

	@Override
	public void setErrorMessage(String message) {
		getNative().setErrorMessage(message);
	}

	@Override
	public String getErrorTitle() {
		return getNative().getErrorTitle();
	}

	@Override
	public String getErrorMessage() {
		return getNative().getErrorMessage();
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zss.api.model.Validation#getValidationType()
	 */
	@Override
	public ValidationType getValidationType() {
		switch(getNative().getValidationType()) {
		case ANY:
			return ValidationType.ANY;
		case CUSTOM:
			return ValidationType.CUSTOM;
		case DATE:
			return ValidationType.DATE;
		case DECIMAL:
			return ValidationType.DECIMAL;
		case INTEGER:
			return ValidationType.INTEGER;
		case LIST:
			return ValidationType.LIST;
		case TEXT_LENGTH:
			return ValidationType.TEXT_LENGTH;
		case TIME:
			return ValidationType.TIME;
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValidationType(ValidationType type) {
		SDataValidation.ValidationType vt = type.getNative();
		getNative().setValidationType(vt);
	}

	@Override
	public OperatorType getOperatorType() {
		switch(getNative().getOperatorType()) {
		case BETWEEN:
			return OperatorType.BETWEEN;
		case EQUAL:
			return OperatorType.EQUAL;
		case GREATER_OR_EQUAL:
			return OperatorType.GREATER_OR_EQUAL;
		case GREATER_THAN:
			return OperatorType.GREATER_THAN;
		case LESS_OR_EQUAL:
			return OperatorType.LESS_OR_EQUAL;
		case LESS_THAN:
			return OperatorType.LESS_THAN;
		case NOT_BETWEEN:
			return OperatorType.NOT_BETWEEN;
		case NOT_EQUAL:
			return OperatorType.NOT_EQUAL;
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOperatorType(OperatorType type) {
		SDataValidation.OperatorType ot = type.getNative();
		getNative().setOperatorType(ot);
	}

	@Override
	public String getFormula1() {
		return getNative().getFormula1();
	}

	@Override
	public String getFormula2() {
		return getNative().getFormula2();
	}

	@Override
	public void setFormula1(String formula) {
		getNative().setFormula1(formula);
	}

	@Override
	public void setFormula2(String formula2) {
		getNative().setFormula2(formula2);
	}
}
