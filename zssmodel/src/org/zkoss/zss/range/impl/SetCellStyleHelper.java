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
import java.util.Set;

import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SColumnArray;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.range.SRange;

/**
 * Helper for set cell, row, column style
 * @author Dennis
 *
 */
public class SetCellStyleHelper extends RangeHelperBase{

	SCellStyle _defaultStyle;
	
	public SetCellStyleHelper(SRange range) {
		super(range);
		_defaultStyle = range.getSheet().getBook().getDefaultCellStyle();
	}
	
	public void setCellStyle(SCellStyle style){
		if(isWholeSheet()){
			//1. it is not possible to set style to all row or columns
			//2. we don't have the way to replace defualt style in component.
			//caller should consider to set cell style to whole column (style on column) and give column from 0 to 1000, because of 
			//in general case we have much less columns then rows. 
			 throw new InvalidModelOpException("don't allow to set style to whole sheet, please consider to set as whole column style");
		}else if(isWholeRow()) {
			setWholeRowCellStyle(style);
		}else if(isWholeColumn()){
			setWholeColumnCellStyle(style);
		}else{
			for(int r = getRow(); r <= getLastRow(); r++){
				for (int c = getColumn(); c <= getLastColumn(); c++){
					SCell cell = sheet.getCell(r,c);
					cell.setCellStyle(style);
				}
			}
		}
	}
	
	public void setWholeRowCellStyle(SCellStyle style){
		for(int r = getRow(); r <= getLastRow(); r++){
			SRow row = sheet.getRow(r);
			row.setCellStyle(style);
			
			HashSet<Integer> cellProcessed = new HashSet<Integer>();
			
			Iterator<SCell> cells = sheet.getCellIterator(r);
			while(cells.hasNext()){
				SCell cell = cells.next();
				//the case the cell or column has local style
				if(cell.getCellStyle(true)!=null ||
						sheet.getColumn(cell.getColumnIndex()).getCellStyle(true)!=null){
					cell.setCellStyle(style);
				}
				
				cellProcessed.add(cell.getColumnIndex());
			}
			
			//has to force set the style on the row/column across cell to avoid row/column style conflict on null cell
			Iterator<SColumn> columns = sheet.getColumnIterator();
			while(columns.hasNext()){
				SColumn column = columns.next();
				if(cellProcessed.contains(column.getIndex())){
					continue;
				}
				if(column.getCellStyle(true)!=null){
					sheet.getCell(r, column.getIndex()).setCellStyle(style);
				}
			}
		}
	}
	
	public void setWholeColumnCellStyle(SCellStyle style){
		for (int c = getColumn(); c <= getLastColumn(); c++){
			SColumn column = sheet.getColumn(c);
			column.setCellStyle(style);
		}
		Iterator<SRow> rows = sheet.getRowIterator();
		while(rows.hasNext()){
			SRow row = rows.next();
			for (int c = getColumn(); c <= getLastColumn(); c++){
				SCell cell = sheet.getCell(row.getIndex(),c);
				//the case the cell or column has local style
				if(cell.getCellStyle(true)!=null ||
					row.getCellStyle(true)!=null){
						cell.setCellStyle(style);
				}
			}
		}
	}
}
