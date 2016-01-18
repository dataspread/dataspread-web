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

package org.zkoss.poi.hssf.usermodel;

import org.zkoss.poi.hssf.model.HSSFFormulaParser;
import org.zkoss.poi.hssf.model.InternalWorkbook;
import org.zkoss.poi.hssf.record.NameRecord;
import org.zkoss.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.zkoss.poi.ss.formula.ptg.NamePtg;
import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.TablePtg;
import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.formula.EvaluationCell;
import org.zkoss.poi.ss.formula.EvaluationName;
import org.zkoss.poi.ss.formula.EvaluationSheet;
import org.zkoss.poi.ss.formula.EvaluationWorkbook;
import org.zkoss.poi.ss.formula.FormulaParseException;
import org.zkoss.poi.ss.formula.FormulaParsingWorkbook;
import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.ss.formula.FormulaType;
import org.zkoss.poi.ss.formula.udf.UDFFinder;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;

/**
 * Internal POI use only
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference
 */
public final class HSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {
	private static POILogger logger = POILogFactory.getLogger(HSSFEvaluationWorkbook.class);
	private final HSSFWorkbook _uBook;
	private final InternalWorkbook _iBook;

	public static HSSFEvaluationWorkbook create(HSSFWorkbook book) {
		if (book == null) {
			return null;
		}
		return new HSSFEvaluationWorkbook(book);
	}

	private HSSFEvaluationWorkbook(HSSFWorkbook book) {
		_uBook = book;
		_iBook = book.getWorkbook();
	}

	public int getExternalSheetIndex(String sheetName) {
		final int j = sheetName.indexOf(':');
		final String sheetName1 = j < 0 ? sheetName : sheetName.substring(0, j);
		final String sheetName2 = j < 0 ? sheetName : sheetName.substring(j+1);
		int sheetIndex1 = _uBook.getSheetIndex(sheetName1);
		int sheetIndex2 = _uBook.getSheetIndex(sheetName2);
		return _iBook.checkExternSheet(sheetIndex1, sheetIndex2);
	}
	public int getExternalSheetIndex(String workbookName, String sheetName) {
		return _iBook.getExternalSheetIndex(workbookName, sheetName);
	}

	public NameXPtg getNameXPtg(String name) {
        return _iBook.getNameXPtg(name, _uBook.getUDFFinder());
	}

	/**
	 * Lookup a named range by its name.
	 *
	 * @param name the name to search
	 * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
	 * The sheet index is required to resolve sheet-level names. <code>-1</code> means workbook-global names
	  */
	public EvaluationName getName(String name, int sheetIndex) {
		for(int i=0; i < _iBook.getNumNames(); i++) {
			NameRecord nr = _iBook.getNameRecord(i);
			if (nr.getSheetNumber() == sheetIndex+1 && name.equalsIgnoreCase(nr.getNameText())) {
				return new Name(nr, i);
			}
		}
		return sheetIndex == -1 ? null : getName(name, -1);
	}

	public int getSheetIndex(EvaluationSheet evalSheet) {
		HSSFSheet sheet = ((HSSFEvaluationSheet)evalSheet).getHSSFSheet();
		return _uBook.getSheetIndex(sheet);
	}
	public int getSheetIndex(String sheetName) {
		return _uBook.getSheetIndex(sheetName);
	}

	public String getSheetName(int sheetIndex) {
		return _uBook.getSheetName(sheetIndex);
	}

	public EvaluationSheet getSheet(int sheetIndex) {
		return new HSSFEvaluationSheet(_uBook.getSheetAt(sheetIndex));
	}
	public int convertFromExternSheetIndex(int externSheetIndex) {
		return _iBook.getSheetIndexFromExternSheetIndex(externSheetIndex);
	}
	public int convertLastIndexFromExternSheetIndex(int externSheetIndex) {
		return _iBook.getLastSheetIndexFromExternSheetIndex(externSheetIndex);
	}

	public ExternalSheet getExternalSheet(int externSheetIndex) {
		return _iBook.getExternalSheet(externSheetIndex);
	}
	
