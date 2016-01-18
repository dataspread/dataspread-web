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

import java.io.Serializable;
/**
 * Represent a anchor position by its left-top cell index, width, height, x and y offset within the cell, 
 * or a right-bottom anchor position by its right-bottom cell index with 0 width and height.
 * @author dennis
 * @since 3.5.0
 */
public class ViewAnchor implements Serializable {

	private int rowIndex;
	private int columnIndex;
	private int xOffset;
	private int yOffset;
	private int width;
	private int height;
	/**
	 * Create a view anchor with indexes for left-top corner cell, width, and height.
	 */
	public ViewAnchor(int rowIndex, int columnIndex, int width, int height) {
		this(rowIndex,columnIndex,0,0,width,height);
		
	}
	
	/**
	 * Create a view anchor with indexes for left-top corner cell, x and y offset in the cell, width, and height.
	 */
	public ViewAnchor(int rowIndex, int columnIndex, int xOffset, int yOffset,
			int width, int height) {
		this.rowIndex = rowIndex;
		this.columnIndex = columnIndex;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the left-top cell's row index
	 */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * set the left-top cell's row index
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	/**
	 * @return the left-top cell's column index
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * set the left-top cell's column index
	 */
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	/**
	 * The x offset to left border of the anchor's left-top cell.
	 * The offset is larger if the anchor's position is closer to the right of the cell. 
	 * @return the x coordinate within the anchor's left-top cell.
	 */
	public int getXOffset() {
		return xOffset;
	}

	public void setXOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	/**
	 * The y offset to top border of the anchor's left-top cell.
	 * The offset is larger if the anchor's position is closer to the bottom of the cell.
	 * @return the y coordinate within the anchor's left-top cell.
	 */
	public int getYOffset() {
		return yOffset;
	}

	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	/**
	 * Width of the anchor, in pixels.
	 * @return Width of the anchor
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Set width of the anchor in pixels.
	 * @param width the width of the anchor in pixels.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return height the height of the anchor in pixels.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * set the height of the anchor in pixels.
	 * @param height height of the anchor in pixels
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * @return Returns the right-bottom anchor with 0 height and width based on the anchor. 
	 * Its offset is calculated depending on a sheet's row height and column width. 
	 */
	public ViewAnchor getRightBottomAnchor(SSheet sheet){
		
		int offsetPlusChartWidth = getXOffset() + this.getWidth();
		int lastColumn = this.getColumnIndex();
		int xOffsetInLastColumn = 0;
		//minus width of each inter-column to find last column index and x offset in last column
		for (int column = this.getColumnIndex(); ;column++){
			int interColumnWidth = sheet.getColumn(column).getWidth();
			if (offsetPlusChartWidth - interColumnWidth < 0){ 
				lastColumn = column;
				xOffsetInLastColumn = offsetPlusChartWidth;
				break;
			}else{
				offsetPlusChartWidth -= interColumnWidth;
			}
		}
		
		int offsetPlusChartHeight = getYOffset() + this.getHeight();
		int lastRow = this.getRowIndex();
		int yOffsetInLastRow = 0;
		//minus height of each inter-row to find last row index and y offset in last row
		for (int row = this.getRowIndex(); ;row++){
			int interRowHeight = sheet.getRow(row).getHeight();
			if (offsetPlusChartHeight - interRowHeight < 0){
				lastRow = row;
				yOffsetInLastRow = offsetPlusChartHeight;
				break;
			}else{
				offsetPlusChartHeight -= interRowHeight;
			}
		}
		return new ViewAnchor(lastRow, lastColumn, xOffsetInLastColumn, yOffsetInLastRow, 0, 0);
	}
	
	//ZSS-688
	//@since 3.6.0
	public ViewAnchor cloneViewAnchor() {
		return new ViewAnchor(this.rowIndex, this.columnIndex, 
				this.xOffset, this.yOffset, this.width, this.height);
	}
}
