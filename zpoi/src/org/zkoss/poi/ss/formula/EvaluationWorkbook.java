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

package org.zkoss.poi.ss.formula;

import java.io.Serializable;

import org.zkoss.poi.ss.formula.ptg.NamePtg;
import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.udf.UDFFinder;

/**
 * Abstracts a workbook for the purpose of formula evaluation.<br/>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference
 */
public interface EvaluationWorkbook {
	String getSheetName(int sheetIndex);
	/**
	 * @return -1 if the specified sheet is from a different book
	 */
	int getSheetIndex(EvaluationSheet sheet);
	/**
	 * Finds a sheet index by case insensitive name.
	 * @return the index of the sheet matching the specified name.  -1 if not found
	 */
	int getSheetIndex(String sheetName);

	EvaluationSheet getSheet(int sheetIndex);

	/**
	 * @return <code>null</code> if externSheetIndex refers to a sheet inside the current workbook
	 */
	ExternalSheet getExternalSheet(int externSheetIndex);
	int convertFromExternSheetIndex(int externSheetIndex);
	int convertLastIndexFromExternSheetIndex(int externSheetIndex);
	ExternalName getExternalName(int externSheetIndex, int externNameIndex);
	EvaluationName getName(NamePtg namePtg);
	//ZSS-790. It can be book scope or sheet scope for a formula refer to a 
	//   Name without prefixing sheet name. Need the context sheet to know 
	//   which sheet scope to look at first in such case.
	/**
	 * @param namePtg
	 * @param contextSheetIndex
	 * @return
	 * @since 3.9.5
	 */
	EvaluationName getName(NamePtg namePtg, int contextSheetIndex);  
    EvaluationName getName(String name, int sheetIndex);
	String resolveNameXText(NameXPtg ptg);
	Ptg[] getFormulaTokens(EvaluationCell cell);
    UDFFinder getUDFFinder();

	class ExternalSheet implements Serializable {
		private static final long serialVersionUID = 3645616749560685687L;
		private final String _workbookName;
		private final String _sheetName;
		private final String _lastSheetName;

		public ExternalSheet(String workbookName, String sheetName, String lastSheetName) {
			_workbookName = workbookName;
			_sheetName = sheetName;
			_lastSheetName = lastSheetName;
		}
		public String getWorkbookName() {
			return _workbookName;
		}
		public String getLastSheetName() {
			return _lastSheetName;
		}
		public String getSheetName() {
			return _sheetName;
		}
	}
	class ExternalName implements Serializable {
		private static final long serialVersionUID = 7001050913754905274L;
		private final String _nameName;
		private final int _nameNumber;
		private final int _ix;

		public ExternalName(String nameName, int nameNumber, int ix) {
			_nameName = nameName;
			_nameNumber = nameNumber;
			_ix = ix;
		}
		public String getName() {
			return _nameName;
		}
		public int getNumber() {
			return _nameNumber;
		}
		public int getIx() {
			return _ix;
		}
	}

	//20111124, henrichen@zkoss.org
	Ptg[] getFormulaTokens(int sheetIndex, String formula);
}
