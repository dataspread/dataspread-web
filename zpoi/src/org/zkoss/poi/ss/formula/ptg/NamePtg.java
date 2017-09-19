/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.ss.formula.ptg;

import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.ss.formula.WorkbookDependentFormula;
import org.zkoss.poi.util.LittleEndianInput;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * 
 * @author andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public class NamePtg extends OperandPtg implements WorkbookDependentFormula {
	public final static short sid = 0x23;
	private final static int SIZE = 5;
	/** one-based index to defined name record */
	private int field_1_label_index;
	private short field_2_zero; // reserved must be 0

	public NamePtg(){
		/* For seralization */
	}

	/**
	 * @param nameIndex zero-based index to name within workbook
	 */
	public NamePtg(int nameIndex) {
		field_1_label_index = 1 + nameIndex; // convert to 1-based
	}

	/** Creates new NamePtg */

	public NamePtg(LittleEndianInput in)  {
		field_1_label_index = in.readShort();
		field_2_zero = in.readShort();
	}

	/**
	 * @return zero based index to a defined name record in the LinkTable
	 */
	public int getIndex() {
		return field_1_label_index - 1; // convert to zero based
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeShort(field_1_label_index);
		out.writeShort(field_2_zero);
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString(FormulaRenderingWorkbook book) {
		//20101114, henrichen@zkoss.org: filter out _xlfn. if any
		//return book.getNameText(this);
		final String name = book.getFullNameText(this); //ZSS-790
		return (name != null && name.startsWith("_xlfn.")) ? name.substring(6) : name;
	}

	public String toFormulaString() {
		throw new RuntimeException("3D references need a workbook to determine formula text");
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	//20120117, henrichen@zkoss.org: return extern index
	//ZSS-81 Cannot input formula with proper external book name
	@Override
	public String toInternalFormulaString(FormulaRenderingWorkbook book) {
		return toFormulaString(book);
	}
}
