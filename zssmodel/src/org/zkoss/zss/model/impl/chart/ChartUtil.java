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
package org.zkoss.zss.model.impl.chart;

import java.util.Map;

import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractChartAdv;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.FormulaExpression;

/**
 * 
 * @author JerryChen
 * @since 3.8.0
 */
public class ChartUtil {
	public static void evalVisibleInfo(AbstractChartAdv chart, FormulaExpression expr, boolean[] hiddenInfos, 
			Map<Integer, Boolean> cachedRowValues, Map<Integer, Boolean> cachedColumnValues) {
		
		if(expr == null)
			return;
		
		Ref[] refs = expr.getAreaRefs();
		if(refs != null && refs.length > 0) {
			SSheet sheet = chart.getSheet();
			SingleLineAreaIterator iter = new SingleLineAreaIterator(refs[0]);
			int index = 0;
			for(CellPosition p; (p = iter.next()) != null;) {
				int row = p.getRow();
				int column = p.getColumn();
				Boolean rowHidden = cachedRowValues.get(row);
				Boolean columnHidden = cachedColumnValues.get(column);
				if(Boolean.TRUE.equals(rowHidden) || Boolean.TRUE.equals(columnHidden)) {
					hiddenInfos[index++] = Boolean.TRUE.equals(rowHidden) || Boolean.TRUE.equals(columnHidden);
					continue;
				}
				
				if(rowHidden == null) {
					rowHidden = sheet.getRow(row).isHidden();
					cachedRowValues.put(row, rowHidden);
				}
				if(columnHidden == null) {
					columnHidden = sheet.getColumn(column).isHidden();
					cachedColumnValues.put(column, columnHidden);
				}
				hiddenInfos[index++] = 
						rowHidden || columnHidden; 
			}
		}
	}
	
	static class SingleLineAreaIterator {
		int row;
		int column;
		int lastRow;
		int lastColumn;
		boolean isVertical;
		public SingleLineAreaIterator(Ref ref) {
			this.row = ref.getRow();
			this.column = ref.getColumn();
			this.lastRow = ref.getLastRow();
			this.lastColumn = ref.getLastColumn();
			isVertical = row != lastRow;
		}
		
		public CellPosition next() {
			return row > lastRow || column > lastColumn ? null :
				new CellPosition(isVertical ? row++ : row, isVertical ? column : column++);
		}
	}
	
	static class CellPosition {
		int row, column;
		public CellPosition(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public int getRow() {
			return row;
		}

		public void setRow(int row) {
			this.row = row;
		}

		public int getColumn() {
			return column;
		}

		public void setColumn(int column) {
			this.column = column;
		}
	}
}
