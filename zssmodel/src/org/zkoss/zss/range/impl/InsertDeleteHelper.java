/* InsertDeleteHelper.java

	Purpose:
		
	Description:
		
	History:
		Feb 18, 2014 Created by Pao Wang

Copyright (C) 2014 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.range.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SChart;
import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SColumnArray;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SSheetViewInfo;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.STableColumn;
import org.zkoss.zss.model.ViewAnchor;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.AbstractColumnArrayAdv;
import org.zkoss.zss.model.impl.AbstractRowAdv;
import org.zkoss.zss.model.impl.AbstractSheetAdv;
import org.zkoss.zss.model.impl.AbstractTableAdv;
import org.zkoss.zss.range.SRange;
import org.zkoss.zss.range.SRange.DeleteShift;
import org.zkoss.zss.range.SRange.InsertCopyOrigin;
import org.zkoss.zss.range.SRange.InsertShift;

/**
 * A helper to perform insert/delete row/column/cells.
 * @author Pao
 * @since 3.5.0
 */
public class InsertDeleteHelper extends RangeHelperBase {

	public InsertDeleteHelper(SRange range) {
		super(range);
	}

	// ZSS-984
	//category tables to toDelete(which to be deleted in delete case or shifted in insert case), 
	//  toShift(which need shift region), overlap(which need partial delete/insert)
	//  return error message if not OK; return null if OK.
	private String getDeletedTables(SRange range, 
			Set<String> toDelete, Set<String> overlapped) { 
		final int row1 = range.getRow();
		final int row2 = range.getLastRow();
		final int col1 = range.getColumn();
		final int col2 = range.getLastColumn();
		final SSheet sheet  = range.getSheet();
		for (STable tb : sheet.getTables()) {
			final CellRegion rgn = tb.getAllRegion().getRegion();
			final int tr1 = rgn.getRow();
			final int tc1 = rgn.getColumn();
			final int tr2 = rgn.getLastRow();
			final int tc2 = rgn.getLastColumn();
			
			if (tr2 < row1 || tr1 > row2 || tc2 < col1 || tc1 > col2) {
				continue; //no overlapping
			}
			
			// table is contained by the range
			if (row1 <= tr1 && tr2 <= row2 && col1 <= tc1 && tc2 <= col2) {
				if (!isWholeColumn() && !isWholeRow() && !toDelete.isEmpty()) {
					return "The operation can only be applied on one table.";
				}
				toDelete.add(tb.getName().toUpperCase()); //total delete
				continue;
			}
			//overlapped more than 2 tables
			if (!overlapped.isEmpty() || !toDelete.isEmpty()) { 
				return "The operation can only be applied on one table.";
			}
			// Range is a row or a column or the table contains the selected range
			if (range.isWholeRow() || range.isWholeColumn() ||
				(tr1 <= row1 && row2 <= tr2 && tc1 <= col1 && col2 <= tc2)) {
				overlapped.add(tb.getName().toUpperCase());
			} else {
				return "The operation can only be applied on one table.";
			}
		}
		return null; // succeed
	}

	//ZSS-985
	//collect Tables needs to be shifted
	private void collectAndCheckShiftTables(int row1, int col1, int row2, int col2, Set<String> toShift, boolean horizontal) {
		if (horizontal) {
			for (STable tb : sheet.getTables()) {
				final CellRegion rgn = tb.getAllRegion().getRegion();
				final int tr1 = rgn.getRow();
				final int tc1 = rgn.getColumn();
				final int tr2 = rgn.getLastRow();
				if (tc1 > col2) { // right side tables
					if (tr2 >= row1 && tr1 <= row2) { // row overlapping
						if (row1 > tr1 || tr2 > row2) { // row is not total cover
							throw new InvalidModelOpException("The operation is attempting to shift cells in table '"+ tb.getAllRegion().getRegion().getReferenceString() + "' on your worksheet."); //ZSS-984
						} else {
							toShift.add(tb.getName().toUpperCase());
						}
					}
				}
			}
		} else {
			for (STable tb : sheet.getTables()) {
				final CellRegion rgn = tb.getAllRegion().getRegion();
				final int tr1 = rgn.getRow();
				final int tc1 = rgn.getColumn();
				final int tc2 = rgn.getLastColumn();
				if (tr1 > row2) { // bottom side tables
					if (tc2 >= col1 && tc1 <= col2) { // column overlapping
						if (col1 > tc1 || tc2 > col2) { // column is not total cover
							throw new InvalidModelOpException("The operation is attempting to shift cells in table '"+ tb.getAllRegion().getRegion().getReferenceString() + "' on your worksheet."); //ZSS-984
						} else {
							toShift.add(tb.getName().toUpperCase());
						}
					}
				}
			}
		}
	}

