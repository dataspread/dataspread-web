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
package org.zkoss.zss.model.sys.format;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.STableStyle;
import org.zkoss.zss.model.impl.TableStyleNone;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public interface FormatEngine {

	/**
	 * Get the editText of cell
	 */
	String getEditText(SCell cell,FormatContext ctx);
	/**
	 * Get the format of the cell, if {@link SCellStyle#isDirectDataFormat()} if false, the return value will be localized 
	 * @see FormatEngine#getLocalizedFormat(String, FormatContext)
	 */
	String getFormat(SCell cell, FormatContext ctx);
	/**
	 * Get the localized-format , if the format is global-format, it will be transfer it to localized one (for example, m/d/yyyy will becom yyyy/m/d in zh_TW) 
	 */
	String getLocalizedFormat(String format, FormatContext ctx);
	
	/**
	 * Format the cell by it's format
	 * @see #getFormat(SCell, FormatContext)
	 */
	FormatResult format(SCell cell, FormatContext ctx);
	/**
	 * Format the value
	 * @see #getLocalizedFormat(String, FormatContext)
	 */
	FormatResult format(String format, Object value, FormatContext ctx, int cellWidth);

	/**
	 * Returns TableStyle of the specified name
	 * @param name
	 * @return
	 * @since 3.8.0
	 */
	STableStyle getTableStyle(String name);
}
