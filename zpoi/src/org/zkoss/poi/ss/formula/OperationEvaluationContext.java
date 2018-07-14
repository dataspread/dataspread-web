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

import org.zkoss.poi.ss.formula.ptg.Area3DPtg;
import org.zkoss.poi.ss.formula.ptg.ArrayPtg;
import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.Ref3DPtg;
import org.zkoss.poi.ss.formula.eval.*;
import org.zkoss.poi.ss.formula.functions.FreeRefFunction;
import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.formula.CollaboratingWorkbooksEnvironment.WorkbookNotFoundException;
import org.zkoss.poi.ss.formula.EvaluationWorkbook.ExternalName;
import org.zkoss.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.ss.util.CellReference.NameType;

/**
 * Contains all the contextual information required to evaluate an operation
 * within a formula
 *
 * For POI internal use only
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference
 */
public final class OperationEvaluationContext {
	public static final FreeRefFunction UDF = UserDefinedFunction.instance;
	private final EvaluationWorkbook _workbook;
	private final int _sheetIndex;
	private final int _rowIndex;
	private final int _columnIndex;
	private final EvaluationTracker _tracker;
	private final WorkbookEvaluator _bookEvaluator;
	int _ptgIndex;
	private final DependencyTracker _dependencyTracker;
	private final Object _dependent;

	public OperationEvaluationContext(WorkbookEvaluator bookEvaluator, EvaluationWorkbook workbook, int sheetIndex, int srcRowNum,
			int srcColNum, EvaluationTracker tracker, DependencyTracker dependencyTracker, Object ref) {
		_bookEvaluator = bookEvaluator;
		_workbook = workbook;
		_sheetIndex = sheetIndex;
		_rowIndex = srcRowNum;
		_columnIndex = srcColNum;
		_tracker = tracker;
		_dependencyTracker = dependencyTracker;
		_dependent = ref;
	}

	public EvaluationWorkbook getWorkbook() {
		return _workbook;
	}

	public int getRowIndex() {
		return _rowIndex;
	}

	public int getColumnIndex() {
		return _columnIndex;
	}
	
	public String getSheetName() {
		return _workbook.getSheetName(_sheetIndex);
	}