	//ZSS-985
	//delete all tables that can be deleted; return overlapped table that can
	//be partial deleted
	private STable deleteTable(DeleteShift shift, Set<String> toShift) {
		final Set<String> toDelete = new HashSet<String>();
		final Set<String> overlapped = new HashSet<String>(2);
		String message = getDeletedTables(range, toDelete, overlapped); //ZSS-984
		if (message != null)
			throw new InvalidModelOpException(message); //ZSS-984
		
		final AbstractBookAdv book = (AbstractBookAdv) sheet.getBook();
		final STable overlap = overlapped.isEmpty() ? null : book.getTable(overlapped.iterator().next());
		
		final int row1 = getRow();
		final int row2 = getLastRow();
		final int col1 = getColumn();
		final int col2 = getLastColumn();
		//check if we can shift the toShift without problem
		if (overlap != null) {
			final CellRegion orgn = overlap.getAllRegion().getRegion();
			final int r1 = orgn.getRow();
			final int r2 = orgn.getLastRow();
			if (shift == DeleteShift.UP) {
				//cover the header row; must shift left
				if ((overlap.getHeaderRowCount() > 0 && row1 <= r1 && r1 <= row2 && r2 > row2)
						|| ((orgn.getRowCount() - overlap.getHeaderRowCount() - overlap.getTotalsRowCount()) == 1)) {
					throw new InvalidModelOpException("Can only applies Delete > Shift Cells Left"); //ZSS-984
				} else {
					final int c01 = Math.min(orgn.getColumn(), col1);
					final int c02 = Math.max(orgn.getLastColumn(), col2);
					collectAndCheckShiftTables(row1, c01, row2, c02, toShift, false /*horizontal*/);
				}
			} else { // shift == DeleteShift.LEFT
				final int r01 = Math.min(r1,  row1);
				final int r02 = Math.max(orgn.getLastRow(), row2);
				collectAndCheckShiftTables(r01, col1, r02, col2, toShift, true /*horizontal*/);
			}
		} else {
			collectAndCheckShiftTables(row1, col1, row2, col2, toShift, shift == DeleteShift.LEFT /*horizontal*/);
		}
		
		//do delete
		deleteTablesByNames(sheet, toDelete);
		
		//do partial delete
		return overlap;
	}
	
	//ZSS-1001
	public static Set<String> collectContainedTables(SSheet sheet, 
			int row1, int col1, int row2, int col2) {
		Set<String> toDelete = new HashSet<String>();
		for (STable tb : sheet.getTables()) {
			final CellRegion rgn = tb.getAllRegion().getRegion();
			final int tr1 = rgn.getRow();
			final int tc1 = rgn.getColumn();
			final int tr2 = rgn.getLastRow();
			final int tc2 = rgn.getLastColumn();
			
			// table is contained by the range
			if (row1 <= tr1 && tr2 <= row2 && col1 <= tc1 && tc2 <= col2) {
				toDelete.add(tb.getName().toUpperCase()); //total delete
			}
		}
		return toDelete;
	}
	
	//ZSS-1001
	public static void deleteTablesByNames(SSheet sheet, Set<String> toDelete) {
		AbstractBookAdv book = (AbstractBookAdv)sheet.getBook();
		if (!toDelete.isEmpty()) {
			for (String tbName : toDelete) {
				book.removeTable(tbName);
			}
			((AbstractSheetAdv)sheet).removeTables(toDelete);
		}
	}

	//ZSS-985
	private void deleteTable0(STable tb) {
		final AbstractBookAdv book = (AbstractBookAdv) sheet.getBook();
		final String tbName = tb.getName();
		book.removeTable(tbName);
		((AbstractSheetAdv)sheet).removeTable(tbName);
	}
	
	//ZSS-985
	//return true means need some extra checking on deleting cells
	private boolean deleteRows(STable tb) {
		if (tb == null) return false;
		final CellRegion rgn = tb.getAllRegion().getRegion();
		if (rgn.getRow() == getRow() && rgn.getLastRow() == getLastRow()) {
			deleteTable0(tb);
			return false;
		} else {
			((AbstractTableAdv)tb).deleteRows(getRow(), getLastRow());
			return true;
		}
	}
	
	//ZSS-985
	private void deleteCols(STable tb) {
		if (tb == null) return;
		final CellRegion rgn = tb.getAllRegion().getRegion();
		// cover all
		if (rgn.getColumn() == getColumn() && rgn.getLastColumn() == getLastColumn()) {
			deleteTable0(tb);
		} else {
			((AbstractTableAdv)tb).deleteCols(getColumn(), getLastColumn());
		}
	}

