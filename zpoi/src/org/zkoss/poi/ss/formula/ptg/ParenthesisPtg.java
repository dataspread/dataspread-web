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

import org.zkoss.poi.util.LittleEndianOutput;

/**
 * While formula tokens are stored in RPN order and thus do not need parenthesis
 * for precedence reasons, Parenthesis tokens ARE written to ensure that user
 * entered parenthesis are displayed as-is on reading back
 * 
 * Avik Sengupta &lt;lists@aviksengupta.com&gt; Andrew C. Oliver (acoliver at
 * apache dot org)
 * 
 * @author Jason Height (jheight at chariot dot net dot au)
 */
public final class ParenthesisPtg extends ControlPtg {

	private final static int SIZE = 1;
	public final static byte sid = 0x15;

	public static final ControlPtg instance = new ParenthesisPtg();

	//ZSS-747
	//20140827, henrichen: So we can render the multiple area formula back;
	// e.g. (A1, A2, Sheet1!A3:B3) generally used in chart series
	//@since 3.9.5
	private final int _numOperands;

	private ParenthesisPtg() {
		// enforce singleton
		//ZSS-747
		_numOperands = 1;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(sid + getPtgClass());
	}

	public int getSize() {
		return SIZE;
	}

	public String toFormulaString() {
		return "()";
	}

	public String toFormulaString(String[] operands) {
		StringBuffer sb = new StringBuffer().append("(").append(operands[0]);
		for (int j = 1; j < operands.length; ++j) {
			sb.append(",").append(operands[j]);
		}
		sb.append(")");
		return sb.toString();
	}
	
	//ZSS-747
	//@since 3.9.5
	public ParenthesisPtg(int operands) {
		_numOperands = operands;
	}
	//ZSS-747
	//@since 3.9.5
	public int getNumberOfOperands() {
		return _numOperands;
	}
}
