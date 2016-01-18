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

import org.zkoss.poi.ss.formula.functions.FreeRefFunction;
import org.zkoss.poi.ss.formula.ptg.NamePtg;
import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.TablePtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.TablePtg.Item;
import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.formula.EvaluationCell;
import org.zkoss.poi.ss.formula.EvaluationName;
import org.zkoss.poi.ss.formula.EvaluationSheet;
import org.zkoss.poi.ss.formula.EvaluationWorkbook;
import org.zkoss.poi.ss.formula.FormulaParser;
import org.zkoss.poi.ss.formula.FormulaParsingWorkbook;
import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.ss.formula.FormulaType;
import org.zkoss.poi.ss.formula.udf.UDFFinder;
import org.zkoss.poi.xssf.model.IndexedUDFFinder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * Internal POI use only
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference
 */
public final class XSSFEvaluationWorkbook implements FormulaRenderingWorkbook, EvaluationWorkbook, FormulaParsingWorkbook {

	private final XSSFWorkbook _uBook;

	public static XSSFEvaluationWorkbook create(XSSFWorkbook book) {
		if (book == null) {
			return null;
		}
		return new XSSFEvaluationWorkbook(book);
	}

	private XSSFEvaluationWorkbook(XSSFWorkbook book) {
		_uBook = book;
	}

	/**
	 * @return the sheet index of the sheet with the given external index.
	 */
	public int convertFromExternSheetIndex(int externSheetIndex) {
		final String[] names = _uBook.convertFromExternSheetIndex(externSheetIndex);
		if (names == null) return -1;
		return getSheetIndex(names[1]);
	}
	/**
	 * @return the sheet index of the 2nd sheet with the given external index.
	 */
	public int convertLastIndexFromExternSheetIndex(int externSheetIndex) {
		final String[] names = _uBook.convertFromExternSheetIndex(externSheetIndex);
		if (names == null) return -1;
		return getSheetIndex(names[2]);
	}
	
	public int getExternalSheetIndex(String sheetName) {
		final int j = sheetName.indexOf(':');
		final String sheetName1 = j < 0 ? sheetName : sheetName.substring(0, j);
		final String sheetName2 = j < 0 ? sheetName : sheetName.substring(j+1);
		return _uBook.getOrCreateExternalSheetIndex(null, sheetName1, sheetName2);
	}

	public EvaluationName getName(String name, int sheetIndex) {
		for (int i = 0; i < _uBook.getNumberOfNames(); i++) {
			XSSFName nm = _uBook.getNameAt(i);
			String nameText = nm.getNameName();
			if (name.equalsIgnoreCase(nameText) && nm.getSheetIndex() == sheetIndex) {
				return new Name(_uBook.getNameAt(i), i, this);
			}
		}
		return sheetIndex == -1 ? null : getName(name, -1);
	}

	public int getSheetIndex(EvaluationSheet evalSheet) {
		XSSFSheet sheet = ((XSSFEvaluationSheet)evalSheet).getXSSFSheet();
		return _uBook.getSheetIndex(sheet);
	}

	public String getSheetName(int sheetIndex) {
		return _uBook.getSheetName(sheetIndex);
	}
	
	public ExternalName getExternalName(int externSheetIndex, int externNameIndex) {
	   throw new RuntimeException("Not implemented yet");
	}

	public NameXPtg getNameXPtg(String name) {
        IndexedUDFFinder udfFinder = (IndexedUDFFinder)getUDFFinder();
        FreeRefFunction func = udfFinder.findFunction(name);
		if(func == null) return null;
        else return new NameXPtg(0, udfFinder.getFunctionIndex(name));
	}

    public String resolveNameXText(NameXPtg n) {
        int idx = n.getNameIndex();
        IndexedUDFFinder udfFinder = (IndexedUDFFinder)getUDFFinder();
        return udfFinder.getFunctionName(idx);
    }

	public EvaluationSheet getSheet(int sheetIndex) {
		return new XSSFEvaluationSheet(_uBook.getSheetAt(sheetIndex));
	}

	/** Return null if in the same workbook */
	public ExternalSheet getExternalSheet(int externSheetIndex) {
		String[] names = _uBook.convertFromExternSheetIndex(externSheetIndex);
		if (names != null && names[0] != null) {
			return new ExternalSheet(names[0], names[1], names[2]);
		}
		return null;
	}
	public int getExternalSheetIndex(String workbookName, String sheetName) {
		final int j = sheetName.indexOf(':');
		final String sheetName1 = j < 0 ? sheetName : sheetName.substring(0, j);
		final String sheetName2 = j < 0 ? sheetName : sheetName.substring(j+1);
		return _uBook.getOrCreateExternalSheetIndex(workbookName, sheetName1, sheetName2);
	}
	public int getSheetIndex(String sheetName) {
		return _uBook.getSheetIndex(sheetName);
	}