	//ZSS-985
	private void shiftTables(AbstractBookAdv book, Set<String> toShift, int offset, boolean horizontal) {
		//do shift
		if (!toShift.isEmpty()) {
			Set<String> toDelete = new HashSet<String>();
			if (horizontal) {
				for (String tbName : toShift) {
					if (!((AbstractTableAdv)book.getTable(tbName)).shiftCols(offset)) {
						toDelete.add(tbName);
					}
				}
			} else {
				for (String tbName : toShift) {
					if (!((AbstractTableAdv)book.getTable(tbName)).shiftRows(offset)) {
						toDelete.add(tbName);
					};
				}
			}
			//do delete
			if (!toDelete.isEmpty()) {
				for (String tbName : toDelete) {
					book.removeTable(tbName);
				}
				((AbstractSheetAdv)sheet).removeTables(toDelete);
			}
		}
	}
	//ZSS-985
	public void delete(DeleteShift shift) {
		// just process on the first sheet even this range over multiple sheetsl
		final AbstractBookAdv book = (AbstractBookAdv)sheet.getBook();
		int row1 = getRow();
		int row2 = getLastRow();
		int col1 = getColumn();
		int col2 = getLastColumn();
		//ZSS-988
		final Set<String> toShift = new HashSet<String>();
		STable overlap = null;
		// insert row/column/cell
		if(isWholeRow()) { // ignore insert direction
			// ZSS:592: Doesn't support inserting/deleting row/columns when current range cross freeze panel
			if(checkInCornerFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support deleting rows/columns operation when current range covers the corner frozen panes");
			}
			// ZSS:595: Doesn't support inserting/deleting when current range cross freeze panel
			if(checkCrossTopFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support deleting rows when current range cross the freeze panes line");
			}
	
			//ZSS-985
//			final Set<String> toShift = new HashSet<String>();
			overlap = deleteTable(DeleteShift.UP, toShift);
			if (overlap != null) {
				CellRegion rgn = overlap.getAllRegion().getRegion();
				if (deleteRows(overlap)) { // might change the region
					final CellRegion rgn0 = overlap.getAllRegion().getRegion();
					//reserve at least one row and clear it's content
					if ((rgn0.getLastRow() - rgn0.getRow() + row2 - row1 + 1) > (rgn.getLastRow() - rgn.getRow())) {
						new ClearCellHelper(new RangeImpl(sheet, row1, col1, row1, col2)).clearCellContent(); 
						row1 = row1 + 1;
					}
				}
			}
			
			// shrink chart size (picture's size won't be changed in Excel)
			// before delete rows (delete rows will make chart move)
			if (row2 >= row1) { //ZSS-985
				shiftTables(book, toShift, -row2 + row1 - 1, false /*horizontal*/);
				shrinkChartHeight(row1, row2);
				sheet.deleteRow(row1, row2);
			}
			
		} else if(isWholeColumn()) { // ignore insert direction
			
			// ZSS:592: Doesn't support inserting/deleting row/columns when current range cross freeze panel
			if(checkInCornerFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support deleting rows/columns operation when current range covers the corner frozen panes");
			}
			// ZSS:595: Doesn't support inserting/deleting when current range cross freeze panel
			if(checkCrossLeftFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support deleting columns when current range cross the freeze panes line");
			}

			//ZSS-985
//			final Set<String> toShift = new HashSet<String>();
			overlap = deleteTable(DeleteShift.LEFT, toShift);
			deleteCols(overlap);
			shiftTables(book, toShift, -col2 + col1 - 1, true /*horizontal*/);
			
			// shrink chart size (picture's size won't be changed in Excel)
			// before delete columns (delete columns will make chart move)
			shrinkChartWidth();
			sheet.deleteColumn(col1, col2);
			
		} else if(shift != DeleteShift.DEFAULT) { // do nothing if "DEFAULT", it's according to XRange.delete() spec.
			//ZSS-985
//			final Set<String> toShift = new HashSet<String>();
			overlap = deleteTable(shift, toShift);
			if (overlap != null) {
				final CellRegion rgn = overlap.getAllRegion().getRegion();
				//cover the header row, must deleteCols
				if (shift == DeleteShift.LEFT) { 
					deleteCols(overlap);
					shiftTables(book, toShift, -col2 + col1 - 1, true /*horizontal*/);
					sheet.deleteCell(rgn.getRow(), col1, rgn.getLastRow(), col2, true /*DeleteShift.LEFT*/);
				} else { //if (shift == DeleteShift.UP) {
					if (deleteRows(overlap)) {
						final CellRegion rgn0 = overlap.getAllRegion().getRegion();
						//reserve at least one row and clear it's content
						if ((rgn0.getLastRow() - rgn0.getRow() + row2 - row1 + 1) > (rgn.getLastRow() - rgn.getRow())) {
							new ClearCellHelper(new RangeImpl(sheet, row1, rgn.getColumn(), row1, rgn.getLastColumn())).clearCellContent(); 
							row1 = row1 + 1;
						}
					}
					if (row2 >= row1) { //ZSS-985
						shiftTables(book, toShift, -row2 + row1 - 1, false /*horizontal*/);
						sheet.deleteCell(row1, rgn.getColumn(), row2, rgn.getLastColumn(), false /*!DeleteShift.LEFT*/);
					}
				}
			} else {
				if (shift == DeleteShift.LEFT) { 
					shiftTables(book, toShift, -col2 + col1 - 1, true /*horizontal*/);
				} else {
					shiftTables(book, toShift, -row2 + row1 - 1, false /*horizontal*/);
				}
				sheet.deleteCell(row1, col1, row2, col2, shift == DeleteShift.LEFT);
			}
		}
		//ZSS-988
		if (overlap != null) {
			((AbstractTableAdv)overlap).refreshFilter();
		}
		//ZSS-988: delete old filter, shift, add new filter
		if (!toShift.isEmpty()) {
			for (String tbName : toShift) {
				final AbstractTableAdv tb = (AbstractTableAdv)book.getTable(tbName);
				if (tb != null) {
					tb.refreshFilter();
				}
			}
		}
	}

