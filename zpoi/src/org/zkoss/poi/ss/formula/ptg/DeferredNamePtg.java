/* PseudoNamePtg.java

	Purpose:
		
	Description:
		
	History:
		Dec 27, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.poi.ss.formula.ptg;

import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.ss.formula.WorkbookDependentFormula;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * A defined name is only used for parsing and evaluation which will not be written out.
 * It doesn't exist in any book.
 * @author 20131227, paowang@potix.com
 */
public class DeferredNamePtg extends OperandPtg implements WorkbookDependentFormula {

	private String namename;

	public DeferredNamePtg(String name) {
		this.namename = name;
	}

	@Override
	public int getSize() {
		return namename.length();
	}

	@Override
	public void write(LittleEndianOutput out) {
		// do nothing
	}

	@Override
	public String toFormulaString() {
		return namename;
	}

	@Override
	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	@Override
	public String toFormulaString(FormulaRenderingWorkbook book) {
		return namename;
	}

	@Override
	public String toInternalFormulaString(FormulaRenderingWorkbook book) {
		return namename;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((namename == null) ? 0 : namename.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		DeferredNamePtg other = (DeferredNamePtg)obj;
		if(namename == null) {
			if(other.namename != null)
				return false;
		} else if(!namename.equals(other.namename))
			return false;
		return true;
	}

}
