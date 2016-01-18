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

package org.zkoss.poi.xssf.usermodel;

import org.zkoss.poi.ss.formula.EvaluationCell;
import org.zkoss.poi.ss.formula.EvaluationSheet;

/**
 * XSSF wrapper for a sheet under evaluation
 * 
 * @author Josh Micich
 */
final class XSSFEvaluationSheet implements EvaluationSheet {

	private final XSSFSheet _xs;

	public XSSFEvaluationSheet(XSSFSheet sheet) {
		_xs = sheet;
	}

	public XSSFSheet getXSSFSheet() {
		return _xs;
	}
	public EvaluationCell getCell(int rowIndex, int columnIndex) {
		XSSFRow row = _xs.getRow(rowIndex);
		if (row == null) {
			return null;
		}
		XSSFCell cell = row.getCell(columnIndex);
		if (cell == null) {
			return null;
		}
		return new XSSFEvaluationCell(cell, this);
	}

	//20140311, dennischen@zkoss.org, ZSS-596 Possible memory leak when formula evaluation
	//implement hashCode and equals, use identity to implement euqlas
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_xs == null) ? 0 : _xs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XSSFEvaluationSheet other = (XSSFEvaluationSheet) obj;
		return _xs == other._xs;
	}

	//ZSS-962: just make compile ok.
	@Override
	public boolean isHidden(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}
}