	// ZSS-986
	//category tables to overlap(which need partial insert)
	//  return error message if not OK; return null if OK.
	private String getInsertedTables(SRange range, Set<String> overlapped, boolean horizontal) {
		final int row1 = range.getRow();
		final int row2 = range.getLastRow();
		final int col1 = range.getColumn();
		final int col2 = range.getLastColumn();
		final SSheet sheet  = range.getSheet();
		final boolean isWholeRow = isWholeRow();
		final boolean isWholeColumn = isWholeColumn();
		final Set<String> toShift = new HashSet<String>();
		
		for (STable tb : sheet.getTables()) {
			final CellRegion rgn = tb.getAllRegion().getRegion();
			final int tr1 = rgn.getRow();
			final int tc1 = rgn.getColumn();
			final int tr2 = rgn.getLastRow();
			final int tc2 = rgn.getLastColumn();
			
			if (tr2 < row1 || tr1 > row2 || tc2 < col1 || tc1 > col2) {
				continue; //no overlapping
			}

			//Shift
			//1. Whole Row and cover the top Table row
			//2. Whole Column and cover the left Table column
			//3. Shift vertical and the selected range contains the whole Table columns (shift down) 
			//4. Shift horizontal and the selected range contains the whole Table rows (shift right)

			if (isWholeRow && row1 <= tr1) {
				toShift.add(tb.getName().toUpperCase());
			} else if (isWholeColumn && col1 <= tc1) {
				toShift.add(tb.getName().toUpperCase());
			} else if ((!horizontal && col1 <= tc1 && tc2 <= col2)
				&& (row1 < tr1 || (row1 == tr1 && (col1 < tc1 || tc2 < col2)))) {
				toShift.add(tb.getName().toUpperCase());
			} else if ((horizontal && row1 <= tr1 && tr2 <= row2)
				&& (col1 < tc1 || (col1 == tc1 && (row1 < tr1 || tr2 < row2)))) { 
				toShift.add(tb.getName().toUpperCase());
			} else {
				if (!overlapped.isEmpty() || !toShift.isEmpty()) { 
					return "The operation can only be applied on one table.";
				}
				// 1. Range is a row or a column 
				// 2. The table contains the selected range
				if (range.isWholeRow() || range.isWholeColumn() 
						|| (tr1 <= row1 && row2 <= tr2 && tc1 <= col1 && col2 <= tc2)) {
					overlapped.add(tb.getName().toUpperCase());
				} else {
					return "The operation can only be applied on one table.";
				}
			}
		}
		return null; // succeed
	}

