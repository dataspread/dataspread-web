/* XSSFTableColumn.java

	Purpose:
		
	Description:
		
	History:
		Mar 16, 2015 2:18:37 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STTotalsRowFunction;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.STTotalsRowFunctionImpl;

/**
 * @author henri
 * @since 3.9.7
 */
public class XSSFTableColumn {
	CTTableColumn ctTableColumn;
	public XSSFTableColumn(CTTableColumn ctTableColumn) {
		this.ctTableColumn = ctTableColumn;
	}
	public String getName() {
		return ctTableColumn.getName();
	}
	public void setName(String name) {
		ctTableColumn.setName(name);
	}
	
	public String getTotalsRowLabel() {
		return ctTableColumn.getTotalsRowLabel();
	}
	public void setTotalsRowLabel(String label) {
		if (label == null) {
			if (ctTableColumn.isSetTotalsRowLabel())
				ctTableColumn.unsetTotalsRowLabel();
		} else
			ctTableColumn.setTotalsRowLabel(label);
	}
	
	public TotalsRowFunction getTotalsRowFunction() {
		final STTotalsRowFunction.Enum func = ctTableColumn.getTotalsRowFunction();
		return func == null ? 
			TotalsRowFunction.none :
			TotalsRowFunction.values()[func.intValue()-1];
	}
	public void setTotalsRowFunction(TotalsRowFunction func) {
		if (func == TotalsRowFunction.none) {
			if (ctTableColumn.isSetTotalsRowFunction())
				ctTableColumn.unsetTotalsRowFunction();
		} else
			ctTableColumn.setTotalsRowFunction(STTotalsRowFunction.Enum.forInt(func.ordinal()+1));
	}
	
	public String getTotalsRowFormula() {
		final CTTableFormula formula = ctTableColumn.getTotalsRowFormula();
		return formula == null ? null : formula.getStringValue();
	}
	
	public void setTotalsRowFormula(String formula) {
		if (ctTableColumn.isSetTotalsRowFormula())
			ctTableColumn.unsetTotalsRowFormula();
		if (formula != null) {
			ctTableColumn.addNewTotalsRowFormula();
			ctTableColumn.getTotalsRowFormula().setStringValue(formula);
		}
	}
	
	public void setId(int j) {
		ctTableColumn.setId(j);
	}
	
	//ZSS-977
	//Order is important; consist with Excel's model order
	public static enum TotalsRowFunction {
		none,
		sum,
		min,
		max,
		average,
		count,
		countNums,
		stdDev,
		var,
		custom,
	}
}
