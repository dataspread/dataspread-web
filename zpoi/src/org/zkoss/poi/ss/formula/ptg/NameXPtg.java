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
 * @author aviks
 */
public final class NameXPtg extends OperandPtg implements WorkbookDependentFormula {
	public final static short sid = 0x39;
	private final static int SIZE = 7;

	/** index to REF entry in externsheet record */
	private int _sheetRefIndex;
	/** index to defined name or externname table(1 based) */
	private int _nameNumber;
	/** reserved must be 0 */
	private int _reserved;


	public NameXPtg(){
		/* For seralization */
	}

	private NameXPtg(int sheetRefIndex, int nameNumber, int reserved) {
		_sheetRefIndex = sheetRefIndex;
		_nameNumber = nameNumber;
		_reserved = reserved;
	}

	/**
	 * @param sheetRefIndex index to REF entry in externsheet record
	 * @param nameIndex index to defined name or externname table
	 */
	public NameXPtg(int sheetRefIndex, int nameIndex) {
		this(sheetRefIndex, nameIndex + 1, 0);
	}

	public NameXPtg(LittleEndianInput in)  {
		this(in.readUShort(), in.readUShort(), in.readUShort());
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeShort(_sheetRefIndex);
		out.writeShort(_nameNumber);
		out.writeShort(_reserved);
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString(FormulaRenderingWorkbook book) {
		// -1 to convert definedNameIndex from 1-based to zero-based
		return book.resolveNameXText(this);
	}
	public String toFormulaString() {
		throw new RuntimeException("3D references need a workbook to determine formula text");
	}
	
	public String toString(){
	   String retValue = "NameXPtg:[sheetRefIndex:" + _sheetRefIndex + 
	      " , nameNumber:" + _nameNumber + "]" ;
	   return retValue;
	}
	
	public byte getDefaultOperandClass() {
		return Ptg.CLASS_VALUE;
	}

	public int getSheetRefIndex() {
		return _sheetRefIndex;
	}
	public int getNameIndex() {
		return _nameNumber - 1;
	}

	//20120117, henrichen@zkoss.org
	//ZSS-81 Cannot input formula with proper external book name
	@Override
	public String toInternalFormulaString(FormulaRenderingWorkbook book) {
		// 20140107, paowang@potix.com, ZSS-533: there is book for resolving function name (only external book reference needs internal form) 
//		throw new RuntimeException("3D references need a workbook to determine formula text");
		return toFormulaString(book);
	}
}
