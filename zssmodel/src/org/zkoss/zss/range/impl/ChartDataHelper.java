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
package org.zkoss.zss.range.impl;

import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.chart.*;
import org.zkoss.zss.range.SRange;

/**
 * Fill {@link SChartData} with series according to a selection. One series could be determined by row or column. 
 * @author Hawk
 * @since 3.5.0
 */
//implementation references org.zkoss.zssex.api.ChartDataUtil
public class ChartDataHelper extends RangeHelperBase {

	public ChartDataHelper(SRange range) {
		super(range);
	}

	public void fillChartData(SChart chart) {
		//avoid extreme large selection
		if (range.isWholeSheet() || range.isWholeColumn() || range.isWholeRow()){
			return;
		}
		SGeneralChartData chartData = (SGeneralChartData)chart.getData();
		CellRegion selection = new CellRegion(range.getRow(), range.getColumn(), range.getLastRow(), range.getLastColumn());
		switch (chart.getType()) {
		case AREA:
		case BAR:
		case COLUMN:
		case DOUGHNUT:
		case LINE:
		case PIE:
		case STOCK:
			fillCategoryData(selection, chartData);
			break;
		case SCATTER:
			fillXYData(selection, chartData);
			break;
		default:
			throw new UnsupportedOperationException("unknow chart type "+chart.getType());
		}
	}
	
	private void fillXYData(CellRegion selection, SGeneralChartData chartData) {
		CellRegion dataArea = getChartDataRange(selection);
		int firstDataColumn = dataArea.getColumn();
		int firstDataRow = dataArea.getRow();
		int columnCount = selection.getLastColumn() - firstDataColumn;
		int rowCount = selection.getLastRow() - firstDataRow;
		String xValueExpression = null;

		if (rowCount > columnCount) { 
			if (firstDataColumn < selection.getLastColumn()) {
				int lCol = selection.getColumn();
				int rCol = lCol;
				if (rCol < firstDataColumn) {
					rCol = firstDataColumn - 1;
				} else {
					firstDataColumn += 1;
				}
				//selection's first column becomes x values
				xValueExpression =new SheetRegion(sheet, new CellRegion(firstDataRow, lCol,selection.getLastRow(),rCol)).getReferenceString();
			}

			//each row is a series
			for (int seriesIndex = firstDataColumn ; seriesIndex <= dataArea.getLastColumn(); seriesIndex++){
				int nameRow = firstDataRow - 1;
				String nameExpression =null;
				if (nameRow >= selection.getRow()) {
					nameExpression = new SheetRegion(sheet, new CellRegion(selection.getRow(), seriesIndex, nameRow, seriesIndex) ).getReferenceString();
				}
				String yValueExpression =new SheetRegion(sheet, new CellRegion(dataArea.getRow(), seriesIndex, dataArea.getLastRow(), seriesIndex)).getReferenceString();
				SSeries series = chartData.addSeries();
				series.setXYFormula(nameExpression, xValueExpression, yValueExpression );
			}
		}else{
			if (firstDataRow < selection.getLastRow()) {
				int tRow = selection.getRow();
				int bRow = tRow;
				if (bRow < firstDataRow) {
					bRow = firstDataRow - 1;
				} else {
					firstDataRow += 1;
				}
				xValueExpression =new SheetRegion(sheet, new CellRegion(tRow,firstDataColumn, tRow,selection.getLastColumn())).getReferenceString();
			}

			//each column is a series
			for (int seriesIndex = firstDataRow ; seriesIndex <= dataArea.getLastRow(); seriesIndex++){
				String nameExpression = null;
				int nameCol = firstDataColumn - 1; //1 column to the left of data area is name column
				if (nameCol >= selection.getColumn()) {
					nameExpression = new SheetRegion(sheet, new CellRegion(seriesIndex, selection.getColumn(), seriesIndex, nameCol) ).getReferenceString();
				}
				String yValueExpression =new SheetRegion(sheet, new CellRegion(seriesIndex, dataArea.getColumn(), seriesIndex, dataArea.getLastColumn())).getReferenceString();
				SSeries series = chartData.addSeries();
				series.setXYFormula(nameExpression, xValueExpression, yValueExpression );
			}
		}
	}

