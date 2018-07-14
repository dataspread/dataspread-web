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

import org.zkoss.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.zkoss.poi.ss.formula.IStabilityClassifier;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.formula.eval.BlankEval;
import org.zkoss.poi.ss.formula.eval.BoolEval;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.udf.UDFFinder;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.CellValue;
import org.zkoss.poi.ss.usermodel.FormulaEvaluator;
import org.zkoss.poi.ss.usermodel.Workbook;

import org.zkoss.poi.ss.formula.eval.HyperlinkEval;
/**
 * Evaluates formula cells.<p/>
 *
 * For performance reasons, this class keeps a cache of all previously calculated intermediate
 * cell values.  Be sure to call {@link #clearAllCachedResultValues()} if any workbook cells are changed between
 * calls to evaluate~ methods on this class.
 *
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * @author Josh Micich
 */
public class XSSFFormulaEvaluator implements FormulaEvaluator {

	private WorkbookEvaluator _bookEvaluator;
	private XSSFWorkbook _book;

	public XSSFFormulaEvaluator(XSSFWorkbook workbook) {
		this(workbook, null, null);
	}
	/**
	 * @param stabilityClassifier used to optimise caching performance. Pass <code>null</code>
	 * for the (conservative) assumption that any cell may have its definition changed after
	 * evaluation begins.
	 * @deprecated (Sep 2009) (reduce overloading) use {@link #create(XSSFWorkbook, org.zkoss.poi.ss.formula.IStabilityClassifier, org.zkoss.poi.hssf.record.formula.udf.UDFFinder)} 
	 */
    @Deprecated
    public XSSFFormulaEvaluator(XSSFWorkbook workbook, IStabilityClassifier stabilityClassifier) {
		_bookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(workbook), stabilityClassifier, null);
		_book = workbook;
	}
	private XSSFFormulaEvaluator(XSSFWorkbook workbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
		_bookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(workbook), stabilityClassifier, udfFinder);
      _book = workbook;
	}

	/**
	 * @param stabilityClassifier used to optimise caching performance. Pass <code>null</code>
	 * for the (conservative) assumption that any cell may have its definition changed after
	 * evaluation begins.
	 * @param udfFinder pass <code>null</code> for default (AnalysisToolPak only)
	 */
	public static XSSFFormulaEvaluator create(XSSFWorkbook workbook, IStabilityClassifier stabilityClassifier, UDFFinder udfFinder) {
		return new XSSFFormulaEvaluator(workbook, stabilityClassifier, udfFinder);
	}


	/**
	 * Should be called whenever there are major changes (e.g. moving sheets) to input cells
	 * in the evaluated workbook.
	 * Failure to call this method after changing cell values will cause incorrect behaviour
	 * of the evaluate~ methods of this class
	 */
	public void clearAllCachedResultValues() {
		_bookEvaluator.clearAllCachedResultValues();
	}
	public void notifySetFormula(Cell cell) {
		_bookEvaluator.notifyUpdateCell(new XSSFEvaluationCell((XSSFCell)cell));
	}
	public void notifyDeleteCell(Cell cell) {
		_bookEvaluator.notifyDeleteCell(new XSSFEvaluationCell((XSSFCell)cell));
	}
    public void notifyUpdateCell(Cell cell) {
        _bookEvaluator.notifyUpdateCell(new XSSFEvaluationCell((XSSFCell)cell));
    }

	/**
	 * If cell contains a formula, the formula is evaluated and returned,
	 * else the CellValue simply copies the appropriate cell value from
	 * the cell and also its cell type. This method should be preferred over
	 * evaluateInCell() when the call should not modify the contents of the
	 * original cell.
	 * @param cell
	 */
	public CellValue evaluate(Cell cell) {
		if (cell == null) {
			return null;
		}

		switch (cell.getCellType()) {
			case XSSFCell.CELL_TYPE_BOOLEAN:
				return CellValue.valueOf(cell.getBooleanCellValue());
			case XSSFCell.CELL_TYPE_ERROR:
				return CellValue.getError(cell.getErrorCellValue());
			case XSSFCell.CELL_TYPE_FORMULA:
				return evaluateFormulaCellValue(cell);
			case XSSFCell.CELL_TYPE_NUMERIC:
				return new CellValue(cell.getNumericCellValue());
			case XSSFCell.CELL_TYPE_STRING:
				return new CellValue(cell.getRichStringCellValue().getString());
            case XSSFCell.CELL_TYPE_BLANK:
                return null;
		}
		throw new IllegalStateException("Bad cell type (" + cell.getCellType() + ")");
	}


	/**
	 * If cell contains formula, it evaluates the formula,
	 *  and saves the result of the formula. The cell
	 *  remains as a formula cell.
	 * Else if cell does not contain formula, this method leaves
	 *  the cell unchanged.
	 * Note that the type of the formula result is returned,
	 *  so you know what kind of value is also stored with
	 *  the formula.
	 * <pre>
	 * int evaluatedCellType = evaluator.evaluateFormulaCell(cell);
	 * </pre>
	 * Be aware that your cell will hold both the formula,
	 *  and the result. If you want the cell replaced with
	 *  the result of the formula, use {@link #evaluate(org.zkoss.poi.ss.usermodel.Cell)} }
	 * @param cell The cell to evaluate
	 * @return The type of the formula result (the cell's type remains as HSSFCell.CELL_TYPE_FORMULA however)
	 */
	public int evaluateFormulaCell(Cell cell) {
		if (cell == null || cell.getCellType() != XSSFCell.CELL_TYPE_FORMULA) {
			return -1;
		}
		CellValue cv = evaluateFormulaCellValue(cell);
		// cell remains a formula cell, but the cached value is changed
		setCellValue(cell, cv);
		return cv.getCellType();
	}

	/**
	 * If cell contains formula, it evaluates the formula, and
	 *  puts the formula result back into the cell, in place
	 *  of the old formula.
	 * Else if cell does not contain formula, this method leaves
	 *  the cell unchanged.
	 * Note that the same instance of HSSFCell is returned to
	 * allow chained calls like:
	 * <pre>
	 * int evaluatedCellType = evaluator.evaluateInCell(cell).getCellType();
	 * </pre>
	 * Be aware that your cell value will be changed to hold the
	 *  result of the formula. If you simply want the formula
	 *  value computed for you, use {@link #evaluateFormulaCell(org.zkoss.poi.ss.usermodel.Cell)} }
	 * @param cell
	 */
	public XSSFCell evaluateInCell(Cell cell) {
		if (cell == null) {
			return null;
		}
		XSSFCell result = (XSSFCell) cell;
		if (cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA) {
			CellValue cv = evaluateFormulaCellValue(cell);
			setCellType(cell, cv); // cell will no longer be a formula cell
			setCellValue(cell, cv);
		}
		return result;
	}
	private static void setCellType(Cell cell, CellValue cv) {
		int cellType = cv.getCellType();
		switch (cellType) {
			case XSSFCell.CELL_TYPE_BOOLEAN:
			case XSSFCell.CELL_TYPE_ERROR:
			case XSSFCell.CELL_TYPE_NUMERIC:
			case XSSFCell.CELL_TYPE_STRING:
				cell.setCellType(cellType);
				return;
			case XSSFCell.CELL_TYPE_BLANK:
				// never happens - blanks eventually get translated to zero
			case XSSFCell.CELL_TYPE_FORMULA:
				// this will never happen, we have already evaluated the formula
		}
		throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
	}

	private static void setCellValue(Cell cell, CellValue cv) {
		int cellType = cv.getCellType();
		switch (cellType) {
			case XSSFCell.CELL_TYPE_BOOLEAN:
				cell.setCellValue(cv.getBooleanValue());
				break;
			case XSSFCell.CELL_TYPE_ERROR:
				cell.setCellErrorValue(cv.getErrorValue());
				break;
			case XSSFCell.CELL_TYPE_NUMERIC:
				cell.setCellValue(cv.getNumberValue());
				break;
			case XSSFCell.CELL_TYPE_STRING:
				cell.setCellValue(new XSSFRichTextString(cv.getStringValue()));
				break;
			case XSSFCell.CELL_TYPE_BLANK:
				// never happens - blanks eventually get translated to zero
			case XSSFCell.CELL_TYPE_FORMULA:
				// this will never happen, we have already evaluated the formula
			default:
				throw new IllegalStateException("Unexpected cell value type (" + cellType + ")");
		}
	}

	/**
	 * Loops over all cells in all sheets of the supplied
	 *  workbook.
	 * For cells that contain formulas, their formulas are
	 *  evaluated, and the results are saved. These cells
	 *  remain as formula cells.
	 * For cells that do not contain formulas, no changes
	 *  are made.
	 * This is a helpful wrapper around looping over all
	 *  cells, and calling evaluateFormulaCell on each one.
	 */
	public static void evaluateAllFormulaCells(XSSFWorkbook wb) {
	   HSSFFormulaEvaluator.evaluateAllFormulaCells((Workbook)wb);
	}
   /**
    * Loops over all cells in all sheets of the supplied
    *  workbook.
    * For cells that contain formulas, their formulas are
    *  evaluated, and the results are saved. These cells
    *  remain as formula cells.
    * For cells that do not contain formulas, no changes
    *  are made.
    * This is a helpful wrapper around looping over all
    *  cells, and calling evaluateFormulaCell on each one.
    */
   public void evaluateAll() {
      HSSFFormulaEvaluator.evaluateAllFormulaCells(_book);
   }

	/**
	 * Returns a CellValue wrapper around the supplied ValueEval instance.
	 */
	private CellValue evaluateFormulaCellValue(Cell cell) {
        if(!(cell instanceof XSSFCell)){
            throw new IllegalArgumentException("Unexpected type of cell: " + cell.getClass() + "." +
                    " Only XSSFCells can be evaluated.");
        }

		ValueEval eval = _bookEvaluator.evaluate(new XSSFEvaluationCell((XSSFCell) cell), null);
		return getCellValueByValueEval(eval);
	}
	
	@Override
	public WorkbookEvaluator getWorkbookEvaluator() {
		return _bookEvaluator;
	}

	//20111124, henrichen@zkoss.org: given eval, return CellValue
	public CellValue getCellValueByValueEval(ValueEval eval) {
		//20100917, henrichen@zkoss.org: handle HYPERLINK function 
		CellValue cv = null;
		if (eval instanceof ArrayEval) {
			return getCellValueByValueEval(((ArrayEval)eval).getValue(0, 0)); //recursive
		}
		if (eval instanceof NumberEval) {
			NumberEval ne = (NumberEval) eval;
			cv = new CellValue(ne.getNumberValue());
		}
		if (eval instanceof BoolEval) {
			BoolEval be = (BoolEval) eval;
			cv =CellValue.valueOf(be.getBooleanValue());
		}
		if (eval instanceof StringEval) {
			StringEval ne = (StringEval) eval;
			cv = new CellValue(ne.getStringValue());
		}
		if (eval instanceof ErrorEval) {
			//20110407, henrichne@zkoss.org: degenerate CIRCULAR_REF_ERROR to REF_INVALID
			cv = CellValue.getError(((ErrorEval)eval).getErrorCode() == ErrorEval.CIRCULAR_REF_ERROR.getErrorCode() ? 
					ErrorEval.REF_INVALID.getErrorCode() : ((ErrorEval)eval).getErrorCode());
		}
		// 20130619, paowang@potix.org: needs to handle blank cell (ZSS-255) 
		if(eval instanceof BlankEval) {
			cv = new CellValue(""); // blank cell is equaled to empty string here
		}
		if (cv != null) {
			if (eval instanceof HyperlinkEval) {
				cv.setHyperlink(((HyperlinkEval)eval).getHyperlink());
			}
			return cv;
		}
		throw new RuntimeException("Unexpected eval class (" + eval.getClass().getName() + ")");
	}
	
    /** {@inheritDoc} */
    public void setDebugEvaluationOutputForNextEval(boolean value){
        _bookEvaluator.setDebugEvaluationOutputForNextEval(value);
    }
	
	//20111124, henrichen@zkoss.org: get left-top cell value 
	@Override
	public CellValue evaluateFormula(int sheetIndex, String formula, Object ref) {
		ValueEval eval = _bookEvaluator.evaluate(sheetIndex, formula, false, ref);
		return getCellValueByValueEval(eval);
	}
	
	//20111128, henrichen@zkoss.org: return ValueEval
	public ValueEval evaluateFormulaValueEval(int sheetIndex, String formula, boolean ignoreDereference, Object ref) {
		return _bookEvaluator.evaluate(sheetIndex, formula, ignoreDereference, ref);
	}
}