	//ZSS-986
	//return overlapped table that can be partial inserted (extended)
	private STable insertTable(InsertShift shift, Set<String> toShift) {
		final Set<String> overlapped = new HashSet<String>(2);
		String message = getInsertedTables(range, overlapped, shift == InsertShift.RIGHT); //ZSS-986
		if (message != null)
			throw new InvalidModelOpException(message);
		
		final AbstractBookAdv book = (AbstractBookAdv) sheet.getBook();
		final STable overlap = overlapped.isEmpty() ? null : book.getTable(overlapped.iterator().next());
		
		final int row1 = getRow();
		final int row2 = getLastRow();
		final int col1 = getColumn();
		final int col2 = getLastColumn();
		//check if we can shift the toShift without problem
		if (overlap != null) {
			final CellRegion orgn = overlap.getAllRegion().getRegion();
			final int r1 = orgn.getRow();
			final int r2 = orgn.getLastRow();
			if (shift == InsertShift.DOWN) {
				//cover the header row; must shift right
				if (overlap.getHeaderRowCount() > 0 && row1 <= r1 && r1 <= row2 && r2 > row2) {
					throw new InvalidModelOpException("Can only applies Insert > Shift Cells Right");
				} else {
					final int c01 = Math.min(orgn.getColumn(), col1);
					final int c02 = Math.max(orgn.getLastColumn(), col2);
					collectAndCheckShiftTables(row1, c01, row1, c02, toShift, false /*horizontal*/);
				}
			} else { // shift == DeleteShift.LEFT
				final int r01 = Math.min(r1,  row1);
				final int r02 = Math.max(orgn.getLastRow(), row2);
				collectAndCheckShiftTables(r01, col1, r02, col1, toShift, true /*horizontal*/);
			}
		} else {
			if (shift == InsertShift.DOWN) {
				//row1-1 for the case that select on first row of the Table 
				collectAndCheckShiftTables(row1, col1, row1-1, col2, toShift, false /*horizontal*/);
			} else {
				//col1-1 for the case that select on first column of the Table
				collectAndCheckShiftTables(row1, col1, row2, col1-1, toShift, true /*horizontal*/);
			}
		}
		
		//do partial insert
		return overlap;
	}

	//ZSS-986
	private void insertTableRows(STable tb) {
		if (tb == null) return;
		((AbstractTableAdv)tb).insertRows(getRow(), getLastRow());
	}
	
	//ZSS-986
	private void insertTableCols(STable tb, boolean insertLeft) {
		if (tb == null) return;
		((AbstractTableAdv)tb).insertCols(getColumn(), getLastColumn(), insertLeft);
	}

	//ZSS-986
	private void fillHeaderRow(STable tb, int col1, int col2) {
		if (tb == null || tb.getHeaderRowCount() == 0) return;
		final CellRegion rgn = tb.getAllRegion().getRegion();
		final int r1 = rgn.getRow();
		for (int j = col1; j <= col2; ++j) {
			final STableColumn tbCol = tb.getColumnAt(j);
			final String name = tbCol.getName();
			final SCell cell = sheet.getCell(r1, j);
			cell.setStringValue(name, null, true);
		}
	}
	
