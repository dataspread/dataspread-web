/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import org.model.AutoRollbackConnection;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.FormulaExpression;

import java.util.Collection;
import java.util.Locale;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */

class CellProxy extends AbstractCellAdv {
	private static final long serialVersionUID = 1L;
	AbstractCellAdv _proxy;
	private int _rowIdx;
	private int _columnIdx;
    private AbstractSheetAdv _sheet;

	public CellProxy(AbstractSheetAdv sheet, int row, int column) {
        this._sheet = sheet;
        this._rowIdx = row;
		this._columnIdx = column;
	}

	@Override
	public SSheet getSheet() {
		if(_proxy!=null){
			return _proxy.getSheet();
		}
        return _sheet;
    }

    @Override
    public void setSheet(AbstractSheetAdv sheet) {
		_sheet = sheet;
    }

	@Override
	public boolean isNull() {
		//if any data in data grid and it is not null, you should handle it.
		if(_proxy==null){
			return true;
		}else{
			return _proxy.isNull();
		}
	}

	@Override
	public CellType getType() {
		if(_proxy==null){
			return CellType.BLANK;
		}else{
			return  _proxy.getType();
		}
	}

	@Override
	public int getRowIndex() {
		return _proxy == null ? _rowIdx : _proxy.getRowIndex();
	}

	@Override
	public int getColumnIndex() {
		return _proxy == null ? _columnIdx : _proxy.getColumnIndex();
	}

	@Override
	public void setFormulaValue(String formula, AutoRollbackConnection connection, boolean updateToDB) {
		if (_proxy == null)
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
		_proxy.setFormulaValue(formula, connection, updateToDB);

	}

	@Override
	public void setFormulaValue(String formula) {
		if (_proxy == null)
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
		_proxy.setFormulaValue(formula);
	}

	//ZSS-565: Support input with Swedish locale into Formula
	@Override
	public void setFormulaValue(String formula, Locale locale, AutoRollbackConnection connection, boolean updateToDB) {
		if (_proxy == null)
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
		_proxy.setFormulaValue(formula, locale, connection, updateToDB);

	}

	@Override
	public void setValue(Object value, AutoRollbackConnection connection, boolean updateToDB) {
		setValue(value, false, connection, updateToDB);
	}

	//ZSS-853
	@Override
	protected void setValue(Object value, boolean aString, AutoRollbackConnection connection, boolean updateToDB) {
		if (_proxy == null && value != null) {
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
			_proxy.setValue(value, aString, connection, updateToDB);
		} else if (_proxy != null) {
			_proxy.setValue(value, aString, connection, updateToDB);
		}
	}

	@Override
	public Object getValue() {
		if(_proxy==null){
			return null;
		}else{
			return  _proxy.getValue();
		}
	}

	@Override
	public Object getValueSync() {
		if(_proxy==null){
			return null;
		}else{
			return  _proxy.getValueSync();
		}
	}

	@Override
	public void setValue(Object value) {
		if (_proxy == null && value != null) {
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
			_proxy.setValue(value);
		} else if (_proxy != null) {
			_proxy.setValue(value);
		}
	}

	@Override
	public String getReferenceString() {
		return _proxy == null ? new CellRegion(_rowIdx, _columnIdx).getReferenceString() : _proxy.getReferenceString();
	}

	@Override
	public SCellStyle getCellStyle() {
		return getCellStyle(false);
	}

	@Override
	public void setCellStyle(SCellStyle cellStyle, boolean updateToDB) {
		if (_proxy == null)
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
		_proxy.setCellStyle(cellStyle, updateToDB);
	}

	@Override
	public void setCellStyle(SCellStyle cellStyle, AutoRollbackConnection connection, boolean updateToDB) {
		if (_proxy == null)
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
		_proxy.setCellStyle(cellStyle, connection, updateToDB);
	}

	@Override
	public void setCellStyle(SCellStyle cellStyle) {
		if (_proxy == null)
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
		_proxy.setCellStyle(cellStyle);
	}

	@Override
	public SCellStyle getCellStyle(boolean local) {
		if (_proxy != null) {
			return _proxy.getCellStyle(local);
		}
		if (local)
			return null;
		AbstractSheetAdv sheet =  ((AbstractSheetAdv)getSheet());
		AbstractRowAdv row = sheet.getRow(_rowIdx, false);
		SCellStyle style = null;
		if (row != null) {
			style = row.getCellStyle(true);
		}
		if (style == null) {
			AbstractColumnArrayAdv carr = (AbstractColumnArrayAdv)sheet.getColumnArray(_columnIdx);
			if (carr != null) {
				style = carr.getCellStyle(true);
			}
		}
		if (style == null) {
			style = sheet.getBook().getDefaultCellStyle();
		}
		return style;
	}

