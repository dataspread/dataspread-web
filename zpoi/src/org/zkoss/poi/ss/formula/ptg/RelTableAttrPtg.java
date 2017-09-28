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
import org.zkoss.poi.util.StringUtil;

/**
 * 
 * @author andy
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public class RelTableAttrPtg extends OperandPtg {
	public final static short sid = 0x75;
	private boolean _is16bitUnicode;
	/**
	 * NOTE: OO doc says 16bit length, but BiffViewer says 8 Book says something
	 * totally different, so don't look there!
	 */
	private String field_3_string;

	public RelTableAttrPtg(){
		/* For seralization */
	}

	/** Create a StringPtg from a stream */
	public RelTableAttrPtg(LittleEndianInput in)  {
		int nChars = in.readUByte(); // Note - nChars is 8-bit
		_is16bitUnicode = (in.readByte() & 0x01) != 0;
		if (_is16bitUnicode) {
			field_3_string = StringUtil.readUnicodeLE(in, nChars);
		} else {
			field_3_string = StringUtil.readCompressedUnicode(in, nChars);
		}
	}

	/**
	 * Create a StringPtg from a string representation of the number Number
	 * format is not checked, it is expected to be validated in the parser that
	 * calls this method.
	 *
	 * @param value :
	 *            String representation of a floating point number
	 */
	public RelTableAttrPtg(String value) {
		if (value.length() > 255) {
			throw new IllegalArgumentException(
					"String literals in formulas can't be bigger than 255 characters ASCII");
		}
		_is16bitUnicode = StringUtil.hasMultibyte(value);
		field_3_string = value;
	}

	public String getName() {
		return field_3_string;
	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeByte(field_3_string.length()); // Note - nChars is 8-bit
		out.writeByte(_is16bitUnicode ? 0x01 : 0x00);
		if (_is16bitUnicode) {
			StringUtil.putUnicodeLE(field_3_string, out);
		} else {
			StringUtil.putCompressedUnicode(field_3_string, out);
		}
	}
	public int getSize() {
		return 3 +  field_3_string.length() * (_is16bitUnicode ? 2 : 1);
	}

	public String toFormulaString() {
		return "ATTR_" + field_3_string;
	}
}