	public void insert(InsertShift shift, InsertCopyOrigin copyOrigin) {
		// just process on the first sheet even this range over multiple sheets
		
		final AbstractBookAdv book = (AbstractBookAdv)sheet.getBook();
		int row1 = getRow();
		int row2 = getLastRow();
		int col1 = getColumn();
		int col2 = getLastColumn();
		//ZSS-988
		final Set<String> toShift = new HashSet<String>();
		STable overlap = null;
		// insert row/column/cell
		if(isWholeRow()) { // ignore insert direction
			
			// ZSS:592: Doesn't support inserting/deleting row/columns when current range cross freeze panel
			if(checkInCornerFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support inserting rows/columns when current range covers the corner frozen panes");
			}
			// ZSS:595: Doesn't support inserting/deleting row/columns when current range cross freeze panel
			if(checkCrossTopFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support inserting rows when current range cross the freeze panes line");
			}

			//ZSS-986
//			final Set<String> toShift = new HashSet<String>();
			overlap = insertTable(InsertShift.DOWN, toShift);
			insertTableRows(overlap);
			shiftTables(book, toShift, row2 - row1 + 1, false /*horizontal*/);
			
			if (overlap != null) {
				copyOrigin = InsertCopyOrigin.FORMAT_LEFT_ABOVE; //always copy left/above style in Table case
			}
			sheet.insertRow(row1, row2);
			
			// copy style/formal/size
			if(copyOrigin == InsertCopyOrigin.FORMAT_LEFT_ABOVE) {
				if(row1 - 1 >= 0) {
					copyRowStyle(row1 - 1, row1, row2);
				}
			} else if(copyOrigin == InsertCopyOrigin.FORMAT_RIGHT_BELOW) {
				if(getLastRow() + 1 <= sheet.getBook().getMaxRowIndex()) {
					copyRowStyle(row2 + 1, row1, row2);
				}
			}
			
			// extend chart size (picture's size won't be changed in Excel)
			extendChartHeight();
			
		} else if(isWholeColumn()) { // ignore insert direction
			
			// ZSS:592: Doesn't support inserting/deleting row/columns when current range cross freeze panel
			if(checkInCornerFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support inserting rows/columns when current range covers the corner frozen panes");
			}
			// ZSS:595: Doesn't support inserting/deleting row/columns when current range cross freeze panel
			if(checkCrossLeftFreezePanel()) {
				throw new InvalidModelOpException("Doesn't support inserting columns when current range cross the freeze panes line");
			}

			//ZSS-986
//			final Set<String> toShift = new HashSet<String>();
			overlap = insertTable(InsertShift.RIGHT, toShift);
			insertTableCols(overlap, copyOrigin == InsertCopyOrigin.FORMAT_LEFT_ABOVE);
			shiftTables(book, toShift, col2 - col1 + 1, true /*horizontal*/);

			if (overlap != null) {
				copyOrigin = InsertCopyOrigin.FORMAT_LEFT_ABOVE; //always copy left/above style in Table case
			}
			sheet.insertColumn(col1, col2);
			fillHeaderRow(overlap, col1, col2);
			
			// copy style/formal/size
			if(copyOrigin == InsertCopyOrigin.FORMAT_LEFT_ABOVE) {
				if(col1 - 1 >= 0) {
					copyColumnStyle(col1 - 1, col1, col2);
				}
			} else if(copyOrigin == InsertCopyOrigin.FORMAT_RIGHT_BELOW) {
				if(col2 + 1 <= sheet.getBook().getMaxColumnIndex()) {
					copyColumnStyle(col2 + 1, col1, col2);
				}
			}
			
			// extend chart size (picture's size won't be changed in Excel)
			extendChartWidth();

		} else if(shift != InsertShift.DEFAULT) { // do nothing if "DEFAULT", it's according to XRange.insert() spec.
			//ZSS-986
//			final Set<String> toShift = new HashSet<String>();
			overlap = insertTable(shift, toShift);
			if (overlap != null) {
				final CellRegion rgn = overlap.getAllRegion().getRegion();
				if (shift == InsertShift.RIGHT) {
					final boolean isLastColumn = col1 == rgn.getLastColumn();
					insertTableCols(overlap, copyOrigin == InsertCopyOrigin.FORMAT_LEFT_ABOVE);
					shiftTables(book, toShift, col2 - col1 + 1, true /*horizontal*/);
					
					if (col1 == col2 && isLastColumn
						&& copyOrigin == InsertCopyOrigin.FORMAT_RIGHT_BELOW) { //Insert Table Column to the Right 
						++col1;
						++col2;
					}

					row1 = rgn.getRow();
					row2 = rgn.getLastRow();
				} else {
					final boolean isLastDataRow = row1 == rgn.getLastRow() - overlap.getTotalsRowCount();
					insertTableRows(overlap);
					shiftTables(book, toShift, row2 - row1 + 1, false /*horizontal*/);
					
					if (row1 == row2 && isLastDataRow 
						&& copyOrigin == InsertCopyOrigin.FORMAT_RIGHT_BELOW) { //Insert Table Column to the Right 
						++row1;
						++row2;
					}
					
					col1 = rgn.getColumn();
					col2 = rgn.getLastColumn();
				}
				copyOrigin = InsertCopyOrigin.FORMAT_LEFT_ABOVE; //always copy left/above style in Table case
			} else {
				if (shift == InsertShift.RIGHT) { 
					shiftTables(book, toShift, col2 - col1 + 1, true /*horizontal*/);
				} else {
					shiftTables(book, toShift, row2 - row1 + 1, false /*horizontal*/);
				}
			}
			
			sheet.insertCell(row1, col1, row2, col2, shift == InsertShift.RIGHT);

			if (shift == InsertShift.RIGHT) {
				fillHeaderRow(overlap, col1, col2);
			}
			
			// copy style/formal/size
			if(shift == InsertShift.RIGHT) { // horizontal
				if(copyOrigin == InsertCopyOrigin.FORMAT_LEFT_ABOVE) {
					if(col1 - 1 >= 0) {
						copyCellStyleFromColumn(col1 - 1);
					}
				} else if(copyOrigin == InsertCopyOrigin.FORMAT_RIGHT_BELOW) {
					if(col2 + 1 <= sheet.getBook().getMaxColumnIndex()) {
						copyCellStyleFromColumn(col2 + 1);
					}
				}
			} else { // vertical
				if(copyOrigin == InsertCopyOrigin.FORMAT_LEFT_ABOVE) {
					if(row1 - 1 >= 0) {
						copyCellStyleFromRow(row1 - 1);
					}
				} else if(copyOrigin == InsertCopyOrigin.FORMAT_RIGHT_BELOW) {
					if(row2 + 1 <= sheet.getBook().getMaxRowIndex()) {
						copyCellStyleFromRow(row2 + 1);
					}
				}
			}
		}
		//ZSS-988
		if (overlap != null) {
			((AbstractTableAdv)overlap).refreshFilter();
		}
		//ZSS-988: delete old filter, shift, add new filter
		if (!toShift.isEmpty()) {
			for (String tbName : toShift) {
				final AbstractTableAdv tb = (AbstractTableAdv)book.getTable(tbName);
				if (tb != null) {
					tb.refreshFilter();
				}
			}
		}
	}

