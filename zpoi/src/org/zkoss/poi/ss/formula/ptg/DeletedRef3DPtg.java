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


import org.zkoss.poi.ss.usermodel.ErrorConstants;
import org.zkoss.poi.ss.formula.ExternSheetReferenceToken;
import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.ss.formula.WorkbookDependentFormula;
import org.zkoss.poi.util.LittleEndianInput;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * Title:        Deleted Reference 3D Ptg <P>
 * Description:  Defined a cell in extern sheet. <P>
 * REFERENCE:  <P>
 * @author Patrick Luby
 * @version 1.0-pre
 */
public final class DeletedRef3DPtg extends OperandPtg implements WorkbookDependentFormula {
	public final static byte sid  = 0x3c;
	private final int field_1_index_extern_sheet;
	private final int unused1;
	
	private final Ref3DPtg ptg; //ZSS-759 for render formula string
	private final String bookName; //ZSS-759 for render formula string
	
	/** Creates new DeletedRef3DPtg */
	public DeletedRef3DPtg(LittleEndianInput in)  {
		field_1_index_extern_sheet = in.readUShort();
		unused1 = in.readInt();
		//ZSS-759
		ptg = null;
		bookName = null;
	}

	public DeletedRef3DPtg(int externSheetIndex) {
		field_1_index_extern_sheet = externSheetIndex;
		unused1 = 0;
		//ZSS-759
		ptg = null;
		bookName = null;
	}

	public String toFormulaString(FormulaRenderingWorkbook book) {
		//ZSS-759
		if (ptg == null) {
			return ExternSheetNameResolver.prependSheetName(book, field_1_index_extern_sheet, 
				ptg == null ? ErrorConstants.getText(ErrorConstants.ERROR_REF) : ((ExternSheetReferenceToken)ptg).format2DRefAsString());
		} else {
			StringBuffer sb = new StringBuffer();
			if(bookName != null) {
				//20140901, henrichen: don't use SheetNameFormatter, it will add quote because of #
				sb.append("[").append(bookName).append("]#REF");
			} else {
				//20140901, henrichen: don't use SheetNameFormatter, it will add quote because of #
				sb.append("#REF");
			}
			return sb.append('!').append(((ExternSheetReferenceToken)ptg).format2DRefAsString()).toString();
		}
	}
	public String toFormulaString() {
		throw new RuntimeException("3D references need a workbook to determine formula text");
	}
	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}
	public int getSize() {
		return 7;
	}
	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeShort(field_1_index_extern_sheet);
		out.writeInt(unused1);
	}
	//20120117, henrichen@zkoss.org: return extern book in book index
	//ZSS-81 Cannot input formula with proper external book name
	@Override
	public String toInternalFormulaString(FormulaRenderingWorkbook book) {
		return ExternSheetNameResolver.prependInternalSheetName(book, field_1_index_extern_sheet, 
				ErrorConstants.getText(ErrorConstants.ERROR_REF));
	}
	
	//ZSS-759
	public DeletedRef3DPtg(int externSheetIndex, Ref3DPtg ptg, String bookName) {
		field_1_index_extern_sheet = externSheetIndex;
		unused1 = 0;
		this.ptg = ptg;
		this.bookName = bookName;
	}
}