	SheetRefEvaluator createExternSheetRefEvaluator(ExternSheetReferenceToken ptg) {
		return createExternSheetRefEvaluator(ptg.getExternSheetIndex());
	}
	SheetRefEvaluator createExternSheetRefEvaluator(int externSheetIndex) {
		ExternalSheet externalSheet = _workbook.getExternalSheet(externSheetIndex);
		WorkbookEvaluator targetEvaluator;
		int otherSheetIndex, otherLastSheetIndex;
		if (externalSheet == null) {
			// sheet is in same workbook
			otherSheetIndex = _workbook.convertFromExternSheetIndex(externSheetIndex);
			otherLastSheetIndex = _workbook.convertLastIndexFromExternSheetIndex(externSheetIndex);
			targetEvaluator = _bookEvaluator;
		} else {
			// look up sheet by name from external workbook
			String workbookName = externalSheet.getWorkbookName();
			try {
				targetEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName);
			} catch (WorkbookNotFoundException e) {
				//20120117, henrichen@zkoss.org: ZSS-82
				return null;
				//throw new RuntimeException(e.getMessage(), e);
			}
			otherSheetIndex = targetEvaluator.getSheetIndex(externalSheet.getSheetName());
			otherLastSheetIndex = targetEvaluator.getSheetIndex(externalSheet.getLastSheetName());
			if (otherSheetIndex < 0) {
				//20120117, henrichen@zkoss.org: ZSS-82
				return null;
				//throw new RuntimeException("Invalid sheet name '" + externalSheet.getSheetName()
				//		+ "' in bool '" + workbookName + "'.");
			}
			if (otherLastSheetIndex < 0) {
				//20120117, henrichen@zkoss.org: ZSS-82
				return null;
				//throw new RuntimeException("Invalid sheet name '" + externalSheet.getLastSheetName()
				//		+ "' in bool '" + workbookName + "'.");
			}
		}
		return new SheetRefEvaluator(targetEvaluator, _tracker, otherSheetIndex, otherLastSheetIndex);
	}

	/**
	 * @return <code>null</code> if either workbook or sheet is not found
	 */
	private SheetRefEvaluator createExternSheetRefEvaluator(String workbookName, String sheetName, String lastSheetName) {
		WorkbookEvaluator targetEvaluator;
		if (workbookName == null) {
			targetEvaluator = _bookEvaluator;
		} else {
			if (sheetName == null) {
				throw new IllegalArgumentException("sheetName must not be null if workbookName is provided");
			}
			try {
				targetEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName);
			} catch (WorkbookNotFoundException e) {
				return null;
			}
		}
		int otherSheetIndex = sheetName == null ? _sheetIndex : targetEvaluator.getSheetIndex(sheetName);
		if (otherSheetIndex < 0) {
			return null;
		}
		int otherLastSheetIndex = lastSheetName == null ? _sheetIndex : targetEvaluator.getSheetIndex(lastSheetName);
		return new SheetRefEvaluator(targetEvaluator, _tracker, otherSheetIndex, otherLastSheetIndex);
	}

	public SheetRefEvaluator getRefEvaluatorForCurrentSheet() {
		return new SheetRefEvaluator(_bookEvaluator, _tracker, _sheetIndex, _sheetIndex);
	}



	/**
	 * Resolves a cell or area reference dynamically.
	 * @param workbookName the name of the workbook containing the reference.  If <code>null</code>
	 * the current workbook is assumed.  Note - to evaluate formulas which use multiple workbooks,
	 * a {@link CollaboratingWorkbooksEnvironment} must be set up.
	 * @param sheetName the name of the sheet containing the reference.  May be <code>null</code>
	 * (when <tt>workbookName</tt> is also null) in which case the current workbook and sheet is
	 * assumed.
	 * @param refStrPart1 the single cell reference or first part of the area reference.  Must not
	 * be <code>null</code>.
	 * @param refStrPart2 the second part of the area reference. For single cell references this
	 * parameter must be <code>null</code>
	 * @param isA1Style specifies the format for <tt>refStrPart1</tt> and <tt>refStrPart2</tt>.
	 * Pass <code>true</code> for 'A1' style and <code>false</code> for 'R1C1' style.
	 * TODO - currently POI only supports 'A1' reference style
	 * @return a {@link RefEval} or {@link AreaEval}
	 */
	public ValueEval getDynamicReference(String workbookName, String sheetName, String lastSheetName, String refStrPart1,
			String refStrPart2, boolean isA1Style) {
		clearIndirectRefPrecedent(); //ZSS-845
		if (!isA1Style) {
			throw new RuntimeException("R1C1 style not supported yet");
		}
		SheetRefEvaluator sre = createExternSheetRefEvaluator(workbookName, sheetName, lastSheetName);
		if (sre == null) {
			return ErrorEval.REF_INVALID;
		}
		// ugly typecast - TODO - make spreadsheet version more easily accessible
		SpreadsheetVersion ssVersion = ((FormulaParsingWorkbook)_workbook).getSpreadsheetVersion();

		NameType part1refType = classifyCellReference(refStrPart1, ssVersion);
		switch (part1refType) {
			case BAD_CELL_OR_NAMED_RANGE:
				return ErrorEval.REF_INVALID;
			case NAMED_RANGE:
                EvaluationName nm = getName(refStrPart1, sheetName == null ? -1 : sre.getSheetIndex());
                if (nm == null) {
					return ErrorEval.REF_INVALID;
                }
                if(!nm.isRange()){
                	return ErrorEval.REF_INVALID;
//                    throw new RuntimeException("Specified name '" + refStrPart1 + "' is not a range as expected.");
                }
                createIndirectRefPrecedent(new NameRangeEval(nm.createPtg())); //ZSS-845
                return _bookEvaluator.evaluateNameFormula(nm.getNameDefinition(), this);
		}
		if (refStrPart2 == null) {
			// no ':'
			switch (part1refType) {
				case COLUMN:
				case ROW:
					return ErrorEval.REF_INVALID;
				case CELL:
					CellReference cr = new CellReference(refStrPart1);
					ValueEval valueEval = new LazyRefEval(cr.getRow(), cr.getCol(), !cr.isRowAbsolute(), !cr.isColAbsolute(), sre);
					createIndirectRefPrecedent(valueEval); //ZSS-845
					return valueEval;
			}
			throw new IllegalStateException("Unexpected reference classification of '" + refStrPart1 + "'.");
		}
		NameType part2refType = classifyCellReference(refStrPart2, ssVersion);
		switch (part2refType) {
			case BAD_CELL_OR_NAMED_RANGE:
				return ErrorEval.REF_INVALID;
			case NAMED_RANGE:
				return ErrorEval.REF_INVALID;
//				throw new RuntimeException("Cannot evaluate '" + refStrPart2
//						+ "'. Indirect evaluation of defined names not supported yet");
		}

		if (part2refType != part1refType) {
			// LHS and RHS of ':' must be compatible
			return ErrorEval.REF_INVALID;
		}
		int firstRow, firstCol, lastRow, lastCol;
		boolean firstRowRel = false, firstColRel = false, lastRowRel = false, lastColRel = false;
		switch (part1refType) {
			case COLUMN:
				firstRow =0;
				lastRow = ssVersion.getLastRowIndex();
				if (refStrPart1.startsWith("$")) {
					refStrPart1 = refStrPart1.substring(1);
				} else {
					firstColRel = true;
				}
				if (refStrPart2.startsWith("$")) {
					refStrPart2 = refStrPart2.substring(1);
				} else {
					lastColRel = true;
				}
				firstCol = parseColRef(refStrPart1);
				lastCol = parseColRef(refStrPart2);
				break;
			case ROW:
				firstCol = 0;
				lastCol = ssVersion.getLastColumnIndex();
				if (refStrPart1.startsWith("$")) {
					refStrPart1 = refStrPart1.substring(1);
				} else {
					firstRowRel = true;
				}
				if (refStrPart2.startsWith("$")) {
					refStrPart2 = refStrPart2.substring(1);
				} else {
					lastRowRel = true;
				}
				firstRow = parseRowRef(refStrPart1);
				lastRow = parseRowRef(refStrPart2);
				break;
			case CELL:
				CellReference cr;
				cr = new CellReference(refStrPart1);
				firstRow = cr.getRow();
				firstCol = cr.getCol();
				firstRowRel = !cr.isRowAbsolute();
				firstColRel = !cr.isColAbsolute();
				cr = new CellReference(refStrPart2);
				lastRow = cr.getRow();
				lastCol = cr.getCol();
				lastRowRel = !cr.isRowAbsolute();
				lastColRel = !cr.isColAbsolute();
				break;
			default:
				throw new IllegalStateException("Unexpected reference classification of '" + refStrPart1 + "'.");
		}
		ValueEval valueEval = new LazyAreaEval(firstRow, firstCol, lastRow, lastCol, 
				firstRowRel, firstColRel, lastRowRel, lastColRel, sre);
		createIndirectRefPrecedent(valueEval); //ZSS-845
		return valueEval;
	}

	private static int parseRowRef(String refStrPart) {
		return Integer.parseInt(refStrPart) - 1;
	}

	private static int parseColRef(String refStrPart) {
		return CellReference.convertColStringToIndex(refStrPart);
	}

	private static NameType classifyCellReference(String str, SpreadsheetVersion ssVersion) {
		int len = str.length();
		if (len < 1) {
			return CellReference.NameType.BAD_CELL_OR_NAMED_RANGE;
		}
		return CellReference.classifyCellReference(str, ssVersion);
	}

	public FreeRefFunction findUserDefinedFunction(String functionName) {
		return _bookEvaluator.findUserDefinedFunction(functionName);
	}
	public ValueEval getRefEval(int rowIndex, int columnIndex, boolean rowRel, boolean colRel) {
		SheetRefEvaluator sre = getRefEvaluatorForCurrentSheet();
		return new LazyRefEval(rowIndex, columnIndex, rowRel, colRel, sre);
	}
	public ValueEval getRef3DEval(int rowIndex, int columnIndex, 
			boolean rowRel, boolean colRel, int extSheetIndex) {
		SheetRefEvaluator sre = createExternSheetRefEvaluator(extSheetIndex);
		//20120117, henrichen@zkoss.org: ZSS-82
		if (sre == null) {
			return ErrorEval.REF_INVALID;
		}
		return new LazyRefEval(rowIndex, columnIndex, rowRel, rowRel, sre);
	}
	public ValueEval getAreaEval(int firstRowIndex, int firstColumnIndex,
			int lastRowIndex, int lastColumnIndex, 
			boolean row1Rel, boolean col1Rel, boolean row2Rel, boolean col2Rel) {
		SheetRefEvaluator sre = getRefEvaluatorForCurrentSheet();
		return new LazyAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex, 
				row1Rel, col1Rel, row2Rel, col2Rel, sre);
	}
	public WorkbookEvaluator getWorkbookEvaluator() {
		return _bookEvaluator;
	}
	public ValueEval getArea3DEval(int firstRowIndex, int firstColumnIndex,
			int lastRowIndex, int lastColumnIndex,
			boolean row1Rel, boolean col1Rel, boolean row2Rel, boolean col2Rel,
			int extSheetIndex) {
		SheetRefEvaluator sre = createExternSheetRefEvaluator(extSheetIndex);
		//20120117, henrichen@zkoss.org: ZSS-82
		if (sre == null) {
			return ErrorEval.REF_INVALID;
		}
		return new LazyAreaEval(firstRowIndex, firstColumnIndex, lastRowIndex, lastColumnIndex, 
				row1Rel, col1Rel, row2Rel, col2Rel, sre);
	}
	public ValueEval getNameXEval(NameXPtg nameXPtg) {
      ExternalSheet externSheet = _workbook.getExternalSheet(nameXPtg.getSheetRefIndex());
      if(externSheet == null)
         return new NameXEval(nameXPtg);
      String workbookName = externSheet.getWorkbookName();
      ExternalName externName = _workbook.getExternalName(
            nameXPtg.getSheetRefIndex(), 
            nameXPtg.getNameIndex()
      );
      try{
         WorkbookEvaluator refWorkbookEvaluator = _bookEvaluator.getOtherWorkbookEvaluator(workbookName);
         EvaluationName evaluationName = refWorkbookEvaluator.getName(externName.getName(),externName.getIx()-1);
         if(evaluationName != null && evaluationName.hasFormula()){
            if (evaluationName.getNameDefinition().length > 1) {
               throw new RuntimeException("Complex name formulas not supported yet");
            }
            Ptg ptg = evaluationName.getNameDefinition()[0];
            if(ptg instanceof Ref3DPtg){
               Ref3DPtg ref3D = (Ref3DPtg)ptg;
               int sheetIndex = refWorkbookEvaluator.getSheetIndexByExternIndex(ref3D.getExternSheetIndex());
               int lastSheetIndex = refWorkbookEvaluator.getLastSheetIndexByExternIndex(ref3D.getExternSheetIndex());
               String sheetName = refWorkbookEvaluator.getSheetName(sheetIndex);
               String lastSheetName = refWorkbookEvaluator.getSheetName(lastSheetIndex);
               SheetRefEvaluator sre = createExternSheetRefEvaluator(workbookName, sheetName, lastSheetName);
               return new LazyRefEval(ref3D.getRow(), ref3D.getColumn(), ref3D.isRowRelative(), ref3D.isColRelative(), sre);
            }else if(ptg instanceof Area3DPtg){
               Area3DPtg area3D = (Area3DPtg)ptg;
               int sheetIndex = refWorkbookEvaluator.getSheetIndexByExternIndex(area3D.getExternSheetIndex());
               int lastSheetIndex = refWorkbookEvaluator.getLastSheetIndexByExternIndex(area3D.getExternSheetIndex());
               String sheetName = refWorkbookEvaluator.getSheetName(sheetIndex);
               String lastSheetName = refWorkbookEvaluator.getSheetName(lastSheetIndex);
               SheetRefEvaluator sre = createExternSheetRefEvaluator(workbookName, sheetName, lastSheetName);
               return new LazyAreaEval(
            		   area3D.getFirstRow(), area3D.getFirstColumn(), 
            		   area3D.getLastRow(), area3D.getLastColumn(), 
            		   area3D.isFirstRowRelative(), area3D.isFirstColRelative(), 
            		   area3D.isLastRowRelative(), area3D.isLastColRelative(), 
            		   sre);
            }
         }
         return ErrorEval.REF_INVALID;
      }catch(WorkbookNotFoundException wnfe){
         return ErrorEval.REF_INVALID;
      }
   }
	
	//ZSS-845
	//@since 3.9.6
	public int getPtgIndex() {
		return _ptgIndex;
	}
	public void setPtgIndex(int index) {
		_ptgIndex = index;
	}
	//ZSS-845
	private void clearIndirectRefPrecedent() {
		_dependencyTracker.clearIndirectRefPrecedent(this);
	}
	//ZSS-845
	private void createIndirectRefPrecedent(ValueEval precedent) {
		_dependencyTracker.setIndirectRefPrecedent(this, precedent);
	}
	//ZSS-845
	public EvaluationName getName(String name, int contextSheetIndex) {
        EvaluationName nm = ((FormulaParsingWorkbook)_workbook).getName(name, contextSheetIndex < 0 ? _sheetIndex : contextSheetIndex);
        if (nm == null && contextSheetIndex < 0) { // check the workbook if not specified an explicit context sheet
        	nm = ((FormulaParsingWorkbook)_workbook).getName(name, null);
        }
        return nm;
	}
	//ZSS-834
	public Object getDependent() {
		return _dependent;
	}
	
	//ZSS-962
	public ValueEval getArrayEval(ArrayPtg ptg) {
		SheetRefEvaluator sre = getRefEvaluatorForCurrentSheet();
		return new ArrayEval(ptg, sre);
	}
}
