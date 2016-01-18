/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public abstract class AbstractDataValidationAdv implements SDataValidation,LinkedModelObject,Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * copy data from src , it doesn't copy some essential field, such as region region
	 * @param src
	 */
	/*package*/ abstract void copyFrom(AbstractDataValidationAdv src);
	
	/**
	 * When sheet name changed.
	 */
	abstract void renameSheet(String oldName, String newName); // ZSS-648
	
	/**
	 * Setup the two formulas.
	 * @param formula1
	 * @param formula2
	 */
	abstract public void setFormulas(String formula1, String formula2);
	
	//ZSS-747
	/**
	 * 
	 * @param fe1
	 * @param fe2
	 * @since 3.6.0
	 */
	abstract public void setFormulas(FormulaExpression fe1, FormulaExpression fe2);

	//ZSS-747
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	abstract public FormulaExpression getFormulaExpression1();
	//ZSS-747
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	abstract public FormulaExpression getFormulaExpression2();
	//ZSS-747
	/**
	 * 
	 * @param formula
	 * @since 3.6.0
	 */
	abstract public void setFormula1(FormulaExpression formula);
	//ZSS-747
	/**
	 * 
	 * @param formula
	 * @since 3.6.0
	 */
	abstract public void setFormula2(FormulaExpression formula);

	//ZSS-810
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	abstract public ValueEval getValueEval1();
	
	//ZSS-810
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	abstract public ValueEval getValueEval2();
	
	//ZSS-833
	/**
	 * 
	 * @param row
	 * @param col
	 * @since 3.7.0
	 */
	abstract public void addDependency(int row, int col);
	
	//ZSS-866
	/**
	 * 
	 * @return
	 * @since 3.7.0
	 */
	abstract public String getEscapedFormula1();
	
	//ZSS-866
	/**
	 * 
	 * @return
	 * @since 3.7.0
	 */
	abstract public String getEscapedFormula2();

	/**
	 * Setup the two formulas which are in escaped POI format.
	 * @param formula1
	 * @param formula2
	 */
	abstract public void setEscapedFormulas(String formula1, String formula2);
}
