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
package org.zkoss.zss.range.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SColumnArray;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.AbstractSheetAdv;
import org.zkoss.zss.range.SRange;

/**
 * Helper for set cell, row, column style
 * @author Dennis
 *
 */
public class ClearCellHelper extends RangeHelperBase{

	SCellStyle _defaultStyle;
	
	public ClearCellHelper(SRange range) {
		super(range);
		_defaultStyle = range.getSheet().getBook().getDefaultCellStyle();
	}

	public void clearCellStyle() {
		if(isWholeSheet()){
			clearWholeSheetStyle();
		}else if(isWholeRow()) { 
			clearWholeRowCellStyle();
		}else if(isWholeColumn()){
			clearWholeColumnCellStyle();
		}else{
			for(int r = getRow(); r <= getLastRow(); r++){
				SRow row = sheet.getRow(r);
				for (int c = getColumn(); c <= getLastColumn(); c++){
					SColumn column = sheet.getColumn(c);
					SCell cell = sheet.getCell(r,c);
					if(row.getCellStyle(true)!=null || column.getCellStyle(true)!=null){
						//set cell style to default one if there are row or column style here
						cell.setCellStyle(_defaultStyle);
					}else if(cell.getCellStyle(true)!=null){
						cell.setCellStyle(null);
					}
				}
			}
		}
	}

	private void clearWholeSheetStyle() {
		Iterator<SColumnArray> columns = sheet.getColumnArrayIterator();
		while(columns.hasNext()){
			SColumnArray column = columns.next();
			if(column.getCellStyle(true)!=null){
				column.setCellStyle(null);
			}
		}
		Iterator<SRow> rows = sheet.getRowIterator();
		while(rows.hasNext()){
			SRow row = rows.next();
			if(row.getCellStyle(true)!=null){
				row.setCellStyle(null);
			}
			Iterator<SCell> cells = sheet.getCellIterator(row.getIndex());
			while(cells.hasNext()){
				SCell cell = cells.next();
				if(cell.getCellStyle(true)!=null){
					cell.setCellStyle(null);
				}
			}
		}
	}

	private void clearWholeColumnCellStyle() {
		for(int c = getColumn(); c <= getLastColumn(); c++){
			SColumn column = sheet.getColumn(c);
			if(!column.isNull()){
				column.setCellStyle(null);
			}
		}
		Iterator<SRow> rows = sheet.getRowIterator();
		while(rows.hasNext()){
			SRow row = rows.next();
			boolean rowHasStyle = row.getCellStyle(true)!=null;
			for(int c = getColumn(); c <= getLastColumn(); c++){
				SCell cell = sheet.getCell(row.getIndex(), c);
				if(rowHasStyle){
					cell.setCellStyle(_defaultStyle);
				}else if(cell.getCellStyle(true)!=null){
					cell.setCellStyle(null);
				}
			}
		}
	}

	private void clearWholeRowCellStyle() {
		Set<Integer> columnHasStyle = new HashSet<Integer>();
		Iterator<SColumn> columns = sheet.getColumnIterator();
		while(columns.hasNext()){
			SColumn column = columns.next();
			if(column.getCellStyle(true)!=null){
				columnHasStyle.add(column.getIndex());
			}
		}
		
		for(int r = getRow(); r <= getLastRow(); r++){
			Set<Integer> rowColumnHasStyle = new HashSet<Integer>(columnHasStyle);
			SRow row = sheet.getRow(r);
			if(!row.isNull()){
				row.setCellStyle(null);
			}
			
			Iterator<SCell> cells = sheet.getCellIterator(r);
			while(cells.hasNext()){
				SCell cell = cells.next();

				if(columnHasStyle.contains(cell.getColumnIndex())){
					cell.setCellStyle(_defaultStyle);
					rowColumnHasStyle.remove(cell.getColumnIndex());
				}else if(cell.getCellStyle(true)!=null){
					cell.setCellStyle(null);
				}
			}
			//there are column has style but no cell is this row, have to create a cell and set the default style to it
			for(Integer c:rowColumnHasStyle){
				sheet.getCell(row.getIndex(), c).setCellStyle(_defaultStyle);
			}
		}
	}
	
	//ZSS-1001
	// Returns true if some tables deleted; false if no tables to be deleted.
	//@since 3.8.0
	public boolean clearCellContentAndTables() {
		final boolean deleted = clearTables();
		clearCellContent();
		return deleted;
	}

	public void clearCellContent() {
		if(isWholeSheet()){
			clearWholeSheetContent();
		}else if(isWholeRow()) { 
			clearWholeRowContent();
		}else if(isWholeColumn()){
			clearWholeColumnContent();
		}else{
			for(int r = getRow(); r <= getLastRow(); r++){
				for (int c = getColumn(); c <= getLastColumn(); c++){
					SCell cell = sheet.getCell(r,c);
					clearCellContent(cell);
				}
			}
		}
	}
	
	//ZSS-1001
	//Return true to indicate delete the tables; or false if no table to delete.
	private boolean clearTables() {
		if (isWholeSheet()) {
			//ZSS-1001
			List<STable> tables = sheet.getTables();
			final boolean deleteTables = !tables.isEmpty();
			AbstractBookAdv book = (AbstractBookAdv) sheet.getBook();
			for (STable tb : tables) {
				book.removeTable(tb.getName());
			}
			((AbstractSheetAdv)sheet).clearTables();
			return deleteTables;
		} else {
			final int row1 = getRow();
			final int row2 = getLastRow();
			final int col1 = getColumn();
			final int col2 = getLastColumn();
			final Set<String> toDelete = 
					InsertDeleteHelper.collectContainedTables(sheet, row1, col1, row2, col2);
			final boolean deleteTables = !toDelete.isEmpty();
			InsertDeleteHelper.deleteTablesByNames(sheet, toDelete);
			return deleteTables;
		}
	}

	private void clearWholeSheetContent() {
		Iterator<SRow> rows = sheet.getRowIterator();
		while(rows.hasNext()){
			SRow row = rows.next();
			Iterator<SCell> cells = sheet.getCellIterator(row.getIndex());
			while(cells.hasNext()){
				SCell cell = cells.next();
				clearCellContent(cell);
			}
		}
	}

	private void clearWholeColumnContent() {
		Iterator<SRow> rows = sheet.getRowIterator();
		while(rows.hasNext()){
			SRow row = rows.next();
			for(int c = getColumn(); c <= getLastColumn(); c++){
				SCell cell = sheet.getCell(row.getIndex(), c);
				clearCellContent(cell);
			}
		}
	}

	private void clearWholeRowContent() {
		for(int r = getRow(); r <= getLastRow(); r++){
			Iterator<SCell> cells = sheet.getCellIterator(r);
			while(cells.hasNext()){
				SCell cell = cells.next();
				clearCellContent(cell);
			}
		}
	}
	
	private void clearCellContent(SCell cell){
		if (!cell.isNull()) {
			cell.setHyperlink(null);
			cell.clearValue();
		}
	}

}
