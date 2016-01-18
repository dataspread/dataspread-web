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

import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.ptg.FuncVarPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.usermodel.Cell;

/**
 *
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference
 */
final public class SheetRefEvaluator {

	private final WorkbookEvaluator _bookEvaluator;
	private final EvaluationTracker _tracker;
	private final int _sheetIndex;
	private final int _lastSheetIndex;
	private EvaluationSheet _sheet;


	public SheetRefEvaluator(WorkbookEvaluator bookEvaluator, EvaluationTracker tracker, int sheetIndex, int lastSheetIndex) {
//20101213, henrichen@zkoss.org: handle deleted sheet		
/*		if (sheetIndex < 0) {
			throw new IllegalArgumentException("Invalid sheetIndex: " + sheetIndex + ".");
		}
		if (lastSheetIndex < 0) {
			throw new IllegalArgumentException("Invalid sheetIndex2: " + lastSheetIndex + ".");
		}
*/		_bookEvaluator = bookEvaluator;
		_tracker = tracker;
		_sheetIndex = sheetIndex;
		_lastSheetIndex = lastSheetIndex;
	}

	public String getSheetName() {
		return _sheetIndex < 0 ? "#REF" : _bookEvaluator.getSheetName(_sheetIndex);
	}
	
	public String getLastSheetName() {
		return _lastSheetIndex < 0 ? "#REF" : _bookEvaluator.getSheetName(_lastSheetIndex);
	}

	public ValueEval getEvalForCell(int rowIndex, int columnIndex) {
		return _bookEvaluator.evaluateReference(getSheetName(), getLastSheetName(), rowIndex, columnIndex, _tracker);
	}
	
	public String getBookName() {
		final CollaboratingWorkbooksEnvironment env = _bookEvaluator.getEnvironment();
		return env.getBookName(_bookEvaluator);
	}

	private EvaluationSheet getSheet() {
		if (_sheet == null) {
			_sheet = _bookEvaluator.getSheet(_sheetIndex);
		}
		return _sheet;
	}
	
    /**
     * @return  whether cell at rowIndex and columnIndex is a subtotal
     * @see org.zkoss.poi.ss.formula.functions.Subtotal
     */
    public boolean isSubTotal(int rowIndex, int columnIndex){
        boolean subtotal = false;
        EvaluationCell cell = getSheet().getCell(rowIndex, columnIndex);
        if(cell != null && cell.getCellType() == Cell.CELL_TYPE_FORMULA){
            EvaluationWorkbook wb = _bookEvaluator.getWorkbook();
            for(Ptg ptg : wb.getFormulaTokens(cell)){
                if(ptg instanceof FuncVarPtg){
                    FuncVarPtg f = (FuncVarPtg)ptg;
                    if("SUBTOTAL".equals(f.getName())) {
                        subtotal = true;
                        break;
                    }
                }
            }
        }
        return subtotal;
    }

    //20111125, henrichen@zkoss.org: return number of sheet of this SheetReference
    public int getSheetCount() {
    	if (_sheetIndex > 0 && _lastSheetIndex > 0) {
    		return _lastSheetIndex  - _sheetIndex + 1;
    	} else if (_sheetIndex < 0 && _lastSheetIndex < 0) {
    		return 0;
    	}
    	return 1;
    }
    
    //ZSS-845
    public int getSheetIndex() {
    	return _sheetIndex;
    }
    //ZSS-845
    public int getLastSheetIndex() {
    	return _lastSheetIndex;
    }
    
    //ZSS-962
    public boolean isHidden(int sheetIdx, int rowIndex, int columnIndex) {
    	EvaluationSheet sheet = _bookEvaluator.getSheet(_sheetIndex);
		return sheet.isHidden(rowIndex, columnIndex);
    }
}
