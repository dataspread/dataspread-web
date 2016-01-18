/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 22, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.chart.SSeries;

/**
 * Handle formula caching for chart series data.
 * @author henri
 * @since 3.6.0
 */
public abstract class AbstractSeriesAdv implements SSeries {

	/**
	 * @since 3.6.0
	 */
	public abstract FormulaExpression getNameFormulaExpression();
	
	/**
	 * 
	 * @return
	 * @since 3.6.0	 
	 */
	public abstract FormulaExpression getValuesFormulaExpression();
	
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	public abstract FormulaExpression getXValuesFormulaExpression();
	
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	public abstract FormulaExpression getYValuesFormulaExpression();
	
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	public abstract FormulaExpression getZValuesFormulaExpression();

	/**
	 *
	 * @since 3.6.0
	 */
	public abstract void setXYZFormula(FormulaExpression nameExpr, FormulaExpression xValuesExpr, FormulaExpression yValuesExpr, FormulaExpression zValuesExpr);
}
