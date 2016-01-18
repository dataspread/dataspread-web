/* TableColumnImpl.java

	Purpose:
		
	Description:
		
	History:
		Dec 9, 2014 7:05:44 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.STableColumn;

/**
 * Table column.
 * @author henri
 * @since 3.8.0
 */
public class TableColumnImpl implements STableColumn, Serializable {
	private static final long serialVersionUID = 4333495215409538027L;
	
	String _name;
	String _totalsRowLabel;
	String _totalsRowFormula;
	STotalsRowFunction _totalsRowFunction;
	
	public TableColumnImpl(String name) {
		_name = name;
	}
	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getTotalsRowLabel() {
		return _totalsRowLabel;
	}

	@Override
	public STotalsRowFunction getTotalsRowFunction() {
		return _totalsRowFunction;
	}

	@Override
	public void setName(String name) {
		_name = name;
	}

	@Override
	public void setTotalsRowLabel(String label) {
		_totalsRowLabel = label;
	}

	@Override
	public void setTotalsRowFunction(STotalsRowFunction func) {
		_totalsRowFunction = func;
	}
	@Override
	public String getTotalsRowFormula() {
		return _totalsRowFormula;
	}
	@Override
	public void setTotalsRowFormula(String formula) {
		_totalsRowFormula = formula;
	}
}