	private void copyRowStyle(int srcRowIdx, int rowIdx, int lastRowIdx) {
		// copy row *local* style/height
		SRow srcRow = sheet.getRow(srcRowIdx);
		if(!srcRow.isNull()) {
			for(int r = rowIdx; r <= lastRowIdx; ++r) {
				SRow row = sheet.getRow(r);
				// style
				SCellStyle style = ((AbstractRowAdv)srcRow).getCellStyle(true);
				if(style != null) {
					row.setCellStyle(style);
				}
				// height
				if(srcRow.isCustomHeight()) { // according to Excel behavior
					row.setHeight(srcRow.getHeight());
					row.setCustomHeight(true);
				}
			}
		}

		// copy cell *local* style/format
		Iterator<SCell> cellsInRow = sheet.getCellIterator(srcRowIdx);
		while(cellsInRow.hasNext()) {
			SCell srcCell = cellsInRow.next();
			SCellStyle cellStyle = ((AbstractCellAdv)srcCell).getCellStyle(true);
			if(cellStyle != null) {
				for(int r = rowIdx; r <= lastRowIdx; ++r) {
					sheet.getCell(r, srcCell.getColumnIndex()).setCellStyle(cellStyle);
				}
			}
		}
	}

	private void copyColumnStyle(int srcColumnIdx, int columnIdx, int lastColumnIdx) {
		// copy column *local* style/height
		SColumnArray srcColumnArray = sheet.getColumnArray(srcColumnIdx);
		if(srcColumnArray!=null) {
			for(int c = columnIdx; c <= lastColumnIdx; ++c) {
				SColumn row = sheet.getColumn(c);
				// style
				SCellStyle style = ((AbstractColumnArrayAdv)srcColumnArray).getCellStyle(true);
				if(style != null) {
					row.setCellStyle(style);
				}
				// height
				if(srcColumnArray.isCustomWidth()) { // according to Excel behavior
					row.setWidth(srcColumnArray.getWidth());
					row.setCustomWidth(true);
				}
			}
		}

		// cell style/format
		Iterator<SRow> srcRows = sheet.getRowIterator();
		while(srcRows.hasNext()) {
			int r = srcRows.next().getIndex();
			SCell srcCell = sheet.getCell(r, srcColumnIdx);
			if(!srcCell.isNull()) {
				SCellStyle cellStyle = ((AbstractCellAdv)srcCell).getCellStyle(true);
				if(cellStyle != null) {
					for(int c = columnIdx; c <= lastColumnIdx; ++c) {
						sheet.getCell(r, c).setCellStyle(cellStyle);
					}
				}
			}
		}
	}

	private void copyCellStyleFromRow(int rowIndex) {
		// skip null cells
		Iterator<SCell> cellsInRow = sheet.getCellIterator(rowIndex);
		while(cellsInRow.hasNext()) {
			SCell srcCell = cellsInRow.next();
			// skip out of boundary cells
			int c = srcCell.getColumnIndex();
			if(c < getColumn()) {
				continue;
			} else if(c > getLastColumn()) {
				break;
			}
			// copy style
			SCellStyle cellStyle = ((AbstractCellAdv)srcCell).getCellStyle(true);
			if(cellStyle != null) {
				for(int r = getRow(); r <= getLastRow(); ++r) {
					sheet.getCell(r, c).setCellStyle(cellStyle);
				}
			}
		}
	}

	private void copyCellStyleFromColumn(int srcColumnIdx) {
		// skip null cells
		Iterator<SRow> srcRows = sheet.getRowIterator();
		while(srcRows.hasNext()) {
			// skip out of boundary cells
			int r = srcRows.next().getIndex();
			if(r < getRow()) {
				continue;
			} else if(r > getLastRow()) {
				break;
			}
			// copy style if cell existed
			SCell srcCell = sheet.getCell(r, srcColumnIdx);
			if(!srcCell.isNull()) {
				SCellStyle cellStyle = ((AbstractCellAdv)srcCell).getCellStyle(true);
				if(cellStyle != null) {
					for(int c = getColumn(); c <= getLastColumn(); ++c) {
						sheet.getCell(r, c).setCellStyle(cellStyle);
					}
				}
			}
		}
	}

