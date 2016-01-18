/* AreaRef.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/15, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.api;

import java.io.Serializable;


/**
 * A class that represents an area reference with 4 value : 
 * 		row(top row), column(left column), last row(bottom row) and last column(right column) 

 * @author Dennis.Chen
 * @since 3.0.0
 */
public class AreaRef implements Serializable {
	private static final long serialVersionUID = 8864825038504082277L;
	protected int _column = -1;
	protected int _row = -1;
	protected int _lastColumn = -1;
	protected int _lastRow = -1;
	
	public AreaRef(){
	}
	
	public AreaRef(int row,int column,int lastRow,int lastColumn){
		setArea(row,column,lastRow,lastColumn);
	}
	public AreaRef(String areaReference){
		org.zkoss.poi.ss.util.AreaReference ar = new org.zkoss.poi.ss.util.AreaReference(areaReference);
		setArea(ar.getFirstCell().getRow(),ar.getFirstCell().getCol(),ar.getLastCell().getRow(),ar.getLastCell().getCol());
	}

	public void setArea(int row,int column,int lastRow,int lastColumn){
		_column = column;
		_row = row;
		_lastColumn = lastColumn;
		_lastRow = lastRow;
	}
	
	public int getColumn() {
		return _column;
	}

	public void setColumn(int column) {
		this._column = column;
	}

	public int getRow() {
		return _row;
	}

	public void setRow(int row) {
		this._row = row;
	}

	public int getLastColumn() {
		return _lastColumn;
	}

	public void setLastColumn(int lastColumn) {
		this._lastColumn = lastColumn;
	}

	public int getLastRow() {
		return _lastRow;
	}

	public void setLastRow(int lastRow) {
		this._lastRow = lastRow;
	}
	
	public Object cloneSelf(){
		return (AreaRef)new AreaRef(_row,_column,_lastRow,_lastColumn);
	}
	
	public boolean contains(int tRow, int lCol, int bRow, int rCol) {
		return	tRow >= _row && lCol >= _column &&
				bRow <= _lastRow && rCol <= _lastColumn;
	}
	
	public boolean overlap(int bTopRow, int bLeftCol, int bBottomRow, int bRightCol) {
		boolean xOverlap = isBetween(_column, bLeftCol, bRightCol) || isBetween(bLeftCol, _column, _lastColumn);
		boolean yOverlap = isBetween(_row, bTopRow, bBottomRow) || isBetween(bTopRow, _row, _lastRow);
		
		return xOverlap && yOverlap;
	}
	
	private boolean isBetween(int value, int min, int max) {
		return (value >= min) && (value <= max);
	}
	
	/**
	 * @return reference string, e.x A1:B2
	 */
	public String asString(){
		return new org.zkoss.poi.ss.util.AreaReference(new org.zkoss.poi.ss.util.CellReference(_row,_column),
					new org.zkoss.poi.ss.util.CellReference(_lastRow,_lastColumn)).formatAsString();
	}

	public int hashCode() {
		return _row << 14 + _column + _lastRow << 14 + _lastColumn;
	}
	
	public boolean equals(Object obj){
		return (this == obj)
			|| (obj instanceof AreaRef 
					&& ((AreaRef)obj)._column == _column && ((AreaRef)obj)._lastColumn == _lastColumn 
					&& ((AreaRef)obj)._row == _row && ((AreaRef)obj)._lastRow == _lastRow);
	}
}
