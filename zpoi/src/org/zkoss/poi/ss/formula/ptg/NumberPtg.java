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

import java.util.Locale;

import org.zkoss.poi.ss.util.NumberToTextConverter;
import org.zkoss.poi.ss.util.ToExcelNumberConverter;
import org.zkoss.poi.util.LittleEndianInput;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * Number Stores a floating point value in a formula value stored in a 8 byte
 * field using IEEE notation
 * 
 * @author Avik Sengupta
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class NumberPtg extends ScalarConstantPtg {
	public final static int SIZE = 9;
	public final static byte sid = 0x1f;
	private double field_1_value;

	public NumberPtg(LittleEndianInput in)  {
		this(in.readDouble());
	}

	NumberPtg()
	{

	}

	/**
	 * Create a NumberPtg from a string representation of the number Number
	 * format is not checked, it is expected to be validated in the parser that
	 * calls this method.
	 * 
	 * @param value String representation of a floating point number
	 */
	public NumberPtg(String value) {
		this(ToExcelNumberConverter.toExcelNumber(Double.parseDouble(value), false));
	}

	public NumberPtg(double value) {
		field_1_value = value;
	}

	public double getValue() {
		return field_1_value;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
		out.writeDouble(getValue());
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString() {
		return NumberToTextConverter.toText(field_1_value);
	}
	
	//ZSS-670: Shall always use US locale for internal formula string
	public String toInternalFormulaString() {
		return NumberToTextConverter.toText(field_1_value, Locale.US);
	}
}