	public String getSheetNameByExternSheet(int externSheetIndex) {
		String[] names = _uBook.convertFromExternSheetIndex(externSheetIndex);
		//20120117, henrichen@zkoss.org: ZSS-82
		return names == null ? "" : names[1].equals(names[2]) ? names[1] : names[1]+':'+names[2];   
	}

	public String getNameText(NamePtg namePtg) {
		return _uBook.getNameAt(namePtg.getIndex()).getNameName();
	}
	public EvaluationName getName(NamePtg namePtg) {
		int ix = namePtg.getIndex();
		return new Name(_uBook.getNameAt(ix), ix, this);
	}
	//ZSS-790
	public EvaluationName getName(NamePtg namePtg, int contextSheetIndex) {
		//Use namePtg.getIndex() to get the associated
		//XSSFName, so we can ignore contextSheetindex
		return getName(namePtg);
	}
	public Ptg[] getFormulaTokens(EvaluationCell evalCell) {
		XSSFCell cell = ((XSSFEvaluationCell)evalCell).getXSSFCell();
		XSSFEvaluationWorkbook frBook = XSSFEvaluationWorkbook.create(_uBook);
		return FormulaParser.parse(cell.getCellFormula(), frBook, FormulaType.CELL, _uBook.getSheetIndex(cell.getSheet()));
	}

    public UDFFinder getUDFFinder(){
        return _uBook.getUDFFinder();
    }

	private static final class Name implements EvaluationName {

		private final XSSFName _nameRecord;
		private final int _index;
		private final FormulaParsingWorkbook _fpBook;

		public Name(XSSFName name, int index, FormulaParsingWorkbook fpBook) {
			_nameRecord = name;
			_index = index;
			_fpBook = fpBook;
		}

		public Ptg[] getNameDefinition() {

			return FormulaParser.parse(_nameRecord.getRefersToFormula(), _fpBook, FormulaType.NAMEDRANGE, _nameRecord.getSheetIndex());
		}

		public String getNameText() {
			return _nameRecord.getNameName();
		}

		public boolean hasFormula() {
			// TODO - no idea if this is right
			CTDefinedName ctn = _nameRecord.getCTName();
			String strVal = ctn.getStringValue();
			return !ctn.getFunction() && strVal != null && strVal.length() > 0;
		}

		public boolean isFunctionName() {
			return _nameRecord.isFunctionName();
		}

		public boolean isRange() {
			return hasFormula(); // TODO - is this right?
		}
		public NamePtg createPtg() {
			return new NamePtg(_index);
		}
	}

	public SpreadsheetVersion getSpreadsheetVersion(){
		return SpreadsheetVersion.EXCEL2007;
	}

	@Override
	public String getBookNameFromExternalLinkIndex(String externalLinkIndex) {
		return _uBook.getBookNameFromExternalLinkIndex(externalLinkIndex);
	}
	
	//20101112, henrichen@zkoss.org: handle parsing of user defined function
	@Override
	public EvaluationName getOrCreateName(String name, int sheetIndex) {
		for (int i = 0; i < _uBook.getNumberOfNames(); i++) {
			XSSFName nm = _uBook.getNameAt(i);
			String nameText = nm.getNameName();
			if (name.equalsIgnoreCase(nameText) && nm.getSheetIndex() == sheetIndex) {
				return new Name(_uBook.getNameAt(i), i, this);
			}
		}
		if (sheetIndex == -1) {
			XSSFName nm = _uBook.createName();
			nm.setNameName(name);
			return new Name(nm, _uBook.getNumberOfNames() - 1, this);
		}
		return getOrCreateName(name, -1); //recursive
	}
	
	//20111124, henrichen@zkoss.org: get formula tokens per the formula string
	public Ptg[] getFormulaTokens(int sheetIndex, String formula) {
		XSSFEvaluationWorkbook frBook = XSSFEvaluationWorkbook.create(_uBook);
		return FormulaParser.parse(formula, frBook, FormulaType.CELL, sheetIndex);
	}

	//20110117, henrichen@zkoss.org: get extern book index from book name
	//ZSS-81 Cannot input formula with proper external book name
	@Override
	public String getExternalLinkIndexFromBookName(String bookname) {
		return _uBook.getExternalLinkIndexFromBookName(bookname);
	}
	