	private void shrinkChartWidth() {
		for(SChart chart : sheet.getCharts()) {
			ViewAnchor anchor = chart.getAnchor();
			int col = anchor.getColumnIndex();
			ViewAnchor rightBottomAnchor = anchor.getRightBottomAnchor(sheet);
			int lastCol = rightBottomAnchor.getColumnIndex();
			if((col <= getColumn() && getColumn() <= lastCol) || (col <= getLastColumn() && getLastColumn() <= lastCol)) {
				int shrunkWidth = 0;
				for(int c = (getColumn() > col ? getColumn() : col); c <= getLastColumn() && c <= lastCol; ++c) {
					if(c != lastCol) {
						shrunkWidth += sheet.getColumn(c).getWidth();
					} else {
						shrunkWidth += rightBottomAnchor.getXOffset(); // fit to last
					} 
				}
				if(anchor.getWidth() > shrunkWidth) {
					anchor.setWidth(anchor.getWidth() - shrunkWidth);
					new NotifyChangeHelper().notifySheetChartUpdate(sheet, chart.getId());
				}
			}
		}
	}

	private void shrinkChartHeight(int row1, int row2) { //ZSS-985
		for(SChart chart : sheet.getCharts()) {
			ViewAnchor anchor = chart.getAnchor();
			int row = anchor.getRowIndex();
			ViewAnchor rightBottomAnchor = anchor.getRightBottomAnchor(sheet);
			int lastRow = rightBottomAnchor.getRowIndex();
			if((row <= row1 && row1 <= lastRow) || (row <= row2 && row2 <= lastRow)) {
				int shrunkHeight = 0;
				for(int r = (row1 > row ? row1 : row); r <= row2 && r <= lastRow; ++r) {
					if(r != lastRow) {
						shrunkHeight += sheet.getRow(r).getHeight();
					} else {
						shrunkHeight += rightBottomAnchor.getYOffset();	// fit to last
					}
				}
				if(anchor.getHeight() > shrunkHeight) {
					anchor.setHeight(anchor.getHeight() - shrunkHeight);
					new NotifyChangeHelper().notifySheetChartUpdate(sheet, chart.getId());
				}
			}
		}
	}

	private void extendChartWidth() {
		int size = 0;
		for(int r = getColumn(); r <= getLastColumn(); ++r) {
			size += sheet.getColumn(r).getWidth();
		}
		for(SChart chart : sheet.getCharts()) {
			ViewAnchor anchor = chart.getAnchor();
			int col = anchor.getColumnIndex();
			int lastCol = anchor.getRightBottomAnchor(sheet).getColumnIndex();
			if(col <= getColumn() && getColumn() <= lastCol) {
				anchor.setWidth(anchor.getWidth() + size);
				new NotifyChangeHelper().notifySheetChartUpdate(sheet, chart.getId());
			}
		}
	}

	private void extendChartHeight() {
		int size = 0;
		for(int r = getRow(); r <= getLastRow(); ++r) {
			size += sheet.getRow(r).getHeight();
		}
		for(SChart chart : sheet.getCharts()) {
			ViewAnchor anchor = chart.getAnchor();
			int row = anchor.getRowIndex();
			int lastRow = anchor.getRightBottomAnchor(sheet).getRowIndex();
			if(row <= getRow() && getRow() <= lastRow) {
				anchor.setHeight(anchor.getHeight() + size);
				new NotifyChangeHelper().notifySheetChartUpdate(sheet, chart.getId());
			}
		}
	}

	private boolean checkInCornerFreezePanel() {
		SSheetViewInfo viewInfo = sheet.getViewInfo();
		int fzr = viewInfo.getNumOfRowFreeze();	// it's number
		int fzc = viewInfo.getNumOfColumnFreeze();
		// check there is corner freeze panel first
		if(fzr > 0 && fzc > 0) {
			// check range
			if(getRow() < fzr && getColumn() < fzc) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkCrossTopFreezePanel() {
		SSheetViewInfo viewInfo = sheet.getViewInfo();
		int fzr = viewInfo.getNumOfRowFreeze();
		if(fzr > 0) {
			if(getRow() < fzr && getLastRow() >= fzr) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkCrossLeftFreezePanel() {
		SSheetViewInfo viewInfo = sheet.getViewInfo();
		int fzc = viewInfo.getNumOfColumnFreeze();
		if(fzc > 0) {
			// check range
			if(getColumn() < fzc && getLastColumn() >= fzc) {
				return true;
			}
		}
		return false;
	}
}
