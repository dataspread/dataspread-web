/* EvaluationSheetImpl.java

	Purpose:
		
	Description:
		
	History:
		Nov 8, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.poi.ss.formula.EvaluationCell;
import org.zkoss.poi.ss.formula.EvaluationSheet;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.FormulaError;
import org.zkoss.zss.model.ErrorValue;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.AbstractSheetAdv;

/**
 * 
 * @author Pao
 * @since 3.5.0
 */
public class EvalSheet implements EvaluationSheet {

	private SSheet _sheet;

	public EvalSheet(SSheet sheet) {
		this._sheet = sheet;
	}
	
	public SSheet getNSheet() {
		return _sheet;
	}

	public EvaluationCell getCell(int rowIndex, int columnIndex) {
		SCell cell = _sheet.getCell(rowIndex, columnIndex);
		return cell != null ? new EvalCell(cell) : null;
	}

	/*package*/ class EvalCell implements EvaluationCell {

		private SCell cell;
		private Key key;

		public EvalCell(SCell cell) {
			this.cell = cell;
			SSheet sheet = cell.getSheet();
			SBook book = sheet.getBook();
			int sheetIndex = book.getSheetIndex(sheet);
			key = new Key(book.getBookName(), sheetIndex, cell.getRowIndex(), cell.getColumnIndex());
		}

		public Object getIdentityKey() {
			return key;
		}

		public EvaluationSheet getSheet() {
			return EvalSheet.this;
		}

		public int getRowIndex() {
			return cell.getRowIndex();
		}

		public int getColumnIndex() {
			return cell.getColumnIndex();
		}

		public int getCellType() {
			switch(cell.getType()) {
				case BLANK:
					return Cell.CELL_TYPE_BLANK;
				case BOOLEAN:
					return Cell.CELL_TYPE_BOOLEAN;
				case ERROR:
					return Cell.CELL_TYPE_ERROR;
				case FORMULA:
					return Cell.CELL_TYPE_FORMULA;
				case NUMBER:
					return Cell.CELL_TYPE_NUMERIC;
				case STRING:
					return Cell.CELL_TYPE_STRING;
				default:
					return -1;
			}
		}

		public double getNumericCellValue() {
			return cell.getNumberValue().doubleValue();
		}

		public String getStringCellValue() {
			if(cell.getType() == CellType.FORMULA) {
				return cell.getFormulaValue();
			} else if(cell.getType() == CellType.STRING) {
				return cell.getStringValue();
			} else {
				return null;
			}
		}

		public boolean getBooleanCellValue() {
			return cell.getBooleanValue();
		}

		public int getErrorCellValue() {
			ErrorValue errorValue = cell.getErrorValue();
			switch(errorValue.getCode()) {
				case ErrorValue.INVALID_FORMULA:
					return FormulaError.NA.getCode(); //TODO zss 3.5 this value is not in zpoi
				default:
					return errorValue.getCode();
			}
		}

		public int getCachedFormulaResultType() {
			return getCellType(); // FIXME
		}

		@Override
		public String toString() {
			return getRowIndex() + ":" + getColumnIndex() + " " + getStringCellValue();
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(!(obj instanceof EvaluationCell))
				return false;
			return key.equals(((EvaluationCell)obj).getIdentityKey());
		}
		
		//ZSS-759
		public FormulaExpression getFormulaExpression() {
			return cell.getType() == CellType.FORMULA ? 
					(FormulaExpression) ((CellImpl)cell).getValue(false) : null;
		}
	}

	private static class Key {
		public String bookName; //ZSS-910
		public int sheet;
		public int row;
		public int column;

		public Key(String bookName, int sheet, int row, int column) {
			this.bookName = bookName; //ZSS-910
			this.sheet = sheet;
			this.row = row;
			this.column = column;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = bookName == null ? 1 : bookName.hashCode(); //ZSS-910
			result = prime * result + column;
			result = prime * result + row;
			result = prime * result + sheet;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Key other = (Key)obj;
			if(bookName != other.bookName || (bookName != null && !bookName.equals(other.bookName))) //ZSS-910
				return false;
			if(column != other.column)
				return false;
			if(row != other.row)
				return false;
			if(sheet != other.sheet)
				return false;
			return true;
		}

	}

	//ZSS-596 Possible memory leak when formula evaluation
	//implement hashCode and equals, use identity to implement equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_sheet == null) ? 0 : _sheet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvalSheet other = (EvalSheet) obj;
		
		return _sheet == other._sheet;
	}

	//ZSS-962
	@Override
	public boolean isHidden(int rowIndex, int columnIndex) {
		return ((AbstractSheetAdv)_sheet).isHidden(rowIndex, columnIndex);
	}
}