	@Override
	public boolean isAllowedDeferredNamePtg() {
		return true;
	}
	
	//20141012, henrichen
	//ZSS-790
	@Override
	public EvaluationName getName(String name, String sheetName) {
		int sheetIndex = _uBook.getSheetIndex(sheetName);
		return getName(name, sheetIndex);
	}

	//ZSS-790. Make compile OK. We will not use this.
	@Override
	public String getFullNameText(NamePtg namePtg) {
		int sheetIndex =  _uBook.getNameAt(namePtg.getIndex()).getSheetIndex();
		String sheetName = sheetIndex < 0 ? null : this.getSheetName(sheetIndex);
		return (sheetName == null ? "" : sheetName + '!') + getNameText(namePtg);
	}

	//ZSS-855
	@Override
	public TablePtg createTablePtg(String tableName, Object[] specifiers, int sheetIndex, int rowIdx, int colIdx) {
		if (specifiers.length > 3) {
			throw new IllegalArgumentException("At most total 3 table specifiers: " + specifiers);
		}
		String tableName0 = tableName;
		if (tableName0 == null) {
			final XSSFSheet sheet = _uBook.getSheetAt(sheetIndex);
			final XSSFTable table = sheet.getTableByRowCol(rowIdx, colIdx);
			tableName0 = table == null ? null : table.getName();
		}
		
		if (tableName0 == null) {
			throw new IllegalArgumentException("Unknown [...] expression; expect a Table");
		}
		TablePtg.Item item1 = null;
		TablePtg.Item item2 = null; 
		String column1 = null;
		String column2 = null;
		for (Object obj : specifiers) {
			if (obj instanceof String) {
				if (column1 == null) {
					column1 = normalize((String)obj);
				} else if (column2 == null) {
					column2 = normalize((String)obj);
				} else {
					throw new IllegalArgumentException("Should not have more than 2 column specifiers: " + specifiers);
				}
			} else if (obj instanceof Item) {
				if (item1 == null) {
					item1 = (Item) obj;
				} else if (item2 == null) {
					item2 = (Item) obj;
				} else {
					throw new IllegalArgumentException("Should not have more than 2 item specifiers: " + specifiers);
				}
			} else {
				throw new IllegalArgumentException("Unknown table specifiers: " + obj);
			}
		}
		if (item2 != null) {
			//sorting Items
			if (item2.ordinal() < item1.ordinal()) {
				final Item tmp = item1;
				item1 = item2;
				item2 = tmp;
			}
			
			//intersect Items
			switch(item1) {
			case ALL:
				item2 = null;
				break;
			case HEADERS:
				if (item2 == Item.HEADERS || item2 == Item.TOTALS || item2 == Item.THIS_ROW) {
					item2 = null;
				}
				break;
			case DATA:
				if (item2 == Item.THIS_ROW || item2 == Item.DATA) {
					item1 = item2;
					item2 = null;
				}
				break;
			case TOTALS:
				if (item2 == Item.TOTALS || item2 == Item.THIS_ROW) {
					item2 = null;
				}
				break;
			case THIS_ROW:
				if (item2 == Item.THIS_ROW) {
					item2 = null;
				}
				break;
			}
		}
		final XSSFTableName tbName = _uBook.getTableName(tableName0);
		XSSFTable table = tbName.getTable();
		final XSSFSheet tbSheet = table.getXSSFSheet();
		final int extIdx = this.getExternalSheetIndex(tbSheet.getSheetName()); //ZSS-1013
		return new TablePtg(extIdx, -1, -1, -1, -1, tableName0, 
				item1 == null ? new Item[0] : 
					item2 == null ? 
						new Item[]{item1} : new Item[] {item1, item2},
				column1 == null ? new String[0] : 
					column2 == null ? 
						new String[] {column1} : new String[]{column1, column2}, tableName == null);
	}

	//ZSS-855
	private static String normalize(String col) {
		int preSingle = -2; //single quote index
		StringBuilder sb = new StringBuilder();
		for (int j = 0, len = col.length(); j < len; ++j) {
			char ch = col.charAt(j);
			if (ch == '\'') {
				if (preSingle == j - 1) {
					sb.append(ch);
					preSingle = -2;
				} else {
					preSingle = j;
				}
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	//ZSS-966
	@Override
	public String getTableName(String name) {
		final XSSFTableName tbName = _uBook.getTableName(name); 
		return tbName == null ? null : tbName.getTableNameName();
	}
}