	public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
		return _iBook.getExternalName(externSheetIndex, externNameIndex);
	}

	public String resolveNameXText(NameXPtg n) {
		return _iBook.resolveNameXText(n.getSheetRefIndex(), n.getNameIndex());
	}

	public String getSheetNameByExternSheet(int externSheetIndex) {
		return _iBook.findSheetNameFromExternSheet(externSheetIndex);
	}
	public String getNameText(NamePtg namePtg) {
		return _iBook.getNameRecord(namePtg.getIndex()).getNameText();
	}
	public EvaluationName getName(NamePtg namePtg) {
		int ix = namePtg.getIndex();
		return new Name(_iBook.getNameRecord(ix), ix);
	}
	//ZSS-790
	public EvaluationName getName(NamePtg namePtg, int contextSheetIndex) {
		//Use namePtg.getIndex() to get the associated
		//NameRecord, so we can ignore contextSheetindex
		return getName(namePtg);
	}
	public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
		HSSFCell cell = ((HSSFEvaluationCell)evalCell).getHSSFCell();
		if (false) {
			// re-parsing the formula text also works, but is a waste of time
			// It is useful from time to time to run all unit tests with this code
			// to make sure that all formulas POI can evaluate can also be parsed.
			try {
				return HSSFFormulaParser.parse(cell.getCellFormula(), _uBook, FormulaType.CELL, _uBook.getSheetIndex(cell.getSheet()));
			} catch (FormulaParseException e) {
				// Note - as of Bugzilla 48036 (svn r828244, r828247) POI is capable of evaluating
				// IntesectionPtg.  However it is still not capable of parsing it.
				// So FormulaEvalTestData.xls now contains a few formulas that produce errors here.
				logger.log( POILogger.ERROR, e.getMessage());
			}
		}
		FormulaRecordAggregate fra = (FormulaRecordAggregate) cell.getCellValueRecord();
		return fra.getFormulaTokens();
	}
    public UDFFinder getUDFFinder(){
        return _uBook.getUDFFinder();
    }

	private static final class Name implements EvaluationName {

		private final NameRecord _nameRecord;
		private final int _index;

		public Name(NameRecord nameRecord, int index) {
			_nameRecord = nameRecord;
			_index = index;
		}
		public Ptg[] getNameDefinition() {
			return _nameRecord.getNameDefinition();
		}
		public String getNameText() {
			return _nameRecord.getNameText();
		}
		public boolean hasFormula() {
			return _nameRecord.hasFormula();
		}
		public boolean isFunctionName() {
			return _nameRecord.isFunctionName();
		}
		public boolean isRange() {
			return _nameRecord.hasFormula(); // TODO - is this right?
		}
		public NamePtg createPtg() {
			return new NamePtg(_index);
		}
	}

	public SpreadsheetVersion getSpreadsheetVersion(){
		return SpreadsheetVersion.EXCEL97;
	}

	@Override
	public String getBookNameFromExternalLinkIndex(String externalLinkIndex) {
		//TODO Excel 97-2003, external link index?
		return externalLinkIndex;
	}
	
	//20101112, henrichen@zkoss.org: handle parsing user defined function name
	/**
	 * Lookup a named range by its name.
	 *
	 * @param name the name to search
	 * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
	 * The sheet index is required to resolve sheet-level names. <code>-1</code> means workbook-global names
	 */
	@Override
	public EvaluationName getOrCreateName(String name, int sheetIndex) {
		for(int i=0; i < _iBook.getNumNames(); i++) {
			NameRecord nr = _iBook.getNameRecord(i);
			if (nr.getSheetNumber() == sheetIndex+1 && name.equalsIgnoreCase(nr.getNameText())) {
				return new Name(nr, i);
			}
		}
		if (sheetIndex == -1) {
			NameRecord nr = _iBook.createName();
			nr.setNameText(name);
			return new Name(nr, _iBook.getNumNames() - 1);
		}
		return getOrCreateName(name, -1); //recursive
	}

	@Override
	public Ptg[] getFormulaTokens(int sheetIndex, String formula) {
		return HSSFFormulaParser.parse(formula, _uBook, FormulaType.CELL, sheetIndex);
	}

	//20120117, henrichen@zkoss.org: get book index from book name
	//ZSS-81 Cannot input formula with proper external book name
	@Override
	public String getExternalLinkIndexFromBookName(String bookname) {
		//TODO see #getBookNameFromExternalLinkIndex(String externalIndex)
		return bookname;
	}
	
	@Override
	public boolean isAllowedDeferredNamePtg() {
		return false;
	}

	//ZSS-790
	@Override
	public EvaluationName getName(String name, String sheetName) {
		int sheetIndex = _uBook.getSheetIndex(sheetName);
		return getName(name, sheetIndex);
	}

	//ZSS-790. Make compile OK. We will not use this.
	@Override
	public String getFullNameText(NamePtg namePtg) {
		int sheetIndex = _iBook.getNameRecord(namePtg.getIndex()).getSheetNumber() - 1;
		String sheetName = sheetIndex >= 0 ? this.getSheetName(sheetIndex) : null;
		return (sheetName == null ? "" : sheetName + '!') + getNameText(namePtg);
	}

	//ZSS-960. Make compile OK. We does not use this.
	@Override
	public TablePtg createTablePtg(String tableName, Object[] specifiers,
			int sheetIndex, int rowIdx, int colIdx) {
		// TODO Auto-generated method stub
		return null;
	}

	//ZSS-966. Make compile OK. We does not use this.
	@Override
	public String getTableName(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
