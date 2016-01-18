/* SimpleCellDisplayLoader.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/7 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.format.FormatResult;
import org.zkoss.zss.ui.sys.CellDisplayLoader;

/**
 * @author dennis
 * @since 3.0.0
 */
public class SimpleCellDisplayLoader implements CellDisplayLoader {

	/* (non-Javadoc)
	 * @see org.zkoss.zss.ui.sys.RichCellContentLoader#getCellHtmlText(org.zkoss.zss.model.sys.XSheet, int, int)
	 */
	@Override
	public String getCellHtmlText(SSheet sheet, int row, int column) {
		return CellFormatHelper.getCellHtmlText(sheet, row, column);
	}

	//ZSS-945, ZSS-1018
	@Override
	public String getCellHtmlText(SSheet sheet, int row, int column, FormatResult ft, SCellStyle tbStyle) {
		return CellFormatHelper.getCellHtmlText(sheet, row, column, ft, tbStyle);
	}
}
