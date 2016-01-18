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

package org.zkoss.zss.model.impl.chart;

import org.zkoss.zss.model.sys.formula.FormulaExpression;

/**
 * Handle formula caching for chart category data.
 * 
 * @author henri
 * @since 3.6.0
 */
public abstract class AbstractGeneralChartDataAdv extends ChartDataAdv {
	public abstract FormulaExpression getCategoriesFormulaExpression();
	
	public abstract void setCategoriesFormula(FormulaExpression fexpr);
}
