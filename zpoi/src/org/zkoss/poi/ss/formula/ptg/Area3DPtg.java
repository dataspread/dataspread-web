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

import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.formula.ExternSheetReferenceToken;
import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.ss.formula.WorkbookDependentFormula;
import org.zkoss.poi.util.LittleEndianInput;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * Title:        Area 3D Ptg - 3D reference (Sheet + Area)<P>
 * Description:  Defined a area in Extern Sheet. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author avik
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public class Area3DPtg extends AreaPtgBase implements WorkbookDependentFormula, ExternSheetReferenceToken { //ZSS-1013
	public final static byte sid = 0x3b;
	private final static int SIZE = 11; // 10 + 1 for Ptg

	private int field_1_index_extern_sheet;

    public Area3DPtg(){
        /* For seralization */
    }

	public Area3DPtg(String arearef, int externIdx) {
		super(new AreaReference(arearef));
		setExternSheetIndex(externIdx);
	}

	public Area3DPtg(LittleEndianInput in)  {
		field_1_index_extern_sheet = in.readShort();
		readCoordinates(in);
	}

	public Area3DPtg(int firstRow, int lastRow, int firstColumn, int lastColumn,
			boolean firstRowRelative, boolean lastRowRelative, boolean firstColRelative, boolean lastColRelative,
			int externalSheetIndex) {
		super(firstRow, lastRow, firstColumn, lastColumn, firstRowRelative, lastRowRelative, firstColRelative, lastColRelative);
		setExternSheetIndex(externalSheetIndex);
	}

	public Area3DPtg(AreaReference arearef, int externIdx) {
		super(arearef);
		setExternSheetIndex(externIdx);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append(" [");
		sb.append("sheetIx=").append(getExternSheetIndex());
		sb.append(" ! ");
		sb.append(formatReferenceAsString());
		sb.append("]");
		return sb.toString();
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeShort(field_1_index_extern_sheet);
		writeCoordinates(out);
	}

	public int getSize() {
		return SIZE;
	}

	public int getExternSheetIndex() {
		return field_1_index_extern_sheet;
	}

	public void setExternSheetIndex(int index) {
		field_1_index_extern_sheet = index;
	}
	public String format2DRefAsString() {
		return formatReferenceAsString();
	}
	/**
	 * @return text representation of this area reference that can be used in text
	 *  formulas. The sheet name will get properly delimited if required.
	 */
	public String toFormulaString(FormulaRenderingWorkbook book) {
		return ExternSheetNameResolver.prependSheetName(book, field_1_index_extern_sheet, formatReferenceAsString());
	}
	public String toFormulaString() {
		throw new RuntimeException("3D references need a workbook to determine formula text");
	}

	//20110324, henrichen@zkoss.org: override hashCode
	public int hashCode() {
		return super.hashCode() ^ field_1_index_extern_sheet;
	}

	//20110324, henrichen@zkoss.org: override equals
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Area3DPtg)) {
			return false;
		}
		final Area3DPtg other = (Area3DPtg) o;
		return super.equals(o) && other.field_1_index_extern_sheet == this.field_1_index_extern_sheet;
	}
	
	//20120117, henrichen@zkoss.org: return extern book in book index
	//ZSS-81 Cannot input formula with proper external book name
	/**
	 * @return text representation of this area reference that can be used in text
	 *  formulas. The sheet name will get properly delimited if required.
	 */
	public String toInternalFormulaString(FormulaRenderingWorkbook book) {
		return ExternSheetNameResolver.prependInternalSheetName(book, field_1_index_extern_sheet, formatReferenceAsString());
	}
}