	@Override
	public void setSemantics(SSemantics.Semantics semantics) {
		if (_proxy == null)
			_proxy = _sheet.createCell(_rowIdx, _columnIdx);
		_proxy.setSemantics(semantics);
	}

	@Override
	public SSemantics.Semantics getSemantics() {
		if (_proxy != null) {
			return _proxy.getSemantics();
		}
		return SSemantics.Semantics.NORMAL;
	}

	@Override
	public CellType getFormulaResultType() {
		return getFormulaResultType(false);
	}


	@Override
	public CellType getFormulaResultType(boolean sync) {
		return _proxy == null ? null : _proxy.getFormulaResultType(sync);
	}

	@Override
	public void clearValue(AutoRollbackConnection connection, boolean updateToDB) {
		if (_proxy != null)
			_proxy.clearValue(connection, updateToDB);
	}

	@Override
	public void clearFormulaResultCache() {
		if (_proxy != null)
			_proxy.clearFormulaResultCache();
	}

	@Override
	protected void evalFormula(boolean sync) {
		if (_proxy != null)
			_proxy.evalFormula(sync);
	}

	@Override
	public Object getValue(boolean eval) {
		return getValue(eval, false);
	}

	@Override
    public Object getValue(boolean eval, boolean sync) {
		return _proxy == null ? null : _proxy.getValue(eval, sync);
	}

	@Override
	public SHyperlink getHyperlink() {
		return _proxy == null ? null : _proxy.getHyperlink();
	}

	@Override
	public void setHyperlink(SHyperlink hyperlink) {
		if (_proxy == null) {
			_proxy = ((AbstractSheetAdv) getSheet()).getOrCreateRow(
					_rowIdx).getOrCreateCell(_columnIdx);
		}
		_proxy.setHyperlink(hyperlink);
	}

	@Override
	public SComment getComment() {
		return _proxy == null ? null : _proxy.getComment();
	}

	@Override
	public void setComment(SComment comment) {
		if (_proxy == null) {
			_proxy = ((AbstractSheetAdv) getSheet()).getOrCreateRow(
					_rowIdx).getOrCreateCell(_columnIdx);
		}
		_proxy.setComment(comment);
	}

	@Override
	public boolean isFormulaParsingError() {
		return _proxy != null && _proxy.isFormulaParsingError();
	}

	public Ref getRef(){
		return new RefImpl(this);
	}

	@Override
	public Collection<Ref> getReferredCells() {
		return null;
	}

	@Override
	public int getComputeCost() {
		return 0;
	}

	//ZSS-688
	//@since 3.6.0
    /* TODO: Remove the idea of clone cell. For sheet cloning use data modle cloning */
    /*
	@Override
    AbstractCellAdv cloneCell(AbstractRowAdv row, Connection connection, boolean updateToDB) {
		if (_proxy == null) {
			return new CellProxy((AbstractSheetAdv)row.getSheet(), row.getIndex(), this.getColumnIndex());
		} else {
			return _proxy.cloneCell(row, connection, updateToDB);
		}
	} */

	@Override
	public void setFormulaResultValue(ValueEval value) {
		if (_proxy != null) {
			_proxy.setFormulaResultValue(value);
		}
	}

	@Override
	public void deleteComment() {
		if (_proxy != null) {
			_proxy.deleteComment();
		}
	}

	@Override
	public CellRegion getCellRegion() {
		return null;
	}

	@Override
	public FormulaExpression getFormulaExpression() {
		if (_proxy != null) {
			return _proxy.getFormulaExpression();
		}
		return null;
	}

	@Override
	protected byte[] toBytes() {
		return new byte[0];
	}

	@Override
	public void shift(int rowShift, int colShift) {
		_rowIdx += rowShift;
		_columnIdx += colShift;
	}

	@Override
	public void translate(int rowShift, int colShift) {
		this._rowIdx += rowShift;
		this._columnIdx += colShift;
	}

	@Override
	public void updateCellTypeFromString(AutoRollbackConnection connection, boolean updateToDB) {}
}