	/**
	 * We find the data area that only contain numeric data first from selection range excluding left and top headers. 
	 * Then, we determine which side is category by comparing row count with column counts.
	 * The side having larger count is treated as the category.
	 * For example, if selection is 4 columns * 10 rows, there will be 10 categories and 3 data series.
	 * One column to the left of data area is category column.
	 */
	private void fillCategoryData(CellRegion selection, SGeneralChartData chartData) {
		CellRegion dataArea = getChartDataRange(selection);
		int firstDataColumn = dataArea.getColumn();
		int firstDataRow = dataArea.getRow();
		int columnCount = selection.getLastColumn() - firstDataColumn;
		int rowCount = selection.getLastRow() - firstDataRow;
		if (rowCount > columnCount) { //category by row, value by column
			int categoryColumn = firstDataColumn - 1; //1 column to the left of data area is category column
			if (categoryColumn >= selection.getColumn()) { //might not be present in selection area
				chartData.setCategoriesFormula(new SheetRegion(sheet, new CellRegion(dataArea.getRow(), categoryColumn, dataArea.getLastRow(), categoryColumn)).getReferenceString());
			}
			//each column is a series
			for (int seriesIndex = firstDataColumn ; seriesIndex <= dataArea.getLastColumn(); seriesIndex++){
				String nameExpression = null;
				int nameRow = firstDataRow - 1;
				if (nameRow >= selection.getRow()) {
					nameExpression = new SheetRegion(sheet, new CellRegion(selection.getRow(), seriesIndex, nameRow, seriesIndex) ).getReferenceString();
				}
				String xValueExpression =new SheetRegion(sheet, new CellRegion(dataArea.getRow(), seriesIndex, dataArea.getLastRow(), seriesIndex)).getReferenceString();
				SSeries series = chartData.addSeries();
				series.setFormula(nameExpression, xValueExpression );
			}
		}else{ //category by column, value by row
			int categoryRow = firstDataRow - 1; //1 row to the top of data area is category row
			if (categoryRow >= selection.getRow()) { 
				chartData.setCategoriesFormula(new SheetRegion(sheet, new CellRegion(categoryRow, dataArea.getColumn(), categoryRow, dataArea.getLastColumn())).getReferenceString());
			}
			//each row is a series
			for (int seriesIndex = firstDataRow ; seriesIndex <= dataArea.getLastRow(); seriesIndex++){
				String nameExpression = null;
				int nameColumn = firstDataColumn - 1;
				if (nameColumn >= selection.getColumn()) {
					nameExpression = new SheetRegion(sheet, new CellRegion(seriesIndex, selection.getColumn(), seriesIndex, nameColumn) ).getReferenceString();
				}
				String xValueExpression =new SheetRegion(sheet, new CellRegion(seriesIndex, dataArea.getColumn(), seriesIndex, dataArea.getLastColumn())).getReferenceString();
				SSeries series = chartData.addSeries();
				series.setFormula(nameExpression, xValueExpression );
			}
		}
	}
	
	/**
	 * Find a range of cells that has only numeric value (or formula) inside selection. Skip those top and left side text headers.
	 * @param sheet
	 * @param selection
	 * @return
	 */
	private CellRegion getChartDataRange(CellRegion selection) {
		SSheet sheet = range.getSheet();
		// assume can't find number cell, use last cell as value
		int colIdx = selection.getColumn();
		int rowIdx = -1;
		for (int r = selection.getLastRow(); r >= selection.getRow(); r--) {
			SRow row = sheet.getRow(r);
			if(row==null) continue;
			int rCol = colIdx;
			for (int c = selection.getLastColumn(); c >= rCol; c--) {
				if (isQualifiedCell(sheet.getCell(r, c))) {
					colIdx = c;
					rowIdx = r;
				} else {
					break;
				}
			}
		}
		if (rowIdx == -1) { // can not find number cell, use last cell as chart's value
			rowIdx = selection.getLastRow();
			colIdx = selection.getLastColumn();
		}
		return new CellRegion(rowIdx, colIdx, selection.getLastRow(),
				selection.getLastColumn());
	}

	private boolean isQualifiedCell(SCell cell) {
		if (cell == null)
			return true;
		CellType cellType = cell.getType();
		//TODO formula might be evaluated to non-numeric
		return cellType == CellType.NUMBER
				|| cellType == CellType.FORMULA
				|| cellType == CellType.BLANK;
	}	
}
