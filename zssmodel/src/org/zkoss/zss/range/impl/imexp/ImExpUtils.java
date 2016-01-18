/* XUtils.java

{{IS_NOTE
	Purpose:

	Description:

	History:
		Mar 20, 2008 12:40:01 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package org.zkoss.zss.range.impl.imexp;

import org.zkoss.poi.ss.usermodel.*;
import org.zkoss.poi.xssf.usermodel.XSSFSheet;


/**
 *
 * Utility methods that involve read or write POI model.
 * @author Hawk
 * @since 3.5.0
 */
public class ImExpUtils {

	public static int getWidthAny(Sheet poiSheet,int col, int charWidth){
		int w = poiSheet.getColumnWidth(col);
		//ZSS-952
		if (poiSheet instanceof XSSFSheet) {
			if (w == ((XSSFSheet) poiSheet).getXssfDefaultColumnWidth() * 256) {
				return UnitUtil.xssfDefaultColumnWidthToPx(w / 256.0, charWidth);
			}
		} else {
			if (w == poiSheet.getDefaultColumnWidth() * 256) { //default column width
				return UnitUtil.defaultColumnWidthToPx(w / 256, charWidth);
			}
		}
		return UnitUtil.fileChar256ToPx(w, charWidth);
	}

	public static int getHeightAny(Sheet poiSheet, int row){
		return getRowHeightInPx(poiSheet, poiSheet.getRow(row));
	}

	public static int getRowHeightInPx(Sheet poiSheet, Row row) {
		final int defaultHeight = poiSheet.getDefaultRowHeight();
		int h = row == null ? defaultHeight : row.getHeight();
		if (h == 0xFF) {
			h = defaultHeight;
		}
		return UnitUtil.twipToPx(h);
	}

}
