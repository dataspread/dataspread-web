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
package org.zkoss.zss.model;

import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * An immutable object that represents a block of cells with 4 indexes which are first and last row index, first and last column index.
 * It doesn't relate to a sheet. 
 * You can use it to compare with another cell region, like diff(), equals(), contains(), or overlaps(). 
 * These methods compare 2 regions by their 4 indexes.
 * @author dennis
 * @since 3.5.0
 */
public class CellRegion implements Serializable {
	private static final long serialVersionUID = 1L;

	public int row;
	public int column;
	public int lastRow;
	public int lastColumn;

	public CellRegion() {
		/* For seralization */
	}

	/**
	 * Create a region based on ref.
	 * Ignore the book and sheet names.
	 */
	public CellRegion(Ref ref)
	{
		this(ref.getRow(), ref.getColumn(),
				ref.getLastRow(), ref.getLastColumn());
	}

	/**
	 * Create a region which only contains 1 cell.
	 */
	public CellRegion(int row, int column) {
		this(row, column, row, column);
	}
	
	/**
	 * create a region with cell reference, e.g. "A1:B2" 
	 */
	public CellRegion(String areaReference) {
		AreaReference ref = new AreaReference(areaReference);
		int row = ref.getFirstCell().getRow();
		int column = ref.getFirstCell().getCol();
		int lastRow = ref.getLastCell().getRow();
		int lastColumn = ref.getLastCell().getCol();
		this.row = Math.min(row, lastRow);
		this.column = Math.min(column,lastColumn);
		this.lastRow = Math.max(row, lastRow);
		this.lastColumn = Math.max(column, lastColumn);
		
		checkLegal();
	}
	
	/**
	 * Create a region with 4 indexes
	 */
	public CellRegion(int row, int column, int lastRow, int lastColumn) {
		this.row = row;
		this.column = column;
		this.lastRow = lastRow;
		this.lastColumn = lastColumn;
		checkLegal();
	}

	private static boolean overlaps0(CellRegion r1, CellRegion r2) {
		return ((r1.lastColumn >= r2.column) &&
				(r1.lastRow >= r2.row) &&
				(r1.column <= r2.lastColumn) &&
				(r1.row <= r2.lastRow));
	}

	public static String convertIndexToColumnString(int columnIdx) {
		return CellReference.convertNumToColString(columnIdx);
	}

	public static int convertColumnStringToIndex(String colRef) {
		return CellReference.convertColStringToIndex(colRef);
	}

	/**
	 * @return a cell reference string it might be A1:B2 for multiple cells or A1 for one cell.
	 */
	public String getReferenceString(){
		AreaReference ref = new AreaReference(new CellReference(row,column),new CellReference(lastRow,lastColumn));
		return isSingle()?ref.getFirstCell().formatAsString():ref.formatAsString();
	}

	private void checkLegal() {
		if ((row > lastRow || column > lastColumn)
				|| (row < 0 || lastRow < 0 || column < 0 || lastColumn < 0)) {

			throw new IllegalArgumentException("the region is illegal " + this);
		}
	}

	/**
	 * @return return TRUE if this region only contains 1 cell, otherwise returns FALSE
	 */
	public boolean isSingle() {
		return row == lastRow && column == lastColumn;
	}

	/**
	 * @return returns TRUE if this region contains (or equals to) the cell specified by row and column index, otherwise returns FALSE
	 */
	public boolean contains(int row, int column) {
		return row >= this.row && row <= this.lastRow && column >= this.column
				&& column <= this.lastColumn;
	}
	
	/**
	 * @return returns TRUE if this region contains (or equals to) specified region, otherwise returns FALSE
	 */
	public boolean contains(CellRegion region) {
		return contains(region.row, region.column)
				&& contains(region.lastRow, region.lastColumn);
	}

	/**
	 * @return returns TRUE if this region overlaps specified region, otherwise returns FALSE
	 */
	public boolean overlaps(CellRegion region) {
		return overlaps0(this,region) || overlaps0(region,this);
	}

