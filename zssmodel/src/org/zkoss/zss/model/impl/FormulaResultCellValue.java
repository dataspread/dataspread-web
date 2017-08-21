package org.zkoss.zss.model.impl;

import org.zkoss.poi.ss.formula.eval.AreaEval;
import org.zkoss.zss.model.ErrorValue;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.sys.formula.EvaluationResult;
import org.zkoss.zss.model.sys.formula.EvaluationResult.ResultType;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * the formula result cell value
 * @author dennis
 * @since 3.5.0
 *
 * Added live value switch for async eval and RWlock for consistency between value and type
 * by zekun.fan@gmail.com
 */
public class FormulaResultCellValue extends CellValue implements Serializable {
	private static final long serialVersionUID = 1L;

	public FormulaResultCellValue(EvaluationResult result) {
		updateByEvaluationResult(result);
	}

	// Added by zekun.fan@gmail.com, @WriteLockProtected
	private void updateByEvaluationResult(EvaluationResult result){
		Object val = result.getValue();
		ResultType type = result.getType();
		if (type == ResultType.ERROR) {
			cellType = CellType.ERROR;
			value = (val instanceof ErrorValue) ? (ErrorValue) val
					: ErrorValue.valueOf(ErrorValue.INVALID_VALUE);
		} else if (type == ResultType.SUCCESS) {
			setByValue(val);
		}
	}

	private void setByValue(Object val) {
		if (val == null || "".equals(val)) {
			cellType = CellType.BLANK;
			value = null;
		} else if (val instanceof String) {
			cellType = CellType.STRING;
			value = (String) val;
		} else if (val instanceof Number) {
			cellType = CellType.NUMBER;
			value = (Number) val;
		} else if (val instanceof Boolean) {
			cellType = CellType.BOOLEAN;
			value = (Boolean) val;
		} else if (val instanceof AreaEval) {
			cellType = CellType.COLLECTION;
			value = (AreaEval) val;
		} else if (val instanceof Collection) {
			// possible a engine return a collection in cell evaluation case?
			// who should take care array formula?
			if (((Collection) val).size() > 0) {
				cellType = CellType.COLLECTION;
				value = (Collection) val;
			} else {
				cellType = CellType.BLANK;
				value = null;
			}
		} else if (val.getClass().isArray()) {
			// possible a engine return a collection in cell evaluation case?
			// who should take care array formula?
			if (((Object[]) val).length > 0) {
				setByValue(((Object[]) val)[0]);
			} else {
				cellType = CellType.BLANK;
				value = null;
			}
		} else {
			cellType = CellType.ERROR;
			value = (val instanceof ErrorValue) ? (ErrorValue) val
					: new ErrorValue(ErrorValue.INVALID_VALUE,
							"Unknow value type " + val);
		}
	}

	public CellType getCellType() {
		return cellType;
	}

	public Object getValue() {
		return value;
	}
}