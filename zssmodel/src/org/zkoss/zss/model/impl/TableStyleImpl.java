/* NamedTableStyle.java

	Purpose:
		
	Description:
		
	History:
		Mar 30, 2015 4:24:06 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.STableStyle;
import org.zkoss.zss.model.STableStyleElem;

/**
 * @author henri
 * @since 3.8.0
 */
public class TableStyleImpl implements STableStyle, Serializable {
	private static final long serialVersionUID = 1378512655196539803L;
	private final String name;
	private final STableStyleElem wholeTable;
	private final STableStyleElem colStripe1;
	private final int colStripe1Size;
	private final STableStyleElem colStripe2;
	private final int colStripe2Size;
	private final STableStyleElem rowStripe1;
	private final int rowStripe1Size;
	private final STableStyleElem rowStripe2;
	private final int rowStripe2Size;
	private final STableStyleElem lastCol;
	private final STableStyleElem firstCol;
	private final STableStyleElem headerRow;
	private final STableStyleElem totalRow;
	private final STableStyleElem firstHeaderCell;
	private final STableStyleElem lastHeaderCell;
	private final STableStyleElem firstTotalCell;
	private final STableStyleElem lastTotalCell;

	public TableStyleImpl(
		String name,
		STableStyleElem wholeTable,
		STableStyleElem colStripe1,
		int colStripe1Size,
		STableStyleElem colStripe2,
		int colStripe2Size,
		STableStyleElem rowStripe1,
		int rowStripe1Size,
		STableStyleElem rowStripe2,
		int rowStripe2Size,
		STableStyleElem lastCol,
		STableStyleElem firstCol,
		STableStyleElem headerRow,
		STableStyleElem totalRow,
		STableStyleElem firstHeaderCell,
		STableStyleElem lastHeaderCell,
		STableStyleElem firstTotalCell,
		STableStyleElem lastTotalCell) {

		this.name = name;
		this.wholeTable = wholeTable;
		this.colStripe1 = colStripe1;
		this.colStripe1Size = colStripe1Size;
		this.colStripe2 = colStripe2;
		this.colStripe2Size = colStripe2Size;
		this.rowStripe1 = rowStripe1;
		this.rowStripe1Size = rowStripe1Size;
		this.rowStripe2 = rowStripe2;
		this.rowStripe2Size = rowStripe2Size;
		this.lastCol = lastCol;
		this.firstCol = firstCol;
		this.headerRow = headerRow;
		this.totalRow = totalRow;
		this.firstHeaderCell = firstHeaderCell;
		this.lastHeaderCell = lastHeaderCell;
		this.firstTotalCell = firstTotalCell;
		this.lastTotalCell = lastTotalCell;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public STableStyleElem getWholeTableStyle() {
		return wholeTable;
	}

	@Override
	public STableStyleElem getColStripe1Style() {
		return colStripe1;
	}

	@Override
	public int getColStrip1Size() {
		return colStripe1Size;
	}

	@Override
	public STableStyleElem getColStripe2Style() {
		return colStripe2;
	}

	@Override
	public int getColStrip2Size() {
		return colStripe2Size;
	}

	@Override
	public STableStyleElem getRowStripe1Style() {
		return rowStripe1;
	}

	@Override
	public int getRowStrip1Size() {
		return rowStripe1Size;
	}

	@Override
	public STableStyleElem getRowStripe2Style() {
		return rowStripe2;
	}

	@Override
	public int getRowStrip2Size() {
		return rowStripe2Size;
	}

	@Override
	public STableStyleElem getLastColumnStyle() {
		return lastCol;
	}

	@Override
	public STableStyleElem getFirstColumnStyle() {
		return firstCol;
	}

	@Override
	public STableStyleElem getHeaderRowStyle() {
		return headerRow;
	}

	@Override
	public STableStyleElem getTotalRowStyle() {
		return totalRow;
	}

	@Override
	public STableStyleElem getFirstHeaderCellStyle() {
		return firstHeaderCell;
	}

	@Override
	public STableStyleElem getLastHeaderCellStyle() {
		return lastHeaderCell;
	}

	@Override
	public STableStyleElem getFirstTotalCellStyle() {
		return firstTotalCell;
	}

	@Override
	public STableStyleElem getLastTotalCellStyle() {
		return lastTotalCell;
	}
}