	/**
	 * @return returns TRUE if this region refers to the same scope as specified region, otherwise returns FALSE
	 */
	public boolean equals(int row, int column, int lastRow, int lastColumn){
		return this.row == row && this.column==column && this.lastRow==lastRow && this.lastColumn == lastColumn;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getReferenceString()).append("[").append(row).append(",").append(column).append(",").append(lastRow)
				.append(",").append(lastColumn).append("]");

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + lastColumn;
		result = prime * result + lastRow;
		result = prime * result + row;
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
		CellRegion other = (CellRegion) obj;
		if (column != other.column)
			return false;
		if (lastColumn != other.lastColumn)
			return false;
		if (lastRow != other.lastRow)
			return false;
		return row == other.row;
	}

	/**
	 * @return first row index
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @return first column index
	 */
	public int getColumn() {
		return column;
	}

	public int getLastRow() {
		return lastRow;
	}

	public int getLastColumn() {
		return lastColumn;
	}
	
	public int getRowCount(){
		return lastRow-row+1;
	}

	public int getColumnCount(){
		return lastColumn-column+1;
	}
	
	/**
	 * @return returns a list of regions that exclude the part which overlaps this region. 
	 * If this region's area equals to specified region, returned list contains nothing. 
	 */
	public List<CellRegion> diff(CellRegion target) {
		List<CellRegion> result = new ArrayList<CellRegion>();
		
		if(!this.overlaps(target)) {
			result.add(this);
		} else {
			
			CellRegion overlapRegion = new CellRegion(
					Math.max(this.row, target.row),
					Math.max(this.column, target.column), 
					Math.min(this.lastRow, target.lastRow), 
					Math.min(this.lastColumn, target.lastColumn));
			
			if(!overlapRegion.equals(this)) {
				// Top
				if(overlapRegion.row - this.row > 0) {
					result.add(new CellRegion(this.row, this.column, overlapRegion.row - 1, this.lastColumn));
				}
				
				// Bottom
				if(this.lastRow - overlapRegion.lastRow > 0) {
					result.add(new CellRegion(overlapRegion.lastRow + 1, this.column, this.lastRow, this.lastColumn));
				}
				
				// Left
				if(overlapRegion.column - this.column > 0) {
					result.add(new CellRegion(overlapRegion.row, this.column, overlapRegion.lastRow, overlapRegion.column - 1));
				}
				
				// Right
				if(this.lastColumn - overlapRegion.lastColumn > 0) {
					result.add(new CellRegion(overlapRegion.row, overlapRegion.lastColumn + 1, overlapRegion.lastRow, this.lastColumn));
				}
			}
		}
		
		return result;
	}

	
	/**
	 * @return returns the overlapping region between this region and the 
	 * specified region; null if no overlapping.
	 */
	public CellRegion getOverlap(CellRegion target) {
		final int row1 = Math.max(this.row, target.row);
		final int row2 = Math.min(this.lastRow, target.lastRow);
		if (row1 > row2) return null; // no overlapping
		
		final int col1 = Math.max(this.column, target.column); 
		final int col2 = Math.min(this.lastColumn, target.lastColumn);
		if (col1 > col2) return null; // no overlapping
		
		return new CellRegion(row1, col1, row2, col2);
	}


	/**
	 * @return returns the overlapping region between this region and the
	 * specified region; null if no overlapping.
	 */
	public CellRegion getBoundingBox(CellRegion target) {
		final int row1 = Math.min(this.row, target.row);
		final int row2 = Math.max(this.lastRow, target.lastRow);
		final int col1 = Math.min(this.column, target.column);
		final int col2 = Math.max(this.lastColumn, target.lastColumn);
		return new CellRegion(row1, col1, row2, col2);
	}


	/**
	 * @return returns the cell count which this region covers 
	 */
	public int getCellCount() {
		return getRowCount() * getColumnCount();
	}


	public CellRegion extendRange(int rows, int cols) {
		return new CellRegion(this.row,
				this.column,
				this.lastRow + rows,
				this.lastColumn + cols);
	}


	public CellRegion shiftedRange(int row_shift, int col_shift) {
		return new CellRegion(this.row + row_shift,
				this.column + col_shift,
				this.lastRow + row_shift,
				this.lastColumn + col_shift);
	}

	public int getHeight() {
		return lastRow - row + 1;
	}

	public int getLength() {
		return lastColumn - column + 1;
	}
}
