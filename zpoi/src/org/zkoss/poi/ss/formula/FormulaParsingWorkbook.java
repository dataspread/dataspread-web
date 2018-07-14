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

import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.TablePtg;
import org.zkoss.poi.ss.SpreadsheetVersion;

/**
 * Abstracts a workbook for the purpose of formula parsing.<br/>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 */
public interface FormulaParsingWorkbook {
	/**
	 *  named range name matching is case insensitive
	 */
	EvaluationName getName(String name, int sheetIndex);

	NameXPtg getNameXPtg(String name);

	/**
	 * gets the externSheet index for a sheet from this workbook
	 */
	int getExternalSheetIndex(String sheetName);
	/**
	 * gets the externSheet index for a sheet from an external workbook
	 * @param workbookName e.g. "Budget.xls"
	 * @param sheetName a name of a sheet in that workbook
	 */
	int getExternalSheetIndex(String workbookName, String sheetName);

	/**
	 * Returns an enum holding spreadhseet properties specific to an Excel version (
	 * max column and row numbers, max arguments to a function, etc.)
	 */
	SpreadsheetVersion getSpreadsheetVersion();
	
	/**
	 * Return the associated book name of the specified ExternalLink index.
	 * Excel stores ExternalLink index in place of the [].(e.g. [1]Sheet1:Sheet3!xxx)
	 * @param externalLinkIndex external link index 
	 * @return the associated book name of the specified ExternalLink index.
	 */
	String getBookNameFromExternalLinkIndex(String externalLinkIndex);

	//20101112, henrichen@zkoss.org: handle user defined name parsing
	/**
	 *  named range name matching is case insensitive
	 */
	EvaluationName getOrCreateName(String name, int sheetIndex);

	/**
	 * In HSSF, we need to render a formula string from stored Ptg, so a formula cannot be parsed to DeferredNamePtg. 
	 * Because DeferredNamePtg will be read as UnknownPtg for it's not written out.
	 * @return true for using DeferredNamePtg to represent a non-existed defined name during formula parsing. 
	 * If false, parser will create a defined name for a non-existed one. 
	 */
	boolean isAllowedDeferredNamePtg(); //a workaround for ZSS-575


	//ZSS-790: 
	/**
	 *  named range name matching is case insensitive
	 *  @since 3.9.5
	 */
	EvaluationName getName(String name, String sheetName);
	
	//ZSS-960
	/**
	 * @since 3.9.7
	 * @param tableName
	 * @param specifiers
	 * @param sheetIndex
	 * @param rowIdx
	 * @param colIdx
	 * @return
	 * @since 3.9.7
	 */
	TablePtg createTablePtg(String tableName, Object[] specifiers, int sheetIndex, int rowIdx, int colIdx);
	
	//ZSS-966
	/**
	 * Given a name and return the real table name(consider uppercase/lowecase);
	 * It can be used to check whether the specified name is a table name if
	 * the returned name is null.
	 * @param name
	 * @return
	 * @since 3.9.7
	 */
	String getTableName(String name);
}