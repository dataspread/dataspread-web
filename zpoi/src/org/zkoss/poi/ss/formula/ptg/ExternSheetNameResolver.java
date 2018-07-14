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
import org.zkoss.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.zkoss.poi.ss.formula.SheetNameFormatter;

/**
 * @author Josh Micich
 */
final class ExternSheetNameResolver {

	private ExternSheetNameResolver() {
		// no instances of this class
	}

	public static String prependSheetName(FormulaRenderingWorkbook book, int field_1_index_extern_sheet, String cellRefText) {
		ExternalSheet externalSheet = book.getExternalSheet(field_1_index_extern_sheet);
		StringBuffer sb;
		if (externalSheet != null) {
			String wbName = externalSheet.getWorkbookName();
			String sheetName =convertSheetName(externalSheet.getSheetName());
			// 20140103, paowang@potix.com, support 3D reference
			String lastSheetName = convertSheetName(externalSheet.getLastSheetName());
			sheetName = sheetName.equals(lastSheetName) ? sheetName : sheetName + ":" + lastSheetName;
			//
			sb = new StringBuffer(wbName.length() + sheetName.length() + cellRefText.length() + 4);
			SheetNameFormatter.appendFormat(sb, wbName, sheetName);
		} else {
			String sheetName = convertSheetName(book.getSheetNameByExternSheet(field_1_index_extern_sheet));
			sb = new StringBuffer(sheetName.length() + cellRefText.length() + 4);
			SheetNameFormatter.appendFormat(sb,sheetName); //according to POI parser limitation, sheet name must be surrounded with single quote
		}
   		sb.append('!');
		sb.append(cellRefText);
		return sb.toString();
	}

	//20131104, hawkchen@potix.com, ZSS-502: sheet name might be empty, convert empty sheet name to '#REF'
	//20150807, henrichen@potix.com, ZSS-1096: sheet nname might be null, convert null sheet to '#REF'
	private static String convertSheetName(String sheetName) {
		if ("".equals(sheetName) || sheetName == null) {
			sheetName = "#REF";
		}
		return sheetName;
	}

	//20120117, henrichen@zkoss.org: prepare sheet name in  internal form
	//ZSS-81 Cannot input formula with proper external book name
	public static String prependInternalSheetName(FormulaRenderingWorkbook book, int field_1_index_extern_sheet, String cellRefText) {
		ExternalSheet externalSheet = book.getExternalSheet(field_1_index_extern_sheet);
		StringBuffer sb;
		if (externalSheet != null) {
			String wbName = externalSheet.getWorkbookName();
			String wbIndex = book.getExternalLinkIndexFromBookName(wbName);
			if (wbIndex == null) {
				wbIndex = wbName;
			}
			String sheetName = convertSheetName(externalSheet.getSheetName());
			// 20140103, paowang@potix.com, support 3D reference
			String lastSheetName = convertSheetName(externalSheet.getLastSheetName());
			sheetName = sheetName.equals(lastSheetName) ? sheetName : sheetName + ":" + lastSheetName;
			//
			sb = new StringBuffer(wbIndex.length() + sheetName.length() + cellRefText.length() + 4);
			SheetNameFormatter.appendFormat(sb, wbIndex, sheetName);
		} else {
			String sheetName = convertSheetName(book.getSheetNameByExternSheet(field_1_index_extern_sheet));
			sb = new StringBuffer(sheetName.length() + cellRefText.length() + 4);
			SheetNameFormatter.appendFormat(sb, sheetName);
		}
   		sb.append('!');
		sb.append(cellRefText);
		return sb.toString();
	}
}
