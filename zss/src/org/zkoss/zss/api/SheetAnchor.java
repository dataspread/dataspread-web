/* SheetAnchor.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api;

import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Picture;

/**
 * The anchor represents a position in a sheet for the objects ( {@link Picture} , or {@link Chart}) by row and column index.
 * @author dennis
 * @see Picture
 * @see Range#addPicture(SheetAnchor, byte[], org.zkoss.zss.api.model.Picture.Format)
 * @see Chart
 * @see Range#addChart(SheetAnchor, org.zkoss.zss.api.model.ChartData, org.zkoss.zss.api.model.Chart.Type, org.zkoss.zss.api.model.Chart.Grouping, org.zkoss.zss.api.model.Chart.LegendPosition)
 * @since 3.0.0
 */
public class SheetAnchor {

	private int _row;
	private int _column;
	private int _lastRow;
	private int _lastColumn;
	private int _xOffset;//px
	private int _yOffset;//px
	private int _lastXOffset;//px
	private int _lastYOffset;//px

	public SheetAnchor(int row, int column, int lastRow, int lastColumn) {
		this(row, column, 0, 0, lastRow, lastColumn, 0, 0);
	}

	public SheetAnchor(int row, int column, int xOffset, int yOffset, int lastRow,
			int lastColumn, int lastXOffset, int lastYOffset) {
		this._row = row;
		this._column = column;
		this._xOffset = xOffset;
		this._yOffset = yOffset;
		this._lastRow = lastRow;
		this._lastColumn = lastColumn;
		this._lastXOffset = lastXOffset;
		this._lastYOffset = lastYOffset;
	}

	public int getRow() {
		return _row;
	}

	public int getColumn() {
		return _column;
	}

	public int getXOffset() {
		return _xOffset;
	}

	public int getYOffset() {
		return _yOffset;
	}

	public int getLastRow() {
		return _lastRow;
	}

	public int getLastColumn() {
		return _lastColumn;
	}

	public int getLastXOffset() {
		return _lastXOffset;
	}

	public int getLastYOffset() {
		return _lastYOffset;
	}

}
