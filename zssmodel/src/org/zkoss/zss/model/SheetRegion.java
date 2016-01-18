/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.io.Serializable;

import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
/**
 * Indicates a immutable region of cells in a sheet. It contains a {@link CellRegion} which specifies the area.
 * @author Dennis
 * @since 3.5.0
 */
public class SheetRegion implements Serializable{
	private static final long serialVersionUID = 1L;

	private final SSheet _sheet;
	private final CellRegion _region;
	
	public SheetRegion(SSheet sheet,CellRegion region){
		this._sheet = sheet;
		this._region = region;
	}
	public SheetRegion(SSheet sheet,int row, int column){
		this(sheet,new CellRegion(row,column));
	}
	public SheetRegion(SSheet sheet,int row, int column, int lastRow, int lastColumn){
		this(sheet,new CellRegion(row,column,lastRow,lastColumn));
	}
	public SheetRegion(SSheet sheet,String areaReference){
		
		//regard to testcase 439, now range should support "1:2" -> means row 1 to 2 
		AreaReference ref = new AreaReference(areaReference,sheet.getBook().getMaxRowIndex());
		int row = ref.getFirstCell().getRow();
		int column = ref.getFirstCell().getCol();
		int lastRow = ref.getLastCell().getRow();
		int lastColumn = ref.getLastCell().getCol();
		
		if(row==-1){
			row = 0;
		}
		if(lastRow==-1){
			lastRow = sheet.getBook().getMaxRowIndex();
		}
		if(column==-1){
			column = 0;
		}
		if(lastColumn==-1){
			lastColumn = sheet.getBook().getMaxColumnIndex();
		}
		this._sheet = sheet;
		this._region = new CellRegion(Math.min(row, lastRow), Math.min(column,
				lastColumn), Math.max(row, lastRow), Math.max(column,
				lastColumn));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(_sheet.getSheetName()).append("!").append(_region.toString());
		return sb.toString();
	}
	
	public SSheet getSheet(){
		return _sheet;
	}
	
	public CellRegion getRegion(){
		return _region;
	}
	
	public int getRow() {
		return _region.row;
	}

	public int getColumn() {
		return _region.column;
	}

	public int getLastRow() {
		return _region.lastRow;
	}

	public int getLastColumn() {
		return _region.lastColumn;
	}
	
	public int getRowCount(){
		return _region.getRowCount();
	}
	public int getColumnCount(){
		return _region.getColumnCount();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_region == null) ? 0 : _region.hashCode());
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
		SheetRegion other = (SheetRegion) obj;
		if (_region == null) {
			if (other._region != null)
				return false;
		} else if (!_region.equals(other._region))
			return false;
		if (_sheet == null) {
			if (other._sheet != null)
				return false;
		} else if (!_sheet.equals(other._sheet))
			return false;
		return true;
	}
	
	/**
	 * @return a cell reference, e.g. Sheet1!A1, or Sheet2!A1:B2 
	 */
	public String getReferenceString(){
		if(_region.isSingle()){
			return new CellReference(_sheet.getSheetName(),_region.getRow(), _region.getColumn(),false,false).formatAsString();
		}else{
			return new AreaReference(new CellReference(_sheet.getSheetName(),_region.getRow(), _region.getColumn(),false,false), 
				new CellReference(_sheet.getSheetName(), _region.getLastRow(),_region.getLastColumn(),false,false)).formatAsString();
		}
	}
	
}
